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
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.EnvModelRuleBean;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.TimeFilter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.BlockingFilterAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.util.RuleUtil;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimeFilterConverter {

    @Autowired
    private ConverterHelper converterHelper;

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

        timeFilter.setId(firmwareRule.getId());
        timeFilter.setName(firmwareRule.getName());

        timeFilter.setNeverBlockRebootDecoupled(false);
        timeFilter.setNeverBlockHttpDownload(false);

        EnvModelRuleBean envModelWhitelist = new EnvModelRuleBean();

        convertConditions(firmwareRule.getRule(), timeFilter, envModelWhitelist);

        timeFilter.setEnvModelWhitelist(envModelWhitelist);

        return timeFilter;
    }


    private void convertConditions(Rule firmwareRule, TimeFilter filter, EnvModelRuleBean envModelWhitelist) {
        for (Rule rule : RuleUtil.getIterableList(firmwareRule)) {
            convertCondition(rule, envModelWhitelist, filter);
        }
    }

    private void convertCondition(Rule r, EnvModelRuleBean envModelWhitelist, TimeFilter timeFilter) {
        Condition condition = r.getCondition();
        if (condition != null) {
            if (RuleFactory.REBOOT_DECOUPLED.equals(condition.getFreeArg())) {
                if (StandardOperation.EXISTS.equals(condition.getOperation())) {
                    timeFilter.setNeverBlockRebootDecoupled(true);
                }
            } else if (RuleFactory.FIRMWARE_DOWNLOAD_PROTOCOL.equals(condition.getFreeArg())) {
                if (StandardOperation.IS.equals(condition.getOperation())) {
                    timeFilter.setNeverBlockHttpDownload(true);
                }
            } else if (ConverterHelper.isLegacyIpFreeArg(condition.getFreeArg())
                    || RuleFactory.IP.equals(condition.getFreeArg())) {
                timeFilter.setIpWhitelist(converterHelper.getIpAddressGroup(condition));
            } else if (RuleFactory.MODEL.equals(condition.getFreeArg())) {
                envModelWhitelist.setModelId((String) condition.getFixedArg().getValue());
            } else if (RuleFactory.ENV.equals(condition.getFreeArg())) {
                envModelWhitelist.setEnvironmentId((String) condition.getFixedArg().getValue());
            } else if (ConverterHelper.isLegacyLocalTimeFreeArg(condition.getFreeArg()) ||
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
            } else if (RuleFactory.TIME_ZONE.equals(condition.getFreeArg())) {
                timeFilter.setLocalTime(r.isNegated());
            }
        }
    }
}
