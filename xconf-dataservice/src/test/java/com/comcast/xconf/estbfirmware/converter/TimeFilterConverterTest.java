/* 
 * If not stated otherwise in this file or this component's Licenses.txt file the 
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Igor Kostrov
 * Created: 1/14/2016
*/
package com.comcast.xconf.estbfirmware.converter;

import com.comcast.xconf.estbfirmware.Capabilities;
import com.comcast.xconf.estbfirmware.EstbFirmwareContext;
import com.comcast.xconf.estbfirmware.TimeFilter;
import com.comcast.xconf.evaluators.RuleProcessorFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

public class TimeFilterConverterTest extends BaseQueriesControllerTest {

    @Autowired
    private RuleProcessorFactory ruleProcessorFactory;

    @Autowired
    private TimeFilterConverter converter;

    @Test
    public void convertDefaultFilter() throws Exception {
        TimeFilter filter = createDefaultTimeFilter("09:00", "10:00");

        convertAndVerify(filter);
    }

    @Test
    public void convertFilterWithRebootDecoupledAndHttp() throws Exception {
        TimeFilter filter = createTimeFilter(
                defaultTimeFilterId, "09:00", "10:00",
                false, true, true);

        convertAndVerify(filter);
    }

    @Test
    public void convertFilterWithLocalTime() throws Exception {
        TimeFilter filter = createTimeFilter(
                defaultTimeFilterId, "09:00", "10:00",
                true, true, true);

        convertAndVerify(filter);
    }

    private void convertAndVerify(TimeFilter filter) {
        FirmwareRule firmwareRule = converter.convert(filter);
        TimeFilter converted = converter.convert(firmwareRule);

        Assert.assertEquals(filter, converted);
    }

    @Test
    public void evaluateDefaultTimeFilter() throws Exception {
        TimeFilter filter = createDefaultTimeFilter("10:00", "15:00");

        evaluateAndVerify(createContextWithTime(12), filter, true);
        evaluateAndVerify(createContextWithTime(16), filter, false);
    }

    @Test
    public void evaluateWhenStartHourBiggerThanEnd() throws Exception {
        TimeFilter filter = createDefaultTimeFilter("15:00", "10:00");

        evaluateAndVerify(createContextWithTime(12), filter, false);
        evaluateAndVerify(createContextWithTime(16), filter, true);
    }

    @Test
    public void evaluateWhenNeverBlockRebootDecoupledIsTrue() throws Exception {
        TimeFilter filter = createTimeFilter(defaultTimeFilterId, "10:00", "15:00", false, true, true);
        EstbFirmwareContext context = createContextWithTime(12);
        context.setCapabilities(Collections.singletonList(Capabilities.rebootDecoupled.toString()));

        evaluateAndVerify(context, filter, false);
        evaluateAndVerify(createContextWithTime(16), filter, false);
    }

    @Test
    public void evaluateFalse_WhenContextInEnvModelWhiteList() throws Exception {
        TimeFilter filter = createDefaultTimeFilterWithEnvModelWhiteList();
        EstbFirmwareContext context = createContextWithTime(12);

        evaluateAndVerify(context, filter, false);
    }

    @Test
    public void evaluateTrue_WhenEnvContextNotInEnvModelWhiteList() throws Exception {
        TimeFilter filter = createDefaultTimeFilterWithEnvModelWhiteList();
        EstbFirmwareContext context = createContextWithTime(12);
        context.setEnv("unknownEnvironment");

        evaluateAndVerify(context, filter, true);
    }

    @Test
    public void evaluateTrue_WhenModelContextNotInEnvModelWhiteList() throws Exception {
        TimeFilter filter = createDefaultTimeFilterWithEnvModelWhiteList();
        EstbFirmwareContext context = createContextWithTime(12);
        context.setModel("unknownModel");

        evaluateAndVerify(context, filter, true);
    }

    @Test
    public void evaluateTrue_WhenBothEnvModelContextNotInEnvModelWhiteList() throws Exception {
        TimeFilter filter = createDefaultTimeFilterWithEnvModelWhiteList();
        EstbFirmwareContext context = createContextWithTime(12);
        context.setModel("unknownModel");

        evaluateAndVerify(context, filter, true);
    }

    private void evaluateAndVerify(EstbFirmwareContext context, TimeFilter filter, boolean block) {
        context.setEnv(context.getEnv().toUpperCase());
        context.setModel(context.getModel().toUpperCase());
        FirmwareRule firmwareRule = converter.convert(filter);
        boolean evaluate = ruleProcessorFactory.get().evaluate(firmwareRule.getRule(), context.getProperties());

        Assert.assertEquals(block, evaluate);
    }

    private EstbFirmwareContext createContextWithTime(Integer hour) {
        EstbFirmwareContext result = createDefaultContext();
        result.setTime(new LocalDateTime(2016, 1, 1, hour, 0));

        return result;
    }

    private TimeFilter createDefaultTimeFilterWithEnvModelWhiteList() {
        TimeFilter filter = createDefaultTimeFilter("10:00", "15:00");
        filter.setEnvModelWhitelist(createAndSaveEnvModelRuleBean(
                defaultEnvModelId, defaultEnvironmentId, defaultModelId,
                defaultFirmwareVersion, defaultFirmwareDownloadProtocol, ApplicationType.STB));
        return filter;
    }
}
