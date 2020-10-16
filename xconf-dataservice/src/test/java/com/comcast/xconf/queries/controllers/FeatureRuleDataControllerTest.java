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

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.service.rfc.FeatureRuleDataService;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FeatureRuleDataControllerTest extends BaseQueriesControllerTest {

    @Autowired
    private FeatureRuleDataService featureRuleDataService;

    @Test
    public void getOne() throws Exception {
        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);

        mockMvc.perform(get(FeatureRuleDataController.API_URL + "/{id}", featureRule.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json(JsonUtil.toJson(featureRule)));
    }

    @Test
    public void create() throws Exception {
        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);

        mockMvc.perform(post(FeatureRuleDataController.API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(featureRule)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(featureRule)));
        assertNotNull(featureRuleDAO.getOne(featureRule.getId()));
    }

    @Test
    public void throwEntityExistsExceptionIfFeatureRuleWithExistingIdIsCreated() throws Exception {
        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);

        mockMvc.perform(post(FeatureRuleDataController.API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(featureRule)))
                .andExpect(status().isConflict())
                .andExpect(content().string("\"Entity with id: " + featureRule.getId() + " already exists\""));
    }

    @Test
    public void throwRuleValidationExceptionIfFeatureRuleWithExistingConditionIsCreated() throws Exception {
        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);
        FeatureRule featureRuleWithDuplicateCondition = createFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);

        mockMvc.perform(post(FeatureRuleDataController.API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(featureRuleWithDuplicateCondition)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"Rule has duplicate: " + featureRule.getName() + "\""));
    }

    @Test
    public void throwExceptionIfFeatureRuleWithNonExistingFeatureIdIsCreated() throws Exception {
        String nonExistingFeatureId = UUID.randomUUID().toString();
        FeatureRule featureRule = createFeatureRule(Lists.newArrayList(nonExistingFeatureId), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);

        mockMvc.perform(post(FeatureRuleDataController.API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(featureRule)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("\"Entity with id: " + nonExistingFeatureId + " does not exist\""));
    }

    @Test
    public void update() throws Exception {
        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);
        FeatureRule featureRuleToUpdate = createFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);
        featureRuleToUpdate.setId(featureRule.getId());
        featureRuleToUpdate.setName("changedName");

        mockMvc.perform(put(FeatureRuleDataController.API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(featureRuleToUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(featureRuleToUpdate)));
        assertEquals(featureRuleToUpdate.getName(), featureRuleDAO.getOne(featureRule.getId()).getName());
    }

    @Test
    public void reorganizePriorityByUpdate() throws Exception {
        Feature feature = createAndSaveFeature();
        FeatureRule xhomeFeatureRule = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), XHOME);

        FeatureRule stbFeatureRule1 = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-2")), STB);

        FeatureRule stbFeatureRule2 = createFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-3")), STB);
        stbFeatureRule2.setPriority(2);
        featureRuleDAO.setOne(stbFeatureRule2.getId(), stbFeatureRule2);

        FeatureRule featureRuleWithUpdatedPriority = createFeatureRule(stbFeatureRule1.getFeatureIds(), stbFeatureRule1.getRule(), STB);
        featureRuleWithUpdatedPriority.setId(stbFeatureRule1.getId());
        featureRuleWithUpdatedPriority.setPriority(2);

        mockMvc.perform(put(FeatureRuleDataController.API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(featureRuleWithUpdatedPriority)))
                .andExpect(status().isOk());

        assertEquals(Integer.valueOf(1), featureRuleDAO.getOne(xhomeFeatureRule.getId()).getPriority());
        assertEquals(Integer.valueOf(1), featureRuleDAO.getOne(stbFeatureRule2.getId()).getPriority());
        assertEquals(Integer.valueOf(2), featureRuleDAO.getOne(stbFeatureRule1.getId()).getPriority());

    }

    @Test
    public void reorganizePriorityByCreate() throws Exception {
        Feature feature = createAndSaveFeature();
        FeatureRule xhomeFeatureRule = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), XHOME);

        FeatureRule stbFeatureRule1 = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-2")), STB);
        FeatureRule stbFeatureRule2 = createFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-3")), STB);

        mockMvc.perform(post(FeatureRuleDataController.API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(stbFeatureRule2)))
                .andExpect(status().isCreated());

        assertEquals(Integer.valueOf(1), featureRuleDAO.getOne(xhomeFeatureRule.getId()).getPriority());
        assertEquals(Integer.valueOf(1), featureRuleDAO.getOne(stbFeatureRule2.getId()).getPriority());
        assertEquals(Integer.valueOf(2), featureRuleDAO.getOne(stbFeatureRule1.getId()).getPriority());
    }

    @Test
    public void reorganizePriorityByDelete() throws Exception {
        Feature feature = createAndSaveFeature();
        FeatureRule xhomeFeatureRule = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), XHOME);

        FeatureRule stbFeatureRule1 = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-2")), STB);

        FeatureRule stbFeatureRule2 = createFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-3")), STB);
        stbFeatureRule2.setPriority(2);
        featureRuleDAO.setOne(stbFeatureRule2.getId(), stbFeatureRule2);

        mockMvc.perform(delete(FeatureRuleDataController.API_URL + "/{id}", stbFeatureRule1.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertEquals(Integer.valueOf(1), featureRuleDAO.getOne(xhomeFeatureRule.getId()).getPriority());
        assertEquals(Integer.valueOf(1), featureRuleDAO.getOne(stbFeatureRule2.getId()).getPriority());
    }

    @Test
    public void deleteOne() throws Exception {
        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createAndSaveFeatureRule(Lists.newArrayList(feature.getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);

        mockMvc.perform(delete(FeatureRuleDataController.API_URL + "/{id}", featureRule.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void importAll() throws Exception {
        List<FeatureRule> featureRules = createFeatureRules();

        assertEquals(0, featureRuleDAO.getAll().size());

        mockMvc.perform(post(FeatureRuleDataController.API_URL + "/importAll")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(featureRules)))
                .andExpect(status().isOk());

        assertEquals(featureRules.size(), featureRuleDAO.getAll().size());
    }

    @Test
    public void getFilteredByName() throws Exception {
        List<FeatureRule> featureRules = createAndSaveFeatureRules();

        mockMvc.perform(get(FeatureRuleDataController.API_URL + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param(SearchFields.NAME, featureRules.get(0).getName())
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(featureRules.get(0).getName()));
    }

    @Test
    public void getFilteredByKey() throws Exception {
        List<FeatureRule> featureRules = createAndSaveFeatureRules();
        String freeArgName = featureRules.get(0).getRule().getCondition().getFreeArg().getName();

        mockMvc.perform(get(FeatureRuleDataController.API_URL + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param(SearchFields.FREE_ARG, freeArgName)
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(featureRules.get(0).getId()))
                .andExpect(jsonPath("$[0].rule.condition.freeArg.name").value(freeArgName));
    }

    @Test
    public void getFilteredByValue() throws Exception {
        List<FeatureRule> featureRules = createAndSaveFeatureRules();
        String fixedArgValue = featureRules.get(0).getRule().getCondition().getFixedArg().getValue().toString();
        Map<String, String> expectedFixedArgResult = new LinkedHashMap<>();
        expectedFixedArgResult.put("java.lang.String", fixedArgValue);

        mockMvc.perform(get(FeatureRuleDataController.API_URL + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param(SearchFields.FIXED_ARG, fixedArgValue)
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(featureRules.get(0).getId()))
                .andExpect(jsonPath("$[0].rule.condition.fixedArg.bean.value").value(expectedFixedArgResult));
    }

    @Test
    public void getFilteredByKeyAndValue() throws Exception {
        List<FeatureRule> featureRules = createAndSaveFeatureRules();
        String freeArgName = featureRules.get(0).getRule().getCondition().getFreeArg().getName();
        String fixedArgValue = featureRules.get(0).getRule().getCondition().getFixedArg().getValue().toString();
        Map<String, String> expectedFixedArgResult = new LinkedHashMap<>();
        expectedFixedArgResult.put("java.lang.String", fixedArgValue);

        mockMvc.perform(get(FeatureRuleDataController.API_URL + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param(SearchFields.FREE_ARG, freeArgName)
                .param(SearchFields.FIXED_ARG, fixedArgValue)
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(featureRules.get(0).getId()))
                .andExpect(jsonPath("$[0].rule.condition.freeArg.name").value(freeArgName))
                .andExpect(jsonPath("$[0].rule.condition.fixedArg.bean.value").value(expectedFixedArgResult));
    }

    @Test
    public void getFilteredByFeature() throws Exception {
        List<FeatureRule> featureRules = createAndSaveFeatureRules();
        String featureId = featureRules.get(0).getFeatureIds().get(0);
        String featureName = featureDAO.getOne(featureId).getFeatureName();
        mockMvc.perform(get(FeatureRuleDataController.API_URL + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param(SearchFields.FEATURE_INSTANCE, featureName)
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(featureRules.get(0).getId()))
                .andExpect(jsonPath("$[0].featureIds[0]").value(featureId));
    }

    private List<FeatureRule> createFeatureRules() {
        return Lists.newArrayList(
                createFeatureRule(Lists.newArrayList(createAndSaveFeature().getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB),
                createFeatureRule(Lists.newArrayList(createAndSaveFeature().getId()), createRule(createCondition(RuleFactory.ENV, StandardOperation.IS, "TEST")), STB)
        );
    }

    private List<FeatureRule> createAndSaveFeatureRules() {
        List<FeatureRule> featureRules = createFeatureRules();
        for (FeatureRule featureRule : featureRules) {
            featureRuleDataService.create(featureRule);
        }
        return featureRules;
    }
}
