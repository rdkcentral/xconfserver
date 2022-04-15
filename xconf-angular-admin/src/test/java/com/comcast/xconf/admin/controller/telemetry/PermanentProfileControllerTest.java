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

import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.change.ApprovedChange;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.service.change.ApprovedChangeCrudService;
import com.comcast.xconf.service.change.ChangeCrudService;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PermanentProfileControllerTest extends BaseControllerTest{

    @Autowired
    protected SimpleDao<String, Change> changeDao;

    @Autowired
    protected SimpleDao<String, ApprovedChange> approvedDao;

    @Autowired
    private ChangeCrudService<PermanentTelemetryProfile> changesCrudService;

    @Autowired
    private ApprovedChangeCrudService<PermanentTelemetryProfile> approvedChangeCrudService;

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
    public void createUpdateProfile() throws Exception {
        PermanentTelemetryProfile telemetryProfile = createTelemetryProfile();

        mockMvc.perform(post("/" + PermanentProfileController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryProfile)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(telemetryProfile)));

        List<Change<PermanentTelemetryProfile>> changesByEntityId = changesCrudService.getChangesByEntityId(telemetryProfile.getId());
        assertTrue(changesByEntityId.get(0).getNewEntity().equals(telemetryProfile));
    }

    @Test
    public void exportOne() throws Exception {
        PermanentTelemetryProfile telemetryProfile = createTelemetryProfile();
        permanentTelemetryDAO.setOne(telemetryProfile.getId(), telemetryProfile);

        performExportRequestAndVerifyResponse("/"+ PermanentProfileController.URL_MAPPING + "/" + telemetryProfile.getId(), Lists.newArrayList(telemetryProfile), ApplicationType.STB);
    }

    @Test
    public void exportAll() throws Exception {
        PermanentTelemetryProfile profile1 = saveTelemetryProfile(createTelemetryProfile("id1", "a"));
        PermanentTelemetryProfile profile2 = saveTelemetryProfile(createTelemetryProfile("id2", "b"));
        List<PermanentTelemetryProfile> expectedResult = Arrays.asList(profile1, profile2);

        performExportRequestAndVerifyResponse("/" + PermanentProfileController.URL_MAPPING, expectedResult, ApplicationType.STB);
    }

    @Test
    public void update() throws Exception {
        PermanentTelemetryProfile telemetryProfile = createTelemetryProfile();
        permanentTelemetryDAO.setOne(telemetryProfile.getId(), telemetryProfile);
        telemetryProfile = createTelemetryProfile();
        telemetryProfile.setUploadRepository("http://changedurl.com");

        mockMvc.perform(put("/" + PermanentProfileController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryProfile)))
                .andExpect(status().isOk());

        List<Change<PermanentTelemetryProfile>> changesByEntityId = changesCrudService.getChangesByEntityId(telemetryProfile.getId());
        assertTrue(changesByEntityId.get(0).getNewEntity().equals(telemetryProfile));
    }

    @Test
    public void getAllTelemetryProfiles() throws Exception {
        PermanentTelemetryProfile telemetryProfile = createTelemetryProfile();
        permanentTelemetryDAO.setOne(telemetryProfile.getId(), telemetryProfile);

        mockMvc.perform(get("/" + PermanentProfileController.URL_MAPPING + "/" + telemetryProfile.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(telemetryProfile)));

        mockMvc.perform(get("/" + PermanentProfileController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(telemetryProfile))));
    }

    @Test
    public void getTelemetryProfiles() throws Exception {
        PermanentTelemetryProfile profile1 = saveTelemetryProfile(createTelemetryProfile("id1", "a"));
        PermanentTelemetryProfile profile2 = saveTelemetryProfile(createTelemetryProfile("id2", "b"));
        PermanentTelemetryProfile profile3 = saveTelemetryProfile(createTelemetryProfile("id3", "c"));
        String expectedNumberOfItems = "3";
        List<PermanentTelemetryProfile> expectedResult = Arrays.asList(profile1, profile2);

        MockHttpServletResponse response = performGetRequestAndVerifyResponse("/" + PermanentProfileController.URL_MAPPING + "/page",
                new HashMap<String, String>(){{
                    put("pageNumber", "1");
                    put("pageSize", "2");
                }}, expectedResult).andReturn().getResponse();

        final Object actualNumberOfItems = response.getHeaderValue("numberOfItems");
        assertEquals(expectedNumberOfItems, actualNumberOfItems);
    }

    @Test
    public void deleteTelemetryProfile() throws Exception {
        PermanentTelemetryProfile telemetryProfile = createTelemetryProfile();
        permanentTelemetryDAO.setOne(telemetryProfile.getId(), telemetryProfile);

        mockMvc.perform(delete("/" + PermanentProfileController.URL_MAPPING + "/" + telemetryProfile.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertTrue(CollectionUtils.isNotEmpty(changesCrudService.getChangesByEntityId(telemetryProfile.getId())));
    }

    @Test
    public void checkSorting() throws Exception {
        List<PermanentTelemetryProfile> profiles = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            PermanentTelemetryProfile profile = changeProfileIdAndName(createTelemetryProfile(), "profileId" + i, "profileName" + i);
            permanentTelemetryDAO.setOne(profile.getId(), profile);
            profiles.add(profile);
        }

        mockMvc.perform(get("/" + PermanentProfileController.URL_MAPPING))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(profiles)));
    }

    @Test
    public void searchTelemetryProfile() throws Exception {
        PermanentTelemetryProfile profile1 = saveTelemetryProfile(createTelemetryProfile("id1", "Profile123"));
        PermanentTelemetryProfile profile2 = saveTelemetryProfile(createTelemetryProfile("id2", "Profile456"));

        List<PermanentTelemetryProfile> expectedResult = Arrays.asList(profile1);

        mockMvc.perform(post("/" + PermanentProfileController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(Collections.singletonMap(SearchFields.NAME, profile1.getName()))))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(expectedResult)));

        expectedResult = Arrays.asList(profile2);

        mockMvc.perform(post("/" + PermanentProfileController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(Collections.singletonMap(SearchFields.NAME, "456"))))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(expectedResult)));

        expectedResult = Arrays.asList(profile1, profile2);

        mockMvc.perform(post("/" + PermanentProfileController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(Collections.singletonMap(SearchFields.NAME, "profile"))))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(expectedResult)));
    }

    private PermanentTelemetryProfile changeProfileIdAndName(PermanentTelemetryProfile profile, String id, String name) {
        profile.setId(id);
        profile.setName(name);
        return profile;
    }
}