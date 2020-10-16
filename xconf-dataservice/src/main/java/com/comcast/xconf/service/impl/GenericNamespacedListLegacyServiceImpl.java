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
 * <p/>
 * Author: Stanislav Menshykov
 * Created: 1/21/16  4:49 PM
 */
package com.comcast.xconf.service.impl;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.NamespacedList;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.comcast.xconf.service.GenericNamespacedListLegacyService;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenericNamespacedListLegacyServiceImpl implements GenericNamespacedListLegacyService {
    @Autowired
    private CachedSimpleDao<String, GenericNamespacedList> genericNamespacedListDAO;
    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;


    @Override
    public List<IpAddressGroupExtended> getAllIpAddressGroups() {
        return GenericNamespacedListsConverter.convertToListOfIpAddressGroups(
                                        genericNamespacedListQueriesService.getAllByType(GenericNamespacedListTypes.IP_LIST));
    }

    @Override
    public List<NamespacedList> getAllNamespacedLists() {
        return GenericNamespacedListsConverter.convertToListOfNamespacedLists(
                genericNamespacedListQueriesService.getAllByType(GenericNamespacedListTypes.MAC_LIST));
    }

    @Override
    public IpAddressGroupExtended getIpAddressGroup(String id) {
        GenericNamespacedList genericNamespacedList = genericNamespacedListDAO.getOne(id);

        return genericNamespacedList != null ? GenericNamespacedListsConverter.convertToIpAddressGroupExtended(genericNamespacedList) : null;
    }

    @Override
    public NamespacedList getNamespacedList(String id) {
        GenericNamespacedList genericNamespacedList = genericNamespacedListDAO.getOne(id);

        return genericNamespacedList != null ? GenericNamespacedListsConverter.convertToNamespacedList(genericNamespacedList) : null;
    }

    public boolean isChangedIpAddressGroup(IpAddressGroup ipAddressGroup) {
        if (ipAddressGroup != null && StringUtils.isNotBlank(ipAddressGroup.getName())) {
            IpAddressGroupExtended existedIpAddressGroup = getIpAddressGroup(ipAddressGroup.getName());
            if (existedIpAddressGroup != null) {
                return  !(CollectionUtils.isEqualCollection(existedIpAddressGroup.getIpAddresses(), ipAddressGroup.getIpAddresses()));
            }
        }
        return true;
    }
}
