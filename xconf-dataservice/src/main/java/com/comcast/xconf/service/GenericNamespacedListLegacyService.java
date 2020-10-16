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
 * Created: 1/21/16  4:48 PM
 */
package com.comcast.xconf.service;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.NamespacedList;

import java.util.List;

public interface GenericNamespacedListLegacyService {

    List<IpAddressGroupExtended> getAllIpAddressGroups();

    List<NamespacedList> getAllNamespacedLists();

    IpAddressGroupExtended getIpAddressGroup(String id);

    NamespacedList getNamespacedList(String id);

    boolean isChangedIpAddressGroup(IpAddressGroup ipAddressGroup);
}
