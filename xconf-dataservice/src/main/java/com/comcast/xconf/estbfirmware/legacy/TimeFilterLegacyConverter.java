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
 * Created: 1/15/2016
*/
package com.comcast.xconf.estbfirmware.legacy;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.Time;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.EnvModelRuleBean;
import com.comcast.xconf.estbfirmware.FirmwareRule;
import com.comcast.xconf.estbfirmware.TimeFilter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.util.RuleUtil;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class TimeFilterLegacyConverter {

    @Autowired
    private LegacyConverterHelper converterHelper;

    public TimeFilter convertFirmwareRuleToTimeFilter(FirmwareRule firmwareRule) {
        TimeFilter timeFilter = new TimeFilter();

        EnvModelRuleBean envModelWhitelist = new EnvModelRuleBean();

        timeFilter.setId(firmwareRule.getId());
        timeFilter.setName(firmwareRule.getName());

        timeFilter.setNeverBlockRebootDecoupled(false);
        timeFilter.setNeverBlockHttpDownload(false);

        convertConditions(firmwareRule, timeFilter, envModelWhitelist);

        envModelWhitelist.setId(firmwareRule.getBoundConfigId());

        timeFilter.setEnvModelWhitelist(envModelWhitelist);

        return timeFilter;
    }

    public void convertConditions(Rule firmwareRule, TimeFilter filter, EnvModelRuleBean envModelWhitelist) {
        for (Rule rule : RuleUtil.getIterableList(firmwareRule)) {
            convertCondition(rule, envModelWhitelist, filter);
        }
    }

    private void convertCondition(Rule r, EnvModelRuleBean envModelWhitelist, TimeFilter timeFilter) {
        Condition condition = r.getCondition();
        if (condition != null) {
            if (FirmwareRule.REBOOT_DECOUPLED.equals(condition.getFreeArg())) {
                if (StandardOperation.EXISTS.equals(condition.getOperation())) {
                    timeFilter.setNeverBlockRebootDecoupled(true);
                }
            } else if (FirmwareRule.FIRMWARE_DOWNLOAD_PROTOCOL.equals(condition.getFreeArg())) {
                if (StandardOperation.IS.equals(condition.getOperation())) {
                    timeFilter.setNeverBlockHttpDownload(true);
                }
            } else if (FirmwareRule.IP.equals(condition.getFreeArg())
                    || RuleFactory.IP.equals(condition.getFreeArg())) {
                timeFilter.setIpWhitelist(converterHelper.getIpAddressGroup(condition));
            } else if (FirmwareRule.MODEL.equals(condition.getFreeArg())) {
                envModelWhitelist.setModelId((String) condition.getFixedArg().getValue());
            } else if (FirmwareRule.ENV.equals(condition.getFreeArg())) {
                envModelWhitelist.setEnvironmentId((String) condition.getFixedArg().getValue());
            } else if (FirmwareRule.LOCAL_TIME.equals(condition.getFreeArg()) ||
                    RuleFactory.LOCAL_TIME.equals(condition.getFreeArg())) {
                if (StandardOperation.GTE.equals(condition.getOperation())) {
                    String rawTime = (String) condition.getFixedArg().getValue();
                    LocalTime locTime = LocalTime.parse(rawTime);
                    timeFilter.setStart(locTime);
                } else if (StandardOperation.LTE.equals(condition.getOperation())) {
                    String rawTime = (String) condition.getFixedArg().getValue();
                    LocalTime locTime = LocalTime.parse(rawTime);
                    timeFilter.setEnd(locTime);
                }
            } else if (FirmwareRule.TIME_ZONE.equals(condition.getFreeArg())) {
                timeFilter.setLocalTime(r.isNegated());
            }
        }
    }

    public FirmwareRule convertTimeFilterToFirmwareRule(TimeFilter timeFilter) {

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

        FirmwareRule rule = FirmwareRule.newTimeFilter(timeFilter.isNeverBlockRebootDecoupled(),
                timeFilter.isNeverBlockHttpDownload(),
                timeFilter.isLocalTime(),
                envId,
                modelId,
                timeFilter.getIpWhitelist(),
                startTime,
                endTime);

        rule.setId(timeFilter.getId());
        rule.setName(timeFilter.getName());


        if (timeFilter.getEnvModelWhitelist() != null) {
            rule.setBoundConfigId(timeFilter.getEnvModelWhitelist().getId());
        }


        return rule;
    }
}
