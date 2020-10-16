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
 * Created: 1/22/2016
*/
package com.comcast.xconf.estbfirmware.legacy;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.estbfirmware.FirmwareRule;
import com.comcast.xconf.estbfirmware.RebootImmediatelyFilter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RebootImmediatelyLegacyConverter {

    @Autowired
    private LegacyConverterHelper converterHelper;


    public RebootImmediatelyFilter convertFirmwareRuleToRebootFilter(FirmwareRule firmwareRule) {
        RebootImmediatelyFilter filter = new RebootImmediatelyFilter();

        filter.setName(firmwareRule.getName());
        filter.setId(firmwareRule.getId());

        convertConditions(filter, firmwareRule);

        return filter;
    }

    private void convertConditions(RebootImmediatelyFilter filter, FirmwareRule next) {
        for (Condition condition : RuleUtil.toConditions(next)) {
            convertCondition(filter, condition);
        }
    }

    private void convertCondition(RebootImmediatelyFilter filter, Condition condition) {
        if (FirmwareRule.IP.equals(condition.getFreeArg())
                || RuleFactory.IP.equals(condition.getFreeArg())) {
            if (filter.getIpAddressGroups() == null) {
                filter.setIpAddressGroups(new HashSet<IpAddressGroup>());
            }
            filter.getIpAddressGroups().add(converterHelper.getIpAddressGroup(condition));

        } else if (FirmwareRule.MAC.equals(condition.getFreeArg())
                    || RuleFactory.MAC.equals(condition.getFreeArg())) {
            HashSet<String> macAddresses = new HashSet<>();
            if (condition.getFixedArg().getValue() instanceof Collection) {
                Iterator iterator = ((Collection) condition.getFixedArg().getValue()).iterator();
                while (iterator.hasNext()) {
                    Object macObject = iterator.next();
                    if (macObject instanceof MacAddress) {
                        MacAddress mac = (MacAddress) macObject;
                        macAddresses.add(mac.toString());
                    } else if (macObject instanceof Map) {
                        Map<String, String> macAddressMap = (Map<String, String>) macObject;
                        macAddresses.addAll(macAddressMap.values());
                    } else if (macObject instanceof String) {
                        macAddresses.add((String) macObject);
                    }
                }
            } else {
                macAddresses.add(condition.getFixedArg().getValue().toString());
            }

            filter.setMacAddresses(StringUtils.join(macAddresses, "\n"));

        } else if (FirmwareRule.ENV.equals(condition.getFreeArg())) {
            filter.setEnvironments(fixedArgValueToCollection(condition));

        } else if (FirmwareRule.MODEL.equals(condition.getFreeArg())) {
            filter.setModels(fixedArgValueToCollection(condition));
        }
    }

    public FirmwareRule convertRebootFilterToFirmwareRule(RebootImmediatelyFilter filter) {

        FirmwareRule rule = FirmwareRule.newRebootImmediatelyFilter(filter.getIpAddressGroups(),
                getNormalizedMacAddresses(filter.getMacAddresses()),
                filter.getEnvironments(),
                filter.getModels());

        rule.setName(filter.getName());
        rule.setId(filter.getId());

        return rule;
    }

    public Set<MacAddress> getNormalizedMacAddresses(String macAddresses) {
        Set<MacAddress> set = Sets.newHashSet();
        if (StringUtils.isBlank(macAddresses)) {
            return set;
        }
        String[] a = macAddresses.split("\\s+");
        for (String ma : a) {
            if (MacAddress.isValid(ma)) {
                set.add(new MacAddress(ma));
            }
        }
        return set;
    }

    private Set<String> fixedArgValueToCollection(Condition condition) {
        Object fixedArgValue = condition.getFixedArg().getValue();
        if (fixedArgValue instanceof Collection) {
            return new HashSet<>((Collection) fixedArgValue);
        } else {
            return Collections.singleton((String) fixedArgValue);
        }
    }
}
