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
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.queries.QueryConstants;
import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static com.comcast.xconf.queries.QueriesHelper.nullifyUnwantedFields;
import static com.comcast.xconf.queries.controllers.FirmwareConfigQueriesController.MAX_ALLOWED_NUMBER_OF_PROPERTIES;
import static com.comcast.xconf.queries.controllers.FirmwareConfigQueriesController.MAX_ALLOWED_NUMBER_OF_PROPERTIES_ERR_MSG_TEMPLATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Class to verify firmware config API.
 */
public class FirmwareConfigQueriesControllerTest extends BaseQueriesControllerTest{

    @Test
    public void testGetFirmwareConfigs() throws Exception {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        nullifyUnwantedFields(firmwareConfig);

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FIRMWARES, Collections.singleton(firmwareConfig));
    }

    @Test
    public void verifyUnwantedFieldsAreNotReturnedInResponse() throws Exception {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);

        mockMvc.perform(get("/" + QueryConstants.QUERIES_FIRMWARES + "/" + firmwareConfig.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.rebootImmediately").doesNotExist())
                .andExpect(jsonPath("$.firmwareDownloadProtocol").doesNotExist())
                .andExpect(jsonPath("$.applicationType").doesNotExist())
                .andExpect(status().isOk());
    }

    @Test
    public void getFirmwareConfigsByApplicationType() throws Exception {
        Map<String, FirmwareConfig> firmwareConfigs = createAndSaveFirmwareConfigs(STB, STB);
        String url = "/" + QueryConstants.QUERIES_FIRMWARES;
        List<FirmwareConfig> xhomeExpectedResult = Collections.singletonList(nullifyUnwantedFields(firmwareConfigs.get(XHOME)));
        performGetWithApplication(url, XHOME, xhomeExpectedResult);

        List<FirmwareConfig> stbExpectedResult = Collections.singletonList(nullifyUnwantedFields(firmwareConfigs.get(STB)));
        performGetWithApplication(url, STB, stbExpectedResult);

        performGetWithApplication(url, "", stbExpectedResult);
    }

    @Test
    public void getFirmwareConfigsByModelAndApplicationType() throws Exception {
        Map<String, FirmwareConfig> firmwareConfigs = createAndSaveFirmwareConfigs(STB, XHOME);
        String url = "/" + QueryConstants.QUERIES_FIRMWARES + "/model/" + defaultModelId.toUpperCase();
        List<FirmwareConfig> xhomeExpectedResult = Collections.singletonList(nullifyUnwantedFields(firmwareConfigs.get(XHOME)));
        performGetWithApplication(url, XHOME, xhomeExpectedResult);
        List<FirmwareConfig> stbExpectedResult = Collections.singletonList(nullifyUnwantedFields(firmwareConfigs.get(STB)));
        performGetWithApplication(url, STB, stbExpectedResult);

        performGetWithApplication(url, "", stbExpectedResult);
    }

    @Test
    public void getFirmwareConfigByWrongId() throws Exception {
        String wrongFirmwareConfigId = "wrongId";
        mockMvc.perform(get("/" + QueryConstants.QUERIES_FIRMWARES + "/" + wrongFirmwareConfigId)
                .param("version", "3.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetFirmwareConfigsByModel() throws Exception {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        firmwareConfig = nullifyUnwantedFields(firmwareConfig);
        String modelId = firmwareConfig.getSupportedModelIds().iterator().next();
        performRequestAndVerifyResponse(QueryConstants.QUERIES_FIRMWARES + "/model/{modelId}", modelId, Collections.singleton(firmwareConfig));
    }

    @Test
    public void testCreateFirmwareConfig() throws Exception {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig = nullifyUnwantedFields(firmwareConfig);

        mockMvc.perform(post("/" + QueryConstants.UPDATE_FIRMWARES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(firmwareConfig)))
                .andExpect(status().isOk());

        assertEquals(firmwareConfig, firmwareConfigDAO.getOne(firmwareConfig.getId()));
    }

    @Test
    public void createFirmwareConfigWithApplicationType() throws Exception {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig = nullifyUnwantedFields(firmwareConfig);

        mockMvc.perform(post("/" + QueryConstants.UPDATE_FIRMWARES)
                .contentType(MediaType.APPLICATION_JSON)
                .param("applicationType", XHOME).content(JsonUtil.toJson(firmwareConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("applicationType").value(XHOME));
    }

    @Test
    public void createFirmwareConfigWithWrongApplicationType() throws Exception {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig = nullifyUnwantedFields(firmwareConfig);
        String url = "/" + QueryConstants.UPDATE_FIRMWARES;

        performPostWithWrongApplicationType(url, firmwareConfig);
    }

    @Test
    public void testUpdateFirmwareConfig() throws Exception {
        FirmwareConfig existingFirmwareConfig = createDefaultFirmwareConfig();
        existingFirmwareConfig.setId(defaultFirmwareConfigId);
        firmwareConfigDAO.setOne(existingFirmwareConfig.getId(), existingFirmwareConfig);
        FirmwareConfig firmwareConfigToUpdate = createDefaultFirmwareConfig();
        firmwareConfigToUpdate.setId(defaultFirmwareConfigId);
        firmwareConfigToUpdate.setDescription("updated description");
        nullifyUnwantedFields(firmwareConfigToUpdate);

        mockMvc.perform(
                put("/" + QueryConstants.UPDATE_FIRMWARES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(firmwareConfigToUpdate)))
                .andExpect(status().isOk());

        assertEquals(firmwareConfigToUpdate, firmwareConfigDAO.getOne(existingFirmwareConfig.getId()));
    }

    @Test
    public void testDeleteFirmwareConfig() throws Exception {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        firmwareConfig = nullifyUnwantedFields(firmwareConfig);
        mockMvc.perform(
                delete("/" + QueryConstants.DELETE_FIRMWARES + "/" + firmwareConfig.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(firmwareConfig)))
                .andExpect(status().isNoContent());
        assertEquals(null, firmwareConfigDAO.getOne(firmwareConfig.getId()));
    }

    @Test
    public void testGetFirmwareConfigsBySupportedModels() throws Exception{
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        mockMvc.perform(
                post("/" + QueryConstants.QUERIES_FIRMWARES + "/bySupportedModels")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(JsonUtil.toJson(firmwareConfig.getSupportedModelIds()))
        ).andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(Lists.newArrayList(QueriesHelper.nullifyUnwantedFields(firmwareConfig)))));
    }

    @Test
    public void getFirmwareConfigsBySupportedModelsAndApplicationType() throws Exception {
        Map<String, FirmwareConfig> firmwareConfigs = createAndSaveFirmwareConfigs(STB, XHOME);
        String url = "/" + QueryConstants.QUERIES_FIRMWARES + "/bySupportedModels";
        ArrayList<FirmwareConfig> stbExpectedResult = Lists.newArrayList(QueriesHelper.nullifyUnwantedFields(firmwareConfigs.get(STB)));
        performPostWithApplication(url, STB, firmwareConfigs.get(STB).getSupportedModelIds(), stbExpectedResult);

        ArrayList<FirmwareConfig> xhomeExpectedResult = Lists.newArrayList(QueriesHelper.nullifyUnwantedFields(firmwareConfigs.get(XHOME)));
        performPostWithApplication(url, XHOME, firmwareConfigs.get(XHOME).getSupportedModelIds(), xhomeExpectedResult);

        performPostWithApplication(url, "", firmwareConfigs.get(STB).getSupportedModelIds(), stbExpectedResult);
    }

    @Test
    public void getFirmwareConfigWithParameters() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Map<String, String> properties = Collections.singletonMap("testKey", "testValue");

        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig.setProperties(properties);
        save(firmwareConfig);

        nullifyUnwantedFields(firmwareConfig);

        mockMvc.perform(get("/" + QueryConstants.QUERIES_FIRMWARES + "/" + firmwareConfig.getId())
                .accept(MediaType.APPLICATION_JSON)).andExpect(content().json(JsonUtil.toJson(firmwareConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.properties.testKey").value("testValue"));
    }

    @Test
    public void updateFirmwareConfigByParameters() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        save(firmwareConfig);

        assertTrue(MapUtils.isEmpty(firmwareConfigDAO.getOne(firmwareConfig.getId()).getProperties()));

        Map<String, String> parameters = Collections.singletonMap("testKey", "testValue");

        FirmwareConfig firmwareConfigToUpdate = new FirmwareConfig(firmwareConfig);
        firmwareConfigToUpdate.setProperties(parameters);

        mockMvc.perform(put("/" + QueryConstants.UPDATE_FIRMWARES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(firmwareConfigToUpdate)))
                .andExpect(status().isOk());

        assertEquals(parameters, firmwareConfigDAO.getOne(firmwareConfigToUpdate.getId()).getProperties());
    }

    @Test
    public void createFirmwareConfigWithParameters() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig.setProperties(Collections.singletonMap("testKey", "testValue"));

        mockMvc.perform(post("/" + QueryConstants.UPDATE_FIRMWARES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(firmwareConfig)))
                .andExpect(status().isOk());

        assertEquals(firmwareConfig.getProperties(), firmwareConfigDAO.getOne(firmwareConfig.getId()).getProperties());
    }

    @Test
    public void createFirmwareConfigWithMoreThanMaxAllowedParametersSize() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();

        Map<String, String> properties = IntStream.range(0, MAX_ALLOWED_NUMBER_OF_PROPERTIES + 1).boxed().collect(Collectors.toMap(number -> "key" + number, number -> "value" + number));
        firmwareConfig.setProperties(properties);

        String expectedErrorMsg = String.format(MAX_ALLOWED_NUMBER_OF_PROPERTIES_ERR_MSG_TEMPLATE, MAX_ALLOWED_NUMBER_OF_PROPERTIES);
        mockMvc.perform(post("/" + QueryConstants.UPDATE_FIRMWARES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(firmwareConfig)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"" + expectedErrorMsg + "\""));
    }

    @Test
    public void removeParametersFromFirmwareConfig() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Map<String, String> parameters = Collections.singletonMap("testKey", "testValue");

        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig.setProperties(parameters);
        save(firmwareConfig);

        assertEquals(parameters, firmwareConfigDAO.getOne(firmwareConfig.getId()).getProperties());

        FirmwareConfig firmwareConfigToUpdate = new FirmwareConfig(firmwareConfig);
        firmwareConfigToUpdate.setProperties(new HashMap<>());

        mockMvc.perform(put("/" + QueryConstants.UPDATE_FIRMWARES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(firmwareConfigToUpdate)))
                .andExpect(status().isOk());

        assertTrue(MapUtils.isEmpty(firmwareConfigDAO.getOne(firmwareConfigToUpdate.getId()).getProperties()));
    }

    @Test
    public void createFirmwareConfigWithEmptyKeyParameter() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig.setProperties(Collections.singletonMap("", "testValue"));

        mockMvc.perform(post("/" + QueryConstants.UPDATE_FIRMWARES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(firmwareConfig)))
                .andExpect(status().isBadRequest()).andExpect(content().string("\"Key is empty\""));
    }

    private Map<String, FirmwareConfig> createAndSaveFirmwareConfigs(String stbDescription, String xhomeDescription) throws Exception {
        Map<String, FirmwareConfig> firmwareConfigs = new HashMap<>();

        FirmwareConfig firmwareConfig1 = createDefaultFirmwareConfig();
        firmwareConfig1.setId(UUID.randomUUID().toString());
        firmwareConfig1.setDescription(stbDescription);
        firmwareConfig1.setApplicationType(STB);
        firmwareConfigDAO.setOne(firmwareConfig1.getId(), CloneUtil.clone(firmwareConfig1));
        firmwareConfigs.put(STB, firmwareConfig1);

        FirmwareConfig firmwareConfig2 = createDefaultFirmwareConfig();
        firmwareConfig2.setId(UUID.randomUUID().toString());
        firmwareConfig2.setDescription(xhomeDescription);
        firmwareConfig2.setApplicationType(XHOME);
        firmwareConfigDAO.setOne(firmwareConfig2.getId(), CloneUtil.clone(firmwareConfig2));
        firmwareConfigs.put(XHOME, firmwareConfig2);

        return firmwareConfigs;
    }

    @Test
    public void getFirmwareConfigsByWrongApplicationType() throws Exception {
        performGetWithWrongApplicationType("/" + QueryConstants.QUERIES_FIRMWARES);
    }
}