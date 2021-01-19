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
package com.comcast.xconf.estbfirmware.converter;

import com.comcast.apps.hesperius.ruleengine.domain.additional.AuxFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConverterHelper {

    private static final Logger log = LoggerFactory.getLogger(ConverterHelper.class);

    @Autowired
    private GenericNamespacedListQueriesService listService;

    public IpAddressGroup getIpAddressGroup(Condition cond) {
        Operation operation = cond.getOperation();
        if (RuleFactory.IN_LIST.equals(operation)) {
            String listId = (String) cond.getFixedArg().getValue();
            GenericNamespacedList list = listService.getListById(listId);
            return list != null ? GenericNamespacedListsConverter.convertToIpAddressGroup(list) : makeIpAddressGroup(listId);
        } else if (StandardOperation.IN.equals(operation)) {
            return (IpAddressGroup) cond.getFixedArg().getValue();
        } else {
            log.warn("Unknown operation for IP freeArg: " + operation);
            return new IpAddressGroup();
        }
    }

    public IpAddressGroup makeIpAddressGroup(String id) {
        IpAddressGroup group = new IpAddressGroup();
        group.setId(id);
        group.setName(id);
        return group;
    }

    public static boolean isLegacyIpCondition(Condition condition) {
        return isLegacyIpFreeArg(condition.getFreeArg())
                && StandardOperation.IN.equals(condition.getOperation())
                && condition.getFixedArg().getValue() instanceof IpAddressGroup;
    }

    public static boolean isLegacyIpFreeArg(FreeArg freeArg) {
        return AuxFreeArgType.IP_ADDRESS.equals(freeArg.getType())
                && StbContext.IP_ADDRESS.equals(freeArg.getName());
    }

    public static boolean isLegacyMacFreeArg(FreeArg freeArg) {
        return AuxFreeArgType.MAC_ADDRESS.equals(freeArg.getType())
                && StbContext.ESTB_MAC.equals(freeArg.getName());
    }

    public static boolean isLegacyLocalTimeFreeArg(FreeArg freeArg) {
        return FreeArgType.forName("LOCAL_TIME").equals(freeArg.getType())
                && StbContext.TIME.equals(freeArg.getName());
    }

}
