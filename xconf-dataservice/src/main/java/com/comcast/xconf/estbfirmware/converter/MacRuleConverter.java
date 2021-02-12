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
 * Created: 18.01.2016
*/
package com.comcast.xconf.estbfirmware.converter;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.MacRuleBean;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.queries.beans.MacRuleBeanWrapper;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.collect.Sets;

import java.util.Collection;

public class MacRuleConverter {

    public static MacRuleBeanWrapper convertFirmwareRuleToMacRuleBeanWrapper(FirmwareRule firmwareRule) {
        MacRuleBeanWrapper macRuleBean = new MacRuleBeanWrapper();

        macRuleBean.setName(firmwareRule.getName());
        macRuleBean.setId(firmwareRule.getId());
        for(Condition condition : RuleUtil.toConditions(firmwareRule.getRule())) {
            if (RuleFactory.MAC.equals(condition.getFreeArg())) {
                Object mac = condition.getFixedArg().getValue();
                if (RuleFactory.IN_LIST.equals(condition.getOperation())) {
                    macRuleBean.setMacListRef((String) mac);
                } else if (StandardOperation.IN.equals(condition.getOperation())
                        && condition.getFixedArg().getValue() instanceof Collection) {
                    macRuleBean.setMacList((Sets.newHashSet((Collection<String>) condition.getFixedArg().getValue())));
                } else if (StandardOperation.IS.equals(condition.getOperation())
                        && mac instanceof String
                        && MacAddress.isValid((String) mac)) {
                    macRuleBean.setMacList(Sets.newHashSet((String) mac));
                }
            }
        }
        return macRuleBean;
    }

    public static FirmwareRule convertMacRuleBeanToFirmwareRule(MacRuleBean bean) {
        FirmwareRule firmwareMacRule = new FirmwareRule();
        firmwareMacRule.setId(bean.getId());
        firmwareMacRule.setName(bean.getName());
        firmwareMacRule.setType(TemplateNames.MAC_RULE);
        if (bean.getFirmwareConfig() != null) {
            firmwareMacRule.setApplicableAction(new RuleAction(bean.getFirmwareConfig().getId()));
            firmwareMacRule.setApplicationType(bean.getFirmwareConfig().getApplicationType());
        } else {
            firmwareMacRule.setApplicableAction(new RuleAction());
        }

        Rule macRule = RuleFactory.newMacRule(bean.getMacListRef());
        firmwareMacRule.setRule(macRule);

        return firmwareMacRule;
    }

}
