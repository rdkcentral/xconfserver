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

package com.comcast.xconf.admin.controller.dcm;

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.importing.OverwriteWrapper;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.DCMRuleWithSettings;
import com.comcast.xconf.logupload.VodSettings;
import com.comcast.xconf.search.SearchFields;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static com.comcast.xconf.admin.core.Utils.nullifyUnwantedFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FormulaQueryControllerTest extends BaseControllerTest {

    @Test
    public void getFormula() throws Exception {
        DCMGenericRule formula = createFormula();
        dcmRuleDAO.setOne(formula.getId(), formula);

        mockMvc.perform(get("/" + FormulaQueryController.URL_MAPPING + "/" + formula.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(formula)));
    }

    @Test
    public void getPage() throws Exception {
        DCMGenericRule formula = createFormula();
        dcmRuleDAO.setOne(formula.getId(), formula);

        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/filtered")
                .param("pageSize", "10").param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(formula))));
    }

    @Test
    public void create() throws Exception {
        DCMGenericRule formula = createFormula();
        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(formula)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(formula)));
    }

    @Test
    public void createWithoutId() throws Exception {
        DCMGenericRule formula = createFormula();
        formula.setId(null);

        String strResponse = mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(formula)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        DCMGenericRule savedFormula = mapper.readValue(strResponse, DCMGenericRule.class);

        assertTrue(StringUtils.isNotBlank(savedFormula.getId()));

        setIdAndAssertEqualsEntities(null, formula, savedFormula);
    }

    @Test
    public void createFormulaNormalizeCondition() throws Exception {
        Rule needToNormalizeRule = createEnvPartnerRule(" envFixedArg   ", "partnerId");
        DCMGenericRule formula = createFormula();
        formula.setCondition(null);
        formula.setCompoundParts(needToNormalizeRule.getCompoundParts());

        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                                 .content(JsonUtil.toJson(formula))).andReturn().getResponse().getContentAsString();

        assertConditionWasNormalized(dcmRuleDAO.getOne(formula.getId()));
    }

    @Test
    public void creatingReturnsValidationException() throws Exception {
        DCMGenericRule formula = createFormula();
        formula.getCondition().setOperation(null);

        ResultActions resultActions = mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(formula)))
                .andExpect(status().isBadRequest());

        Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals(RuleValidationException.class, exception.getClass());
        assertEquals("Operation is null", exception.getMessage());
    }

    @Test
    public void updateFormula() throws Exception {
        DCMGenericRule formula = createFormula();
        dcmRuleDAO.setOne(formula.getId(), formula);
        formula.setDescription("changed description");
        Condition condition = createCondition();
        condition.setFixedArg(FixedArg.from("anotherhost"));
        formula.setCondition(condition);
        mockMvc.perform(put("/" + FormulaQueryController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(formula)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(formula)));
    }

    @Test
    public void updateFormulaNormalizeCondition() throws Exception {
        Rule needToNormalizeRule = createEnvPartnerRule(" envFixedArg   ", "partnerId");
        DCMGenericRule formula = createFormula();
        formula.setCondition(null);
        formula.setCompoundParts(needToNormalizeRule.getCompoundParts());
        dcmRuleDAO.setOne(formula.getId(), formula);
        DCMGenericRule formulaToUpdate = createFormula();
        formulaToUpdate.setCompoundParts(needToNormalizeRule.getCompoundParts());
        formulaToUpdate.setCondition(null);
        mockMvc.perform(put("/" + FormulaQueryController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                                .content(JsonUtil.toJson(formulaToUpdate)));

        assertConditionWasNormalized(dcmRuleDAO.getOne(formulaToUpdate.getId()));
    }

    @Test
    public void updatingReturnsValidationException() throws Exception {
        String id = "id";
        DCMGenericRule formula = createAndSaveFormula(id, 1, Relation.AND);
        DCMGenericRule formulaToUpdate = createFormula(id, 1);
        formulaToUpdate.getCondition().setOperation(null);

        ResultActions resultActions = mockMvc.perform(put("/" + FormulaQueryController.URL_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(formulaToUpdate)))
                .andExpect(status().isBadRequest());

        Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals(RuleValidationException.class, exception.getClass());
        assertEquals("Operation is null", exception.getMessage());
    }

    @Test
    public void changePriorities() throws Exception {
        DCMGenericRule formula = createFormula();
        dcmRuleDAO.setOne(formula.getId(), formula);
        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/" + formula.getId() + "/priority/" + 3)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(formula))));
    }

    @Test
    public void verifyPrioritiesNotChangedForXHome() throws Exception {
        int listSize = 5;
        List<DCMGenericRule> stbFormulas = createFormulasList(listSize, ApplicationType.STB);
        List<DCMGenericRule> xhomeFormulas = createFormulasList(listSize, ApplicationType.XHOME);

        DCMGenericRule formula = CloneUtil.clone(stbFormulas.get(0));
        formula.setPriority(5);
        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/" + formula.getId() + "/priority/" + listSize)
                .cookie(stbCookie))
                .andExpect(status().isOk());
        assertXhomeFormulasAreTheSame(xhomeFormulas);

        formula.setPriority(1);
        formula.setCondition(createCondition(formula.getId()));
        performPutRequestAndVerify(FormulaQueryController.URL_MAPPING, formula);
        assertXhomeFormulasAreTheSame(xhomeFormulas);

        performDeleteRequestAndVerify( FormulaQueryController.URL_MAPPING + "/" + formula.getId());
        assertXhomeFormulasAreTheSame(xhomeFormulas);
    }

    @Test
    public void getSettingsAvailability() throws Exception {
        DCMGenericRule formula = createFormula();
        dcmRuleDAO.setOne(formula.getId(), formula);
        Map<String, Boolean> settings = new HashMap();
        Map<String, Map<String, Boolean>> settingsAvailability = new HashMap();
        settings.put("deviceSettings", false);
        settings.put("vodSettings", false);
        settings.put("logUploadSettings", false);
        settingsAvailability.put(formula.getId(), settings);
        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/settingsAvailability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(Lists.newArrayList(formula.getId()))))
                .andExpect(status().isOk()).andExpect(content().json(JsonUtil.toJson(settingsAvailability)));
    }

    @Test
    public void deleteFormula() throws Exception {
        DCMGenericRule formula = createFormula();
        dcmRuleDAO.setOne(formula.getId(), formula);
        mockMvc.perform(delete("/" + FormulaQueryController.URL_MAPPING + "/" + formula.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/" + FormulaQueryController.URL_MAPPING + "/" + formula.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getFormulasSize() throws Exception {
        DCMGenericRule formula = createFormula();
        dcmRuleDAO.setOne(formula.getId(), formula);
        mockMvc.perform(get("/" + FormulaQueryController.URL_MAPPING + "/size"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson("1")));
    }

    @Test
    public void getFormulasNames() throws Exception {
        DCMGenericRule formula = createFormula();
        dcmRuleDAO.setOne(formula.getId(), formula);
        mockMvc.perform(get("/" + FormulaQueryController.URL_MAPPING + "/names"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(formula.getName()))));
    }

    @Test
    public void importAlreadyExistingFormulasWithOverwriteFalse() throws Exception {
        String formulaId1 = "formula1";
        String formulaId2 = "formula2";
        List<OverwriteWrapper<DCMRuleWithSettings>> wrappedFormulas = Lists.newArrayList(
                wrapFormulaWithSettings(createFormulaWithSettings(createAndSaveFormula(formulaId1, 1,  Relation.OR)), false),
                wrapFormulaWithSettings(createFormulaWithSettings(createAndSaveFormula(formulaId2, 2,  Relation.OR)), false)
        );
        String actualResult = mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedFormulas)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put("success", new ArrayList<String>());
        expectedResult.put("failure", Lists.newArrayList(
                "Entity with id: " + formulaId1 + " already exists",
                "Entity with id: " + formulaId2 + " already exists"
        ));
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, true);
    }

    @Test
    public void importNewFormulasWithOverwriteTrue() throws Exception {
        String formulaId1 = "formula1";
        String formulaId2 = "formula2";
        List<OverwriteWrapper<DCMRuleWithSettings>> wrappedFormulas = Lists.newArrayList(
                wrapFormulaWithSettings(createFormulaWithSettings(createFormula(formulaId1, 1)), true),
                wrapFormulaWithSettings(createFormulaWithSettings(createFormula(formulaId2, 2)), true)
        );
        String actualResult = mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedFormulas)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put("success", new ArrayList<String>());
        expectedResult.put("failure", Lists.newArrayList(
                "Entity with id: " + formulaId1 + " does not exist",
                "Entity with id: " + formulaId2 + " does not exist"
        ));
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, true);
    }

    @Test
    public void importAlreadyExistingFormulasWithOverwriteTrue() throws Exception {
        String formulaId1 = "formula1";
        String formulaId2 = "formula2";
        List<OverwriteWrapper<DCMRuleWithSettings>> wrappedFormulas = Lists.newArrayList(
                wrapFormulaWithSettings(createFormulaWithSettings(createAndSaveFormula(formulaId1, 1, Relation.OR)), true),
                wrapFormulaWithSettings(createFormulaWithSettings(createAndSaveFormula(formulaId2, 2, Relation.AND)), true)
        );
        String actualResult = mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedFormulas)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put("success", Lists.newArrayList(formulaId1, formulaId2));
        expectedResult.put("failure", new ArrayList<String>());
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, true);
    }

    @Test
    public void importOneExistingFormulaAndOneNewWithOverwriteFalse() throws Exception {
        String formulaId1 = "formula1";
        String formulaId2 = "formula2";
        List<OverwriteWrapper<DCMRuleWithSettings>> wrappedFormulas = Lists.newArrayList(
                wrapFormulaWithSettings(createFormulaWithSettings(createAndSaveFormula(formulaId1, 1, Relation.OR)), false),
                wrapFormulaWithSettings(createFormulaWithSettings(createFormula(formulaId2, 2, Relation.AND)), false)
        );
        String actualResult = mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedFormulas)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put("success", Collections.singletonList(formulaId2));
        expectedResult.put("failure", Collections.singletonList("Entity with id: " + formulaId1 + " already exists"));
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, true);
    }

    @Test
    public void importManyFormulasCheckThatPriorityWasSavedCorrectly() throws Exception {
        String partName = "formulaWithPriority";
        List<DCMGenericRule> expectedResult = new ArrayList<>();
        List<OverwriteWrapper<DCMRuleWithSettings>> wrappedFormulasWithSettings = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            DCMGenericRule formula = createFormula(partName + i, i);
            formula.getCondition().getFixedArg().setValue(partName + i); //to generate rules with unique conditions
            expectedResult.add(formula);
            wrappedFormulasWithSettings.add(wrapFormulaWithSettings(createFormulaWithSettings(formula), false));
        }

        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedFormulasWithSettings)))
                .andExpect(status().isOk());
        List<DCMGenericRule> actualResult = dcmRuleDAO.getAll();

        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), JsonUtil.toJson(actualResult), false);
    }

    @Test
    public void importFormulaNormalizeConditions() throws Exception {
        DCMGenericRule formula = createFormula();
        formula.setCondition(null);
        formula.setCompoundParts(createEnvPartnerRule(" envFixedArg   ", "partnerId").getCompoundParts());
        DCMRuleWithSettings formulaWithSettings = createFormulaWithSettings(formula);

        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/import/false").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(formulaWithSettings)))
                .andExpect(status().isOk());

        assertConditionWasNormalized(dcmRuleDAO.getOne(formula.getId()));
    }

    @Test
    public void importFormulasNormalizeConditions() throws Exception {
        Rule needToNormalizeRule = createEnvPartnerRule(" envFixedArg   ", "partnerId");
        DCMGenericRule formula = createFormula();
        formula.setCondition(null);
        formula.setCompoundParts(needToNormalizeRule.getCompoundParts());
        List<OverwriteWrapper<DCMRuleWithSettings>> wrappedFormulasWithSettings =
                        Collections.singletonList(wrapFormulaWithSettings(createFormulaWithSettings(formula), false));

        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING  + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedFormulasWithSettings)))
                .andExpect(status().isOk());

        assertConditionWasNormalized(dcmRuleDAO.getOne(formula.getId()));
    }

    @Test
    public void importingFormulaReturnsValidationException() throws Exception {
        DCMGenericRule formula = createFormula();
        formula.getCondition().setOperation(null);
        DCMRuleWithSettings formulaWithSettings = createFormulaWithSettings(formula);

        ResultActions resultActions = mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/import/false").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(formulaWithSettings)))
                .andExpect(status().isBadRequest());

        Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals(RuleValidationException.class, exception.getClass());
        assertEquals("Operation is null", exception.getMessage());
    }

    @Test
    public void importingFormulasReturnsValidationException() throws Exception {
        DCMGenericRule formula = createFormula();
        formula.getCondition().setOperation(null);
        List<OverwriteWrapper<DCMRuleWithSettings>> wrappedFormulas = Lists.newArrayList(
                wrapFormulaWithSettings(createFormulaWithSettings(formula), false)
        );
        String actualResult = mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/import").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(wrappedFormulas)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put("success", Collections.<String>emptyList());
        expectedResult.put("failure", Collections.singletonList("Operation is null"));
        JSONAssert.assertEquals(JsonUtil.toJson(expectedResult), actualResult, true);
    }

    @Test
    public void getFormulasAvailability() throws Exception {
        final String vodSettingsId = "testId";
        VodSettings vodSettings = createVodSettings();
        vodSettings.setId(vodSettingsId);
        vodSettingsDAO.setOne(vodSettings.getId(), vodSettings);
        DCMGenericRule dcmRule = createFormula();
        dcmRule.setId(vodSettingsId);
        dcmRuleDAO.setOne(dcmRule.getId(), dcmRule);
        vodSettings = createVodSettings();
        vodSettings.setId(vodSettingsId + 1);
        vodSettingsDAO.setOne(vodSettings.getId(), vodSettings);

        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/formulasAvailability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(Lists.newArrayList(vodSettingsId, vodSettingsId + 1))))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(new HashMap() {{
                    put(vodSettingsId, true);
                    put(vodSettingsId + 1, false);
                }})));
    }

    @Test
    public void searchFormulaByFreeAndFixedArg() throws Exception {
        DCMGenericRule dcmGenericRule = createFormula("formulaId", 1);
        List<Rule> compoundParts = new ArrayList<>();
        compoundParts.add(createRule(createCondition("modelId", "model", StandardOperation.IS)));
        Rule rule = createRule(createCondition("envId", "env", StandardOperation.IS));
        rule.setRelation(Relation.AND);
        compoundParts.add(rule);
        dcmGenericRule.setCompoundParts(compoundParts);
        dcmRuleDAO.setOne(dcmGenericRule.getId(), dcmGenericRule);
        DCMGenericRule dcmGenericRule1 = createFormula("formulaId2", 2);
        dcmGenericRule1.setCondition(createCondition("modelId", "model", StandardOperation.IS));
        dcmRuleDAO.setOne(dcmGenericRule1.getId(), dcmGenericRule1);

        Map<String, String> searchContext = new HashMap<>();
        searchContext.put(SearchFields.FREE_ARG, "model");

        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(dcmGenericRule))));

        searchContext.put(SearchFields.FIXED_ARG, "modelId");

        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(dcmGenericRule))));

        searchContext.remove(SearchFields.FREE_ARG);

        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(dcmGenericRule))));

        searchContext.put(SearchFields.FREE_ARG, "model");
        searchContext.put(SearchFields.FIXED_ARG, "wrongModelId");

        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(JsonUtil.toJson(new HashMap<SearchFields, String>())))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(dcmGenericRule))));
    }

    @Test
    public void exportOne() throws Exception {
        DCMGenericRule dcmRule = createFormula();
        dcmRuleDAO.setOne(dcmRule.getId(), dcmRule);

        DCMRuleWithSettings dcmRuleWithSettings = nullifyUnwantedFields(createAndSaveDcmRuleWithSettings(dcmRule));
        List<DCMRuleWithSettings> entity = Collections.singletonList(dcmRuleWithSettings);

        performExportRequestAndVerifyResponse("/" + FormulaQueryController.URL_MAPPING + "/" + dcmRule.getId(), entity, ApplicationType.STB);
    }

    @Test
    public void exportAll() throws Exception {
        DCMGenericRule dcmRule = createFormula();
        dcmRuleDAO.setOne(dcmRule.getId(), dcmRule);

        DCMRuleWithSettings dcmRuleWithSettings = nullifyUnwantedFields(createAndSaveDcmRuleWithSettings(dcmRule));
        List<DCMRuleWithSettings> resultList = Collections.singletonList(dcmRuleWithSettings);

        performExportRequestAndVerifyResponse("/" + FormulaQueryController.URL_MAPPING, resultList, ApplicationType.STB);
    }

    private void assertXhomeFormulasAreTheSame(List<DCMGenericRule> xhomeFormulas) throws Exception {
        mockMvc.perform(post("/" + FormulaQueryController.URL_MAPPING + "/filtered")
                .cookie(xhomeCookie)
                .param("pageSize", "10")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(xhomeFormulas)));
    }

    private List<DCMGenericRule> createFormulasList(int listSize, String applicationType) {
        List<DCMGenericRule> xhomeFormulas = new ArrayList<>();
        for (int i = 1; i <= listSize; i++) {
            xhomeFormulas.add(createAndSaveFormula(i + applicationType, i, applicationType));
        }
        return xhomeFormulas;
    }

    private DCMGenericRule createAndSaveFormula(String id, Integer priority, Relation relation) {
        DCMGenericRule result = createFormula(id, priority, relation);
        dcmRuleDAO.setOne(result.getId(), result);
        return result;
    }

    private DCMGenericRule createAndSaveFormula(String id, Integer priority, String applicationType) {
        DCMGenericRule result = createFormula(id, priority);
        result.setApplicationType(applicationType);

        dcmRuleDAO.setOne(result.getId(), result);

        return result;
    }

    private DCMGenericRule createFormula(String id, Integer priority) {
        return createFormula(id, priority, null);
    }

    private DCMGenericRule createFormula(String id, Integer priority, Relation relation) {
        DCMGenericRule result = createFormula();
        result.setId(id);
        result.setName(id);
        result.setPriority(priority);
        result.setRelation(relation);

        return result;
    }

    private DCMRuleWithSettings createFormulaWithSettings(DCMGenericRule formula) {
        return new DCMRuleWithSettings(formula, null, null, null);
    }

    private OverwriteWrapper<DCMRuleWithSettings> wrapFormulaWithSettings(DCMRuleWithSettings formulaWithSettings, Boolean overwrite) {
        OverwriteWrapper<DCMRuleWithSettings> result = new OverwriteWrapper<>();
        result.setEntity(formulaWithSettings);
        result.setOverwrite(overwrite);

        return result;
    }

    private void assertConditionWasNormalized(Rule actualResult) {
        assertEquals(actualResult.getCompoundParts(), createEnvPartnerRule("ENVFIXEDARG", "PARTNERID").getCompoundParts());
    }
}