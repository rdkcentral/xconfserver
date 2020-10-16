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

import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.*;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;
import com.comcast.xconf.thucydides.util.RuleUtils;
import com.comcast.xconf.thucydides.util.common.EnvironmentUtils;
import com.comcast.xconf.thucydides.util.common.ModelUtils;

import java.util.Collections;
import java.util.TreeMap;

import static com.comcast.xconf.thucydides.util.firmware.FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate;
import static com.comcast.xconf.thucydides.util.firmware.FirmwareRuleTemplateUtils.saveFirmwareRuleTemplate;

public class FirmwareRuleUtils {
    public static final String FIRMWARE_RULE_URL = "firmwarerule";

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(FIRMWARE_RULE_URL + "?pageSize=9999&pageNumber=1", FIRMWARE_RULE_URL, FirmwareRule.class);
        FirmwareRuleTemplateUtils.doCleanup();
        FirmwareConfigUtils.doCleanup();
        EnvironmentUtils.doCleanup();
        ModelUtils.doCleanup();
    }

    public static FirmwareRule createAndSaveDefaultRuleActionRule() throws Exception {
        return saveFirmwareRule(createDefaultRuleActionRule());
    }

    public static FirmwareRule createAndSaveDefaultDefinePropertiesRule() throws Exception {
        return saveFirmwareRule(createDefaultDefinePropertiesRule());
    }

    public static FirmwareRule createAndSaveDefaultBlockingFilterRule() throws Exception {
        return saveFirmwareRule(createDefaultBlockingFilterRule());
    }

    public static FirmwareRule createDefaultRuleActionRule() throws Exception {
        FirmwareRuleTemplate template = createAndSaveDefaultRuleActionsRuleTemplate();
        return createRuleActionRule(template.getId(), template.getRule());
    }

    public static FirmwareRule createDefaultDefinePropertiesRule() throws Exception {
        FirmwareRuleTemplate template = FirmwareRuleTemplateUtils.createAndSaveDefaultDefinePropertiesRuleTemplate();
        FirmwareRule result = new FirmwareRule();
        result.setName("definePropertiesRule");
        result.setType(template.getId());
        result.setApplicableAction(createDefinePropertiesAction());
        result.setRule(template.getRule());

        return result;
    }

    public static FirmwareRule createDefaultBlockingFilterRule() throws Exception {
        FirmwareRuleTemplate template = FirmwareRuleTemplateUtils.createAndSaveDefaultBlockingFiltersRuleTemplate();
        FirmwareRule result = new FirmwareRule();
        result.setName("blockingFilterRule");
        result.setType(template.getId());
        result.setApplicableAction(new BlockingFilterAction());
        result.setRule(template.getRule());

        return result;
    }

    public static FirmwareRule createDefaultEnvModelRule() throws Exception {
        Rule envModelRule = RuleUtils.buildEnvModelRule();
        FirmwareRuleTemplate envModelRuleTemplate = FirmwareRuleTemplateUtils.createRuleActionsRuleTemplate(TemplateNames.ENV_MODEL_RULE, envModelRule);
        envModelRuleTemplate.setRule(envModelRule);
        FirmwareRuleTemplate template = saveFirmwareRuleTemplate(envModelRuleTemplate);
        FirmwareRule firmwareEnvModelRule = createRuleActionRule(template.getTemplateId(), envModelRule);
        firmwareEnvModelRule.setId("envModelRuleId");
        firmwareEnvModelRule.setName("envModelRuleName");
        return firmwareEnvModelRule;
    }

    public static FirmwareRule createDefaultMacRule() throws Exception {
        Rule macRule = RuleUtils.buildMacrule();
        FirmwareRuleTemplate template = saveFirmwareRuleTemplate(FirmwareRuleTemplateUtils.createRuleActionsRuleTemplate(TemplateNames.MAC_RULE, macRule));
        FirmwareRule firmwareMacRule = createRuleActionRule(template.getId(), macRule);
        firmwareMacRule.setId("macRuleId");
        firmwareMacRule.setName("macRuleName");
        return firmwareMacRule;
    }

    public static FirmwareRule createMacRule(ApplicableAction applicableAction) throws Exception {
        Rule macRule = RuleUtils.buildMacrule();
        FirmwareRuleTemplate template = saveFirmwareRuleTemplate(FirmwareRuleTemplateUtils.createRuleActionsRuleTemplate(TemplateNames.MAC_RULE, macRule));
        FirmwareRule firmwareMacRule = createRuleActionRule(template.getId(), macRule);
        firmwareMacRule.setApplicableAction(applicableAction);
        firmwareMacRule.setId("macRuleId");
        firmwareMacRule.setName("macRuleName");
        return firmwareMacRule;
    }

    public static FirmwareRule createAndSaveDefaultEnvModelRule() throws Exception {
        return saveFirmwareRule(createDefaultEnvModelRule());
    }

    public static FirmwareRule createAndSaveEnvModelRule(String id, String name, Rule rule) throws Exception {
        FirmwareRule envModelFirmwareRule = createDefaultEnvModelRule();
        envModelFirmwareRule.setId(id);
        envModelFirmwareRule.setName(name);
        envModelFirmwareRule.setRule(rule);
        return saveFirmwareRule(envModelFirmwareRule);
    }

    public static FirmwareRule createAndSaveDefaultMacRule() throws Exception {
        return saveFirmwareRule(createDefaultMacRule());
    }

    public static FirmwareRule createAndSaveMacRule(RuleAction ruleAction) throws Exception {
        return saveFirmwareRule(createMacRule(ruleAction));
    }

    public static FirmwareRule saveFirmwareRule(FirmwareRule rule) throws Exception {
        HttpClient.post(GenericTestUtils.buildFullUrl(FIRMWARE_RULE_URL), rule);

        return rule;
    }

    private static FirmwareRule createRuleActionRule(String ruleType, Rule rule) throws Exception {
        FirmwareConfig config = FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();
        FirmwareRule result = new FirmwareRule();
        result.setId("ruleId");
        result.setName("ruleActionRule");
        RuleAction action = new RuleAction();
        action.setConfigId(config.getId());
        result.setApplicableAction(action);
        result.setType(ruleType);
        result.setRule(rule);

        return result;
    }

    private static DefinePropertiesAction createDefinePropertiesAction() throws Exception {
        DefinePropertiesAction result = new DefinePropertiesAction();
        result.setProperties(new TreeMap<String, String>() {{
            put("key", "value");
        }});
        FirmwareRuleTemplate blockingFilter = FirmwareRuleTemplateUtils.createAndSaveDefaultBlockingFiltersRuleTemplate();
        result.setByPassFilters(Collections.singletonList(blockingFilter.getId()));

        return result;
    }
}
