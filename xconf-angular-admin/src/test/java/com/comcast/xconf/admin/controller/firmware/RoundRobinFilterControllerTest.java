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
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.DownloadLocationRoundRobinFilterValue;
import com.comcast.xconf.queries.QueriesHelper;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RoundRobinFilterControllerTest extends BaseControllerTest {

    @Test
    public void testExport() throws Exception {
        DownloadLocationRoundRobinFilterValue filter = createDownloadLocationFilter();
        singletonFilterValueDAO.setOne(filter.getId(), filter);

        MockHttpServletResponse response = mockMvc.perform(
                get(roundRobinFilterGetUrl(STB)).param("export", "export"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(QueriesHelper.nullifyUnwantedFields(filter))))
                .andReturn().getResponse();

        assertEquals(Sets.newHashSet("Content-Disposition", "Content-Type"), response.getHeaderNames());
    }

    @Test
    public void testGetDownloadLocationRoundRobinFilter() throws Exception {
        DownloadLocationRoundRobinFilterValue filter = createDownloadLocationFilter();
        singletonFilterValueDAO.setOne(DownloadLocationRoundRobinFilterValue.SINGLETON_ID, filter);
        filter = QueriesHelper.nullifyUnwantedFields(filter);

        mockMvc.perform(
                get(roundRobinFilterGetUrl(STB)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(filter)));
    }

    @Test
    public void getRoundRobinFilterByApplication() throws Exception {
        mockMvc.perform(
                get(roundRobinFilterGetUrl(XHOME)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationType").value(XHOME));
    }

    @Test
    public void testUpdateDownloadLocationRoundRobinFilter() throws Exception {
        DownloadLocationRoundRobinFilterValue filter = createDownloadLocationFilter();
        singletonFilterValueDAO.setOne(DownloadLocationRoundRobinFilterValue.SINGLETON_ID, filter);
        filter.setHttpLocation("http://www.changedLocation.com");
        filter = QueriesHelper.nullifyUnwantedFields(filter);

        mockMvc.perform(
                post("/" + RoundRobinFilterController.URL_MAPPING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationType").value(STB));
    }

    private String roundRobinFilterGetUrl(String applicationType) {
        return "/" + RoundRobinFilterController.URL_MAPPING + "/" + applicationType;
    }
}