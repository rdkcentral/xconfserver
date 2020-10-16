/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.admin.controller.shared;

import com.comcast.apps.dataaccess.cache.dao.impl.TwoKeys;
import com.comcast.xconf.CfNames;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChangeLogControllerTest extends BaseControllerTest {

    @After
    @Before
    public void cleanData() {
        Iterable<TwoKeys<Long, UUID>> keys = changeLogDao.getKeys();
        for (TwoKeys<Long, UUID> key : keys) {
            changeLogDao.deleteOne(key.getKey(), key.getKey2());
        }
    }

    @Test
    public void testGetChangeLog() throws Exception {
        GenericNamespacedList macList = createMacList();
        if (genericNamespacedListQueriesService.getOneByType(macList.getId(), GenericNamespacedListTypes.MAC_LIST) != null) {
            genericNamespacedListQueriesService.updateNamespacedList(macList, GenericNamespacedListTypes.MAC_LIST);
        } else {
            genericNamespacedListQueriesService.createNamespacedList(macList, GenericNamespacedListTypes.MAC_LIST);
        }

        MockHttpServletResponse response = mockMvc.perform(get("/" + ChangeLogController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse();
        Map<Long, List<ChangeLogController.Change>> changelog = mapper.readValue(response.getContentAsString(), new TypeReference<Map<Long, List<ChangeLogController.Change>>>(){});
        for (List<ChangeLogController.Change> changes : changelog.values()) {
            for (ChangeLogController.Change change : changes) {
                Assert.assertEquals(CfNames.Common.GENERIC_NS_LIST, change.getCfName());
            }
        }
    }
}