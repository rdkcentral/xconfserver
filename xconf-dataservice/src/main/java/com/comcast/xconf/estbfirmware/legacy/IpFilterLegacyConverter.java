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
import com.comcast.xconf.estbfirmware.FirmwareRule;
import com.comcast.xconf.estbfirmware.IpFilter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.util.RuleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IpFilterLegacyConverter {

    @Autowired
    private LegacyConverterHelper converterHelper;

    public IpFilter convertFirmwareRuleToIpFilter(FirmwareRule firmwareRule){
        IpFilter filter = new IpFilter();

        filter.setName(firmwareRule.getName());
        filter.setId(firmwareRule.getId());
        for (Condition condition : RuleUtil.toConditions(firmwareRule)) {
            if (RuleFactory.IP.equals(condition.getFreeArg())) {
                filter.setIpAddressGroup(converterHelper.getIpAddressGroup(condition));
            }
        }

        return filter;
    }

    public FirmwareRule convertIpFilterToFirmwareRule(IpFilter ipFilter){

        FirmwareRule rule = FirmwareRule.newIpFilter(ipFilter.getIpAddressGroup());
        rule.setName(ipFilter.getName());
        rule.setId(ipFilter.getId());
        return rule;
    }

}
