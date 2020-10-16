/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
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
 * Author: mdolina
 */
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.service.firmware.ActivationVersionDataService;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.*;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static com.comcast.xconf.util.ImportHelper.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ActivationVersionDataControllerTest extends BaseQueriesControllerTest {

    @Autowired
    private ActivationVersionDataService activationVersionDataService;

    @Test
    public void getAll() throws Exception {
        List<ActivationVersion> activationVersions = createAndSaveActivationVersions(10);

        mockMvc.perform(get(ActivationVersionDataController.URL_MAPPING)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(activationVersions)));
    }

    @Test
    public void getOne() throws Exception {
        Model model = createAndSaveModel(defaultModelId);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId().toUpperCase(), FirmwareConfig.DownloadProtocol.http);
        ActivationVersion activationVersion = createActivationVersion(defaultPartnerId,
                Sets.newHashSet(firmwareConfig.getFirmwareVersion()), new HashSet<String>());

        activationVersionDataService.create(activationVersion);

        mockMvc.perform(get(ActivationVersionDataController.URL_MAPPING + "/{id}", activationVersion.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(activationVersion)));
    }

    @Test
    public void importAllSuccessfulResult() throws Exception {
        List<ActivationVersion> activationVersions = createActivationVersions(10);
        Map<String, List<String>> expectedSuccessImportResult = buildImportSuccessResult(activationVersions);

        mockMvc.perform(post(ActivationVersionDataController.URL_MAPPING + "/importAll")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(activationVersions)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(expectedSuccessImportResult)));
    }

    @Test
    public void importAllWithException() throws Exception {
        List<ActivationVersion> activationVersions = createActivationVersions(2);
        activationVersions.get(0).setModel("NOT_EXISTING_MODEL_ID");

        Map<String, List<String>> importResult = buildImportResultMap();
        importResult.get(NOT_IMPORTED).add(activationVersions.get(0).getId());
        importResult.get(IMPORTED).add(activationVersions.get(1).getId());

        mockMvc.perform(post(ActivationVersionDataController.URL_MAPPING + "/importAll")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(activationVersions)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(importResult)));
    }

    @Test
    public void deleteOne() throws Exception {
        Model model = createAndSaveModel(defaultModelId);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId().toUpperCase(), FirmwareConfig.DownloadProtocol.http);
        ActivationVersion activationVersion = createActivationVersion(defaultPartnerId,
                Sets.newHashSet(firmwareConfig.getFirmwareVersion()), new HashSet<String>());

        activationVersionDataService.create(activationVersion);
        assertEquals(1, firmwareRuleDao.getAll().size());

        mockMvc.perform(delete(ActivationVersionDataController.URL_MAPPING + "/{id}", activationVersion.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(0, firmwareRuleDao.getAll().size());
    }

    @Test
    public void update() throws Exception {
        Model model = createAndSaveModel(defaultModelId);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId().toUpperCase(), FirmwareConfig.DownloadProtocol.http);
        ActivationVersion activationVersion = createActivationVersion(defaultPartnerId,
                Sets.newHashSet(firmwareConfig.getFirmwareVersion()), new HashSet<String>());

        activationVersionDataService.create(activationVersion);

        ActivationVersion updatedActivationVersion = createActivationVersion("CHANGED_PARTNER_ID",
                Sets.newHashSet(firmwareConfig.getFirmwareVersion()), new HashSet<String>());
        updatedActivationVersion.setId(activationVersion.getId());

        mockMvc.perform(put(ActivationVersionDataController.URL_MAPPING)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(updatedActivationVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partnerId").value(updatedActivationVersion.getPartnerId()));
        assertEquals(updatedActivationVersion.getPartnerId(), activationVersionDataService.getOne(activationVersion.getId()).getPartnerId());
    }

    @Test
    public void create() throws Exception {
        Model model = createAndSaveModel(defaultModelId);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId().toUpperCase(), FirmwareConfig.DownloadProtocol.http);
        ActivationVersion activationVersion = createActivationVersion(defaultPartnerId,
                Sets.newHashSet(firmwareConfig.getFirmwareVersion()), new HashSet<String>());

        mockMvc.perform(post(ActivationVersionDataController.URL_MAPPING)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(activationVersion)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(activationVersion)));
        assertEquals(activationVersion, activationVersionDataService.getOne(activationVersion.getId()));
    }

    @Test
    public void throwEntityExistsExceptionIfActivationVersionWithExistingIdIsCreated() throws Exception {
        Model model = createAndSaveModel(defaultModelId);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId().toUpperCase(), FirmwareConfig.DownloadProtocol.http);
        ActivationVersion activationVersion = createActivationVersion(defaultPartnerId,
                Sets.newHashSet(firmwareConfig.getFirmwareVersion()), new HashSet<String>());

        activationVersionDataService.create(activationVersion);

        mockMvc.perform(post(ActivationVersionDataController.URL_MAPPING)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(activationVersion)))
                .andExpect(status().isConflict())
                .andExpect(content().string("\"Entity with id: " + activationVersion.getId() + " already exists\""));

    }

    @Test
    public void filterByModel() throws Exception {
        List<ActivationVersion> activationVersions = createAndSaveActivationVersions(3);

        mockMvc.perform(get(ActivationVersionDataController.URL_MAPPING + "/filtered")
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.MODEL, activationVersions.get(0).getModel())
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(activationVersions.get(0)))));
    }

    @Test
    public void filterByFirmwareVersion() throws Exception {
        List<ActivationVersion> activationVersions = createAndSaveActivationVersions(3);

        mockMvc.perform(get(ActivationVersionDataController.URL_MAPPING + "/filtered")
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.FIRMWARE_VERSION, activationVersions.get(0).getFirmwareVersions().iterator().next())
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(activationVersions.get(0)))));
    }

    @Test
    public void filterByPartnerId() throws Exception {
        List<ActivationVersion> activationVersions = createAndSaveActivationVersions(3);

        mockMvc.perform(get(ActivationVersionDataController.URL_MAPPING + "/filtered")
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.PARTNER_ID, activationVersions.get(0).getPartnerId())
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(activationVersions.get(0)))));
    }

    @Test
    public void filterByDescription() throws Exception {
        List<ActivationVersion> activationVersions = createAndSaveActivationVersions(3);

        mockMvc.perform(get(ActivationVersionDataController.URL_MAPPING + "/filtered")
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.DESCRIPTION, activationVersions.get(0).getDescription())
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(activationVersions.get(0)))));
    }

    @Test
    public void filterByRegularExpression() throws Exception {
        List<ActivationVersion> activationVersions = createAndSaveActivationVersions(3);

        mockMvc.perform(get(ActivationVersionDataController.URL_MAPPING + "/filtered")
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.REGULAR_EXPRESSION, activationVersions.get(0).getRegularExpressions().iterator().next())
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(activationVersions.get(0)))));
    }

    @Test
    public void filterByApplicationType() throws Exception {
        createAndSaveActivationVersions(3);
        ActivationVersion xhomeActivationVersion = createAndSaveXhomeActivationVersion();

        mockMvc.perform(get(ActivationVersionDataController.URL_MAPPING + "/filtered")
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.APPLICATION_TYPE, XHOME))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(xhomeActivationVersion))));
    }

    private List<ActivationVersion> createAndSaveActivationVersions(Integer size) {
        List<ActivationVersion> activationVersions = createActivationVersions(size);
        for (ActivationVersion activationVersion : activationVersions) {
            activationVersionDataService.create(activationVersion);
        }
        return activationVersions;
    }

    private List<ActivationVersion> createActivationVersions(Integer size) {
        List<ActivationVersion> activationVersions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Model model = createAndSaveModel(defaultModelId + i);
            FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion + i, model.getId().toUpperCase(), FirmwareConfig.DownloadProtocol.http);
            ActivationVersion activationVersion = createActivationVersion("DESCRIPTION" + i, model.getId().toUpperCase(),
                    defaultPartnerId + i, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), Sets.newHashSet("regex" + i));
            activationVersions.add(activationVersion);
        }
        return activationVersions;
    }

    private Map<String, List<String>> buildImportSuccessResult(List<ActivationVersion> activationVersions) {
        Map<String, List<String>> importResult = buildImportResultMap();
        for (ActivationVersion activationVersion : activationVersions) {
            importResult.get(IMPORTED).add(activationVersion.getId());
        }
        return importResult;
    }

    private ActivationVersion createAndSaveXhomeActivationVersion() {
        Model xhomeModel = createAndSaveModel("XHOME_MODEL");
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion,
                xhomeModel.getId().toUpperCase(), FirmwareConfig.DownloadProtocol.http, XHOME);

        ActivationVersion activationVersion = createActivationVersion("XHOME_ACTIVATION_VERSION", xhomeModel.getId(), defaultPartnerId,
                Sets.newHashSet(firmwareConfig.getFirmwareVersion()), new HashSet<String>());
        activationVersion.setApplicationType(XHOME);
        activationVersionDataService.create(activationVersion);
        return activationVersion;
    }
}
