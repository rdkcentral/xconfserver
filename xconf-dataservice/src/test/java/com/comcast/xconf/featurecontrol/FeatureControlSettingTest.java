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
 *  Author: ystagit
 *  Created: 02/09/17 00:00 PM
 */
package com.comcast.xconf.featurecontrol;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureControl;
import com.comcast.xconf.rfc.FeatureResponse;
import com.comcast.xconf.rfc.FeatureRule;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.*;

import static com.comcast.xconf.featurecontrol.FeatureControlSettingsController.URL_MAPPING;
import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static com.comcast.xconf.queries.QueriesHelper.nullifyUnwantedFields;
import static com.comcast.xconf.util.HashCalculator.calculateHash;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FeatureControlSettingTest extends BaseQueriesControllerTest {

    @Test
    public void testFeatureSetting() throws Exception {

        List<String> featureIds = new ArrayList<>();
        Set<FeatureResponse> features = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            Feature feature = createAndSaveFeature();
            featureIds.add(feature.getId());
            FeatureResponse featureResponse = new FeatureResponse(feature);
            features.add(nullifyUnwantedFields(featureResponse));
        }

        createAndSaveFeatureRule(featureIds, createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);

        performGetSettingsRequestAndVerifyFeatureControl("model=X1-1", features);
    }

    private void performGetSettingsRequestAndVerifyFeatureControl(String url, Set<FeatureResponse> features) throws Exception {
        FeatureControl featureControl = new FeatureControl();
        featureControl.setFeatures(features);

        mockMvc.perform(get("/" + URL_MAPPING  + "/getSettings?" + url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featureControl.features[0].applicationType").doesNotExist())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonMap("featureControl", featureControl))));
    }

    @Test
    public void getFeatureSettingByApplicationType() throws Exception {
        Map<String, Feature> features = createAndSaveFeatures();
        createAndSaveFeatureRules(features);
        String url = "/" + FeatureControlSettingsController.URL_MAPPING + "/getSettings";
        FeatureControl stbResponse = new FeatureControl();
        stbResponse.setFeatures(toFeatureResponse(features.get(STB)));
        performGetWithApplication(url + "?model=X1-1", "", Collections.singletonMap("featureControl", stbResponse));

        FeatureControl xhomeResponse = new FeatureControl();
        xhomeResponse.setFeatures(toFeatureResponse(features.get(XHOME)));
        performGetWithApplication(url + "/xhome?model=X1-1", "", Collections.singletonMap("featureControl", xhomeResponse));
    }
    @Test
    public void verify304StatusIfResponseWasNotModified() throws Exception {
        Feature feature = createAndSaveFeature();
        Rule rule = createEnvModelRule();
        FeatureRule featureRule = createFeatureRule(Collections.singletonList(feature.getId()), rule, STB);
        featureRuleDAO.setOne(featureRule.getId(), featureRule);

        FeatureControl expectedResponse = createExpectedResponse(feature);

        String configSetHash = calculateHash(JsonUtil.toJson(expectedResponse.getFeatures()).getBytes());

        assertNotModifiedStatus(configSetHash, expectedResponse);

        assertConfigSetHashChange(configSetHash, expectedResponse);
    }

    @Test
    public void verifyFeatureIfFeatureRuleIsAppliedByRangeOperation() throws Exception {
        Feature feature = createAndSaveFeature();
        createAndSaveFeatureRule(Collections.singletonList(feature.getId()), createPercentRangeRule(), STB);

        FeatureControl expectedResponse = createExpectedResponse(feature);

        String macFits50To100Range = "B4:F2:E8:15:67:46";

        verifyPercentRangeRuleApplying(macFits50To100Range, expectedResponse);
    }

    @Test
    public void verifyIfFeatureRuleIsNotAppliedByRangeOperation() throws Exception {
        Feature feature = createAndSaveFeature();
        createAndSaveFeatureRule(Collections.singletonList(feature.getId()), createPercentRangeRule(), STB);

        FeatureControl expectedResponse = createExpectedResponse(feature);

        String macDoesntFit50To100Range = "04:02:10:00:00:01";

        verifyPercentRangeRuleApplying(macDoesntFit50To100Range, expectedResponse);
    }

    @Test
    public void featureInstanceFieldAddedToRFCResponse() throws Exception {
        Feature feature = createAndSaveFeature();
        Rule rule = createRule(RuleFactory.MODEL.getName(), defaultModelId.toUpperCase());
        createAndSaveFeatureRule(Collections.singletonList(feature.getId()), rule, STB);
        mockMvc.perform(get("/" + URL_MAPPING + "/getSettings")
                .accept(MediaType.APPLICATION_JSON)
                .param("version", API_VERSION)
                .param(APPLICATION_TYPE_PARAM, STB)
                .param(RuleFactory.MODEL.getName(), defaultModelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featureControl.features[0].featureInstance").value(feature.getFeatureName()));
    }

    private FeatureControl createExpectedResponse(Feature feature) {
        FeatureControl expectedResponse = new FeatureControl();
        FeatureResponse featureResponse = new FeatureResponse(feature);
        expectedResponse.getFeatures().add(nullifyUnwantedFields(featureResponse));
        return expectedResponse;
    }

    private Rule createPercentRangeRule() {
        return Rule.Builder.of(new Condition(new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.ESTB_MAC), RuleFactory.RANGE, FixedArg.from("50-100"))).build();
    }

    private ResultMatcher featureControlJsonMatcher(FeatureControl expectedFeatureControl) {
        return content().json(JsonUtil.toJson(Collections.singletonMap("featureControl", expectedFeatureControl)));
    }

    private void verifyPercentRangeRuleApplying(String macAddress, FeatureControl expectedResponse) throws Exception {
        mockMvc.perform(get("/" + URL_MAPPING  + "/getSettings")
                .accept(MediaType.APPLICATION_JSON)
                .param(LogUploaderContext.ESTB_MAC, macAddress))
                .andExpect(status().isOk())
                .andExpect(featureControlJsonMatcher(expectedResponse));
    }

    private void assertNotModifiedStatus(String configSetHash, FeatureControl expectedResponse) throws Exception {
        mockMvc.perform(get("/" + URL_MAPPING  + "/getSettings")
                .accept(MediaType.APPLICATION_JSON)
                .header(LogUploaderContext.CONFIG_SET_HASH, configSetHash)
                .param(LogUploaderContext.MODEL, defaultModelId.toUpperCase())
                .param(LogUploaderContext.ENV, defaultEnvironmentId.toUpperCase()))
                .andExpect(status().isNotModified())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonMap("featureControl", expectedResponse))))
                .andExpect(header().string(LogUploaderContext.CONFIG_SET_HASH, configSetHash));
    }

    private void assertConfigSetHashChange(String configSetHash, FeatureControl expectedResponse) throws Exception {
        mockMvc.perform(get("/" + URL_MAPPING  + "/getSettings")
                .accept(MediaType.APPLICATION_JSON)
                .header(LogUploaderContext.CONFIG_SET_HASH, "")
                .param(LogUploaderContext.MODEL, defaultModelId.toUpperCase())
                .param(LogUploaderContext.ENV, defaultEnvironmentId.toUpperCase()))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonMap("featureControl", expectedResponse))))
                .andExpect(header().string(LogUploaderContext.CONFIG_SET_HASH, configSetHash));
    }

    public Feature createAndSaveFeature() {
        Feature feature = createFeature();
        featureDAO.setOne(feature.getId(), feature);
        return feature;
    }

    public Feature createFeature() {
        String id = UUID.randomUUID().toString();
        Feature feature = new Feature();
        feature.setId(id);
        feature.setName(id + "-name");
        feature.setFeatureName("featureInstance");
        feature.setEffectiveImmediate(false);
        feature.setEnable(false);
        Map<String, String> configData = new LinkedHashMap<>();
        configData.put(id + "-key", "id" + "-value");
        feature.setConfigData(configData);
        return feature;
    }

    public FeatureRule createFeatureRule(List<String> featureIds, Rule rule, String applicationType) {
        String id = UUID.randomUUID().toString();
        FeatureRule featureRule = new FeatureRule();
        featureRule.setId(id);
        featureRule.setName(id + "-name");
        featureRule.setApplicationType(applicationType);
        featureRule.setFeatureIds(featureIds);
        featureRule.setRule(rule);
        return featureRule;
    }

    public Rule createRule(Condition condition) {
        return Rule.Builder.of(condition).build();
    }

    private Map<String, FeatureRule> createAndSaveFeatureRules(Map<String, Feature> features) throws Exception {
        Map<String, FeatureRule> featureRules = new HashMap<>();
        FeatureRule stbFeatureRule = createFeatureRule(Collections.singletonList(features.get(STB).getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), STB);
        featureRuleDAO.setOne(stbFeatureRule.getId(), stbFeatureRule);
        featureRules.put(STB, stbFeatureRule);

        FeatureRule xhomeFeatureRule = createFeatureRule(Collections.singletonList(features.get(XHOME).getId()), createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, "X1-1")), XHOME);
        featureRuleDAO.setOne(xhomeFeatureRule.getId(), xhomeFeatureRule);
        featureRules.put(XHOME, xhomeFeatureRule);
        return featureRules;
    }

    private Map<String, Feature> createAndSaveFeatures() {
        Map<String, Feature> features = new HashMap<>();
        Feature stbFeature = createFeature();
        stbFeature.setApplicationType(STB);
        featureDAO.setOne(stbFeature.getId(), stbFeature);
        features.put(STB, stbFeature);

        Feature xhomeFeature = createFeature();
        xhomeFeature.setApplicationType(XHOME);
        featureDAO.setOne(xhomeFeature.getId(), xhomeFeature);
        features.put(XHOME, xhomeFeature);
        return features;
    }

    private Set<FeatureResponse> toFeatureResponse(Feature feature) {
        FeatureResponse featureResponse = new FeatureResponse(feature);
        return Sets.newHashSet(nullifyUnwantedFields(featureResponse));
    }
}
