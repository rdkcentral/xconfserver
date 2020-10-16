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
 *  Author: mdolina
 *  Created: 5:35 PM
 */
package com.comcast.xconf.util;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.comcast.xconf.dcm.core.Utils.collectionToUpperCase;

public class GenericNamespacedListUtils {

    public static boolean isInIpRange(final GenericNamespacedList nsList, String ipAddressStr) {
        if (!IpAddress.isValid(ipAddressStr)) {
            return false;
        }
        IpAddress ipAddress = IpAddress.parse(ipAddressStr);

        for (String ip : nsList.getData()) {
            if (GenericNamespacedListsConverter.convertToIpAddress(ip).isInRange(ipAddress)) {
                return true;
            }
        }
        return false;
    }

    public static void validateListData(final GenericNamespacedList namespacedList) {
        validateListData(namespacedList.getTypeName(), namespacedList.getData());
    }

    public static void validateListData(final String listType, Set<String> listData) {
        if (CollectionUtils.isEmpty(listData)) {
            throw new ValidationRuntimeException("List must not be empty");
        }

        if (GenericNamespacedListTypes.IP_LIST.equals(listType)) {
            validateIpAddress(listData);
        }

        if (GenericNamespacedListTypes.MAC_LIST.equals(listType)) {
            validateMacAddress(listData);
        }
    }

    public static void validateMacAddress(Set<String> macListData) {
        final Set<String> invalidMacs = new HashSet<>();
        final Set<String> normalizedMacs = new HashSet<>();
        for (String mac : macListData) {
            if (!MacAddress.isValid(mac)) {
                invalidMacs.add(mac);
            } else {
                normalizedMacs.add(MacAddress.normalize(mac));
            }
        }

        if (!invalidMacs.isEmpty()) {
            throw new ValidationRuntimeException("List contains invalid address(es): " + invalidMacs);
        }
    }

    public static void validateIpAddress(Set<String> ipAddresses) {
        final Set<String> invalidIpAddresses = new HashSet<>();

        for (String ipAddress : ipAddresses) {
            if (!IpAddressUtils.isValidIpAddress(ipAddress))
                invalidIpAddresses.add(ipAddress);
        }

        if (!invalidIpAddresses.isEmpty()) {
            throw new ValidationRuntimeException("List contains invalid address(es): " + invalidIpAddresses);
        }
    }

    public static void validateDataIntersection(GenericNamespacedList nsList, Set<String> nsListData, Iterable<GenericNamespacedList> namespacedListsByType) {
        if (GenericNamespacedListTypes.MAC_LIST.equals(nsList.getTypeName())) {
            Map<Sets.SetView<String>, String> intersectionMap = new HashedMap();
            for (GenericNamespacedList nsListByType : namespacedListsByType) {
                Sets.SetView<String> intersection = Sets.intersection(collectionToUpperCase(nsListData), collectionToUpperCase(nsListByType.getData()));
                if (!intersection.isEmpty() && !nsListByType.getId().equals(nsList.getId())) {
                    intersectionMap.put(intersection, nsListByType.getId());
                }
            }
            if (!intersectionMap.isEmpty()) {
                String errorMessage = "MAC addresses are already used in other lists: ";
                errorMessage += Joiner.on(", ").withKeyValueSeparator(" in ").join(intersectionMap);
                throw new EntityConflictException(errorMessage);
            }
        }
    }

    public static Set<String> normalizeMacAddress(Set<String> macAddresses) {
        Set<String> result = new HashSet<>();
        for (String address : macAddresses) {
            result.add(MacAddress.normalize(address));
        }

        return result;
    }

    public static String getTypeName(String type) {
        if (GenericNamespacedListTypes.IP_LIST.equals(type)) {
            return "IpList";
        } else if (GenericNamespacedListTypes.MAC_LIST.equals(type)) {
            return "MacList";
        }
        return type;
    }
}
