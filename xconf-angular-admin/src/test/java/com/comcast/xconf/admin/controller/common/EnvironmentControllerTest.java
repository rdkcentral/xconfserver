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
 *  Created: 11/30/15 4:13 PM
 */

package com.comcast.xconf.admin.controller.common;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.Environment;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.search.SearchFields;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.*;

import static com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation.IS;
import static com.comcast.xconf.estbfirmware.factory.RuleFactory.ENV;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EnvironmentControllerTest extends BaseControllerTest {


    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCreateEnvironment() throws Exception {
        Environment environment = createEnvironment();

        mockMvc.perform(post("/" + EnvironmentController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(environment)))
                .andExpect(status().isCreated());

        Environment savedEnvironment = environmentDAO.getOne(environment.getId());

        assertEquals(environment, QueriesHelper.nullifyUnwantedFields(savedEnvironment));

    }

    @Test
    public void updateEnvironment() throws Exception {
        Environment environment = createEnvironment();
        environmentDAO.setOne(environment.getId(), environment);
        environment.setDescription("changed environment description");

        mockMvc.perform(put("/" + EnvironmentController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(environment)))
                .andExpect(status().isOk());

        Environment savedEnvironment = environmentDAO.getOne(environment.getId());

        assertEquals(QueriesHelper.nullifyUnwantedFields(environment), QueriesHelper.nullifyUnwantedFields(savedEnvironment));
    }

    @Test
    public void testGetAllEnvironments() throws Exception {
        Environment environment = createEnvironment();
        environmentDAO.setOne(environment.getId(), environment);

        String responseOneEntity = mockMvc.perform(get("/" + EnvironmentController.URL_MAPPING + "/" + environment.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Environment savedEnvironment = mapper.readValue(responseOneEntity, Environment.class);
        savedEnvironment.setUpdated(null);

        assertEquals(environment, savedEnvironment);

        String responseList = mockMvc.perform(get("/" + EnvironmentController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<Environment> savedEnvironments = mapper.readValue(responseList, new TypeReference<List<Environment>>() {});
        savedEnvironments.get(0).setUpdated(null);

        assertEquals(Lists.newArrayList(environment), savedEnvironments);
    }

    @Test
    public void getEnvironments() throws Exception {
        Environment env1 = saveEnvironment(createEnvironment("id1"));
        Environment env2 = saveEnvironment(createEnvironment("id2"));
        Environment env3 = saveEnvironment(createEnvironment("id3"));
        String expectedNumberOfItems = "3";
        List<Environment> expectedResult = Arrays.asList(env1, env2);

        MockHttpServletResponse response = performGetRequestAndVerifyResponse("/" + EnvironmentController.URL_MAPPING + "/page",
                new HashMap<String, String>(){{
                    put("pageNumber", "1");
                    put("pageSize", "2");
                }}, expectedResult).andReturn().getResponse();

        final Object actualNumberOfItems = response.getHeaderValue("numberOfItems");
        assertEquals(expectedNumberOfItems, actualNumberOfItems);
    }

    @Test
    public void deleteEnvironment() throws Exception {
        Environment environment = createEnvironment();
        environmentDAO.setOne(environment.getId(), environment);
        mockMvc.perform(delete("/" + EnvironmentController.URL_MAPPING + "/" + environment.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/" + EnvironmentController.URL_MAPPING + "/" + environment.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void cantDeleteEnvironmentIfUsedByRuleOfDifferentApplicationType() throws Exception {
        Environment environment = createEnvironment();
        environmentDAO.setOne(environment.getId(), environment);

        FirmwareRule firmwareRule = createFirmwareRule(new Condition(ENV, IS, FixedArg.from(environment.getId())), "configId", TemplateNames.ENV_MODEL_RULE);
        firmwareRule.setApplicationType(XHOME);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        String message = String.format("Environment %s is used", environment.getId());
        mockMvc.perform(delete("/" + EnvironmentController.URL_MAPPING + "/" + environment.getId()).cookie(stbCookie))
                .andExpect(status().isConflict())
                .andExpect(content().string(new Contains(message)));
    }

    @Test
    public void exportOne() throws Exception {
        Environment environment = createEnvironment();
        environmentDAO.setOne(environment.getId(), environment);

        MockHttpServletResponse response = mockMvc.perform(get("/" + EnvironmentController.URL_MAPPING + "/" + environment.getId())
                .param("export", "export")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(QueriesHelper.nullifyUnwantedFields(environment)))))
                .andReturn().getResponse();
        assertEquals(Lists.newArrayList("Content-Disposition", "Content-Type"), new ArrayList<>(response.getHeaderNames()));
    }

    @Test
    public void exportAll() throws Exception {
        Environment environment = createEnvironment();
        environmentDAO.setOne(environment.getId(), environment);

        MockHttpServletResponse response = mockMvc.perform(get("/" + EnvironmentController.URL_MAPPING)
                .param("export", "export")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(QueriesHelper.nullifyUnwantedFields(environment)))))
                .andReturn().getResponse();
        assertEquals(Lists.newArrayList("Content-Disposition", "Content-Type"), new ArrayList<>(response.getHeaderNames()));
    }

    @Test
    public void checkSorting() throws Exception {
        List<XMLPersistable> environments = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            Environment environment = changeId(createEnvironment(), "environmentId" + i);
            environmentDAO.setOne(environment.getId(), environment);
            environments.add(QueriesHelper.nullifyUnwantedFields(environment));
        }

        mockMvc.perform(get("/" + EnvironmentController.URL_MAPPING))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(environments)));
    }

    @Test
    public void searchByContext() throws Exception {
        Environment environment = createEnvironment();
        environment.setId(UUID.randomUUID().toString());
        environmentDAO.setOne(environment.getId(), environment);
        Environment environment1 = createEnvironment();
        environment1.setId(UUID.randomUUID().toString());
        environment1.setDescription("environmentDescription2");
        environmentDAO.setOne(environment1.getId(), environment1);
        Map<String, String> context = new HashMap<>();
        context.put(SearchFields.ID, environment.getId());

        verifySearchResult("/" + EnvironmentController.URL_MAPPING + "/filtered", context, Lists.newArrayList(environment));

        context.clear();
        context.put(SearchFields.DESCRIPTION, environment1.getDescription());

        verifySearchResult("/" + EnvironmentController.URL_MAPPING + "/filtered", context, Lists.newArrayList(environment1));
    }

    private Environment changeId(Environment environment, String id) {
        environment.setId(id);
        return environment;
    }

}