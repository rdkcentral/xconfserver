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
 * Created: 13.01.2016
*/
package com.comcast.xconf.estbfirmware.converter;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.Time;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.EnvModelRuleBean;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.TimeFilter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.estbfirmware.legacy.TimeFilterLegacyConverter;
import com.comcast.xconf.firmware.BlockingFilterAction;
import com.comcast.xconf.firmware.FirmwareRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimeFilterConverter {

    @Autowired
    private TimeFilterLegacyConverter legacyConverter;

    public FirmwareRule convert(TimeFilter timeFilter) {

        String envId = null;
        String modelId = null;

        if (timeFilter.getEnvModelWhitelist() != null) {
            envId = timeFilter.getEnvModelWhitelist().getEnvironmentId();
        }

        if (timeFilter.getEnvModelWhitelist() != null) {
            modelId = timeFilter.getEnvModelWhitelist().getModelId();
        }

        Time startTime = null;
        Time endTime = null;

        String TIME_PATTERN = "HH:mm";

        if (timeFilter.getStart() != null) {
            startTime = new Time(timeFilter.getStart().toString(TIME_PATTERN));
        }

        if (timeFilter.getEnd() != null) {
            endTime = new Time(timeFilter.getEnd().toString(TIME_PATTERN));
        }

        String ipList = null;
        if (timeFilter.getIpWhitelist() != null) {
            ipList = timeFilter.getIpWhitelist().getName();
        }

        Rule rule = RuleFactory.newTimeFilter(
                timeFilter.isNeverBlockRebootDecoupled(),
                timeFilter.isNeverBlockHttpDownload(),
                timeFilter.isLocalTime(),
                envId, modelId, ipList,
                startTime, endTime);

        FirmwareRule firmwareRule = new FirmwareRule();
        firmwareRule.setId(timeFilter.getId());
        firmwareRule.setName(timeFilter.getName());
        firmwareRule.setRule(rule);
        firmwareRule.setType(TemplateNames.TIME_FILTER);
        firmwareRule.setApplicableAction(new BlockingFilterAction());
        return firmwareRule;
    }

    public TimeFilter convert(FirmwareRule firmwareRule) {
        TimeFilter timeFilter = new TimeFilter();

        EnvModelRuleBean envModelWhitelist = new EnvModelRuleBean();

        timeFilter.setId(firmwareRule.getId());
        timeFilter.setName(firmwareRule.getName());

        timeFilter.setNeverBlockRebootDecoupled(false);
        timeFilter.setNeverBlockHttpDownload(false);

        legacyConverter.convertConditions(firmwareRule.getRule(), timeFilter, envModelWhitelist);

        timeFilter.setEnvModelWhitelist(envModelWhitelist);

        return timeFilter;
    }
}
