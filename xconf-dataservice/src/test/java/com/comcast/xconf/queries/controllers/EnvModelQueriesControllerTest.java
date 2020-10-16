/* 
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
 *
 * Author: ikostrov
 * Created: 31.08.15 17:52
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.Environment;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.firmware.FirmwareRule;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;
import org.springframework.http.MediaType;

import java.util.Collections;

import static com.comcast.xconf.queries.QueriesHelper.nullifyUnwantedFields;
import static com.comcast.xconf.queries.QueryConstants.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EnvModelQueriesControllerTest extends BaseQueriesControllerTest {

    @Test
    public void testGetModels() throws Exception {
        Model model = createDefaultModel();
        mockMvc.perform(post("/" + UPDATE_MODELS).contentType(MediaType.APPLICATION_JSON)
            .content(JsonUtil.toJson(model)))
            .andExpect(status().isCreated());

        performRequestAndVerifyResponse(QUERIES_MODELS, Collections.singleton(nullifyUnwantedFields(model)));
        performRequestAndVerifyResponse(QUERIES_MODELS + "/{id}", model.getId(), nullifyUnwantedFields(model));


        mockMvc.perform(delete("/" + DELETE_MODELS + "/{id}", model.getId()))
                .andExpect(status().isNoContent());

        performRequestAndVerifyResponse(QUERIES_MODELS, Collections.emptyList());
    }

    @Test
    public void cantDeleteModelIfUsedByRule() throws Exception {
        Model model = createAndSaveModel(defaultModelId);

        FirmwareRule firmwareRule = createEnvModelFirmwareRule("testEnvModelRule", "configId", "envId", model.getId(),null);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        mockMvc.perform(delete("/" + DELETE_MODELS + "/{id}", model.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(new Contains("Can't delete model.")));
    }

    @Test
    public void getModelByWrongId() throws Exception {
        String wrongModelId = "wrongId";

        mockMvc.perform(get("/" + QUERIES_MODELS + "/" + wrongModelId)
                .param("version", "3.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createModelWithWhitespaceInId() throws Exception {
        Model model = createModel("test modelId");

        mockMvc.perform(post("/" + UPDATE_MODELS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(model)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(model)));

        assertNotNull(modelDAO.getOne(model.getId()));
    }

    @Test
    public void createEnvironmentWithWhitespaceInId() throws Exception {
        Environment environment = createEnvironment("test envId");

        mockMvc.perform(post("/" + UPDATE_ENVIRONMENTS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(environment)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(environment)));

        assertNotNull(environmentDAO.getOne(environment.getId()));
    }

    @Test
    public void createEnvironmentWithWrongName() throws Exception {
        Environment environment = createEnvironment("!@#$%^&");

        mockMvc.perform(post("/" + UPDATE_ENVIRONMENTS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(environment)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"Id is invalid\""));
    }

    @Test
    public void verifyEnvironmentDuplicates() throws Exception {
        Environment environment = createAndSaveEnvironment("testEnvironment");

        mockMvc.perform(post("/" + UPDATE_ENVIRONMENTS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(environment)))
                .andExpect(status().isConflict())
                .andExpect(content().string("\"Environment with current name already exists\""));
    }

    @Test
    public void createModelWithWrongName() throws Exception {
        Model model = createModel("!@#$#$%^");

        mockMvc.perform(post("/" + UPDATE_MODELS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(model)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"Id is invalid\""));
    }

    @Test
    public void verifyModelDuplicates() throws Exception {
        Model model = createAndSaveModel("testModel");

        mockMvc.perform(post("/" + UPDATE_MODELS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(model)))
                .andExpect(status().isConflict())
                .andExpect(content().string("\"Model with current name already exists\""));
    }

    @Test
    public void updateModelDescritpion() throws Exception {
        Model model = createAndSaveModel(defaultModelId);
        Model updatedModel = new Model(model.getId(), "updated description");

        mockMvc.perform(put("/" + UPDATE_MODELS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(updatedModel)))
                .andExpect(status().isOk());

        assertEquals(updatedModel.getDescription(), modelDAO.getOne(model.getId()).getDescription());
    }

    @Test
    public void modelUpdateThrowsExceptionIfModelDoesNotExist() throws Exception {
        Model updatedModel = new Model(defaultModelId, "updated description");

        mockMvc.perform(put("/" + UPDATE_MODELS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(updatedModel)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("\"" + updatedModel.getId() + " model does not exist\""));

        assertNull(modelDAO.getOne(updatedModel.getId()));
    }

    @Test
    public void testEnvironments() throws Exception {
        Environment environment = createEnvironment();

        performRequestAndVerifyResponse(QUERIES_ENVIRONMENTS, Collections.singleton(nullifyUnwantedFields(environment)));
        performRequestAndVerifyResponse(QUERIES_ENVIRONMENTS + "/{id}", environment.getId(), nullifyUnwantedFields(environment));

        mockMvc.perform(delete("/" + DELETE_ENVIRONMENTS + "/{id}", environment.getId()))
                .andExpect(status().isNoContent());

        performRequestAndVerifyResponse(QUERIES_ENVIRONMENTS, Collections.emptyList());
    }

    @Test
    public void cantDeleteEnvironmentIfUsedByRule() throws Exception {
        Environment environment = createEnvironment();

        FirmwareRule firmwareRule = createEnvModelFirmwareRule("testEnvModelRule", "configId", environment.getId(), "modelId", null);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        mockMvc.perform(delete("/" + DELETE_ENVIRONMENTS + "/{id}", environment.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(new Contains("Environment is used")));

    }

    private Environment createEnvironment() throws Exception {
        Environment environment = new Environment();
        environment.setDescription("environmentDescription");
        environment.setId("environmentId");
        mockMvc.perform(post("/" + UPDATE_ENVIRONMENTS).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(environment)))
                .andExpect(status().isCreated());
        return environment;
    }

    @Test
    public void getNotExistedEnvironment() throws Exception {
        Environment environment = createEnvironment("id111");
        environmentDAO.setOne(environment.getId(), environment);

        mockMvc.perform(get("/" + QUERIES_ENVIRONMENTS + "/{id}", "wrongId"))
                .andExpect(status().isBadRequest());
    }
}
