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

import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.estbfirmware.EnvModelRuleBean;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import org.springframework.stereotype.Component;
import com.comcast.xconf.util.RuleUtil;

@Component
public class EnvModelRuleConverter {

    public static EnvModelRuleBean convertFirmwareRuleToEnvModelRuleBean(FirmwareRule firmwareRule) {
        EnvModelRuleBean bean = new EnvModelRuleBean();

        bean.setName(firmwareRule.getName());
        bean.setId(firmwareRule.getId());

        for (Condition condition : RuleUtil.toConditions(firmwareRule.getRule())) {
            if (condition.getFreeArg().equals(RuleFactory.ENV)) {
                bean.setEnvironmentId((String) condition.getFixedArg().getValue());
            } else if (condition.getFreeArg().equals(RuleFactory.MODEL)) {
                bean.setModelId((String) condition.getFixedArg().getValue());
            }
        }
        return bean;
    }

    public static FirmwareRule convertModelRuleBeanToFirmwareRule(EnvModelRuleBean bean) {
        FirmwareRule rule = new FirmwareRule();

        rule.setRule(RuleFactory.newEnvModelRule(bean.getEnvironmentId(), bean.getModelId()));

        rule.setType(TemplateNames.ENV_MODEL_RULE);
        rule.setName(bean.getName());
        rule.setId(bean.getId());

        if (bean.getFirmwareConfig() != null) {
            rule.setApplicableAction(new RuleAction(bean.getFirmwareConfig().getId()));
        }

        return rule;
    }

}
