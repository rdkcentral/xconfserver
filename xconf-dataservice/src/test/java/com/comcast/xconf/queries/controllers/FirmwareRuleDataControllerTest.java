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

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.*;
import com.comcast.xconf.service.firmware.FirmwareRuleDataService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.*;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.util.ImportHelper.IMPORTED;
import static com.comcast.xconf.util.ImportHelper.NOT_IMPORTED;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FirmwareRuleDataControllerTest extends BaseQueriesControllerTest {

    @Before
    public void initPermissionService() {
        when(firmwarePermissionService.getReadApplication()).thenReturn(STB);
        when(firmwarePermissionService.getWriteApplication()).thenReturn(STB);
        when(firmwarePermissionService.canWrite()).thenReturn(true);
    }

    @Test
    public void getAll() throws Exception {
        List<FirmwareRule> firmwareRules = createFirmwareRules(10);
        save(firmwareRules);

        performGetWithApplication(FirmwareRuleDataController.API_URL, "", firmwareRules);
    }

    @Test
    public void getByType() throws Exception {
        List<FirmwareRule> firmwareRules = createFirmwareRules(10);
        save(firmwareRules);
        String url = FirmwareRuleDataController.API_URL + "/filtered";
        Map<String, String> context = new HashMap<>();
        context.put(FirmwareRuleDataService.APPLICATION_TYPE, STB);
        context.put("templateId", "TEST_TEMPLATE");
        performGetAndVerify(url, context, firmwareRules);
        context.put("templateId", "WRONG_TEMPLATE");
        performGetAndVerify(url, context, new ArrayList<>());
    }

    @Test
    public void importAll() throws Exception {
        List<FirmwareRule> firmwareRules = createFirmwareRules(10);
        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put(IMPORTED, getNames(firmwareRules));
        expectedResult.put(NOT_IMPORTED, new ArrayList<String>());

        performPostAndVerifyResponse(FirmwareRuleDataController.API_URL + "/importAll", firmwareRules, expectedResult);
    }

    @Test
    public void importAllWithException() throws Exception {
        FirmwareRule firmwareRuleWithException = createFirmwareRule(UUID.randomUUID().toString(), "TEST_TEMPLATE", createRuleAction(ApplicableAction.Type.RULE, UUID.randomUUID().toString()), createRule("specialKey", "specialValue"));
        List<FirmwareRule> firmwareRulesToImport = Collections.singletonList(firmwareRuleWithException);
        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put(IMPORTED, new ArrayList<String>());
        expectedResult.put(NOT_IMPORTED, getNames(firmwareRulesToImport));

        performPostAndVerifyResponse(FirmwareRuleDataController.API_URL + "/importAll", firmwareRulesToImport, expectedResult);
    }

    @Test
    public void getWithoutApplicationType() throws Exception {
        save(createFirmwareRules(5));
        String url = FirmwareRuleDataController.API_URL + "/filtered";

        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"ApplicationType is not specified\""));
    }

    @Test
    public void importingPercentageBeanAsFirmwareRule() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, defaultModelId, FirmwareConfig.DownloadProtocol.http);
        prepareEnvModelRule(defaultEnvironmentId, defaultModelId, defaultMacListId, firmwareConfig.getId());
        ApplicableAction distributionAction = createDistributionRuleAction(firmwareConfig.getId(), 0.0, 100.0);
        Rule envModelRule = createEnvModelRule(defaultModelId, defaultEnvironmentId);
        FirmwareRule envModelFirmwareRule = createFirmwareRule(UUID.randomUUID().toString(), TemplateNames.ENV_MODEL_RULE, distributionAction, envModelRule);
        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put(IMPORTED, Collections.singletonList(envModelFirmwareRule.getName()));
        expectedResult.put(NOT_IMPORTED, new ArrayList<String>());

        performPostAndVerifyResponse(FirmwareRuleDataController.API_URL + "/importAll", Collections.singletonList(envModelFirmwareRule), expectedResult);
        assertNotNull(percentageBeanQueriesService.getOne(envModelFirmwareRule.getId()));
    }

    @Test
    public void updateFirmwareRuleUnderXhomeApplicationType() throws Exception {
        Model model = createAndSaveModel("TEST_MODEL_ID");
        Rule rule = Rule.Builder.of(new Condition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from(model.getId()))).build();
        FirmwareRuleTemplate template = createAndSaveFirmwareRuleTemplate("TEST_FIRMWARE_RULE_TEMPLATE", rule, createDefinePropertiesTemplateAction(), 1);
        FirmwareRule firmwareRule = createFirmwareRule(UUID.randomUUID().toString(), template.getId(), createDefinePropertiesAction(), rule);
        firmwareRule.setApplicationType(ApplicationType.XHOME);

        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        ArrayList<FirmwareRule> firmwareRulesToUpdate = Lists.newArrayList(firmwareRule);

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put(IMPORTED, getNames(firmwareRulesToUpdate));
        expectedResult.put(NOT_IMPORTED, new ArrayList<>());

        performPostAndVerifyResponse(FirmwareRuleDataController.API_URL + "/importAll", firmwareRulesToUpdate, expectedResult);

        firmwareRuleTemplateDao.deleteOne(template.getId());
    }

    @Test
    public void updateFirmwareRuleWithNotCorrespondingApplicationType() throws Exception {
        Model model = createAndSaveModel("TEST_MODEL_ID");
        Rule rule = Rule.Builder.of(new Condition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from(model.getId()))).build();
        FirmwareRuleTemplate template = createAndSaveFirmwareRuleTemplate("TEST_FIRMWARE_RULE_TEMPLATE", rule, createDefinePropertiesTemplateAction(), 1);
        FirmwareRule firmwareRule = createAndSaveFirmwareRule(UUID.randomUUID().toString(), template.getId(), createDefinePropertiesAction(), rule);

        FirmwareRule firmwareRuleToChange = CloneUtil.clone(firmwareRule);
        firmwareRuleToChange.setApplicationType(ApplicationType.XHOME);

        ArrayList<FirmwareRule> firmwareRulesToUpdate = Lists.newArrayList(firmwareRuleToChange);

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put(IMPORTED, new ArrayList<>());
        expectedResult.put(NOT_IMPORTED, getNames(firmwareRulesToUpdate));

        performPostAndVerifyResponse(FirmwareRuleDataController.API_URL + "/importAll", firmwareRulesToUpdate, expectedResult);

        firmwareRuleTemplateDao.deleteOne(template.getId());
    }

    private void prepareEnvModelRule(String environmentId, String modelId, String macListId, String firmwareConfigId) {
        createAndSaveEnvironment(environmentId);
        createAndSaveModel(modelId);
        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate(TemplateNames.ENV_MODEL_RULE, createEnvModelRule(environmentId, modelId, macListId), createRuleAction(ApplicableAction.Type.RULE_TEMPLATE, firmwareConfigId), 1);
        saveTemplate(firmwareRuleTemplate);
    }

    private ApplicableAction createDistributionRuleAction(String configId, Double startPercentage, Double endPercentage) {
        RuleAction action = new RuleAction();
        RuleAction.ConfigEntry configEntry = new RuleAction.ConfigEntry(configId, startPercentage, endPercentage);
        action.setConfigEntries(Lists.newArrayList(configEntry));
        action.setFirmwareVersions(Sets.newHashSet(defaultFirmwareVersion));
        action.setFirmwareCheckRequired(true);
        action.setActive(true);
        action.setIntermediateVersion(configId);
        return action;
    }

    private FirmwareRuleTemplate createFirmwareRuleTemplate(String templateId, Rule rule, ApplicableAction action, Integer priority) {
        FirmwareRuleTemplate template = new FirmwareRuleTemplate();
        template.setId(templateId);
        template.setRule(rule);
        template.setPriority(priority);
        template.setApplicableAction(action);
        template.setEditable(true);
        return template;
    }

    private FirmwareRuleTemplate saveTemplate(FirmwareRuleTemplate template) {
        firmwareRuleTemplateDao.setOne(template.getId(), template);
        return template;
    }

    private FirmwareRuleTemplate createAndSaveFirmwareRuleTemplate(String templateId, Rule rule, ApplicableAction action, Integer priority) {
        FirmwareRuleTemplate template = createFirmwareRuleTemplate(templateId, rule, action, priority);
        return saveTemplate(template);
    }

    private List<FirmwareRule> createFirmwareRules(Integer size) {
        Model model = createAndSaveModel(defaultModelId);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareRuleTemplate template = createAndSaveFirmwareRuleTemplate("TEST_TEMPLATE", createRule("key", "value"), createRuleAction(ApplicableAction.Type.RULE_TEMPLATE, firmwareConfig.getId()), 1);
        List<FirmwareRule> firmwareRules = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            FirmwareRule firmwareRule = createFirmwareRule(UUID.randomUUID().toString(), template.getId(), createRuleAction(ApplicableAction.Type.RULE, firmwareConfig.getId()), createRule("key", "testValue" + i));
            firmwareRules.add(firmwareRule);
        }
        return firmwareRules;
    }

    private void save(List<FirmwareRule> firmwareRules) {
        for (FirmwareRule firmwareRule : firmwareRules) {
            firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);
        }
    }

    private List<String> getNames(List<FirmwareRule> firmwareRules) {
        List<String> names = new ArrayList<>();
        for (FirmwareRule firmwareRule : firmwareRules) {
            names.add(firmwareRule.getName());
        }
        return names;
    }
}