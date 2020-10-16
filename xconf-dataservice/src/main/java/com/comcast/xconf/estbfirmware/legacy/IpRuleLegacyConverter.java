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
package com.comcast.xconf.estbfirmware.legacy;

import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.FirmwareRule;
import com.comcast.xconf.estbfirmware.IpRuleBean;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IpRuleLegacyConverter {

    @Autowired
    private LegacyConverterHelper converterHelper;

    public FirmwareRule convertIpRuleBeanToFirmwareRule(IpRuleBean bean) {
        FirmwareRule ipRule = FirmwareRule.newIpRule(bean.getIpAddressGroup(), bean.getEnvironmentId(), bean.getModelId());

        ipRule.setId(bean.getId());
        ipRule.setName(bean.getName());

        if (bean.getFirmwareConfig() != null) {
            ipRule.setBoundConfigId(bean.getFirmwareConfig().getId());
        }

        return ipRule;
    }

    public IpRuleBean convertFirmwareRuleToIpRuleBean(FirmwareRule firmwareRule) {
        IpRuleBean bean = new IpRuleBean();

        bean.setName(firmwareRule.getName());
        bean.setId(firmwareRule.getId());

        List<Rule> rules = firmwareRule.getCompoundParts();
        for (Rule r : rules) {
            Condition cond = r.getCondition();
            if (FirmwareRule.IP.equals(cond.getFreeArg())
                    || RuleFactory.IP.equals(cond.getFreeArg())) {
                bean.setIpAddressGroup(converterHelper.getIpAddressGroup(cond));
            } else if (FirmwareRule.ENV.equals(cond.getFreeArg())) {
                bean.setEnvironmentId((String) cond.getFixedArg().getValue());
            } else if (FirmwareRule.MODEL.equals(cond.getFreeArg())) {
                bean.setModelId((String) cond.getFixedArg().getValue());
            }
        }

        return bean;
    }

}
