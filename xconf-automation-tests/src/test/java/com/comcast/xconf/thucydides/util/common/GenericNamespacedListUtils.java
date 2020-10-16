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
 * Created: 3/18/16  2:01 PM
 */
package com.comcast.xconf.thucydides.util.common;

import com.beust.jcommander.internal.Lists;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GenericNamespacedListUtils {
    private static final String GENERIC_NS_LIST_URL = "genericnamespacedlist/";
    private static final String MAC_LIST_URL = GENERIC_NS_LIST_URL + GenericNamespacedListTypes.MAC_LIST;
    private static final String IP_LIST_URL = GENERIC_NS_LIST_URL + GenericNamespacedListTypes.IP_LIST;

    public static String defaultMacAddress = "11:11:11:11:11:11";
    public static String defaultIpAddress = "1.1.1.1";

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(GENERIC_NS_LIST_URL, GENERIC_NS_LIST_URL, GenericNamespacedList.class);
        GenericTestUtils.deleteEntities(GENERIC_NS_LIST_URL, GENERIC_NS_LIST_URL, GenericNamespacedList.class);
    }

    public static GenericNamespacedList createDefaultMacList() {
        return createGenericNsList("macList", GenericNamespacedListTypes.MAC_LIST, defaultMacAddress);
    }

    public static GenericNamespacedList createAndSaveDefaultMacList() throws Exception {
        return  saveGenericNamespacedList(createDefaultMacList(), GENERIC_NS_LIST_URL);
    }

    public static GenericNamespacedList createAndSaveMacList(String id, String nsListData) throws Exception {
        GenericNamespacedList macList = createGenericNsList(id, GenericNamespacedListTypes.MAC_LIST, nsListData);
        saveGenericNamespacedList(macList, GENERIC_NS_LIST_URL);
        return macList;
    }

    public static GenericNamespacedList createAndSaveIpList(String id, String nsListData) throws Exception {
        GenericNamespacedList ipList = createGenericNsList(id, GenericNamespacedListTypes.IP_LIST, nsListData);
        saveGenericNamespacedList(ipList, GENERIC_NS_LIST_URL);
        return ipList;
    }

    public static GenericNamespacedList createDefaultIpList() {
        return createGenericNsList("ipList", GenericNamespacedListTypes.IP_LIST, defaultIpAddress);
    }

    public static GenericNamespacedList createAndSaveDefaultIpList() throws Exception {
        return saveGenericNamespacedList(createDefaultIpList(), GENERIC_NS_LIST_URL);
    }

    private static GenericNamespacedList saveGenericNamespacedList(GenericNamespacedList list, String url) throws Exception {
        HttpClient.post(GenericTestUtils.buildFullUrl(url), list);

        return list;
    }

    private static GenericNamespacedList createGenericNsList(String id, String typeName, String data) {
        GenericNamespacedList result = new GenericNamespacedList();
        result.setId(id);
        result.setTypeName(typeName);
        result.setData(Collections.singleton(data));

        return result;
    }

    public static List<GenericNamespacedList> createAndSaveIpLists() throws Exception {
        return Lists.newArrayList(
            createAndSaveIpList("ipListId123", "12.12.12.12"),
            createAndSaveIpList("ipListId456", "13.13.13.13")
        );
    }

    public static GenericNamespacedList createAndSaveIpList(String id, Set<String> ipAddresses) throws Exception {
        GenericNamespacedList ipList = new GenericNamespacedList(GenericNamespacedListTypes.IP_LIST);
        ipList.setId(id);
        ipList.setData(ipAddresses);
        saveGenericNamespacedList(ipList, GENERIC_NS_LIST_URL);
        return ipList;
    }

    public static GenericNamespacedList createAndSaveMacList(String id, Set<String> macAddresses) throws Exception {
        GenericNamespacedList macList = new GenericNamespacedList(GenericNamespacedListTypes.MAC_LIST);
        macList.setId(id);
        macList.setData(macAddresses);
        saveGenericNamespacedList(macList, GENERIC_NS_LIST_URL);
        return macList;
    }

    public static List<GenericNamespacedList> createAndSaveMacLists() throws Exception {
        return Lists.newArrayList(
            createAndSaveMacList("macListId123", "A1:B1:C1:D1:E1:F1"),
            createAndSaveMacList("macListId456", "A2:B2:C2:D2:E2:F2")
        );
    }
}
