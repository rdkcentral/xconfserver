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

package com.comcast.xconf.validators.firmware;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

public class FirmwareRuleTemplateValidatorTest extends BaseQueriesControllerTest {

    @Autowired
    private FirmwareRuleTemplateValidator validator;

    @org.junit.Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void validateWrongActionType() throws Exception {
        exceptionRule.expect(ValidationRuntimeException.class);
        exceptionRule.expectMessage("RULE action type is not supported by template");
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, defaultModelId.toUpperCase(), FirmwareConfig.DownloadProtocol.http);
        Rule rule = Rule.Builder.of(new Condition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from(defaultModelId.toUpperCase()))).build();
        RuleAction ruleAction = createRuleAction(ApplicableAction.Type.RULE, firmwareConfig.getId());
        FirmwareRuleTemplate templateToValidate = createFirmwareRuleTemplate("TEST_TEMPLATE", rule, ruleAction);
        validator.validate(templateToValidate);
    }

    @Test
    public void validateDuplicateConditions() {
        exceptionRule.expect(RuleValidationException.class);
        exceptionRule.expectMessage("Please, remove duplicate conditions first: [model IS ]");
        Condition modelCondition = new Condition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from(""));
        Rule rule = Rule.Builder.of(modelCondition)
                .and(modelCondition).build();
        RuleAction templateAction = createRuleAction(ApplicableAction.Type.RULE_TEMPLATE, "");
        FirmwareRuleTemplate templateToValidate = createFirmwareRuleTemplate("TEST_TEMPLATE", rule, templateAction);

        validator.validate(templateToValidate);
    }

    @Test
    public void validateTemplateWithConditionAndCompoundRuleShouldBeFailed() {
        exceptionRule.expect(RuleValidationException.class);
        exceptionRule.expectMessage("Rule should have only condition or compoundParts field");
        Rule rule = createCompoundRule();
        rule.setCondition(new Condition(RuleFactory.PARTNER_ID, StandardOperation.IS, FixedArg.from("")));
        RuleAction templateAction = createRuleAction(ApplicableAction.Type.RULE_TEMPLATE, "");
        FirmwareRuleTemplate templateToValidate = createFirmwareRuleTemplate("TEST_TEMPLATE", rule, templateAction);

        validator.validate(templateToValidate);
    }

    @Test
    public void validateCompoundRuleShouldNotHaveAnotherCompoundRule() {
        exceptionRule.expect(RuleValidationException.class);
        exceptionRule.expectMessage("CompoundPart rule should not have one more compoundParts");
        Rule rule = createCompoundRule();
        rule.getCompoundParts().get(0).setCompoundParts(createCompoundRule().getCompoundParts());
        RuleAction templateAction = createRuleAction(ApplicableAction.Type.RULE_TEMPLATE, "");
        FirmwareRuleTemplate templateToValidate = createFirmwareRuleTemplate("TEST_TEMPLATE", rule, templateAction);

        validator.validate(templateToValidate);
    }
}