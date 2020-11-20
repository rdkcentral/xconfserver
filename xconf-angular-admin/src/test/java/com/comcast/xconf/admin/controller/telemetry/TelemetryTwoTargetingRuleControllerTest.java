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
 *  Author: Jeyabala Murugan
 *  Created: 08/13/2020
 */

package com.comcast.xconf.admin.controller.telemetry;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.search.SearchFields;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TelemetryTwoTargetingRuleControllerTest extends BaseControllerTest {

    @Test
    public void create() throws Exception {
        TelemetryTwoRule telemetryTwoRule = createTelemetryTwoRule();
        mockMvc.perform(post("/" + TelemetryTwoRuleController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryTwoRule)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(telemetryTwoRule)));
    }

    @Test
    public void exportOne() throws Exception {
        TelemetryTwoRule telemetryTwoRule = createTelemetryTwoRule();
        telemetryTwoRuleDAO.setOne(telemetryTwoRule.getId(), telemetryTwoRule);

        List<TelemetryTwoRule> result = Lists.newArrayList(telemetryTwoRule);
        performExportRequestAndVerifyResponse("/" + TelemetryTwoRuleController.URL_MAPPING + "/" + telemetryTwoRule.getId(), result, ApplicationType.STB);
    }

    @Test
    public void exportAll() throws Exception {
        TelemetryTwoRule rule1 = saveTelemetryTwoRule(createTelemetryTwoRule("1-2-3-4-5", "a"));
        TelemetryTwoRule rule2 = saveTelemetryTwoRule(createTelemetryTwoRule("2-3-4-5-6", "b"));

        List<TelemetryTwoRule> result = Lists.newArrayList(rule1, rule2);
        performExportRequestAndVerifyResponse("/" + TelemetryTwoRuleController.URL_MAPPING, result, ApplicationType.STB);
    }

    @Test
    public void createRuleNormalizeCondition() throws Exception {
        TelemetryTwoRule telemetryTwoRule = createTelemetryTwoRule();
        telemetryTwoRule.setCondition(createCondition("   envFixedArg   ", " env ", StandardOperation.IS));

        String actualResult = mockMvc.perform(post("/" + TelemetryTwoRuleController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryTwoRule))).andReturn().getResponse().getContentAsString();

        assertConditionWasNormalized(actualResult);
    }

    @Test
    public void creatingReturnsValidationException() throws Exception {
        TelemetryTwoRule telemetryTwoRule = createTelemetryTwoRule();
        telemetryTwoRule.getCondition().setOperation(null);
        TelemetryTwoProfile telemetryTwoProfile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(telemetryTwoProfile.getId(), telemetryTwoProfile);

        ResultActions resultActions = mockMvc.perform(post("/" + TelemetryTwoRuleController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryTwoRule)))
                .andExpect(status().isBadRequest());

        Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals(RuleValidationException.class, exception.getClass());
        assertEquals("Operation is null", exception.getMessage());
    }

    @Test
    public void update() throws Exception {
        TelemetryTwoRule telemetryRule = createTelemetryTwoRule();
        telemetryTwoRuleDAO.setOne(telemetryRule.getId(), telemetryRule);
        Condition condition = telemetryRule.getCondition();
        condition.setFreeArg(new FreeArg(FreeArgType.forName("STRING"),  "devlogin"));
        telemetryRule.setCondition(condition);

        mockMvc.perform(put("/" + TelemetryTwoRuleController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryRule)))
                .andExpect(status().isOk());
    }

    @Test
    public void updatingReturnsValidationException() throws Exception {
        TelemetryTwoRule telemetryRule = createTelemetryTwoRule();
        TelemetryTwoProfile telemetryTwoProfile = createTelemetryTwoProfile();
        telemetryTwoProfileDAO.setOne(telemetryTwoProfile.getId(), telemetryTwoProfile);
        telemetryTwoRuleDAO.setOne(telemetryRule.getId(), telemetryRule);
        TelemetryTwoRule ruleToUpdate = createTelemetryTwoRule();
        ruleToUpdate.getCondition().setOperation(null);

        ResultActions resultActions = mockMvc.perform(put("/" + TelemetryTwoRuleController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(ruleToUpdate)))
                .andExpect(status().isBadRequest());

        Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals(RuleValidationException.class, exception.getClass());
        assertEquals("Operation is null", exception.getMessage());
    }

    @Test
    public void updateRuleNormalizeCondition() throws Exception {
        TelemetryTwoRule telemetryRule = createTelemetryTwoRule();
        telemetryTwoRuleDAO.setOne(telemetryRule.getId(), telemetryRule);
        TelemetryTwoRule telemetryRuleToUpdate = createTelemetryTwoRule();
        telemetryRuleToUpdate.setCondition(createCondition("   envFixedArg   ", " env ", StandardOperation.IS));

        String actualResult = mockMvc.perform(put("/" + TelemetryTwoRuleController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(telemetryRuleToUpdate))).andReturn().getResponse().getContentAsString();

        assertConditionWasNormalized(actualResult);
    }

    @Test
    public void getAllTelemetryRules() throws Exception {
        TelemetryTwoRule telemetryRule = createTelemetryTwoRule();
        telemetryTwoRuleDAO.setOne(telemetryRule.getId(), telemetryRule);

        mockMvc.perform(get("/" + TelemetryTwoRuleController.URL_MAPPING + "/" + telemetryRule.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(telemetryRule)));

        mockMvc.perform(get("/" + TelemetryTwoRuleController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(telemetryRule))));
    }

    @Test
    public void getTelemetryRules() throws Exception {
        TelemetryTwoRule rule1 = saveTelemetryTwoRule(createTelemetryTwoRule("1-2-3-4-5", "a"));
        TelemetryTwoRule rule2 = saveTelemetryTwoRule(createTelemetryTwoRule("2-3-4-5-6", "b"));
        TelemetryTwoRule rule3 = saveTelemetryTwoRule(createTelemetryTwoRule("3-4-5-6-7", "c"));
        String expectedNumberOfItems = "3";
        List<TelemetryTwoRule> expectedResult = Arrays.asList(rule1, rule2, rule3);

        MockHttpServletResponse response = mockMvc.perform(post("/" + TelemetryTwoRuleController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "100")
                .param("pageNumber", "1")).andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(expectedResult)))
                .andReturn().getResponse();

        final Object actualNumberOfItems = response.getHeaderValue("numberOfItems");
        assertEquals(expectedNumberOfItems, actualNumberOfItems);
    }

    @Test
    public void deleteTelemetryRule() throws Exception {
        TelemetryTwoRule telemetryRule = createTelemetryTwoRule();
        telemetryTwoRuleDAO.setOne(telemetryRule.getId(), telemetryRule);

        mockMvc.perform(delete("/" + TelemetryTwoRuleController.URL_MAPPING + "/" + telemetryRule.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/" + TelemetryTwoRuleController.URL_MAPPING + "/" + telemetryRule.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void checkSorting() throws Exception {
        List<TelemetryTwoRule> telemetryRules = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            TelemetryTwoRule telemetryRule = changeRuleIdAndName(createTelemetryTwoRule(), "telemetryRuleId" + i, "telemetryRuleName" + i);
            telemetryTwoRuleDAO.setOne(telemetryRule.getId(), telemetryRule);
            telemetryRules.add(telemetryRule);
        }

        mockMvc.perform(get("/" + TelemetryTwoRuleController.URL_MAPPING))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(telemetryRules)));
    }

    @Test
    public void searchFirmwareRuleByFreeAndFixedArg() throws Exception {
        TelemetryTwoRule telemetryRule = createTelemetryTwoRule();
        telemetryRule.setCondition(null);
        List<Rule> compoundParts = new ArrayList<>();
        compoundParts.add(createRule(createCondition("modelId", "model", StandardOperation.IS)));
        Rule rule1 = createRule(createCondition("envId", "env", StandardOperation.IS));
        rule1.setRelation(Relation.AND);
        compoundParts.add(rule1);
        telemetryRule.setCompoundParts(compoundParts);
        telemetryTwoRuleDAO.setOne(telemetryRule.getId(), telemetryRule);

        TelemetryTwoRule telemetryRule1 = createTelemetryTwoRule(createCondition("modelId", "model", StandardOperation.IS));
        telemetryRule1.setId("telemetryRuleId2");
        telemetryTwoRuleDAO.setOne(telemetryRule1.getId(), telemetryRule1);
        Map<String, String> searchContext = new HashMap<>();
        searchContext.put(SearchFields.FREE_ARG, "model");

        mockMvc.perform(post("/" + TelemetryTwoRuleController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(telemetryRule1))));

        searchContext.put(SearchFields.FIXED_ARG, "modelId");

        mockMvc.perform(post("/" + TelemetryTwoRuleController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(telemetryRule1))));

        searchContext.remove(SearchFields.FREE_ARG);

        mockMvc.perform(post("/" + TelemetryTwoRuleController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(telemetryRule1))));

        searchContext.put(SearchFields.FREE_ARG, "model");
        searchContext.put(SearchFields.FIXED_ARG, "wrongModelId");

        mockMvc.perform(post("/" + TelemetryTwoRuleController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList())));
    }

    private TelemetryTwoRule changeRuleIdAndName(TelemetryTwoRule rule, String id, String name) {
        rule.setId(id);
        rule.setName(name);
        return rule;
    }

    private void assertConditionWasNormalized(String actualResult) throws Exception {
        TelemetryTwoRule expectedResult = createTelemetryTwoRule();
        expectedResult.setCondition(createCondition("ENVFIXEDARG", "env", StandardOperation.IS));
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, true);
    }
}