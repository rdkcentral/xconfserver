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

import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.change.ApprovedChange;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.change.EntityType;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.service.change.ChangeCrudService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.comcast.xconf.queries.controllers.TelemetryProfileDataController.TELEMETRY_PROFILE_URL;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TelemetryProfileDataControllerTest extends BaseQueriesControllerTest {

    @Autowired
    protected SimpleDao<String, Change> changeDao;

    @Autowired
    protected SimpleDao<String, ApprovedChange> approvedDao;

    @Autowired
    private ChangeCrudService<PermanentTelemetryProfile> changesCrudService;

    @Before
    @After
    public void cleanChangeData() {
        deleteAllEntities();

        List<SimpleDao<String, ? extends Change>> daoList = Arrays.asList(changeDao, approvedDao);
        for (SimpleDao<String, ? extends IPersistable> dao : daoList) {
            for (IPersistable iPersistable : dao.getAll()) {
                dao.deleteOne(iPersistable.getId());
            }
        }
    }

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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

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
                        .content(JsonUtil.toJson(Lists.newArrayList(telemetryEntryToAdd))))
                .andExpect(status().isOk());

        PermanentTelemetryProfile updatedProfile = permanentTelemetryDAO.getOne(profile.getId());
        Optional<TelemetryProfile.TelemetryElement> addedEntry = updatedProfile.getTelemetryProfile().stream()
                .filter(entry -> entry.equalTelemetryData(telemetryEntryToAdd))
                .findFirst();

        assertTrue(addedEntry.isPresent());
        assertTrue(StringUtils.isNotBlank(addedEntry.get().getId()));
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
                        .content(JsonUtil.toJson(Lists.newArrayList(telemetryEntryToRemove))))
                .andExpect(status().isOk());

        PermanentTelemetryProfile updatedProfile = permanentTelemetryDAO.getOne(profile.getId());
        Optional<TelemetryProfile.TelemetryElement> removedEntry = updatedProfile.getTelemetryProfile().stream()
                .filter(entry -> entry.equalTelemetryData(telemetryEntryToRemove))
                .findFirst();

        assertFalse(removedEntry.isPresent());
    }

    @Test
    public void profileIsNotRemovedIfUsedByRule() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        permanentTelemetryDAO.setOne(profile.getId(), profile);

        Model model = createAndSaveModel(defaultModelId.toUpperCase());

        Condition condition = new Condition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from(model.getId()));
        TelemetryRule telemetryRule = createTelemetryRule(profile.getId(), condition);
        telemetryRuleDAO.setOne(telemetryRule.getId(), telemetryRule);

        mockMvc.perform(delete(TELEMETRY_PROFILE_URL + "/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("\"Can't delete profile as it's used in telemetry rule: " + telemetryRule.getName() + "\""));
    }

    @Test
    public void telemetryEntriesDuplicatesAreNotAllowed() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        TelemetryProfile.TelemetryElement telemetryEntryToAdd = createTelemetryEntry();
        profile.getTelemetryProfile().add(telemetryEntryToAdd);

        permanentTelemetryDAO.setOne(profile.getId(), profile);

        mockMvc.perform(put(TELEMETRY_PROFILE_URL + "/entry/add/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(Lists.newArrayList(telemetryEntryToAdd))))
                .andExpect(status().isConflict())
                .andExpect(content().string("\"Telemetry entry already exists\""));
    }

    @Test
    public void telemetryEntryShouldNotContainEmptyFields() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        TelemetryProfile.TelemetryElement telemetryEntryToAdd = createTelemetryEntry();
        telemetryEntryToAdd.setContent("");

        permanentTelemetryDAO.setOne(profile.getId(), profile);

        mockMvc.perform(put(TELEMETRY_PROFILE_URL + "/entry/add/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(Lists.newArrayList(telemetryEntryToAdd))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"Content should not be empty\""));
    }

    @Test
    public void removingLatestTelemetryEntryIsNotAllowed() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        List<TelemetryProfile.TelemetryElement> telemetriesEntryToRemove = Lists.newArrayList(createTelemetryEntry());
        profile.setTelemetryProfile(telemetriesEntryToRemove);

        permanentTelemetryDAO.setOne(profile.getId(), profile);

        mockMvc.perform(put(TELEMETRY_PROFILE_URL + "/entry/remove/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(telemetriesEntryToRemove)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"Telemetry entry list should not be empty\""));
    }

    @Test
    public void exceptionIsThrownIfEntryDoesNotExists() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        List<TelemetryProfile.TelemetryElement> telemetryEntriesToRemove = Lists.newArrayList(createTelemetryEntry());

        permanentTelemetryDAO.setOne(profile.getId(), profile);

        mockMvc.perform(put(TELEMETRY_PROFILE_URL + "/entry/remove/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(telemetryEntriesToRemove)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("\"Telemetry entry does not exist\""));
    }

    @Test
    public void createWithApproval() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();

        mockMvc.perform(post(TELEMETRY_PROFILE_URL + "/change")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(profile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entityId").value(profile.getId()))
                .andExpect(jsonPath("$.entityType").value(EntityType.TELEMETRY_PROFILE.toString()));

        List<Change<PermanentTelemetryProfile>> changesByEntityId = changesCrudService.getChangesByEntityId(profile.getId());
        assertEquals(1, changesByEntityId.size());

        approveChangeByEntityId(profile.getId());

        assertNotNull(permanentTelemetryDAO.getOne(profile.getId()));
    }

    @Test
    public void updateWithApproval() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        permanentTelemetryDAO.setOne(profile.getId(), profile);

        PermanentTelemetryProfile updatedProfile = CloneUtil.clone(profile);
        updatedProfile.setSchedule("2 2 2 2 2");

        mockMvc.perform(put(TELEMETRY_PROFILE_URL + "/change")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(updatedProfile)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId").value(profile.getId()))
                .andExpect(jsonPath("$.entityType").value(EntityType.TELEMETRY_PROFILE.toString()));

        List<Change<PermanentTelemetryProfile>> changesByEntityId = changesCrudService.getChangesByEntityId(profile.getId());
        assertEquals(1, changesByEntityId.size());

        approveChangeByEntityId(profile.getId());

        assertEquals(updatedProfile.getSchedule(), permanentTelemetryDAO.getOne(profile.getId()).getSchedule());
    }

    @Test
    public void deleteProfileWithApproval() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        permanentTelemetryDAO.setOne(profile.getId(), profile);

        mockMvc.perform(delete(TELEMETRY_PROFILE_URL + "/change/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        List<Change<PermanentTelemetryProfile>> changesByEntityId = changesCrudService.getChangesByEntityId(profile.getId());
        assertEquals(1, changesByEntityId.size());

        approveChangeByEntityId(profile.getId());

        assertNull(permanentTelemetryDAO.getOne(profile.getId()));
    }

    @Test
    public void addEntriesWithApproval() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        permanentTelemetryDAO.setOne(profile.getId(), profile);

        TelemetryProfile.TelemetryElement telemetryEntryToAdd = createTelemetryEntry();

        mockMvc.perform(put(TELEMETRY_PROFILE_URL + "/change/entry/add/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(Lists.newArrayList(telemetryEntryToAdd))))
                .andExpect(status().isOk());

        List<Change<PermanentTelemetryProfile>> changesByEntityId = changesCrudService.getChangesByEntityId(profile.getId());
        assertEquals(1, changesByEntityId.size());
        assertEquals(profile, permanentTelemetryDAO.getOne(profile.getId()));

        approveChangeByEntityId(profile.getId());

        PermanentTelemetryProfile updatedProfile = permanentTelemetryDAO.getOne(profile.getId());
        Optional<TelemetryProfile.TelemetryElement> addedEntry = updatedProfile.getTelemetryProfile().stream()
                .filter(entry -> entry.equalTelemetryData(telemetryEntryToAdd))
                .findFirst();

        assertTrue(addedEntry.isPresent());
        assertTrue(StringUtils.isNotBlank(addedEntry.get().getId()));
    }

    @Test
    public void removeEntriesWithApproval() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        TelemetryProfile.TelemetryElement telemetryEntryToRemove = createTelemetryEntry();
        profile.getTelemetryProfile().add(telemetryEntryToRemove);

        permanentTelemetryDAO.setOne(profile.getId(), profile);

        mockMvc.perform(put(TELEMETRY_PROFILE_URL + "/change/entry/remove/" + profile.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(Lists.newArrayList(Lists.newArrayList(telemetryEntryToRemove)))))
                .andExpect(status().isOk());

        List<Change<PermanentTelemetryProfile>> changesByEntityId = changesCrudService.getChangesByEntityId(profile.getId());
        assertEquals(1, changesByEntityId.size());
        assertEquals(profile, permanentTelemetryDAO.getOne(profile.getId()));

        approveChangeByEntityId(profile.getId());

        PermanentTelemetryProfile updatedProfile = permanentTelemetryDAO.getOne(profile.getId());
        Optional<TelemetryProfile.TelemetryElement> removedEntry = updatedProfile.getTelemetryProfile().stream()
                .filter(entry -> entry.equalTelemetryData(telemetryEntryToRemove))
                .findFirst();

        assertFalse(removedEntry.isPresent());
    }

    private void approveChangeByEntityId(String entityId) throws Exception {
        mockMvc.perform(get(TelemetryProfileChangeDataController.CHANGE_URL + "/approve/byEntity/" + entityId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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
