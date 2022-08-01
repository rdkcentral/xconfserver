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
import com.comcast.xconf.change.ApprovedTelemetryTwoChange;
import com.comcast.xconf.change.ChangeOperation;
import com.comcast.xconf.change.EntityType;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.service.telemetrytwochange.TelemetryTwoChangeCrudService;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class TelemetryTwoProfileControllerTest extends BaseControllerTest{

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
	
    @Autowired
    protected SimpleDao<String, TelemetryTwoChange> changeDao;

    @Autowired
    protected SimpleDao<String, ApprovedTelemetryTwoChange> approvedDao;

    @Autowired
    private TelemetryTwoChangeCrudService<TelemetryTwoProfile> changeCrudService;

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
    public void create() throws Exception {
        TelemetryTwoProfile telemetryTwoProfile = createTelemetryTwoProfile();
        mockMvc.perform(post("/" + TelemetryTwoProfileController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryTwoProfile)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(telemetryTwoProfile)));
        
        List<TelemetryTwoChange<TelemetryTwoProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(telemetryTwoProfile.getId());
        assertEquals(changesByEntityId.get(0).getNewEntity().getId(),telemetryTwoProfile.getId());
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
        telemetryProfile.setJsonconfig(TELEMETRY_2_CONFIG_TO_UPDATE);

        mockMvc.perform(put("/" + TelemetryTwoProfileController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryProfile)))
                .andExpect(status().isOk());
        
        List<TelemetryTwoChange<TelemetryTwoProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(telemetryProfile.getId());
        assertEquals(changesByEntityId.get(0).getNewEntity().getId(),telemetryProfile.getId());
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

        assertEquals(0, changeCrudService.getAll().size());

        mockMvc.perform(delete("/" + TelemetryTwoProfileController.URL_MAPPING + "/" + telemetryProfile.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.entityType").value(EntityType.TELEMETRY_TWO_PROFILE.toString()))
                .andExpect(jsonPath("$.operation").value(ChangeOperation.DELETE.toString()))
                .andExpect(jsonPath("$.oldEntity").exists())
                .andExpect(jsonPath("$.newEntity").doesNotExist());

        assertNotNull(telemetryTwoProfileDAO.getOne(telemetryProfile.getId()));
        assertEquals(1, changeCrudService.getAll().size());
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
    public void deleteTelemetryProfileIfThereIsChangeForIt() throws Exception {
        TelemetryTwoProfile telemetryProfile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(telemetryProfile.getId(), telemetryProfile);
        telemetryProfile = createTelemetryTwoProfile();
        telemetryProfile.setJsonconfig(TELEMETRY_2_CONFIG_TO_UPDATE);

        assertTrue(CollectionUtils.isEmpty(changeCrudService.getAll()));

        mockMvc.perform(put("/" + TelemetryTwoProfileController.URL_MAPPING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(telemetryProfile)))
                .andExpect(status().isOk());

        assertEquals(1, changeCrudService.getAll().size());

        mockMvc.perform(delete("/" + TelemetryTwoProfileController.URL_MAPPING + "/" + telemetryProfile.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.entityType").value(EntityType.TELEMETRY_TWO_PROFILE.toString()))
                .andExpect(jsonPath("$.operation").value(ChangeOperation.DELETE.toString()))
                .andExpect(jsonPath("$.oldEntity").exists())
                .andExpect(jsonPath("$.newEntity").doesNotExist());
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