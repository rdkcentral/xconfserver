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

package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.search.SearchFields;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ActivationVersionControllerTest extends BaseControllerTest {

    @Test
    public void getAll() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        List<ActivationVersion> activationFirmwareList = createAndSaveActivationFirmwareList(10, defaultModelId, firmwareConfig.getFirmwareVersion());

        mockMvc.perform(get("/" + ActivationVersionController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(activationFirmwareList)));
    }

    @Test
    public void getOne() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        ActivationVersion activationVersion = createAndSaveActivationMinimumVersion(defaultModelId, defaultPartnerId, firmwareConfig.getFirmwareVersion());
        String url = "/" + ActivationVersionController.URL_MAPPING + "/" + activationVersion.getId();

        mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(activationVersion)));
    }

    @Test
    public void deleteOne() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        List<ActivationVersion> activationFirmwareList = createAndSaveActivationFirmwareList(10, defaultModelId, firmwareConfig.getFirmwareVersion());

        ActivationVersion activationVersionToDelete = activationFirmwareList.get(2);

        String url = "/" + ActivationVersionController.URL_MAPPING + "/" + activationVersionToDelete.getId();
        mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertNull(activationVersionService.getOne(activationVersionToDelete.getId()));
    }

    @Test
    public void create() throws Exception {
        Model model = saveModel(createModel(defaultModelId));
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();

        ActivationVersion activationVersion = createActivationMinimumVersion(model.getId(), defaultPartnerId, firmwareConfig.getFirmwareVersion());

        mockMvc.perform(post("/" + ActivationVersionController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(activationVersion)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(activationVersion)));

        assertEquals(activationVersion, activationVersionService.getOne(activationVersion.getId()));
    }

    @Test
    public void update() throws Exception {
        Model model = saveModel(createModel(defaultModelId));
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();

        ActivationVersion savedActivationVersion = createAndSaveActivationMinimumVersion(model.getId(), defaultPartnerId, firmwareConfig.getFirmwareVersion());
        FirmwareConfig newFirmwareConfig = createFirmwareConfig(UUID.randomUUID().toString(), "changedVersion", Sets.newHashSet(model.getId()), "changed firmware description");
        ActivationVersion activationVersionToUpdate = createActivationMinimumVersion(model.getId(), defaultPartnerId, newFirmwareConfig.getFirmwareVersion());
        activationVersionToUpdate.setId(savedActivationVersion.getId());

        mockMvc.perform(put("/" + ActivationVersionController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(activationVersionToUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(activationVersionToUpdate)));
    }

    @Test
    public void exportOne() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        ActivationVersion activationVersion = createAndSaveActivationMinimumVersion(defaultModelId, defaultPartnerId, firmwareConfig.getFirmwareVersion());
        String url = "/" + ActivationVersionController.URL_MAPPING + "/" + activationVersion.getId();

        MockHttpServletResponse response = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param("export", "export"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(activationVersion))))
                .andReturn().getResponse();

        assertTrue(response.getHeader("Content-Disposition").contains(ExportFileNames.ACTIVATION_MINIMUM_VERSION.getName() + activationVersion.getId() + "_" + STB));
    }

    @Test
    public void exportAll() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        List<ActivationVersion> activationFirmwareList = createAndSaveActivationFirmwareList(10, defaultModelId, firmwareConfig.getFirmwareVersion());

        MockHttpServletResponse response = mockMvc.perform(get("/" + ActivationVersionController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param("export", "export"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(activationFirmwareList)))
                .andReturn().getResponse();

        assertTrue(response.getHeader("Content-Disposition").contains(ExportFileNames.ALL_ACTIVATION_MINIMUM_VERSIONS.getName() + "_" + STB));
    }

    @Test
    public void verifySorting() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        List<ActivationVersion> activationFirmwareList = createAndSaveActivationFirmwareList(5, defaultModelId, firmwareConfig.getFirmwareVersion());

        String activationVersionStrResponse = mockMvc.perform(get("/" + ActivationVersionController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(activationFirmwareList)))
                .andReturn().getResponse().getContentAsString();

        List<ActivationVersion> activationVersionsResponse = JsonUtil.fromJson(new TypeReference<List<ActivationVersion>>() {}, activationVersionStrResponse);

        assertTrue(Ordering.natural().isOrdered(activationVersionsResponse));
    }

    @Test
    public void createWithNonExistedFirmwareVersion() throws Exception {
        Model model = saveModel(createModel(defaultModelId));
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();

        ActivationVersion activationVersion = createActivationMinimumVersion(model.getId(), defaultPartnerId, firmwareConfig.getFirmwareVersion());
        activationVersion.getFirmwareVersions().add("NON_EXISTED_FIRMWARE_VERSION");

        mockMvc.perform(post("/" + ActivationVersionController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(activationVersion)))
                .andExpect(status().isCreated());

        assertEquals(Sets.newHashSet(firmwareConfig.getFirmwareVersion()), activationVersionService.getOne(activationVersion.getId()).getFirmwareVersions());
    }

    @Test
    public void tryToCreateActivationVersionWithoutFirmwareVersionAndRegEx() throws Exception {
        Model model = saveModel(createModel(defaultModelId));
        ActivationVersion activationVersion = createActivationMinimumVersion(model.getId(), defaultPartnerId, "");
        activationVersion.setRegularExpressions(new HashSet<String>());
        activationVersion.setFirmwareVersions(new HashSet<String>());

        mockMvc.perform(post("/" + ActivationVersionController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(activationVersion)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("FirmwareVersion or regular expression should be specified"));
    }

    public void getPage() throws Exception {
        Model model = createModel(defaultModelId);
        modelDAO.setOne(model.getId(), model);

        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        List<ActivationVersion> activationFirmwareList = createAndSaveActivationFirmwareList(15, defaultModelId, firmwareConfig.getFirmwareVersion());
        Collections.sort(activationFirmwareList);
        mockMvc.perform(post("/" + ActivationVersionController.URL_MAPPING + "/filtered")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(activationFirmwareList.subList(0, 10))));
    }

    @Test
    public void searchByContext() throws Exception {
        Model model = createModel(defaultModelId);
        modelDAO.setOne(model.getId(), model);

        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        List<ActivationVersion> activationFirmwareList = createAndSaveActivationFirmwareList(5, defaultModelId, firmwareConfig.getFirmwareVersion());
        Collections.sort(activationFirmwareList);
        mockMvc.perform(post("/" + ActivationVersionController.URL_MAPPING + "/filtered")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1").content(JsonUtil.toJson(Collections.singletonMap(SearchFields.DESCRIPTION, activationFirmwareList.get(0).getDescription()))))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(activationFirmwareList.subList(0, 1))));
    }

    @Test
    public void createWithWrongApplicationType() throws Exception {
        Model model = saveModel(createModel(defaultModelId));
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();

        ActivationVersion activationVersion = createActivationMinimumVersion(model.getId(), defaultPartnerId, firmwareConfig.getFirmwareVersion());
        activationVersion.setApplicationType("sstb");

        mockMvc.perform(post("/" + ActivationVersionController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(activationVersion)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ApplicationType is not valid: notValidApplicationType= " + activationVersion.getApplicationType()));
    }
}
