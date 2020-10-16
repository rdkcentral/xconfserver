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
 * Author: ikostrov
 * Created: 01.09.15 16:59
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.queries.beans.StringListWrapper;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.Set;

import static com.comcast.xconf.queries.QueryConstants.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IpAddressGroupsQueriesControllerTest extends BaseQueriesControllerTest {

    @Test
    public void verifyCreateAndDelete() throws Exception {
        IpAddressGroupExtended group = createDefaultIpAddressGroupExtended();
        group.setId(group.getName());
        mockMvc.perform(post("/" + UPDATE_IP_ADDRESS_GROUPS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(group)))
                .andExpect(status().isCreated());

        group.setUpdated(null);
        Set<IpAddressGroupExtended> set = Collections.singleton(group);
        performRequestAndVerifyResponse(QUERIES_IP_ADDRESS_GROUPS, set);
        performRequestAndVerifyResponse(QUERIES_IP_ADDRESS_GROUPS + "/byIp/{ipAddress}/", defaultIpAddress, set);
        performRequestAndVerifyResponse(QUERIES_IP_ADDRESS_GROUPS + "/byName/{name}/", group.getName(), set);


        mockMvc.perform(delete("/" + DELETE_IP_ADDRESS_GROUPS + "/{id}/", group.getId()))
                .andExpect(status().isNoContent());

        performRequestAndVerifyResponse(QUERIES_IP_ADDRESS_GROUPS, Collections.emptyList());
    }

    @Test
    public void addNamespacedListDataTest() throws Exception {
        IpAddressGroupExtended group = createDefaultIpAddressGroupExtended();
        group.setId(group.getName());
        String listId = group.getId();
        mockMvc.perform(post("/" + UPDATE_IP_ADDRESS_GROUPS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(group)))
                .andExpect(status().isCreated());

        String ipToAdd = "2.2.2.2";
        StringListWrapper stringListWrapper = wrapList(ipToAdd);

        mockMvc.perform(post("/" + UPDATE_IP_ADDRESS_GROUPS + "/{listId}/addData", listId).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(stringListWrapper)))
                .andExpect(status().isOk());

        GenericNamespacedList namespacedListInResponse = genericNamespacedListDAO.getOne(listId);
        assertEquals(group.getId(), namespacedListInResponse.getId());
        assertEquals((group.getIpAddresses().size() + stringListWrapper.getList().size()), namespacedListInResponse.getData().size());
        assertTrue(namespacedListInResponse.getData().contains(ipToAdd));
        assertTrue(namespacedListInResponse.getData().containsAll(stringListWrapper.getList()));
    }

    @Test
    public void removeNamespacedListDataTest() throws Exception {
        String ipToRemove = "2.2.2.2";
        Set<String> ipAddresses = Sets.newHashSet(defaultIpAddress, ipToRemove);
        IpAddressGroupExtended group = createIpAddressGroupExtended(ipAddresses);

        mockMvc.perform(post("/" + UPDATE_IP_ADDRESS_GROUPS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(group)))
                .andExpect(status().isCreated());

        StringListWrapper stringListWrapper = wrapList(ipToRemove);

        mockMvc.perform(post("/" + UPDATE_IP_ADDRESS_GROUPS + "/{listId}/removeData", group.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(stringListWrapper)))
                .andExpect(status().isOk());

        GenericNamespacedList namespacedListInResponse = genericNamespacedListDAO.getOne(group.getId());
        assertEquals(group.getId(), namespacedListInResponse.getId());
        assertEquals((group.getIpAddresses().size() - stringListWrapper.getList().size()), namespacedListInResponse.getData().size());
        assertTrue(namespacedListInResponse.getData().contains(defaultIpAddress));
        assertFalse(namespacedListInResponse.getData().contains(ipToRemove));
    }

    @Test
    public void createIpAddressGroupWithWhitespaceInName() throws Exception {
        IpAddressGroupExtended ipAddressGroup = createDefaultIpAddressGroupExtended();
        ipAddressGroup.setId("test id");

        mockMvc.perform(post("/" + UPDATE_IP_ADDRESS_GROUPS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(ipAddressGroup)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(ipAddressGroup)));

        assertNotNull(genericNamespacedListDAO.getOne(ipAddressGroup.getName()));
    }

    private StringListWrapper wrapList(String ipToAdd) {
        StringListWrapper stringListWrapper = new StringListWrapper();
        stringListWrapper.setList(Collections.singletonList(ipToAdd));
        return stringListWrapper;
    }
}
