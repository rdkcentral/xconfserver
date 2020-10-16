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
 * Author: Stanislav Menshykov
 * Created: 02.10.15  12:09
 */
package com.comcast.xconf.evaluators;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.hesperius.ruleengine.domain.additional.AuxFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.domain.standard.BaseEvaluator;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IpAddressGroupEvaluator extends BaseEvaluator {

    private static Logger log = LoggerFactory.getLogger(IpAddressGroupEvaluator.class);

    @Autowired
    private CachedSimpleDao<String, GenericNamespacedList> nsListDao;

    public IpAddressGroupEvaluator() {
        super(AuxFreeArgType.IP_ADDRESS, Operation.forName("IN_LIST"), new Class[]{IpAddressGroup.class});
    }

    @Override
    protected boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
        GenericNamespacedList nsList = nsListDao.getOne((String) fixedArgValue, false);
        if (nsList == null) {
            log.warn("Can't evaluate rule because NsList doesn't exist. ID: " + fixedArgValue);
            return false;
        }
        IpAddressGroup ipAddressGroup = GenericNamespacedListsConverter.convertToIpAddressGroup(nsList);
        return ipAddressGroup.isInRange(freeArgValue);
    }
}
