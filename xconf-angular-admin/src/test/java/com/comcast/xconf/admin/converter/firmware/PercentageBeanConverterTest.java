/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.admin.converter.firmware;

import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.estbfirmware.converter.PercentageBeanConverter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PercentageBeanConverterTest {

    private PercentageBeanConverter percentageBeanConverter = new PercentageBeanConverter();

    @Test
    public void convertPercentageBeanIntoRuleWithPercentRange() throws Exception {
        PercentageBean percentageBean = createPercentageBean();
        FirmwareRule firmwareRule = percentageBeanConverter.convertIntoRule(percentageBean);
        RuleAction action = (RuleAction)firmwareRule.getApplicableAction();
        List<RuleAction.ConfigEntry> configEntries = action.getConfigEntries();
        assertEquals(new Double(0.0), configEntries.get(0).getStartPercentRange());
        assertEquals(new Double(12.0), configEntries.get(0).getEndPercentRange());
        assertEquals(new Double(12.0), configEntries.get(1).getStartPercentRange());
        assertEquals(new Double(35.0), configEntries.get(1).getEndPercentRange());
    }

    @Test
    public void convertFirmwareRuleIntoPercentageBeanWithPercentRange() throws Exception {
        FirmwareRule firmwareRule = createFirmwareRule();
        PercentageBean bean = percentageBeanConverter.convertIntoBean(firmwareRule);
        List<RuleAction.ConfigEntry> configEntries = bean.getDistributions();
        assertEquals(new Double(0.0), configEntries.get(0).getStartPercentRange());
        assertEquals(new Double(10.0), configEntries.get(0).getEndPercentRange());
        assertEquals(new Double(10.0), configEntries.get(1).getStartPercentRange());
        assertEquals(new Double(65.0), configEntries.get(1).getEndPercentRange());
    }

    private PercentageBean createPercentageBean() {
        PercentageBean percentageBean = new PercentageBean();
        percentageBean.setId("testId");
        percentageBean.setName("testName");
        percentageBean.setModel("testModel");
        percentageBean.setEnvironment("testEnvironment");
        percentageBean.setOptionalConditions(RuleFactory.newEnvModelRule("testModel", "testEnvironment"));
        percentageBean.setApplicationType(ApplicationType.STB);
        percentageBean.setActive(true);

        List<RuleAction.ConfigEntry> distributions = new ArrayList<>();
        RuleAction.ConfigEntry entry1 = new RuleAction.ConfigEntry();
        entry1.setConfigId("id1");
        entry1.setPercentage(12.0);
        distributions.add(entry1);
        RuleAction.ConfigEntry entry2 = new RuleAction.ConfigEntry();
        entry2.setConfigId("id2");
        entry2.setPercentage(23.0);
        distributions.add(entry2);
        percentageBean.setDistributions(distributions);
        return percentageBean;
    }

    private FirmwareRule createFirmwareRule() {
        FirmwareRule firmwareRule = new FirmwareRule();
        RuleAction ruleAction = new RuleAction();
        ruleAction.setConfigId("testId");
        List<RuleAction.ConfigEntry> configEntries = new ArrayList<>();

        RuleAction.ConfigEntry entry1 = new RuleAction.ConfigEntry();
        entry1.setConfigId("id1");
        entry1.setPercentage(10.0);
        configEntries.add(entry1);
        RuleAction.ConfigEntry entry2 = new RuleAction.ConfigEntry();
        entry2.setConfigId("id2");
        entry2.setPercentage(55.0);
        configEntries.add(entry2);
        ruleAction.setConfigEntries(configEntries);
        firmwareRule.setApplicableAction(ruleAction);
        return firmwareRule;
    }
}
