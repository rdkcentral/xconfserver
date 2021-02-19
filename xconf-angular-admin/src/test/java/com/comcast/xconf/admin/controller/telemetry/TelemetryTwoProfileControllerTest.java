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
package com.comcast.xconf.admin.controller.telemetry;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.search.SearchFields;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class TelemetryTwoProfileControllerTest extends BaseControllerTest{

    @Test
    public void create() throws Exception {
        TelemetryTwoProfile telemetryTwoProfile = createTelemetryTwoProfile();
        mockMvc.perform(post("/" + TelemetryTwoProfileController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryTwoProfile)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(telemetryTwoProfile)));
    }

    @Test
    public void exportOne() throws Exception {
        TelemetryTwoProfile telemetryProfile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(telemetryProfile.getId(), telemetryProfile);

        performExportRequestAndVerifyResponse("/"+ TelemetryTwoProfileController.URL_MAPPING + "/" + telemetryProfile.getId(), Lists.newArrayList(telemetryProfile), ApplicationType.STB);
    }

    @Test
    public void update() throws Exception {
        TelemetryTwoProfile telemetryProfile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(telemetryProfile.getId(), telemetryProfile);
        telemetryProfile = createTelemetryTwoProfile();
        telemetryProfile.setJsonconfig("{\n" +
                "    \"Description\": \"Telemetry 2.0 test\",\n" +
                "    \"Version\": \"0.2\",\n" +
                "    \"Protocol\": \"HTTP\",\n" +
                "    \"EncodingType\": \"JSON\",\n" +
                "    \"ReportingInterval\": 180,\n" +
                "    \"TimeReference\": \"0001-01-01T00:00:00Z\",\n" +
                "    \"Parameter\": [{\n" +
                "        \"type\": \"dataModel\",\n" +
                "        \"name\": \"TestMac\",\n" +
                "        \"reference\": \"Device.ABC\"\n" +
                "    }],\n" +
                "    \"HTTP\": {\n" +
                "        \"URL\": \"https://test.com/\",\n" +
                "        \"Compression\": \"None\",\n" +
                "        \"Method\": \"POST\",\n" +
                "        \"RequestURIParameter\": [{\n" +
                "            \"Name\": \"profileName\",\n" +
                "            \"Reference\": \"Test.Name\"\n" +
                "        }, {\n" +
                "            \"Name\": \"testVersion\",\n" +
                "            \"Reference\": \"Test.Version\"\n" +
                "        }]\n" +
                "    },\n" +
                "    \"JSONEncoding\": {\n" +
                "        \"ReportFormat\": \"NameValuePair\",\n" +
                "        \"ReportTimestamp\": \"None\"\n" +
                "    }\n" +
                "}");

        mockMvc.perform(put("/" + TelemetryTwoProfileController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryProfile)))
                .andExpect(status().isOk());
    }

    @Test
    public void getAllTelemetryTwoProfiles() throws Exception {
        TelemetryTwoProfile telemetryTwoProfile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(telemetryTwoProfile.getId(), telemetryTwoProfile);

        mockMvc.perform(get("/" + TelemetryTwoProfileController.URL_MAPPING + "/" + telemetryTwoProfile.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(telemetryTwoProfile)));

        mockMvc.perform(get("/" + TelemetryTwoProfileController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteTelemetryProfile() throws Exception {
        TelemetryTwoProfile telemetryProfile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(telemetryProfile.getId(), telemetryProfile);

        mockMvc.perform(delete("/" + TelemetryTwoProfileController.URL_MAPPING + "/" + telemetryProfile.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertNull(telemetryTwoProfileDAO.getOne(telemetryProfile.getId()));
    }

    @Test
    public void deleteTelemetryProfileThatIsUsedInRule() throws Exception {
        TelemetryTwoRule telemetryTwoRule = createTelemetryTwoRule();
        telemetryTwoRuleDAO.setOne(telemetryTwoRule.getId(), telemetryTwoRule);
        String boundProfileId = telemetryTwoRule.getBoundTelemetryIds().get(0);

        mockMvc.perform(delete("/" + TelemetryTwoProfileController.URL_MAPPING + "/" + boundProfileId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        assertNotNull(telemetryTwoProfileDAO.getOne(boundProfileId));
    }

    @Test
    public void searchTelemetryProfile() throws Exception {
        TelemetryTwoProfile profile1 = saveTelemetryTwoProfile(createTelemetryTwoProfile("id1", "TelemetryTwoProfile456"));
        TelemetryTwoProfile profile2 = saveTelemetryTwoProfile(createTelemetryTwoProfile("id2", "TelemetryTwoProfile789"));

        List<TelemetryTwoProfile> expectedResult = Arrays.asList(profile1);

        mockMvc.perform(post("/" + TelemetryTwoProfileController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(Collections.singletonMap(SearchFields.NAME, profile1.getName()))))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(expectedResult)));

        expectedResult = Arrays.asList(profile1);

        mockMvc.perform(post("/" + TelemetryTwoProfileController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(Collections.singletonMap(SearchFields.NAME, "456"))))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(expectedResult)));

        expectedResult = Arrays.asList(profile1, profile2);

        mockMvc.perform(post("/" + TelemetryTwoProfileController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(Collections.singletonMap(SearchFields.NAME, "profile"))))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(expectedResult)));
    }
}