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
 * Created: 7/12/2017
*/
package com.comcast.xconf.util;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.google.common.collect.Sets;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenericNamespacedListUtilsTest {

    @Test
    public void isInIpRange_ValidIp() throws Exception {
        GenericNamespacedList ipList = createGenericNamespacedList("12.34.56.78/31", "1.2.3.4");

        assertTrue(GenericNamespacedListUtils.isInIpRange(ipList, "12.34.56.79"));
        assertTrue(GenericNamespacedListUtils.isInIpRange(ipList, "1.2.3.4"));
        assertFalse(GenericNamespacedListUtils.isInIpRange(ipList, "12.34.56.1"));
        assertFalse(GenericNamespacedListUtils.isInIpRange(ipList, "1.1.1.1"));
    }

    @Test
    public void isInIpRange_InvalidIp() throws Exception {
        GenericNamespacedList ipList = createGenericNamespacedList("1.2.3.4");

        assertFalse(GenericNamespacedListUtils.isInIpRange(ipList, "abcd"));
        assertFalse(GenericNamespacedListUtils.isInIpRange(ipList, "1.1.1"));
    }

    protected GenericNamespacedList createGenericNamespacedList(String... data) {
        GenericNamespacedList namespacedList = new GenericNamespacedList(GenericNamespacedListTypes.IP_LIST);
        namespacedList.setId("ipListName");
        namespacedList.setData(Sets.newHashSet(data));
        return namespacedList;
    }
}
