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
 * Author: Stanislav Menshykov
 * Created: 3/18/16  10:22 AM
 */
package com.comcast.xconf.thucydides.util.firmware;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.*;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.firmware.*;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;
import com.comcast.xconf.thucydides.util.common.EnvironmentUtils;
import com.comcast.xconf.thucydides.util.common.ModelUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class FirmwareRuleTemplateUtils {
    private static final String FIRMWARE_RULE_TEMPLATE_URL = "firmwareruletemplate";
    private static final String FIRMWARE_RULE_URL = "firmwarerule";

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(FIRMWARE_RULE_TEMPLATE_URL, FirmwareRuleTemplate.class);
        GenericTestUtils.deleteEntities(FIRMWARE_RULE_URL + "?pageSize=9999&pageNumber=1", FIRMWARE_RULE_URL, FirmwareRule.class);
        EnvironmentUtils.doCleanup();
        ModelUtils.doCleanup();
    }

    public static FirmwareRuleTemplate createDefaultRuleActionsRuleTemplate() {
        return createRuleActionsRuleTemplate("ruleActionRuleTemplate");
    }

    public static FirmwareRuleTemplate createRuleActionsRuleTemplate(String name) {
        FirmwareRuleTemplate result = new FirmwareRuleTemplate();
        result.setId(name);
        ApplicableAction action = new RuleAction();
        action.setActionType(ApplicableAction.Type.RULE_TEMPLATE);
        result.setApplicableAction(action);
        result.setRule(createDefaultRule());

        return result;
    }

    public static FirmwareRuleTemplate createRuleActionsRuleTemplate(String name, Rule rule) {
        FirmwareRuleTemplate firmwareRuleTemplate = createRuleActionsRuleTemplate(name);
        firmwareRuleTemplate.setRule(rule);
        return firmwareRuleTemplate;
    }

    public static FirmwareRuleTemplate createDefaultDefinePropertiesRuleTemplate() throws Exception {
        return  createDefinePropertiesRuleTemplate("definePropertiesRuleTemplate");
    }

    public static FirmwareRuleTemplate createDefinePropertiesRuleTemplate(String name) throws Exception {
        FirmwareRuleTemplate result = new FirmwareRuleTemplate();
        result.setId(name);
        result.setApplicableAction(createDefinePropertiesTemplateAction());
        result.setRule(createDefaultRule());

        return result;
    }

    public static FirmwareRuleTemplate createDefinePropertiesRuleTemplateWithConditions() throws Exception {
        String[][] conditions = {
                {"freeArg", "IS", "fixedArg"},
                {StbContext.MODEL, "IS", "XG1", "OR"},
                {StbContext.IP_ADDRESS, "IS", "1.1.1.1", "AND", "TRUE"},
                {StbContext.ESTB_MAC, "IS", "AA:BB:CC:DD:EE:FF", "AND"},
        };
        Rule mainRule = new Rule();
        mainRule.setCompoundParts(createRules(conditions));
        return createAndSaveFirmwareRuleTemplate("definePropertiesRuleTemplate", null, mainRule);
    }

    public static FirmwareRuleTemplate createDefaultBlockingFiltersRuleTemplate() {
        return createBlockingFiltersRuleTemplate("blockingFilterRuleTemplate");
    }

    public static FirmwareRuleTemplate createBlockingFiltersRuleTemplate(String name) {
        FirmwareRuleTemplate result = new FirmwareRuleTemplate();
        result.setId(name);
        ApplicableAction action = new BlockingFilterAction();
        action.setActionType(ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE);
        result.setApplicableAction(action);
        result.setRule(createDefaultRule());

        return result;
    }

    public static FirmwareRuleTemplate createBlockingFiltersRuleTemplateWithConditions() throws Exception {
        String[][] conditions = {
                {"timeZone", "IS", "UTC"},
                {"time", "GTE", "02:00:00", "OR"},
                {"time", "LTE", "01:00:00", "AND", "TRUE"},
        };
        Rule mainRule = new Rule();
        mainRule.setCompoundParts(createRules(conditions));
        return createAndSaveFirmwareRuleTemplate("blockingFilterRuleTemplate", ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE, mainRule);
    }

    public static FirmwareRuleTemplate createFirmwareRuleTemplate(String name, ApplicableAction.Type type, Rule rule) throws Exception {
        FirmwareRuleTemplate result = new FirmwareRuleTemplate();
        result.setId(name);

        ApplicableAction action;
        if (type != null) {
            action = new BlockingFilterAction();
            action.setActionType(type);
        } else {
            action = createDefinePropertiesTemplateAction();
        }

        result.setApplicableAction(action);
        result.setRule(rule);

        return result;
    }

    public static FirmwareRuleTemplate createAndSaveFirmwareRuleTemplate(String name, ApplicableAction.Type type, Rule rule) throws Exception {
        return saveFirmwareRuleTemplate(createFirmwareRuleTemplate(name, type, rule));
    }

    public static FirmwareRuleTemplate createAndSaveDefaultRuleActionsRuleTemplate() throws Exception {
        return saveFirmwareRuleTemplate(createDefaultRuleActionsRuleTemplate());
    }

    public static FirmwareRuleTemplate createAndSaveRuleActionsRuleTemplate(String templateName) throws Exception {
        return saveFirmwareRuleTemplate(createRuleActionsRuleTemplate(templateName));
    }

    public static FirmwareRuleTemplate createAndSaveDefaultRuleActionsRuleTemplate(String templateName, Rule rule) throws Exception {
        return saveFirmwareRuleTemplate(createRuleActionsRuleTemplate(templateName, rule));
    }

    public static FirmwareRuleTemplate createAndSaveDefaultDefinePropertiesRuleTemplate() throws Exception {
        return saveFirmwareRuleTemplate(createDefaultDefinePropertiesRuleTemplate());
    }

    public static FirmwareRuleTemplate createAndSaveDefaultBlockingFiltersRuleTemplate() throws Exception {
        return  saveFirmwareRuleTemplate(createDefaultBlockingFiltersRuleTemplate());
    }

    public static FirmwareRuleTemplate saveFirmwareRuleTemplate(FirmwareRuleTemplate template) throws Exception {
        HttpClient.post(GenericTestUtils.buildFullUrl(FIRMWARE_RULE_TEMPLATE_URL), template);

        return template;
    }

    private static DefinePropertiesTemplateAction createDefinePropertiesTemplateAction() throws Exception {
        DefinePropertiesTemplateAction result = new DefinePropertiesTemplateAction();
        result.setProperties(new TreeMap<String, DefinePropertiesTemplateAction.PropertyValue>() {{
            DefinePropertiesTemplateAction.PropertyValue value = new DefinePropertiesTemplateAction.PropertyValue();
            value.setValue("value");
            value.setOptional(false);
            value.setValidationTypes(Collections.singletonList(DefinePropertiesTemplateAction.ValidationType.STRING));
            put("key", value);
        }});
        FirmwareRuleTemplate blockingFilter = createAndSaveDefaultBlockingFiltersRuleTemplate();
        result.setByPassFilters(Collections.singletonList(blockingFilter.getId()));


        return result;
    }

    public static ArrayList<Rule> createRules(String[][] conditions) {
        ArrayList<Rule> compoundPartsList = new ArrayList();
        for (String[] condition : conditions) {
            Rule rule = null;
            if (condition.length >= 3 && condition.length <= 5) { //freeArg, operation, fixedArg, relation, negated
                if (StringUtils.isNotBlank(condition[0]) && StringUtils.isNotBlank(condition[1])) {
                    rule = new Rule();
                    rule.setCondition(new Condition(
                            new FreeArg(FreeArgType.forName("STRING"), condition[0])
                            , Operation.forName(condition[1])
                            , FixedArg.from(condition[2])));

                    if (condition.length >= 4 && StringUtils.isNotBlank(condition[3])) {
                        Relation relation = Relation.valueOf(condition[3].toUpperCase());
                        if (relation != null) {
                            rule.setRelation(relation);
                        }
                    }

                    if (condition.length == 5) {
                        rule.setNegated(Boolean.valueOf(condition[4]));
                    }
                }
            }

            if (rule != null) {
                compoundPartsList.add(rule);
            }
        }

        return compoundPartsList;
    }

    private static Rule createDefaultRule() {
        Rule rule = new Rule();
        rule.setCondition(new Condition(new FreeArg(FreeArgType.forName("STRING"), "freeArg"), StandardOperation.IS, FixedArg.from("fixedArg")));
        return rule;
    }
}
