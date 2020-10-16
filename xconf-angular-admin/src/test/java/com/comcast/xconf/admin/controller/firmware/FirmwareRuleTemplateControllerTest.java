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
 *  Created: 12/9/15 4:35 PM
 */

package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.importing.OverwriteWrapper;
import com.comcast.xconf.search.SearchFields;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FirmwareRuleTemplateControllerTest extends BaseControllerTest {

    @Test
    public void getFirmwareRuleTemplate() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate();
        firmwareRuleTemplateDao.setOne(firmwareRuleTemplate.getId(), firmwareRuleTemplate);

        mockMvc.perform(get("/" + FirmwareRuleTemplateController.URL_MAPPING + "/" + firmwareRuleTemplate.getId()))
                .andExpect(status().isOk()).andExpect(content().json(JsonUtil.toJson(firmwareRuleTemplate)));
        mockMvc.perform(get("/" + FirmwareRuleTemplateController.URL_MAPPING))
                .andExpect(status().isOk()).andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(firmwareRuleTemplate))));
    }

    @Test
    public void create() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate();

        mockMvc.perform(post("/" + FirmwareRuleTemplateController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(firmwareRuleTemplate)))
                .andExpect(status().isCreated());

    }

    @Test
    public void update() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate();
        mockMvc.perform(post("/" + FirmwareRuleTemplateController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(firmwareRuleTemplate)))
                .andExpect(status().isCreated());
        firmwareRuleTemplate.getRule().getCondition().getFreeArg().setName("changedName");

        mockMvc.perform(put("/" + FirmwareRuleTemplateController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(firmwareRuleTemplate)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(firmwareRuleTemplate)));
    }

    @Test
    public void deleteFirmwareRuleTemplate() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate();
        firmwareRuleTemplateDao.setOne(firmwareRuleTemplate.getId(), firmwareRuleTemplate);

        mockMvc.perform(delete("/" + FirmwareRuleTemplateController.URL_MAPPING + "/" + firmwareRuleTemplate.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/" + FirmwareRuleTemplateController.URL_MAPPING + "/" + firmwareRuleTemplate.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void importAlreadyExistingTemplatesWithOverwriteFalse() throws Exception {
        String templateId1 = "template1";
        String templateId2 = "template2";
        List<OverwriteWrapper<FirmwareRuleTemplate>> wrappedTemplates = Lists.newArrayList(
                wrapTemplate(createAndSaveFirmwareRuleTemplate(templateId1, 1), false),
                wrapTemplate(createAndSaveFirmwareRuleTemplate(templateId2, 2), false)
        );
        String actualResult = mockMvc.perform(post("/" + FirmwareRuleTemplateController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedTemplates)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put("success", new ArrayList<String>());
        expectedResult.put("failure", Lists.newArrayList(
                "FirmwareRuleTemplate with id " + templateId1 + " already exists",
                "FirmwareRuleTemplate with id " + templateId2 + " already exists"
        ));
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, true);
    }

    @Test
    public void importNewTemplatesWithOverwriteTrue() throws Exception {
        String templateId1 = "template1";
        String templateId2 = "template2";
        List<OverwriteWrapper<FirmwareRuleTemplate>> wrappedTemplates = Lists.newArrayList(
                wrapTemplate(createFirmwareRuleTemplate(templateId1, 1), true),
                wrapTemplate(createFirmwareRuleTemplate(templateId2, 2), true)
        );
        String actualResult = mockMvc.perform(post("/" + FirmwareRuleTemplateController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedTemplates)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put("success", new ArrayList<String>());
        expectedResult.put("failure", Lists.newArrayList(
                "FirmwareRuleTemplate " + templateId1 + " doesn't exist",
                "FirmwareRuleTemplate " + templateId2 + " doesn't exist"
        ));
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, true);
    }

    @Test
    public void importAlreadyExistingTemplatesWithOverwriteTrue() throws Exception {
        String templateId1 = "template1";
        String templateId2 = "template2";
        FirmwareRuleTemplate template2 = createFirmwareRuleTemplate(templateId2, 2);
        template2.setRule(createRule(new Condition(RuleFactory.ENV, StandardOperation.IS, FixedArg.from("ENVID"))));
        firmwareRuleTemplateDao.setOne(templateId2, template2);

        List<OverwriteWrapper<FirmwareRuleTemplate>> wrappedTemplates = Lists.newArrayList(
                wrapTemplate(createAndSaveFirmwareRuleTemplate(templateId1, 1), true),
                wrapTemplate(template2, true)
        );
        String actualResult = mockMvc.perform(post("/" + FirmwareRuleTemplateController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedTemplates)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put("success", Lists.newArrayList(templateId1, templateId2));
        expectedResult.put("failure", new ArrayList<String>());
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, true);
    }

    @Test
    public void importOneExistingTemplateAndOneNewWithOverwriteFalse() throws Exception {
        String templateId1 = "template1";
        String templateId2 = "template2";
        FirmwareRuleTemplate template2 = createFirmwareRuleTemplate(templateId2, 2);
        template2.setRule(createRule(new Condition(RuleFactory.ENV, StandardOperation.IS, FixedArg.from("ENVID"))));

        List<OverwriteWrapper<FirmwareRuleTemplate>> wrappedTemplates = Lists.newArrayList(
                wrapTemplate(createAndSaveFirmwareRuleTemplate(templateId1, 1), false),
                wrapTemplate(template2, false)
        );
        String actualResult = mockMvc.perform(post("/" + FirmwareRuleTemplateController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedTemplates)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put("success", Collections.singletonList(templateId2));
        expectedResult.put("failure", Collections.singletonList("FirmwareRuleTemplate with id " + templateId1 + " already exists"));
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, true);
    }

    @Test
    public void importManyTemplatesCheckThatPriorityWasSavedCorrectly() throws Exception {
        String partName = "templateWithPriority";
        List<FirmwareRuleTemplate> expectedResult = new ArrayList<>();
        List<OverwriteWrapper<FirmwareRuleTemplate>> wrappedTemplates = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            FirmwareRuleTemplate template = createFirmwareRuleTemplate(partName + i, "version"+i, i);
            expectedResult.add(template);
            wrappedTemplates.add(wrapTemplate(template, false));
        }

        mockMvc.perform(post("/" + FirmwareRuleTemplateController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedTemplates)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<FirmwareRuleTemplate> actualResult = firmwareRuleTemplateDao.getAll();

        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), JsonUtil.toJson(actualResult), false);
    }

    @Test
    public void getAllFirmwareRuleTemplates() throws Exception {
        Integer numberOfTemplatesOfRuleTemplateType = 4;
        Integer numberOfTemplatesOfDefinePropertiesTemplateType = 2;
        Integer numberOfTemplatesOfBlockingFilterTemplateType = 8;
        ApplicableAction.Type typeForQuery = ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE;
        createAndSaveFirmwareTemplatesOfRuleTemplateType(numberOfTemplatesOfRuleTemplateType);
        createAndSaveFirmwareTemplatesOfDefinePropertiesTemplateType(numberOfTemplatesOfDefinePropertiesTemplateType);
        List<FirmwareRuleTemplate> rulesOfBlockingFilterType = createAndSaveFirmwareTemplatesOfBlockingFilterTemplateTypeType(numberOfTemplatesOfBlockingFilterTemplateType);
        Map<String, String> searchContext = Collections.singletonMap(SearchFields.APPLICABLE_ACTION_TYPE, typeForQuery.name());
        MockHttpServletResponse result = mockMvc.perform(
                post("/" + FirmwareRuleTemplateController.URL_MAPPING + "/filtered")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(searchContext))
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk()).andReturn().getResponse();

        String actualContent = result.getContentAsString();
        String expectedContent = JsonUtil.toJson(rulesOfBlockingFilterType);
        JSONAssert.assertEquals(expectedContent, actualContent, false);
        assertEquals(numberOfTemplatesOfRuleTemplateType.toString(), result.getHeaderValue(ApplicableAction.Type.RULE_TEMPLATE.toString()).toString());
        assertEquals(numberOfTemplatesOfDefinePropertiesTemplateType.toString(), result.getHeaderValue(ApplicableAction.Type.DEFINE_PROPERTIES_TEMPLATE.toString()).toString());
        assertEquals(numberOfTemplatesOfBlockingFilterTemplateType.toString(), result.getHeaderValue(ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE.toString()).toString());
    }

    @Test
    public void exportFirmwareRuleTemplate() throws Exception {
        final String id = "idFoExport";
        final FirmwareRuleTemplate ruleToBeExported = createAndSaveFirmwareRuleTemplate(id, 1);
        final List<FirmwareRuleTemplate> expectedResult = Lists.newArrayList(ruleToBeExported);

        final MockHttpServletResponse response = mockMvc.perform(get("/" + FirmwareRuleTemplateController.URL_MAPPING + "/" + id).param("export", ""))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(expectedResult))).andReturn().getResponse();

        final Object actualContentDisposition = response.getHeaderValue("Content-Disposition");
        final String expectedContentDisposition = "attachment; filename=firmwareRuleTemplate_" + id + ".json";
        assertEquals(expectedContentDisposition, actualContentDisposition);
    }

    @Test
    public void exportFirmwareRuleTemplateNegative() throws Exception {
        final String nonExistentId = "someId";

        final ResultActions resultActions = mockMvc.perform(get("/" + FirmwareRuleController.URL_MAPPING + "/" + nonExistentId).param("export", ""));
        resultActions.andExpect(status().isNotFound());

        final Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals("Entity with id: " + nonExistentId + " does not exist", exception.getMessage());
        assertEquals(EntityNotFoundException.class, exception.getClass());
    }

    @Test
    public void exportAllFirmwareRuleTemplates() throws Exception {
        final String id = "forExport";
        final List<FirmwareRuleTemplate> expectedResult = Lists.newArrayList(createAndSaveFirmwareRuleTemplates(id, 42));

        final MockHttpServletResponse response = mockMvc.perform(get("/" + FirmwareRuleTemplateController.URL_MAPPING).param("export", ""))
                .andExpect(status().isOk()).andReturn().getResponse();

        final String expectedContentDisposition = "attachment; filename=allFirmwareRuleTemplates.json";
        final String actualResult = response.getContentAsString();
        final Object actualContentDisposition = response.getHeaderValue("Content-Disposition");
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, false);
        assertEquals(expectedContentDisposition, actualContentDisposition);
    }

    @Test
    public void exportAllFirmwareRuleTemplatesByType() throws Exception {
        final String id = "forExport";
        final List<FirmwareRuleTemplate> expectedResult = Lists.newArrayList(createAndSaveFirmwareRuleTemplates(id, 42));

        final MockHttpServletResponse response = mockMvc.perform(get("/" + FirmwareRuleTemplateController.URL_MAPPING + "/export/").param("type", "RULE_TEMPLATE"))
                .andExpect(status().isOk()).andReturn().getResponse();

        final String expectedContentDisposition = "attachment; filename=allFirmwareRuleTemplates_RULE_ACTION_TEMPLATE.json";
        final String actualResult = response.getContentAsString();
        final Object actualContentDisposition = response.getHeaderValue("Content-Disposition");
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, false);
        assertEquals(expectedContentDisposition, actualContentDisposition);
    }

    @Test
    public void searchFirmwareRuleByFreeAndFixedArg() throws Exception {
        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate("id123", ApplicableAction.Type.RULE_TEMPLATE);
        Rule rule = new Rule();
        List<Rule> compoundParts = new ArrayList<>();
        compoundParts.add(createRule(createCondition("modelId", "model", StandardOperation.IS)));
        Rule rule1 = createRule(createCondition("envId", "env", StandardOperation.IS));
        rule1.setRelation(Relation.AND);
        compoundParts.add(rule1);
        rule.setCompoundParts(compoundParts);
        firmwareRuleTemplate.setRule(rule);
        firmwareRuleTemplateDao.setOne(firmwareRuleTemplate.getId(), firmwareRuleTemplate);

        FirmwareRuleTemplate firmwareRuleTemplate1 = createFirmwareRuleTemplate("id1234", ApplicableAction.Type.RULE_TEMPLATE);
        firmwareRuleTemplate1.getRule().setCondition(createCondition("modelId", "model", StandardOperation.IS));
        firmwareRuleTemplateDao.setOne(firmwareRuleTemplate1.getId(), firmwareRuleTemplate1);

        Map<String, String> searchContext = new HashMap<>();
        searchContext.put(SearchFields.FREE_ARG, "model");
        searchContext.put(SearchFields.APPLICABLE_ACTION_TYPE, ApplicableAction.Type.RULE_TEMPLATE.name());
        verifySearchResult("/" + FirmwareRuleTemplateController.URL_MAPPING + "/filtered", searchContext, Lists.newArrayList(firmwareRuleTemplate, firmwareRuleTemplate1));

        searchContext.put(SearchFields.FIXED_ARG, "modelId");

        verifySearchResult("/" + FirmwareRuleTemplateController.URL_MAPPING + "/filtered", searchContext, Lists.newArrayList(firmwareRuleTemplate, firmwareRuleTemplate1));

        searchContext.remove(SearchFields.FREE_ARG);

        verifySearchResult("/" + FirmwareRuleTemplateController.URL_MAPPING + "/filtered", searchContext, Lists.newArrayList(firmwareRuleTemplate, firmwareRuleTemplate1));

        searchContext.put(SearchFields.FREE_ARG, "model");
        searchContext.put(SearchFields.FIXED_ARG, "wrongModelId");

        verifySearchResult("/" + FirmwareRuleTemplateController.URL_MAPPING + "/filtered", searchContext, new ArrayList<>());

        verifySearchResult("/" + FirmwareRuleTemplateController.URL_MAPPING + "/filtered", new HashMap<SearchFields, String>(), Lists.newArrayList(firmwareRuleTemplate, firmwareRuleTemplate1));

    }

    @Test
    public void createTemplateWithoutConditions() throws Exception {
        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate();
        firmwareRuleTemplate.getRule().setCondition(null);
        firmwareRuleTemplate.getRule().setCompoundParts(new ArrayList<Rule>());

        ResultActions resultActions = mockMvc.perform(post("/" + FirmwareRuleTemplateController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(firmwareRuleTemplate)))
                .andExpect(status().isBadRequest());

        assertEquals("FirmwareRuleTemplate " + firmwareRuleTemplate.getId() + " should have as minimum one condition", resultActions.andReturn().getResolvedException().getMessage());
    }

    @Test
    public void deleteTemplateWhichIsUsedByFirmwareRule() throws Exception {
        FirmwareRuleTemplate template = createFirmwareRuleTemplate();
        firmwareRuleTemplateDao.setOne(template.getId(), template);

        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRule.setType(template.getId());
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        String errorMessage = mockMvc.perform(delete("/" + FirmwareRuleTemplateController.URL_MAPPING + "/" + template.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException().getMessage();

        assertEquals("Template " + template.getId() + " is used by rules: " + firmwareRule.getName(), errorMessage);
    }

    @Test
    public void deleteTemplateWhichIsUsedByFirmwareRulesWithDifferentApplicationTypes() throws Exception {
        FirmwareRuleTemplate template = createFirmwareRuleTemplate();
        firmwareRuleTemplateDao.setOne(template.getId(), template);

        Map<String, FirmwareRule> firmwareRules = createAndSaveFirmwareRules(template);
        Set<String> firmwareRuleNames = Sets.newHashSet(STB, XHOME);
        mockMvc.perform(delete("/" + FirmwareRuleTemplateController.URL_MAPPING + "/" + template.getId()))
                .andExpect(status().isConflict())
                .andExpect(errorMessageMatcher("Template " + template.getId() + " is used by rules: " + Joiner.on(", ").join(firmwareRuleNames)));
    }

    private FirmwareRuleTemplate createAndSaveFirmwareTemplate(String id, ApplicableAction.Type type) {
        FirmwareRuleTemplate result = createFirmwareRuleTemplate(id, type);
        firmwareRuleTemplateDao.setOne(result.getId(), result);

        return result;
    }

    private List<FirmwareRuleTemplate> createAndSaveFirmwareRuleTemplates(String idPattern, int number) {
        List<FirmwareRuleTemplate> result = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            result.add(createAndSaveFirmwareRuleTemplate(idPattern + i, 1));
        }

        return result;
    }

    private FirmwareRuleTemplate createFirmwareRuleTemplate(String id, ApplicableAction.Type type) {
        FirmwareRuleTemplate result = createFirmwareRuleTemplate();
        result.setId(id);
        result.getApplicableAction().setActionType(type);

        return result;
    }

    private List<FirmwareRuleTemplate> createAndSaveFirmwareTemplates(String idPart, ApplicableAction.Type type, Integer numberOfTemplatesToCreate) {
        List<FirmwareRuleTemplate> result = new ArrayList<>();
        for (int i = 0; i < numberOfTemplatesToCreate; i++) {
            result.add(createAndSaveFirmwareTemplate(idPart + i, type));
        }

        return result;
    }

    private List<FirmwareRuleTemplate> createAndSaveFirmwareTemplatesOfRuleTemplateType(Integer numberOfTemplatesToCreate) {
        return createAndSaveFirmwareTemplates("ruleType", ApplicableAction.Type.RULE_TEMPLATE, numberOfTemplatesToCreate);
    }

    private List<FirmwareRuleTemplate> createAndSaveFirmwareTemplatesOfDefinePropertiesTemplateType(Integer numberOfTemplatesToCreate) {
        return createAndSaveFirmwareTemplates("definePropertiesType", ApplicableAction.Type.DEFINE_PROPERTIES_TEMPLATE, numberOfTemplatesToCreate);
    }

    private List<FirmwareRuleTemplate> createAndSaveFirmwareTemplatesOfBlockingFilterTemplateTypeType(Integer numberOfTemplatesToCreate) {
        return createAndSaveFirmwareTemplates("blockingFilterType", ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE, numberOfTemplatesToCreate);
    }

    private FirmwareRuleTemplate createAndSaveFirmwareRuleTemplate(String id, Integer priority) {
        FirmwareRuleTemplate result = createFirmwareRuleTemplate(id, priority);
        firmwareRuleTemplateDao.setOne(result.getId(), result);

        return result;
    }

    private FirmwareRuleTemplate createFirmwareRuleTemplate(String id, Integer priority) {
        FirmwareRuleTemplate result = createFirmwareRuleTemplate();
        result.setId(id);
        result.setPriority(priority);

        return result;
    }

    private FirmwareRuleTemplate createFirmwareRuleTemplate(String id, String version, int priority) {
        FirmwareRuleTemplate result = createFirmwareRuleTemplate();
        result.setId(id);
        result.setRule(createRule(createCondition(version)));
        result.setPriority(priority);
        return result;
    }

    private OverwriteWrapper<FirmwareRuleTemplate> wrapTemplate(FirmwareRuleTemplate template, Boolean overwrite) {
        OverwriteWrapper<FirmwareRuleTemplate> result = new OverwriteWrapper<>();
        result.setEntity(template);
        result.setOverwrite(overwrite);

        return result;
    }


}