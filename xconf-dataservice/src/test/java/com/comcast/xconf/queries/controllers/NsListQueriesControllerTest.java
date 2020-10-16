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
 * Created: 01.09.15 21:06
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.NamespacedList;
import com.comcast.xconf.queries.beans.StringListWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.*;

import static com.comcast.xconf.queries.QueryConstants.QUERIES_NS_LISTS;
import static com.comcast.xconf.queries.QueryConstants.UPDATE_NS_LISTS;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NsListQueriesControllerTest extends BaseQueriesControllerTest {


    @Test
    public void getListsTest() throws Exception {
        NamespacedList namespacedList = createNamespacedList();
        mockMvc.perform(post("/" + UPDATE_NS_LISTS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(namespacedList)))
                .andExpect(status().isCreated());

        NsListQueriesController.nullifyUnwantedFields(namespacedList);
        performRequestAndVerifyResponse(QUERIES_NS_LISTS, Collections.singleton(namespacedList));
        performRequestAndVerifyResponse(QUERIES_NS_LISTS + "/byId/{listId}", namespacedList.getId(), namespacedList);

        String mac = namespacedList.getData().iterator().next();
        performRequestAndVerifyResponse(QUERIES_NS_LISTS + "/byMacPart/{macPart}", mac, Lists.newArrayList(namespacedList));
    }

    @Test
    public void addNamespacedListDataTest() throws Exception {
        String listId = "ListID";
        NamespacedList namespacedList = createNamespacedList("ListID");
        mockMvc.perform(post("/" + UPDATE_NS_LISTS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(namespacedList)))
                .andExpect(status().isCreated());
        StringListWrapper stringListWrapper = new StringListWrapper("11:22:33:44:55:66");

        mockMvc.perform(post("/" + UPDATE_NS_LISTS + "/{listId}/addData", listId).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(stringListWrapper)))
                .andExpect(status().isOk());

        GenericNamespacedList namespacedListInResponse = genericNamespacedListDAO.getOne(listId);
        assertEquals(namespacedList.getId(), namespacedListInResponse.getId());
        assertEquals((namespacedList.getData().size() + stringListWrapper.getList().size()), namespacedListInResponse.getData().size());
        assertTrue(namespacedListInResponse.getData().containsAll(namespacedList.getData()));
        assertTrue(namespacedListInResponse.getData().containsAll(stringListWrapper.getList()));
    }

    @Test
    public void verifyLatestChangesArePeekedFromDBBeforeAddingNewMac() throws Exception {
        String listId = "ListID";
        NamespacedList namespacedList = createNamespacedList("ListID");
        mockMvc.perform(post("/" + UPDATE_NS_LISTS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(namespacedList)))
                .andExpect(status().isCreated());

        String mac1 = "11:11:11:11:11:11";
        GenericNamespacedList genericNamespacedList = genericNamespacedListDAO.getOne(listId);
        genericNamespacedList.getData().add(mac1);
        nonCachedNamespacedListDao.setOne(listId, genericNamespacedList);

        String mac2 = "22:22:22:22:22:22";
        mockMvc.perform(post("/" + UPDATE_NS_LISTS + "/{listId}/addData", listId).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new StringListWrapper(mac2))))
                .andExpect(status().isOk());

        GenericNamespacedList one = nonCachedNamespacedListDao.getOne(listId);
        assertTrue(one.getData().contains(mac1));
        assertTrue(one.getData().contains(mac2));
    }

    @Test
    public void getNotExistedNamespacedList() throws Exception {
        mockMvc.perform(get("/" + QUERIES_NS_LISTS + "/byId/" + "wrongId"))
                .andExpect(status().isOk());
    }

    @Test
    public void removeNamespacedListDataTest() throws Exception {
        String macId = "ListID";
        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setId(macId);
        String macToLeave = "11:22:33:44:55:66";
        String macToRemove = "22:33:44:55:66:77";
        namespacedList.setData(Sets.newHashSet(macToLeave, macToRemove));
        mockMvc.perform(post("/" + UPDATE_NS_LISTS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(namespacedList)))
                .andExpect(status().isCreated());
        StringListWrapper stringListWrapper = new StringListWrapper(macToRemove);

        mockMvc.perform(post("/" + UPDATE_NS_LISTS + "/{listId}/removeData", macId).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(stringListWrapper)))
                .andExpect(status().isOk());

        GenericNamespacedList namespacedListInResponse = genericNamespacedListDAO.getOne(macId);
        assertEquals(namespacedList.getId(), namespacedListInResponse.getId());
        assertEquals((namespacedList.getData().size() - stringListWrapper.getList().size()), namespacedListInResponse.getData().size());
        assertTrue(namespacedListInResponse.getData().contains(macToLeave));
        assertFalse(namespacedListInResponse.getData().contains(macToRemove));
    }

    @Test
    public void verifyLatestChangesArePeekedFromDBBeforeDeleteOperation() throws Exception {
        String newMac = "11:11:11:11:11:11";
        String macToRemove = "22:22:22:22:22:22";
        String listId = "ListID";
        NamespacedList namespacedList = createNamespacedList("ListID");
        namespacedList.getData().add(macToRemove);
        mockMvc.perform(post("/" + UPDATE_NS_LISTS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(namespacedList)))
                .andExpect(status().isCreated());

        GenericNamespacedList genericNamespacedList = genericNamespacedListDAO.getOne(listId);
        genericNamespacedList.getData().add(newMac);
        nonCachedNamespacedListDao.setOne(listId, genericNamespacedList);

        mockMvc.perform(post("/" + UPDATE_NS_LISTS + "/{listId}/removeData", listId).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new StringListWrapper(macToRemove))))
                .andExpect(status().isOk());

        GenericNamespacedList one = nonCachedNamespacedListDao.getOne(listId);
        assertTrue(one.getData().contains(newMac));
        assertFalse(one.getData().contains(macToRemove));
    }

    @Test
    public void createNamespacedListWithWhitespaceInName() throws Exception {
        NamespacedList namespacedList = createNamespacedList("test id");
        namespacedList.setTypeName(GenericNamespacedListTypes.MAC_LIST);

        mockMvc.perform(post("/" + UPDATE_NS_LISTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(NsListQueriesController.nullifyUnwantedFields(namespacedList))))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(namespacedList)));

        assertNotNull(genericNamespacedListDAO.getOne(namespacedList.getId()));
    }

    @Test
    public void macListDataIsValidatedNotUsingCache() throws Exception {
        GenericNamespacedList macList = createGenericNamespacedList(defaultMacListId, GenericNamespacedListTypes.MAC_LIST, defaultMacAddress);
        nonCachedNamespacedListDao.setOne(macList.getId(), macList);

        NamespacedList namespacedList = createNamespacedList();
        namespacedList.setData(macList.getData());

        mockMvc.perform(post("/" + UPDATE_NS_LISTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(namespacedList)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"MAC addresses are already used in other lists: [11:11:11:11:11:11] in macListId\""));
    }

    private NamespacedList createNamespacedList() {
        String mac = "AA:BB:CC:DD:EE:FF";
        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setId("NsListID");
        namespacedList.setData(new HashSet<>(Collections.singleton(mac)));
        return namespacedList;
    }

    private NamespacedList createNamespacedList(String id) {
        NamespacedList namespacedList = createNamespacedList();
        namespacedList.setId(id);
        return namespacedList;
    }
}
