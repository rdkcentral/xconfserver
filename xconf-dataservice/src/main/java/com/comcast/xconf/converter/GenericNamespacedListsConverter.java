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
 * Created: 14.10.15  11:44
 */
package com.comcast.xconf.converter;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.NamespacedList;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GenericNamespacedListsConverter {

    private static final Map<String, IpAddress> ipAddressMap = new ConcurrentHashMap<>();

    public static IpAddressGroup convertToIpAddressGroup(GenericNamespacedList genericIpList) {
        IpAddressGroup result = new IpAddressGroup();
        final List<String> ipAddresses = new ArrayList<>(genericIpList.getData());
        result.setId(genericIpList.getId());
        result.setName(genericIpList.getId());
        result.setIpAddresses(Sets.newHashSet(stringsToIpAddresses(ipAddresses)));

        return result;
    }

    public static IpAddressGroupExtended convertToIpAddressGroupExtended(GenericNamespacedList genericIpList) {
        IpAddressGroupExtended result = new IpAddressGroupExtended();
        final List<String> ipAddresses = new ArrayList<>(genericIpList.getData());
        result.setId(genericIpList.getId());
        result.setName(genericIpList.getId());
        result.setIpAddresses(Sets.newHashSet(stringsToIpAddresses(ipAddresses)));

        return result;
    }

    public static NamespacedList convertToNamespacedList(GenericNamespacedList genericMacList) {
        final NamespacedList result = new NamespacedList();
        result.setId(genericMacList.getId());
        result.setData(genericMacList.getData());

        return result;
    }

    public static GenericNamespacedList convertFromNamespacedList(NamespacedList macList) {
        final GenericNamespacedList result = GenericNamespacedList.newMacList();
        result.setId(macList.getId());
        result.setData(macList.getData());

        return result;
    }

    public static GenericNamespacedList convertFromIpAddressGroupExtended(IpAddressGroupExtended ipGroup) {
        final GenericNamespacedList result = GenericNamespacedList.newIpList();
        result.setId(ipGroup.getName());
        Set<IpAddress> ipAddresses = ipGroup.getIpAddresses();
        if (ipAddresses.contains(null)) {
            ipAddresses.remove(null);
        }
        result.setData(Sets.newHashSet(Iterables.transform(ipAddresses, new Function<IpAddress, String>() {
            @Nullable
            @Override
            public String apply(@Nullable IpAddress ipAddress) {
                return ipAddress.toString();
            }
        })));

        return result;
    }

    public static List<NamespacedList> convertToListOfNamespacedLists(List<GenericNamespacedList> genericLists) {
        final List<NamespacedList> result = new ArrayList<>();
        for (GenericNamespacedList genericList : genericLists) {
            result.add(convertToNamespacedList(genericList));
        }

        return result;
    }

    public static List<IpAddressGroupExtended> convertToListOfIpAddressGroups(List<GenericNamespacedList> genericLists) {
        final List<IpAddressGroupExtended> result = new ArrayList<>();
        for (GenericNamespacedList genericList : genericLists) {
            result.add(convertToIpAddressGroupExtended(genericList));
        }

        return result;
    }

    public static IpAddress convertToIpAddress(String input) {
        if (ipAddressMap.containsKey(input)) {
            return ipAddressMap.get(input);
        } else {
            final IpAddress ipAddress = new IpAddress(input);
            ipAddressMap.put(input, ipAddress);
            return ipAddress;
        }
    }

    private static List<IpAddress> stringsToIpAddresses(List<String> input) {
        return Lists.transform(input, new Function<String, IpAddress>() {
            @Nullable
            @Override
            public IpAddress apply(@Nullable String s) {
                if (StringUtils.isNotBlank(s)) {
                    return convertToIpAddress(s);
                }
                return null;
            }
        });
    }
}
