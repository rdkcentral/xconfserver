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
 * Author: Yury Stagit
 * Created: 12/14/16  12:00 PM
 */
package com.comcast.xconf.admin.controller.rfc;

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.hydra.astyanax.config.XconfSpecificConfig;
import com.comcast.xconf.admin.controller.AbstractControllerTest;
import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.controller.rfc.feature.FeatureController;
import com.comcast.xconf.admin.controller.rfc.featurerule.FeatureRuleController;
import com.comcast.xconf.admin.utils.TestDataBuilder;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FeatureRuleControllerTest extends AbstractControllerTest<FeatureRule> {

    @Autowired
    private XconfSpecificConfig xconfSpecificConfig;

    @Override
    public String getUrlMapping() {
        return FeatureRuleController.URL_MAPPING;
    }

    @Test
    public void testCreatingRuleWithExistsOperation() throws Exception {
        Feature feature = TestDataBuilder.createFeature();
        performPostRequest(FeatureController.URL_MAPPING, feature);
        Rule rule = TestDataBuilder.createRule(TestDataBuilder.createCondition(RuleFactory.MODEL, StandardOperation.EXISTS, null));
        FeatureRule featureRule = TestDataBuilder.createFeatureRule(Collections.singletonList(feature.getId()), rule);
        entityList.add(featureRule);

        performPostRequestAndVerify(getUrlMapping(), featureRule);
    }

    @Override
    public FeatureRule createEntity() throws Exception {
        List<String> featureIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Feature feature = TestDataBuilder.createFeature();
            performPostRequest(FeatureController.URL_MAPPING, feature);
            featureIds.add(feature.getId());
        }

        return TestDataBuilder.createFeatureRule(featureIds, TestDataBuilder.createRule(TestDataBuilder.createCondition(RuleFactory.MODEL, StandardOperation.IS, UUID.randomUUID().toString().toUpperCase())));
    }

    @Override
    public FeatureRule updateEntity(FeatureRule featureRule) throws Exception {
        return TestDataBuilder.modifyFeatureRule(featureRule, "new-" + featureRule.getId(), featureRule.getFeatureIds(), featureRule.getRule());
    }

    @Override
    public void assertEntity(ResultActions resultActions, Object featureRule) throws Exception {
        resultActions.andExpect(content().json(JsonUtil.toJson(featureRule)));
    }

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.FEATURE_RULE.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_FEATURE_RUlES.getName();
    }


    @Test
    public void verifyPrioritiesNotChangedForXHome() throws Exception {
        int listSize = 5;
        List<FeatureRule> stbRules = createFeatureRulesList(listSize, ApplicationType.STB);
        List<FeatureRule> xhomeRules = createFeatureRulesList(listSize, ApplicationType.XHOME);

        FeatureRule rule = CloneUtil.clone(stbRules.get(0));
        rule.setPriority(5);
        mockMvc.perform(post("/" + getUrlMapping() + "/" + rule.getId() + "/priority/" + listSize)
                .cookie(stbCookie))
                .andExpect(status().isOk());
        assertXhomeRulesAreTheSame(xhomeRules);

        rule.setPriority(1);
        rule.getRule().setCondition(createCondition(rule.getId()));
        performPutRequestAndVerify(getUrlMapping(), rule);
        assertXhomeRulesAreTheSame(xhomeRules);

        performDeleteRequestAndVerify(getUrlMapping() + "/" + rule.getId());
        assertXhomeRulesAreTheSame(xhomeRules);
    }

    @Test
    public void validateNumberOfFeatures() throws Exception {
        xconfSpecificConfig.setAllowedNumberOfFeatures(20);
        Rule rule = Rule.Builder.of(new Condition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from("test"))).build();
        FeatureRule featureRule = TestDataBuilder.createFeatureRule(new ArrayList<String>(), rule);

        assertFeatureNumberValidationError(featureRule, "Features should be specified");

        List<String> featureIds = createFeatures(23);
        featureRule.setFeatureIds(featureIds);

        assertFeatureNumberValidationError(featureRule, "Number of Features should be up to " + xconfSpecificConfig.getAllowedNumberOfFeatures() + " items");
    }

    @Test
    public void getAllowedNumberOfFeatures() throws Exception {
        xconfSpecificConfig.setAllowedNumberOfFeatures(5);
        mockMvc.perform(get("/" + getUrlMapping() + "/allowedNumberOfFeatures")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(xconfSpecificConfig.getAllowedNumberOfFeatures().toString()));
    }

    private void assertFeatureNumberValidationError(FeatureRule featureRule, String message) throws Exception {
        mockMvc.perform(post("/" + getUrlMapping())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(featureRule)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));
    }

    private void assertXhomeRulesAreTheSame(List<FeatureRule> featureRules) throws Exception {
        mockMvc.perform(post("/" + getUrlMapping() + "/filtered")
                .cookie(xhomeCookie)
                .param("pageSize", "10")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(featureRules)));
    }

    private List<FeatureRule> createFeatureRulesList(int listSize, String applicationType) throws Exception {
        List<FeatureRule> rules = new ArrayList<>();
        for (int i = 1; i <= listSize; i++) {
            rules.add(createAndSaveFeatureRule(i + applicationType, i, applicationType));
        }
        return rules;
    }

    private FeatureRule createAndSaveFeatureRule(String s, int i, String applicationType) throws Exception {
        FeatureRule rule = createEntity();
        rule.setPriority(i);
        rule.setApplicationType(applicationType);
        featureRuleDAO.setOne(rule.getId(), rule);
        return rule;
    }

    private List<String> createFeatures(Integer numberOfFeatures) {
        List<String> featureIds = new ArrayList<>();
        for (int i = 0; i < numberOfFeatures; i++) {
            Feature feature = new Feature();
            String id = UUID.randomUUID().toString();
            feature.setId(id);
            feature.setName(id + "_name");
            feature.setFeatureName(id + "_featureName");
            feature.setEnable(true);
            featureDAO.setOne(feature.getId(), feature);
            featureIds.add(id);
        }
        return featureIds;
    }

}
