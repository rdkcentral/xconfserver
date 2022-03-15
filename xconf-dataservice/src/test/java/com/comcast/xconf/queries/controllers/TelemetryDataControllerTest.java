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
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Optional;

import static com.comcast.xconf.queries.controllers.TelemetryProfileDataController.TELEMETRY_PROFILE_URL;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TelemetryDataControllerTest extends BaseQueriesControllerTest {

    @Test
    public void getTelemetryProfiles() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();

        permanentTelemetryDAO.setOne(profile.getId(), profile);

        performGetAndVerify(TELEMETRY_PROFILE_URL, new HashMap<>(), Lists.newArrayList(profile));
    }

    @Test
    public void getById() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();

        permanentTelemetryDAO.setOne(profile.getId(), profile);

        performGetAndVerify(TELEMETRY_PROFILE_URL + "/" + profile.getId(), new HashMap<>(), profile);
    }

    @Test
    public void create() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();

        mockMvc.perform(post(TELEMETRY_PROFILE_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(profile)))
                .andExpect(status().isCreated());

        assertEquals(1, permanentTelemetryDAO.getAll().size());
        PermanentTelemetryProfile createdProfile = permanentTelemetryDAO.getAll().get(0);

        verifyProfileData(profile, createdProfile);
    }

    @Test
    public void update() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        permanentTelemetryDAO.setOne(profile.getId(), profile);

        PermanentTelemetryProfile updatedProfile = CloneUtil.clone(profile);
        updatedProfile.setSchedule("2 2 2 2 2");

        mockMvc.perform(put(TELEMETRY_PROFILE_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(updatedProfile)))
                .andExpect(status().isOk());

        assertEquals(updatedProfile.getSchedule(), permanentTelemetryDAO.getOne(profile.getId()).getSchedule());
    }

    @Test
    public void deleteProfile() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        permanentTelemetryDAO.setOne(profile.getId(), profile);

        mockMvc.perform(delete(TELEMETRY_PROFILE_URL + "/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertNull(permanentTelemetryDAO.getOne(profile.getId()));
    }

    @Test
    public void addTelemetryEntry() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        permanentTelemetryDAO.setOne(profile.getId(), profile);

        TelemetryProfile.TelemetryElement telemetryEntryToAdd = createTelemetryEntry();


        mockMvc.perform(put(TELEMETRY_PROFILE_URL + "/entry/add/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(telemetryEntryToAdd)))
                .andExpect(status().isOk());

        PermanentTelemetryProfile updatedProfile = permanentTelemetryDAO.getOne(profile.getId());
        Optional<TelemetryProfile.TelemetryElement> addedEntry = updatedProfile.getTelemetryProfile().stream()
                .filter(entry -> entry.equalTelemetryData(telemetryEntryToAdd))
                .findFirst();

        assertTrue(addedEntry.isPresent());
    }

    @Test
    public void removeTelemetryEntry() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        TelemetryProfile.TelemetryElement telemetryEntryToRemove = createTelemetryEntry();
        profile.getTelemetryProfile().add(telemetryEntryToRemove);

        permanentTelemetryDAO.setOne(profile.getId(), profile);



        mockMvc.perform(put(TELEMETRY_PROFILE_URL + "/entry/remove/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(telemetryEntryToRemove)))
                .andExpect(status().isOk());

        PermanentTelemetryProfile updatedProfile = permanentTelemetryDAO.getOne(profile.getId());
        Optional<TelemetryProfile.TelemetryElement> removedEntry = updatedProfile.getTelemetryProfile().stream()
                .filter(entry -> entry.equalTelemetryData(telemetryEntryToRemove))
                .findFirst();

        assertFalse(removedEntry.isPresent());
    }

    private TelemetryProfile.TelemetryElement createTelemetryEntry() {
        TelemetryProfile.TelemetryElement entry = new TelemetryProfile.TelemetryElement();
        entry.setHeader("New Header");
        entry.setType("New Type");
        entry.setContent("New Content");
        entry.setPollingFrequency("10");
        entry.setComponent("New Component");
        return entry;
    }

    private void verifyProfileData(PermanentTelemetryProfile expectedProfile, PermanentTelemetryProfile actualProfile) {
        assertEquals(expectedProfile.getName(), actualProfile.getName());
        assertEquals(expectedProfile.getSchedule(), expectedProfile.getSchedule());
        assertEquals(expectedProfile.getApplicationType(), actualProfile.getApplicationType());
        assertEquals(expectedProfile.getUploadProtocol(), actualProfile.getUploadProtocol());
        assertEquals(expectedProfile.getUploadRepository(), actualProfile.getUploadRepository());
        assertTrue(expectedProfile.getTelemetryProfile().equals(actualProfile.getTelemetryProfile()));
    }
}
