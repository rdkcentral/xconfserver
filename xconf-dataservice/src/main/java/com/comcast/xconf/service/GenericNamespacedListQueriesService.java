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
 * Created: 09.10.15  10:42
 */
package com.comcast.xconf.service;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.queries.beans.StringListWrapper;

import java.util.List;
import java.util.Set;

public interface GenericNamespacedListQueriesService {

    List<String> getNamespacedListsIds();

    List<String> getNamespacedListsIdsByType(String typeName);

    List<GenericNamespacedList> getAllNamespacedLists();

    List<GenericNamespacedList> getAllByType(String typeName);

    List<GenericNamespacedList> getListsByIds(final Set<String> ids);

    GenericNamespacedList getListById(final String listId);

    GenericNamespacedList createNamespacedList(final GenericNamespacedList list, final String type);

    GenericNamespacedList updateNamespacedList(final GenericNamespacedList list, final String type);

    GenericNamespacedList updateNamespacedList(final GenericNamespacedList list, final String type, final String newId);

    GenericNamespacedList deleteNamespacedList(final String typeName, final String id);

    List<GenericNamespacedList> getListsByMacPart(final String macPart);

    GenericNamespacedList addNamespacedListData(String listId, String listType, StringListWrapper macList);

    GenericNamespacedList removeNamespacedListData(final String listId, String listType, final StringListWrapper wrappedMacList);

    List<GenericNamespacedList> getListsByIp(String ip);

    GenericNamespacedList getOneNonCached(String id);

    void checkUsage(String id);

    GenericNamespacedList getOneByType(final String id, final String type);

    GenericNamespacedList getOneByTypeNonCached(final String id, final String type);

    List<GenericNamespacedList> getNonCachedAllByType(final String type);

    boolean isMacListHasMacPart(String macPart, Set<String> macs);

    boolean isIpAddressHasIpPart(final String ipPart, Set<String> ipAddresses);
}
