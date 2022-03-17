/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.admin.controller.change;

import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.admin.controller.telemetry.PermanentProfileController;
import com.comcast.xconf.admin.service.change.TelemetryProfileChangeService;
import com.comcast.xconf.admin.service.telemetry.PermanentTelemetryProfileService;
import com.comcast.xconf.change.ApprovedChange;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.service.change.ApprovedChangeCrudService;
import com.comcast.xconf.service.change.ChangeCrudService;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.*;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChangeControllerTest extends BaseControllerTest {

    public static final String PROFILE_NAME_PREFFIX = "profileName_";

    @Autowired
    protected SimpleDao<String, Change> changeDao;

    @Autowired
    protected SimpleDao<String, ApprovedChange> approvedDao;

    @Autowired
    private ChangeCrudService<PermanentTelemetryProfile> changeCrudService;

    @Autowired
    private ApprovedChangeCrudService<PermanentTelemetryProfile> approvedChangeCrudService;

    @Autowired
    private TelemetryProfileChangeService telemetryProfileChangeService;

    @Autowired
    private PermanentTelemetryProfileService telemetryProfileService;

    @Before
    @After
    public void cleanChangeData() {
        List<SimpleDao<String, ? extends Change>> daoList = Arrays.asList(changeDao, approvedDao);
        for (SimpleDao<String, ? extends IPersistable> dao : daoList) {
            for (IPersistable iPersistable : dao.getAll()) {
                dao.deleteOne(iPersistable.getId());
            }
        }
    }

    @Test
    public void getAllApprovedChanges() throws Exception {
        Integer approvedChangesSize = 10;
        createProfileChanges(10);
        List<ApprovedChange<PermanentTelemetryProfile>> approvedChanges = createApprovedChanges(changeCrudService.getAll());

        mockMvc.perform(get(ChangeController.URL + "/approved")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(approvedChanges)));
        assertEquals(approvedChangesSize, Integer.valueOf(approvedChanges.size()));
    }

    @Test
    public void getFilteredApprovedChanges_All() throws Exception {
        createProfileChanges(10);
        List<ApprovedChange<PermanentTelemetryProfile>> approvedChanges = createApprovedChanges(changeCrudService.getAll());

        Map<String, List<ApprovedChange<PermanentTelemetryProfile>>> expectedResult = Collections.singletonMap("changesPerPage", approvedChanges);
        performSearchRequestAndVerifyResult("/approved/filtered", PROFILE_NAME_PREFFIX, JsonUtil.toJson(expectedResult));
    }

    @Test
    public void getFilteredApprovedChanges_One() throws Exception {
        createProfileChanges(10);
        List<ApprovedChange<PermanentTelemetryProfile>> approvedChanges = createApprovedChanges(changeCrudService.getAll());

        Map<String, List<ApprovedChange<PermanentTelemetryProfile>>> expectedResult = Collections.singletonMap("changesPerPage", Collections.singletonList(approvedChanges.get(0)));
        performSearchRequestAndVerifyResult("/approved/filtered", PROFILE_NAME_PREFFIX + "0", JsonUtil.toJson(expectedResult));
    }

    @Test
    public void getFilteredPendingChange() throws Exception {
        createProfileChanges(10);
        List<Change<PermanentTelemetryProfile>> changes = changeCrudService.getAll();

        String entityId = changes.get(0).getEntityId();
        Map<String, List<Change<PermanentTelemetryProfile>>> expectedResult = Collections.singletonMap(entityId, Collections.singletonList(changes.get(0)));
        performSearchRequestAndVerifyResult("/changes/filtered", PROFILE_NAME_PREFFIX + "0", JsonUtil.toJson(expectedResult));
    }

    private void performSearchRequestAndVerifyResult(String url, String entityParamValue, String s) throws Exception {
        mockMvc.perform(post(ChangeController.URL + url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(Collections.singletonMap(SearchFields.ENTITY, entityParamValue))))
                .andExpect(status().isOk())
                .andExpect(content().json(s));
    }

    @Test
    public void approveCreate() throws Exception {
        PermanentTelemetryProfile profile = createProfileChange();
        List<Change<PermanentTelemetryProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());

        assertNull(telemetryProfileService.getEntityDAO().getOne(profile.getId()));
        Change<PermanentTelemetryProfile> change = changesByEntityId.get(0);

        approveChange(change.getId());
        assertEquals(profile, telemetryProfileService.getOne(profile.getId()));
    }

    @Test
    public void approveUpdate() throws Exception {
        PermanentTelemetryProfile profile = createProfileChange();
        List<Change<PermanentTelemetryProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());
        telemetryProfileChangeService.approve(changesByEntityId.get(0).getId());
        String newProfileName = "CHANGED_PROFILE_NAME";
        PermanentTelemetryProfile changedProfile = createTelemetryProfile(profile.getId(), newProfileName);

        saveChangedProfile(changedProfile);
        changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());

        approveChange(changesByEntityId.get(0).getId());
        assertEquals(changedProfile, telemetryProfileService.getOne(changedProfile.getId()));
    }

    @Test
    public void revertCreate() throws Exception {
        PermanentTelemetryProfile profile = createProfileChange();
        List<Change<PermanentTelemetryProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());
        Change<PermanentTelemetryProfile> change = changesByEntityId.get(0);

        approveChange(change.getId());
        assertNotNull(telemetryProfileService.getEntityDAO().getOne(profile.getId()));

        revertChange(change.getId());
        assertNull(telemetryProfileService.getEntityDAO().getOne(profile.getId()));
    }

    @Test
    public void revertUpdate() throws Exception {
        PermanentTelemetryProfile profile = createProfileChange();
        List<Change<PermanentTelemetryProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());
        Change<PermanentTelemetryProfile> change = changesByEntityId.get(0);

        approveChange(change.getId());
        assertNotNull(telemetryProfileService.getEntityDAO().getOne(profile.getId()));

        String newProfileName = "CHANGED_PROFILE_NAME";
        PermanentTelemetryProfile changedProfile = createTelemetryProfile(profile.getId(), newProfileName);

        saveChangedProfile(changedProfile);
        change = changeCrudService.getChangesByEntityId(profile.getId()).get(0);
        approveChange(change.getId());

        assertEquals(changedProfile, telemetryProfileService.getOne(changedProfile.getId()));
        revertChange(change.getId());
        assertEquals(profile, telemetryProfileService.getOne(profile.getId()));
    }

    @Test
    public void cancelChange() throws Exception {
        PermanentTelemetryProfile profile = createProfileChange();
        List<Change<PermanentTelemetryProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());
        assertEquals(1, changesByEntityId.size());
        Change<PermanentTelemetryProfile> change = changesByEntityId.get(0);

        mockMvc.perform(get(ChangeController.URL + "/cancel/{changeId}", change.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        List<Change<PermanentTelemetryProfile>> changesAfterCanceling = changeCrudService.getChangesByEntityId(profile.getId());
        assertEquals(0, changesAfterCanceling.size());
    }

    @Test
    public void getGroupedChanges() throws Exception {
        String profileId = UUID.randomUUID().toString();
        createAndSaveProfile(profileId);
        Map<String, List<Change<PermanentTelemetryProfile>>> groupedChanges = createProfileChangesForTheSameId(profileId, 10);
        mockMvc.perform(get(ChangeController.URL + "/changes/grouped/byId")
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(groupedChanges)));
    }

    @Test
    public void getChangeIds() throws Exception {
        Integer changeSize = 10;
        List<Change<PermanentTelemetryProfile>> changes = createProfileChanges(changeSize);
        List<String> entityIds = getEntityIds(changes);

        mockMvc.perform(get(ChangeController.URL + "/entityIds")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(entityIds)));
    }

    @Test
    public void verifyErrorMessagesIfChangeIsNotApproved() throws Exception {
        String profileId = UUID.randomUUID().toString();
        List<String> changeIds = createChangesForTheSameTelemetryProfile(profileId);
         mockMvc.perform(post(ChangeController.URL + "/approveChanges")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(changeIds)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonMap(changeCrudService.getChangesByEntityId(profileId).get(0).getId(), "There is change for " + permanentTelemetryDAO.getOne(profileId).getName() + " telemetry profile"))));
    }

    @Test
    public void notCancelChangesWhichHasAnErrorDuringApproving() throws Exception {
        String profileId = UUID.randomUUID().toString();
        List<String> changeIds = createChangesForTheSameTelemetryProfile(profileId);

        mockMvc.perform(post(ChangeController.URL + "/approveChanges")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(changeIds)))
                .andExpect(status().isOk());

        assertTrue(changeCrudService.getChangedEntityIds().contains(profileId));
        assertEquals(1, changeCrudService.getChangedEntityIds().size());
    }

    @Test
    public void applyTheLatestChangeOfTheSameEntityFieldWasChanged() throws Exception {
        String profileId = UUID.randomUUID().toString();
        PermanentTelemetryProfile profile = createAndSaveProfile(profileId);
        TelemetryProfile.TelemetryElement telemetryElement = profile.getTelemetryProfile().get(0);
        telemetryProfileService.writeUpdateChange(createTelemetryProfile(profileId, createTelemetryElement(telemetryElement.getId(), "FIRST CHANGE OF HEADER", telemetryElement.getType())));
        PermanentTelemetryProfile expectedMergeResult = createTelemetryProfile(profileId, createTelemetryElement(telemetryElement.getId(), "SECOND CHANGE OF HEADER", telemetryElement.getType()));
        telemetryProfileService.writeUpdateChange(expectedMergeResult);

        List<String> changeIds = getChangeIds(changeCrudService.getChangesByEntityId(profileId));

        approveChanges(changeIds);
        assertEquals(expectedMergeResult, telemetryProfileService.getOne(profileId));
    }

    @Test
    public void dontRemoveTelemetryElementIfItWasRemovedInFirstChangeButChangedInSecond() throws Exception {
        List<TelemetryProfile.TelemetryElement> expectedTelemetryElements = new ArrayList<>();
        PermanentTelemetryProfile profile = createAndSaveProfile(UUID.randomUUID().toString());
        PermanentTelemetryProfile updateProfile1 = changeProfileAndWriteChange(profile, expectedTelemetryElements);

        PermanentTelemetryProfile updateProfile2 = CloneUtil.clone(profile);
        updateProfile2.getTelemetryProfile().get(0).setHeader("CHANGED REMOVED HEADER");
        expectedTelemetryElements.add(updateProfile2.getTelemetryProfile().get(0));
        updateProfile2.getTelemetryProfile().addAll(updateProfile1.getTelemetryProfile());
        telemetryProfileService.writeUpdateChange(updateProfile2);

        List<String> changeIds = getChangeIds(changeCrudService.getChangesByEntityId(profile.getId()));

        mockMvc.perform(post(ChangeController.URL + "/approveChanges")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(changeIds)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(new HashMap<>())));

        assertEquals(expectedTelemetryElements, telemetryProfileService.getOne(profile.getId()).getTelemetryProfile());
    }

    @Test
    public void removeTelemetryElementIfItWasRemovedInFirstChangeAndNotChangedInSecond() throws Exception {
        List<TelemetryProfile.TelemetryElement> expectedTelemetryElements = new ArrayList<>();
        PermanentTelemetryProfile profile = createAndSaveProfile(UUID.randomUUID().toString());
        PermanentTelemetryProfile updateProfile1 = changeProfileAndWriteChange(profile, expectedTelemetryElements);

        PermanentTelemetryProfile updateProfile2 = CloneUtil.clone(profile);
        updateProfile2.getTelemetryProfile().addAll(updateProfile1.getTelemetryProfile());
        telemetryProfileService.writeUpdateChange(updateProfile2);

        List<String> changeIds = getChangeIds(changeCrudService.getChangesByEntityId(profile.getId()));

        mockMvc.perform(post(ChangeController.URL + "/approveChanges")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(changeIds)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(new HashMap<>())));

        assertEquals(expectedTelemetryElements, telemetryProfileService.getOne(profile.getId()).getTelemetryProfile());
    }

    @Test
    public void mergeChangesOfTheSameEntityIfDifferentFieldsWereChangedAfterEntityWasCreated() throws Exception {
        PermanentTelemetryProfile profile = createProfileChange();
        String profileId = profile.getId();
        approveChange(changeCrudService.getChangesByEntityId(profileId).get(0));
        TelemetryProfile.TelemetryElement telemetryElement = profile.getTelemetryProfile().get(0);
        String changedHeader = "CHANGED HEADER";
        String changedType = "CHANGED TYPE";
        telemetryProfileService.writeUpdateChange(createTelemetryProfile(profileId, createTelemetryElement(telemetryElement.getId(), changedHeader, telemetryElement.getType())));
        telemetryProfileService.writeUpdateChange(createTelemetryProfile(profileId, createTelemetryElement(telemetryElement.getId(), telemetryElement.getHeader(), changedType)));
        List<String> changeIds = getChangeIds(changeCrudService.getChangesByEntityId(profileId));

        approveChanges(changeIds);
        PermanentTelemetryProfile changedProfile = telemetryProfileService.getOne(profileId);
        assertEquals(changedHeader, changedProfile.getTelemetryProfile().get(0).getHeader());
        assertEquals(changedType, changedProfile.getTelemetryProfile().get(0).getType());
    }

    @Test
    public void removeProfileAndRevert() {
        String profileId = UUID.randomUUID().toString();
        createAndSaveProfile(profileId);
        telemetryProfileService.delete(profileId);
        assertNull(telemetryProfileService.getEntityDAO().getOne(profileId));
        ApprovedChange<PermanentTelemetryProfile> change = approvedChangeCrudService.getAll().get(0);
        telemetryProfileChangeService.revert(change.getId());
        assertNotNull(telemetryProfileService.getOne(profileId));
    }

    private PermanentTelemetryProfile changeProfileAndWriteChange(PermanentTelemetryProfile profile, List<TelemetryProfile.TelemetryElement> expectedTelemetryElements) {
        PermanentTelemetryProfile updateProfile1 = CloneUtil.clone(profile);
        TelemetryProfile.TelemetryElement telemetryHeader1 = createTelemetryElement(UUID.randomUUID().toString(), "telemetryHeader1", "type");
        expectedTelemetryElements.add(telemetryHeader1);
        updateProfile1.setTelemetryProfile(Collections.singletonList(telemetryHeader1));
        telemetryProfileService.writeUpdateChange(updateProfile1);
        return updateProfile1;
    }

    private List<String> createChangesForTheSameTelemetryProfile(String profileId) {
        createAndSaveProfile(profileId);
        telemetryProfileService.writeDeleteChange(profileId);
        telemetryProfileService.writeUpdateChange(createTelemetryProfile(profileId, createTelemetryElement(UUID.randomUUID().toString(), "newHeader", "type")));
        List<String> changeIds = getChangeIds(changeCrudService.getChangesByEntityId(profileId));
        return changeIds;
    }

    private PermanentTelemetryProfile createProfileChange() {
        PermanentTelemetryProfile profile = createTelemetryProfile();
        telemetryProfileService.writeCreateChange(profile);
        return profile;
    }

    private List<Change<PermanentTelemetryProfile>> createProfileChanges(Integer size) {
        List<Change<PermanentTelemetryProfile>> changes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            PermanentTelemetryProfile profile = createTelemetryProfile(UUID.randomUUID().toString(), PROFILE_NAME_PREFFIX + i);
            Change<PermanentTelemetryProfile> change = telemetryProfileService.writeCreateChange(profile);
            changes.add(change);
        }
        return changes;
    }

    private List<ApprovedChange<PermanentTelemetryProfile>> createApprovedChanges(List<Change<PermanentTelemetryProfile>> changes) {
        List<ApprovedChange<PermanentTelemetryProfile>> approvedChanges = new ArrayList<>();
        for (Change<PermanentTelemetryProfile> change : changes) {
            approvedChanges.add(approveChange(change));
        }
        return approvedChanges;
    }

    private ApprovedChange<PermanentTelemetryProfile> approveChange(Change<PermanentTelemetryProfile> change) {
        return telemetryProfileChangeService.approve(change.getId());
    }

    private void approveChange(String changeId) throws Exception {
        mockMvc.perform(get(ChangeController.URL + "/approve/{changeId}", changeId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private void approveChanges(List<String> changeIds) throws Exception {
        mockMvc.perform(post(ChangeController.URL + "/approveChanges")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(changeIds)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(new HashMap<>())));
    }

    private List<String> getChangeIds(List<Change<PermanentTelemetryProfile>> changes) {
        List<String> entityIds = new ArrayList<>();
        for (Change<PermanentTelemetryProfile> change : changes) {
            entityIds.add(change.getId());
        }
        return entityIds;
    }

    private List<String> getEntityIds(List<Change<PermanentTelemetryProfile>> changes) {
        List<String> entityIds = new ArrayList<>();
        for (Change<PermanentTelemetryProfile> change : changes) {
            entityIds.add(change.getEntityId());
        }
        return entityIds;
    }

    private void revertChange(String approveId) throws Exception {
        mockMvc.perform(get(ChangeController.URL + "/revert/{approveId}", approveId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private void saveChangedProfile(PermanentTelemetryProfile profile) throws Exception {
        mockMvc.perform(put("/" + PermanentProfileController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(profile)))
                .andExpect(status().isOk());
    }

    private PermanentTelemetryProfile createAndSaveProfile(String id) {
        PermanentTelemetryProfile profile = createTelemetryProfile(id);
        permanentTelemetryDAO.setOne(profile.getId(), profile);
        return profile;
    }

    private PermanentTelemetryProfile createTelemetryProfile(String id, TelemetryProfile.TelemetryElement telemetryElement) {
        PermanentTelemetryProfile profile = createTelemetryProfile(id);
        profile.setTelemetryProfile(Lists.newArrayList(telemetryElement));
        return profile;
    }

    private TelemetryProfile.TelemetryElement createTelemetryElement(String id, String header, String type) {
        TelemetryProfile.TelemetryElement telemetryElement = createTelemetryElement();
        telemetryElement.setId(id);
        telemetryElement.setHeader(header);
        telemetryElement.setType(type);
        return telemetryElement;
    }
}