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
 *  Author: mdolina
 *  Created: 11/30/15 4:55 PM
 */

package com.comcast.xconf.admin.controller.common;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.search.SearchFields;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.*;

import static com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation.IS;
import static com.comcast.xconf.estbfirmware.factory.RuleFactory.MODEL;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ModelControllerTest extends BaseControllerTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCreateModel() throws Exception {
        Model model = createModel();

        mockMvc.perform(post("/" + ModelController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(model)))
                .andExpect(status().isCreated());

        Model savedModel = modelDAO.getOne(model.getId());

        assertEquals(model, QueriesHelper.nullifyUnwantedFields(savedModel));
    }

    @Test
    public void updateModel() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        model.setDescription("changed model description");

        mockMvc.perform(put("/" + ModelController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(model)))
                .andExpect(status().isOk());

        Model savedModel = modelDAO.getOne(model.getId());

        assertEquals(QueriesHelper.nullifyUnwantedFields(model), QueriesHelper.nullifyUnwantedFields(savedModel));
    }

    @Test
    public void cantDeleteModelIfUsedByRuleOfDifferentApplicationType() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);

        FirmwareRule firmwareRule = createFirmwareRule(new Condition(MODEL, IS, FixedArg.from(model.getId())), "configId", TemplateNames.ENV_MODEL_RULE);
        firmwareRule.setApplicationType(XHOME);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        String message = String.format("Model %s is used", model.getId());
        mockMvc.perform(delete("/" + ModelController.URL_MAPPING + "/" + model.getId()).cookie(stbCookie))
                .andExpect(status().isConflict())
                .andExpect(content().string(new Contains(message)));
    }

    @Test
    public void testGetAllModels() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);

        String responseOneEntity = mockMvc.perform(get("/" + ModelController.URL_MAPPING + "/" + model.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Model savedModel = mapper.readValue(responseOneEntity, Model.class);
        savedModel.setUpdated(null);

        assertEquals(model, savedModel);

        String responseList = mockMvc.perform(get("/" + ModelController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<Model> savedModels = mapper.readValue(responseList, new TypeReference<List<Model>>() {});
        savedModels.get(0).setUpdated(null);

        assertEquals(Lists.newArrayList(model), savedModels);
    }

    @Test
    public void getModels() throws Exception {
        Model model1 = saveModel(createModel("id1"));
        Model model2 = saveModel(createModel("id2"));
        Model model3 = saveModel(createModel("id3"));
        String expectedNumberOfItems = "3";
        List<Model> expectedResult = Arrays.asList(model1, model2);

        MockHttpServletResponse response = performGetRequestAndVerifyResponse("/" + ModelController.URL_MAPPING + "/page",
                new HashMap<String, String>(){{
                    put("pageNumber", "1");
                    put("pageSize", "2");
                }}, expectedResult).andReturn().getResponse();

        final Object actualNumberOfItems = response.getHeaderValue("numberOfItems");
        assertEquals(expectedNumberOfItems, actualNumberOfItems);
    }

    @Test
    public void deleteModel() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        mockMvc.perform(delete("/" + ModelController.URL_MAPPING + "/" + model.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/" + ModelController.URL_MAPPING + "/" + model.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void exportOne() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);

        MockHttpServletResponse response = mockMvc.perform(get("/" + ModelController.URL_MAPPING + "/" + model.getId())
                .param("export", "export")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(QueriesHelper.nullifyUnwantedFields(model)))))
                .andReturn().getResponse();

        assertEquals(Lists.newArrayList("Content-Disposition", "Content-Type"), new ArrayList<>(response.getHeaderNames()));
    }

    @Test
    public void exportAll() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);

        MockHttpServletResponse response = mockMvc.perform(get("/" + ModelController.URL_MAPPING)
                .param("export", "export")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(QueriesHelper.nullifyUnwantedFields(model)))))
                .andReturn().getResponse();

        assertEquals(Lists.newArrayList("Content-Disposition", "Content-Type"), new ArrayList<>(response.getHeaderNames()));
    }

    @Test
    public void checkSorting() throws Exception {
        List<XMLPersistable> models = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            Model model1 = changeId(createModel(), "modelId" + i);
            modelDAO.setOne(model1.getId(), model1);
            models.add(QueriesHelper.nullifyUnwantedFields(model1));
        }

        mockMvc.perform(get("/" + ModelController.URL_MAPPING))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(models)));
    }

    @Test
    public void searchByContext() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        Model model1 = createModel();
        model1.setId(UUID.randomUUID().toString());
        model.setDescription("modelDescription2");
        modelDAO.setOne(model1.getId(), model1);
        Map<String, String> context = new HashMap<>();
        context.put(SearchFields.ID, model.getId());

        verifySearchResult("/" + ModelController.URL_MAPPING + "/filtered", context, Lists.newArrayList(model));

        context.clear();
        context.put(SearchFields.DESCRIPTION, model1.getDescription());

        verifySearchResult("/" + ModelController.URL_MAPPING + "/filtered", context, Lists.newArrayList(model1));
    }

    @Test
    public void modelIsUsedByActivationVersion() throws Exception {
        Model model = createModel(defaultModelId.toUpperCase());
        saveModel(model);

        FirmwareConfig firmwareConfig = createFirmwareConfig(UUID.randomUUID().toString(), defaultFirmwareVersion, Sets.newHashSet(defaultModelId), "description");
        saveFirmwareConfig(firmwareConfig);
        ActivationVersion activationVersion = createAndSaveActivationMinimumVersion(model.getId(), null, firmwareConfig.getFirmwareVersion());

        mockMvc.perform(delete("/" + ModelController.URL_MAPPING + "/" + model.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Model " + model.getId() + " is used by " + activationVersion.getDescription() + " ActivationVersion"));
    }

    private Model changeId(Model model, String id) {
        model.setId(id);
        return model;
    }
}