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
 * Created: 28.08.15 19:00
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.estbfirmware.RebootImmediatelyFilter;
import com.comcast.xconf.queries.QueryConstants;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.*;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RiFilterQueriesControllerTest extends BaseQueriesControllerTest {

    @Test
    public void testGetFilter() throws Exception {
        RebootImmediatelyFilter filter = createFilter();
        mockMvc.perform(post("/" + QueryConstants.UPDATES_FILTERS_RI).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(filter)))
                .andExpect(status().isCreated());

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_RI, Collections.singleton(filter));
        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_RI + "/{name}", filter.getName(), filter);

        mockMvc.perform(delete("/" + QueryConstants.DELETES_FILTERS_RI + "/{name}", filter.getName()))
                .andExpect(status().isNoContent());

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_RI, Collections.emptyList());
    }

    @Test
    public void getFiltersByApplicationType() throws Exception {
        Map<String, RebootImmediatelyFilter> riFilters = createAndSaveRiFilters(STB, XHOME);

        String url = "/" + QueryConstants.QUERIES_FILTERS_RI;
        List<RebootImmediatelyFilter> xhomeExpectedResult = Collections.singletonList(riFilters.get(XHOME));
        performGetWithApplication(url, XHOME, xhomeExpectedResult);

        List<RebootImmediatelyFilter> stbExpectedResult = Collections.singletonList(riFilters.get(STB));
        performGetWithApplication(url, STB, stbExpectedResult);

        performGetWithApplication(url, "", stbExpectedResult);
    }

    @Test
    public void getFilterByNameAndApplicationType() throws Exception {
        Map<String, RebootImmediatelyFilter> riFilters = createAndSaveRiFilters(STB, STB);
        String url = "/" + QueryConstants.QUERIES_FILTERS_RI + "/" + STB;

        performGetWithApplication(url, XHOME, riFilters.get(XHOME));

        performGetWithApplication(url, STB, riFilters.get(STB));

        performGetWithApplication(url, "", riFilters.get(STB));
    }

    @Test
    public void deleteByNameAndApplicationType() throws Exception {
        createAndSaveRiFilters(STB, STB);

        mockMvc.perform(delete("/" + QueryConstants.DELETES_FILTERS_RI + "/" + STB)
                .param("applicationType", XHOME))
                .andExpect(status().isNoContent());

        assertEquals(0, rebootImmediatelyFilterService.getByApplicationType(XHOME).size());
        assertEquals(1, rebootImmediatelyFilterService.getByApplicationType(STB).size());
    }

    @Test
    public void saveWithWrongApplicationType() throws Exception {
        RebootImmediatelyFilter riFilter = createFilter();

        String url = "/" + QueryConstants.UPDATES_FILTERS_RI;
        performPostWithWrongApplicationType(url, riFilter);
    }

    @Test
    public void getRiFilterByWrongName() throws Exception {
        String wrongRiFilterName = "wrongName";

        mockMvc.perform(get("/" + QueryConstants.QUERIES_FILTERS_RI + "/" + wrongRiFilterName)
                .param("version", "3.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSaveInvalidFilter() throws Exception {
        RebootImmediatelyFilter filter = createFilter();
        filter.setName(null);
        mockMvc.perform(post("/" + QueryConstants.UPDATES_FILTERS_RI).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(filter)))
                .andExpect(status().isBadRequest());

        filter.setName("SampleName");
        filter.setMacAddresses(null);

        mockMvc.perform(post("/" + QueryConstants.UPDATES_FILTERS_RI).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(filter)))
                .andExpect(status().isBadRequest());

    }

    private RebootImmediatelyFilter createFilter() {
        RebootImmediatelyFilter filter = new RebootImmediatelyFilter();
        filter.setId("RiFilterID");
        filter.setName("RiFilterName");
        filter.setMacAddresses("11:22:33:44:55:66");
        return filter;
    }

    private Map<String, RebootImmediatelyFilter> createAndSaveRiFilters(String stbName, String xhomeName) throws Exception {
        Map<String, RebootImmediatelyFilter> riFilters = new HashMap<>();
        RebootImmediatelyFilter riFilter1 = createFilter();
        riFilter1.setId(UUID.randomUUID().toString());
        riFilter1.setName(stbName);
        rebootImmediatelyFilterService.save(riFilter1, STB);
        riFilters.put(STB, riFilter1);

        RebootImmediatelyFilter riFilter2 = createFilter();
        riFilter2.setId(UUID.randomUUID().toString());
        riFilter2.setName(xhomeName);
        rebootImmediatelyFilterService.save(riFilter2, XHOME);
        riFilters.put(XHOME, riFilter2);

        return riFilters;
    }


}
