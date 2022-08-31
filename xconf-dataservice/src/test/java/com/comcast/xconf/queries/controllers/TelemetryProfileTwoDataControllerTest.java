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
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.change.EntityType;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.service.telemetrytwochange.TelemetryTwoChangeCrudService;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TelemetryProfileTwoDataControllerTest extends BaseQueriesControllerTest {

    public static final String TELEMETRY_2_CONFIG_TO_UPDATE = "{\n" +
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

    @Autowired
    private TelemetryTwoChangeCrudService<TelemetryTwoProfile> changeCrudService;

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

    @Test
    public void profileIsNotRemovedIfUsedByRule() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(profile.getId(), profile);

        Model model = createAndSaveModel(defaultModelId.toUpperCase());

        Condition condition = new Condition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from(model.getId()));
        TelemetryTwoRule telemetryTwoRule = createTelemetryTwoRule(profile.getId(), condition);
        telemetryTwoRuleDAO.setOne(telemetryTwoRule.getId(), telemetryTwoRule);

        mockMvc.perform(delete(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("\"Can't delete profile as it's used in telemetry rule: " + telemetryTwoRule.getName() + "\""));
    }

    @Test
    public void createTelemetryTwoProfileWithApproval() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();

        mockMvc.perform(post(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/change")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(profile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.entityId").value(profile.getId()))
                .andExpect(jsonPath("$.entityType").value(EntityType.TELEMETRY_TWO_PROFILE.toString()))
                .andExpect(jsonPath("$.newEntity").exists())
                .andExpect(jsonPath("$.oldEntity").doesNotExist());

        approveChangeByEntityId(profile.getId());

        assertEquals(1, telemetryTwoProfileDAO.getAll().size());
        assertEquals(profile, telemetryTwoProfileDAO.getAll().get(0));

        assertTrue(CollectionUtils.isEmpty(changeCrudService.getAll()));
    }

    @Test
    public void createTelemetryTwoProfileWithApprovalRdkloudApplicationType() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();
        profile.setApplicationType(ApplicationType.RDKCLOUD);

        mockMvc.perform(post(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/change")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(profile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.entityId").value(profile.getId()))
                .andExpect(jsonPath("$.applicationType").value(ApplicationType.RDKCLOUD))
                .andExpect(jsonPath("$.entityType").value(EntityType.TELEMETRY_TWO_PROFILE.toString()))
                .andExpect(jsonPath("$.newEntity").exists())
                .andExpect(jsonPath("$.oldEntity").doesNotExist());

        approveChangeByEntityId(profile.getId());

        assertEquals(1, telemetryTwoProfileDAO.getAll().size());

        TelemetryTwoProfile savedProfile = telemetryTwoProfileDAO.getAll().get(0);
        assertEquals(profile, savedProfile);

        assertEquals(ApplicationType.RDKCLOUD, savedProfile.getApplicationType());

        assertTrue(CollectionUtils.isEmpty(changeCrudService.getAll()));
    }

    @Test
    public void updateTelemetryTwoProfileWithApproval() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(profile.getId(), profile);

        TelemetryTwoProfile profileToUpdate = CloneUtil.clone(profile);
        profileToUpdate.setJsonconfig(TELEMETRY_2_CONFIG_TO_UPDATE);

        mockMvc.perform(put(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/change")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(profileToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.entityId").value(profile.getId()))
                .andExpect(jsonPath("$.entityType").value(EntityType.TELEMETRY_TWO_PROFILE.toString()))
                .andExpect(jsonPath("$.newEntity").exists())
                .andExpect(jsonPath("$.oldEntity").exists());

        approveChangeByEntityId(profileToUpdate.getId());

        assertEquals(1, telemetryTwoProfileDAO.getAll().size());
        assertEquals(TELEMETRY_2_CONFIG_TO_UPDATE, telemetryTwoProfileDAO.getOne(profileToUpdate.getId()).getJsonconfig());

        assertTrue(CollectionUtils.isEmpty(changeCrudService.getAll()));
    }

    @Test
    public void deleteTelemetryTwoProfileWithApproval() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(profile.getId(), profile);

        assertNotNull(telemetryTwoProfileDAO.getOne(profile.getId()));
        assertTrue(CollectionUtils.isEmpty(changeCrudService.getAll()));

        mockMvc.perform(delete(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/change/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.entityId").value(profile.getId()))
                .andExpect(jsonPath("$.entityType").value(EntityType.TELEMETRY_TWO_PROFILE.toString()))
                .andExpect(jsonPath("$.newEntity").doesNotExist())
                .andExpect(jsonPath("$.oldEntity").exists());

        assertNotNull(telemetryTwoProfileDAO.getOne(profile.getId()));
        assertTrue(CollectionUtils.isNotEmpty(changeCrudService.getAll()));

        approveChangeByEntityId(profile.getId());

        assertNull(telemetryTwoProfileDAO.getOne(profile.getId()));
        assertTrue(CollectionUtils.isEmpty(changeCrudService.getAll()));
    }

    private TelemetryTwoRule createTelemetryTwoRule(String profileId, Condition condition) {
        TelemetryTwoRule telemetryTwoRule = new TelemetryTwoRule();
        telemetryTwoRule.setId(UUID.randomUUID().toString());
        telemetryTwoRule.setName("Test Telemetry 2.0 Rule");
        telemetryTwoRule.setApplicationType(STB);
        telemetryTwoRule.setBoundTelemetryIds(Lists.newArrayList(profileId));

        telemetryTwoRule.setCondition(condition);
        return telemetryTwoRule;
    }

    private void approveChangeByEntityId(String entityId) throws Exception {
        mockMvc.perform(get(TelemetryTwoChangeDataController.URL_MAPPING + "/approve/byEntity/" + entityId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
