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

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StatisticsControllerTest extends BaseControllerTest {

    @Test
    public void testStatistic() throws Exception {
        GenericNamespacedList macList = createMacList();
        if (genericNamespacedListQueriesService.getOneByType(macList.getId(), GenericNamespacedListTypes.MAC_LIST) != null) {
            genericNamespacedListQueriesService.updateNamespacedList(macList, GenericNamespacedListTypes.MAC_LIST);
        } else {
            genericNamespacedListQueriesService.createNamespacedList(macList, GenericNamespacedListTypes.MAC_LIST);
        }

        mockMvc.perform(get("/" + StatisticsController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        MockHttpServletResponse response = mockMvc.perform(get("/" + StatisticsController.URL_MAPPING + "/cache/"+ "GenericXconfNamedList" + "/reload")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse();
        Map<String, Long> cacheInfo = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String, Long>>() {});
        assertTrue(cacheInfo.get("cacheSize").intValue() > 0);

        MockHttpServletResponse response1 = mockMvc.perform(
                get("/" + StatisticsController.URL_MAPPING + "/cache/reloadAll")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse();
        Map<String, Map<String, Long>> cacheInfoMap = mapper.readValue(response1.getContentAsString(), new TypeReference<Map<String, Map<String, Long>>>() {});
        assertTrue(cacheInfoMap.get("GenericXconfNamedList").get("cacheSize").intValue() > 0);
    }
}