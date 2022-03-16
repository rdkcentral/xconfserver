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
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.admin.controller.telemetry.TelemetryTwoProfileController;
import com.comcast.xconf.admin.service.telemetry.TelemetryTwoProfileService;
import com.comcast.xconf.admin.service.telemetrytwochange.TelemetryTwoProfileChangeService;
import com.comcast.xconf.change.ApprovedTelemetryTwoChange;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.service.telemetrytwochange.ApprovedTelemetryTwoChangeCrudService;
import com.comcast.xconf.service.telemetrytwochange.TelemetryTwoChangeCrudService;
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

public class TelemetryTwoChangeControllerTest extends BaseControllerTest {

    public static final String PROFILE_NAME_PREFFIX = "profileName_";

    @Autowired
    protected SimpleDao<String, TelemetryTwoChange> changeDao;

    @Autowired
    protected SimpleDao<String, ApprovedTelemetryTwoChange> approvedDao;

    @Autowired
    private TelemetryTwoChangeCrudService<TelemetryTwoProfile> changeCrudService;

    @Autowired
    private ApprovedTelemetryTwoChangeCrudService<TelemetryTwoChange> approvedChangeCrudService;

    @Autowired
    private TelemetryTwoProfileChangeService telemetryProfileChangeService;

    @Autowired
    private TelemetryTwoProfileService telemetryTwoProfileService;

    @Before
    @After
    public void cleanChangeData() {
        List<SimpleDao<String, ? extends TelemetryTwoChange>> daoList = Arrays.asList(changeDao, approvedDao);
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
        List<ApprovedTelemetryTwoChange<TelemetryTwoProfile>> approvedChanges = createApprovedChanges(changeCrudService.getAll());

        mockMvc.perform(get(TelemetryTwoChangeController.URL + "/approved")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(approvedChanges)));
        assertEquals(approvedChangesSize, Integer.valueOf(approvedChanges.size()));
    }

    @Test
    public void getFilteredApprovedChanges_All() throws Exception {
        createProfileChanges(10);
        List<ApprovedTelemetryTwoChange<TelemetryTwoProfile>> approvedChanges = createApprovedChanges(changeCrudService.getAll());

        Map<String, List<ApprovedTelemetryTwoChange<TelemetryTwoProfile>>> expectedResult = Collections.singletonMap("changesPerPage", approvedChanges);
        performSearchRequestAndVerifyResult("/approved/filtered", PROFILE_NAME_PREFFIX, JsonUtil.toJson(expectedResult));
    }

    @Test
    public void getFilteredApprovedChanges_One() throws Exception {
        createProfileChanges(10);
        List<ApprovedTelemetryTwoChange<TelemetryTwoProfile>> approvedChanges = createApprovedChanges(changeCrudService.getAll());

        Map<String, List<ApprovedTelemetryTwoChange<TelemetryTwoProfile>>> expectedResult = Collections.singletonMap("changesPerPage", Collections.singletonList(approvedChanges.get(0)));
        performSearchRequestAndVerifyResult("/approved/filtered", PROFILE_NAME_PREFFIX + "0", JsonUtil.toJson(expectedResult));
    }

    @Test
    public void getFilteredPendingChange() throws Exception {
        createProfileChanges(10);
        List<TelemetryTwoChange<TelemetryTwoProfile>> changes = changeCrudService.getAll();

        String entityId = changes.get(0).getEntityId();
        Map<String, List<TelemetryTwoChange<TelemetryTwoProfile>>> expectedResult = Collections.singletonMap(entityId, Collections.singletonList(changes.get(0)));
        performSearchRequestAndVerifyResult("/changes/filtered", PROFILE_NAME_PREFFIX + "0", JsonUtil.toJson(expectedResult));
    }

    private void performSearchRequestAndVerifyResult(String url, String entityParamValue, String s) throws Exception {
        mockMvc.perform(post(TelemetryTwoChangeController.URL + url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(Collections.singletonMap(SearchFields.ENTITY, entityParamValue))))
                .andExpect(status().isOk())
                .andExpect(content().json(s));
    }

    @Test
    public void approveCreate() throws Exception {
    	TelemetryTwoProfile profile = createProfileChange();
        List<TelemetryTwoChange<TelemetryTwoProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());

        assertNull(telemetryTwoProfileService.getEntityDAO().getOne(profile.getId()));
        TelemetryTwoChange<TelemetryTwoProfile> change = changesByEntityId.get(0);

        approveChange(change.getId());
        assertEquals(profile.getId(), telemetryTwoProfileService.getOne(profile.getId()).getId());
    }

    @Test
    public void approveUpdate() throws Exception {
    	TelemetryTwoProfile profile = createProfileChange();
        List<TelemetryTwoChange<TelemetryTwoProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());
        telemetryProfileChangeService.approve(changesByEntityId.get(0).getId());
        String newProfileName = "Changed Telemetry2 profile name";
        TelemetryTwoProfile changedProfile = createTelemetryTwoProfile(profile.getId(), newProfileName);

        saveChangedProfile(changedProfile);
        changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());

        approveChange(changesByEntityId.get(0).getId());
        assertEquals(changedProfile.getId(), telemetryTwoProfileService.getOne(changedProfile.getId()).getId());
    }

    @Test
    public void revertCreate() throws Exception {
    	TelemetryTwoProfile profile = createProfileChange();
        List<TelemetryTwoChange<TelemetryTwoProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());
        TelemetryTwoChange<TelemetryTwoProfile> change = changesByEntityId.get(0);

        approveChange(change.getId());
        assertNotNull(telemetryTwoProfileService.getEntityDAO().getOne(profile.getId()));

        revertChange(change.getId());
        assertNull(telemetryTwoProfileService.getEntityDAO().getOne(profile.getId()));
    }

    @Test
    public void revertUpdate() throws Exception {
    	TelemetryTwoProfile profile = createProfileChange();
        List<TelemetryTwoChange<TelemetryTwoProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());
        TelemetryTwoChange<TelemetryTwoProfile> change = changesByEntityId.get(0);

        approveChange(change.getId());
        assertNotNull(telemetryTwoProfileService.getEntityDAO().getOne(profile.getId()));

        String newProfileName = "Changed Telemetry2 profine name";
        TelemetryTwoProfile changedProfile = createTelemetryTwoProfile(profile.getId(), newProfileName);

        saveChangedProfile(changedProfile);
        change = changeCrudService.getChangesByEntityId(profile.getId()).get(0);
        approveChange(change.getId());

        assertEquals(changedProfile.getId(), telemetryTwoProfileService.getOne(changedProfile.getId()).getId());
        revertChange(change.getId());
        assertEquals(profile.getId(), telemetryTwoProfileService.getOne(profile.getId()).getId());
    }

    @Test
    public void cancelChange() throws Exception {
    	TelemetryTwoProfile profile = createProfileChange();
        List<TelemetryTwoChange<TelemetryTwoProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profile.getId());
        assertEquals(1, changesByEntityId.size());
        TelemetryTwoChange<TelemetryTwoProfile> change = changesByEntityId.get(0);

        mockMvc.perform(get(TelemetryTwoChangeController.URL + "/cancel/{changeId}", change.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        List<TelemetryTwoChange<TelemetryTwoProfile>> changesAfterCanceling = changeCrudService.getChangesByEntityId(profile.getId());
        assertEquals(0, changesAfterCanceling.size());
    }

    @Test
    public void getGroupedChanges() throws Exception {
        String profileId = UUID.randomUUID().toString();
        createAndSaveProfile(profileId);
        Map<String, List<TelemetryTwoChange<TelemetryTwoProfile>>> groupedChanges = createTelemetryTwoProfileChangesForTheSameId(profileId, 10);
        mockMvc.perform(get(TelemetryTwoChangeController.URL + "/changes/grouped/byId")
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(groupedChanges)));
    }

    @Test
    public void getChangeIds() throws Exception {
        Integer changeSize = 10;
        List<TelemetryTwoChange<TelemetryTwoProfile>> changes = createProfileChanges(changeSize);
        List<String> entityIds = getEntityIds(changes);

        mockMvc.perform(get(TelemetryTwoChangeController.URL + "/entityIds")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(entityIds)));
    }

    @Test
    public void verifyErrorMessagesIfChangeIsNotApproved() throws Exception {
        String profileId = UUID.randomUUID().toString();
        List<String> changeIds = createChangesForTheSameTelemetryProfile(profileId);
         mockMvc.perform(post(TelemetryTwoChangeController.URL + "/approveChanges")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(changeIds)))
                .andExpect(status().isOk());
    }


    @Test
    public void removeProfileAndRevert() {
        String profileId = UUID.randomUUID().toString();
        createAndSaveProfile(profileId);
        telemetryTwoProfileService.delete(profileId);
        assertNull(telemetryTwoProfileService.getEntityDAO().getOne(profileId));
        ApprovedTelemetryTwoChange<TelemetryTwoChange> change = approvedChangeCrudService.getAll().get(0);
        telemetryProfileChangeService.revert(change.getId());
        assertNotNull(telemetryTwoProfileService.getOne(profileId));
    }



    private List<String> createChangesForTheSameTelemetryProfile(String profileId) {
        createAndSaveProfile(profileId);
        telemetryTwoProfileService.writeDeleteChange(profileId);
        telemetryTwoProfileService.writeUpdateChange(createTelemetryTwoProfile(profileId));
        List<String> changeIds = getChangeIds(changeCrudService.getChangesByEntityId(profileId));
        return changeIds;
    }

    private TelemetryTwoProfile createProfileChange() {
    	TelemetryTwoProfile profile = createTelemetryTwoProfile();
    	telemetryTwoProfileService.writeCreateChange(profile);
        return profile;
    }

    private List<TelemetryTwoChange<TelemetryTwoProfile>> createProfileChanges(Integer size) {
        List<TelemetryTwoChange<TelemetryTwoProfile>> changes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
        	TelemetryTwoProfile profile = createTelemetryTwoProfile(UUID.randomUUID().toString(), PROFILE_NAME_PREFFIX + i);
        	TelemetryTwoChange<TelemetryTwoProfile> change = telemetryTwoProfileService.writeCreateChange(profile);
            changes.add(change);
        }
        return changes;
    }

    private List<ApprovedTelemetryTwoChange<TelemetryTwoProfile>> createApprovedChanges(List<TelemetryTwoChange<TelemetryTwoProfile>> changes) {
        List<ApprovedTelemetryTwoChange<TelemetryTwoProfile>> approvedChanges = new ArrayList<>();
        for (TelemetryTwoChange<TelemetryTwoProfile> change : changes) {
            approvedChanges.add(approveChange(change));
        }
        return approvedChanges;
    }

    private ApprovedTelemetryTwoChange<TelemetryTwoProfile> approveChange(TelemetryTwoChange<TelemetryTwoProfile> change) {
        return telemetryProfileChangeService.approve(change.getId());
    }

    private void approveChange(String changeId) throws Exception {
        mockMvc.perform(get(TelemetryTwoChangeController.URL + "/approve/{changeId}", changeId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private void approveChanges(List<String> changeIds) throws Exception {
        mockMvc.perform(post(TelemetryTwoChangeController.URL + "/approveChanges")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(changeIds)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(new HashMap<>())));
    }

    private List<String> getChangeIds(List<TelemetryTwoChange<TelemetryTwoProfile>> changes) {
        List<String> entityIds = new ArrayList<>();
        for (TelemetryTwoChange<TelemetryTwoProfile> change : changes) {
            entityIds.add(change.getId());
        }
        return entityIds;
    }

    private List<String> getEntityIds(List<TelemetryTwoChange<TelemetryTwoProfile>> changes) {
        List<String> entityIds = new ArrayList<>();
        for (TelemetryTwoChange<TelemetryTwoProfile> change : changes) {
            entityIds.add(change.getEntityId());
        }
        return entityIds;
    }

    private void revertChange(String approveId) throws Exception {
        mockMvc.perform(get(TelemetryTwoChangeController.URL + "/revert/{approveId}", approveId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private void saveChangedProfile(TelemetryTwoProfile profile) throws Exception {
        mockMvc.perform(put("/" + TelemetryTwoProfileController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(profile)))
                .andExpect(status().isOk());
    }

    private TelemetryTwoProfile createAndSaveProfile(String id) {
    	TelemetryTwoProfile profile = createTelemetryTwoProfile(id);
    	telemetryTwoProfileDAO.setOne(profile.getId(), profile);
        return profile;
    }

}