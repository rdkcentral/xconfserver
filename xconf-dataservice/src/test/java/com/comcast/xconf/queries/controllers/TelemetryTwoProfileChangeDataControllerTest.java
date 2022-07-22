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
import com.comcast.xconf.change.EntityType;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.service.telemetrytwochange.ApprovedTelemetryTwoChangeCrudService;
import com.comcast.xconf.service.telemetrytwochange.TelemetryTwoChangeCrudService;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static com.comcast.xconf.queries.controllers.TelemetryProfileTwoDataControllerTest.TELEMETRY_2_CONFIG_TO_UPDATE;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TelemetryTwoProfileChangeDataControllerTest extends BaseQueriesControllerTest {

    @Autowired
    private TelemetryTwoChangeCrudService<TelemetryTwoProfile> changeCrudService;

    @Autowired
    private ApprovedTelemetryTwoChangeCrudService approvedChangeCrudService;

    @Test
    public void approveByChangeId() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();

        mockMvc.perform(post(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/change")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(profile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.entityId").value(profile.getId()))
                .andExpect(jsonPath("$.entityType").value(EntityType.TELEMETRY_TWO_PROFILE.toString()))
                .andExpect(jsonPath("$.newEntity").exists())
                .andExpect(jsonPath("$.oldEntity").doesNotExist());

        assertNull(telemetryTwoProfileDAO.getOne(profile.getId()));

        List<TelemetryTwoChange<TelemetryTwoProfile>> allChanges = changeCrudService.getAll();
        assertEquals(1, allChanges.size());

        mockMvc.perform(post(TelemetryTwoChangeDataController.URL_MAPPING + "/approve/byChangeIds")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(Lists.newArrayList(allChanges.get(0).getId()))))
                .andExpect(status().isOk());

        assertEquals(1, telemetryTwoProfileDAO.getAll().size());
        assertTrue(CollectionUtils.isEmpty(changeCrudService.getAll()));
        assertTrue(CollectionUtils.isNotEmpty(approvedChangeCrudService.getChangesByEntityId(profile.getId())));
    }

    @Test
    public void approveByEntityId() throws Exception {
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

        List<TelemetryTwoChange<TelemetryTwoProfile>> allChanges = changeCrudService.getAll();
        assertEquals(1, allChanges.size());

        mockMvc.perform(get(TelemetryTwoChangeDataController.URL_MAPPING + "/approve/byEntity/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(0, telemetryTwoProfileDAO.getAll().size());
        assertTrue(CollectionUtils.isEmpty(changeCrudService.getAll()));
        assertTrue(CollectionUtils.isNotEmpty(approvedChangeCrudService.getChangesByEntityId(profile.getId())));
    }

    @Test
    public void cancelChange() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();

        List<TelemetryTwoChange<TelemetryTwoProfile>> allChanges = changeCrudService.getAll();
        assertEquals(0, allChanges.size());

        mockMvc.perform(post(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/change")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(profile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.entityId").value(profile.getId()))
                .andExpect(jsonPath("$.entityType").value(EntityType.TELEMETRY_TWO_PROFILE.toString()))
                .andExpect(jsonPath("$.newEntity").exists())
                .andExpect(jsonPath("$.oldEntity").doesNotExist());

        allChanges = changeCrudService.getAll();
        assertEquals(1, allChanges.size());

        mockMvc.perform(get(TelemetryTwoChangeDataController.URL_MAPPING + "/cancel/" + allChanges.get(0).getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(0, changeCrudService.getAll().size());
        assertTrue(CollectionUtils.isEmpty(telemetryTwoProfileDAO.getAll()));
    }

    @Test
    public void getAllChanges() throws Exception {
        TelemetryTwoProfile profile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(profile.getId(), profile);

        assertTrue(CollectionUtils.isEmpty(changeCrudService.getAll()));

        TelemetryTwoProfile profileToUpdate = CloneUtil.clone(profile);
        profileToUpdate.setJsonconfig(TELEMETRY_2_CONFIG_TO_UPDATE);

        mockMvc.perform(put(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/change")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(profileToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        assertEquals(1, changeCrudService.getAll().size());

        mockMvc.perform(delete(TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API + "/change/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        List<TelemetryTwoChange<TelemetryTwoProfile>> allChanges = changeCrudService.getAll();
        assertEquals(2, allChanges.size());

        mockMvc.perform(get(TelemetryTwoChangeDataController.URL_MAPPING + "/all")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(allChanges)));
    }
}
