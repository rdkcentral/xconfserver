/**
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2022 RDK Management
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
 * Author: Maksym Dolina
 */

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TelemetryTwoDataControllerTest extends BaseQueriesControllerTest {

    private static final String TELEMETRY_2_CONFIG_TO_UPDATE = "{\n" +
            "    \"Description\": \"Telemetry 2.0 test - CHANGED\",\n" +
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
            "   \"TriggerCondition\": [{\n" +
            "       \"type\":\"dataModel\",\n" +
            "       \"operator\":\"lt\",\n" +
            "       \"threshold\":14,\n" +
            "       \"minThresholdDuration\":12,\n" +
            "       \"reference\":\"test reference\"\n" +
            "   }]," +
            "    \"JSONEncoding\": {\n" +
            "        \"ReportFormat\": \"NameValuePair\",\n" +
            "        \"ReportTimestamp\": \"None\"\n" +
            "    }\n" +
            "}";

    @Test
    public void getAll() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(profile.getId(), profile);

        mockMvc.perform(get(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(profile))));
    }

    @Test
    public void getById() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(profile.getId(), profile);

        mockMvc.perform(get(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(profile)));
    }

    @Test
    public void create() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();

        mockMvc.perform(post(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(profile)))
                .andExpect(status().isCreated());

        assertEquals(1, telemetryTwoProfileDAO.getAll().size());
        assertEquals(profile, telemetryTwoProfileDAO.getAll().get(0));
    }

    @Test
    public void update() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(profile.getId(), profile);

        TelemetryTwoProfile profileToUpdate = CloneUtil.clone(profile);
        profileToUpdate.setJsonconfig(TELEMETRY_2_CONFIG_TO_UPDATE);

        mockMvc.perform(put(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(profileToUpdate)))
                .andExpect(status().isOk());

        assertEquals(1, telemetryTwoProfileDAO.getAll().size());
        assertEquals(TELEMETRY_2_CONFIG_TO_UPDATE, telemetryTwoProfileDAO.getOne(profile.getId()).getJsonconfig());
    }

    @Test
    public void deleteProfile() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(profile.getId(), profile);

        mockMvc.perform(delete(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertNull(telemetryTwoProfileDAO.getOne(profile.getId()));
    }

    protected TelemetryTwoProfile createTelemetryTwoProfile() {
        TelemetryTwoProfile telemetryTwoProfile = new TelemetryTwoProfile();
        telemetryTwoProfile.setId(UUID.randomUUID().toString());
        telemetryTwoProfile.setName("Test Telemetry 2.0 Profile");
        telemetryTwoProfile.setJsonconfig("{\n" +
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
        telemetryTwoProfile.setApplicationType(STB);
        return telemetryTwoProfile;
    }
}
