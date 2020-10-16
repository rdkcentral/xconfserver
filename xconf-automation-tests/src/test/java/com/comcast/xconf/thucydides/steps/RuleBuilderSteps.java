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
 * <p>
 * Author: Stanislav Menshykov
 * Created: 3/15/16  12:28 PM
 */
package com.comcast.xconf.thucydides.steps;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.xconf.thucydides.pages.RuleBuilderDirectivePageObjects;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.pages.WebElementFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleBuilderSteps {

    private RuleBuilderDirectivePageObjects page;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Step
    public RuleBuilderSteps typeFixedArg(Object value) {
        page.typeFixedArg(value.toString());
        return this;
    }

    @Step
    public RuleBuilderSteps typeFreeArg(String value) {
        page.typeFreeArg(value);
        return this;
    }

    @Step
    public RuleBuilderSteps clickFixedArgIn() {
        page.clickFixedArgIn();
        return this;
    }

    @Step
    public RuleBuilderSteps checkNegateRelation() {
        page.checkNegateRelation();
        return this;
    }

    @Step
    public RuleBuilderSteps waitModalDialog() {
        page.waitModalDialog();
        return this;
    }

    @Step
    public RuleBuilderSteps typeSearchText(String value) {
        page.typeSearchText(value);
        return this;
    }

    @Step
    public RuleBuilderSteps selectSuggestion() {
        page.selectSuggestion();
        return this;
    }

    @Step
    public RuleBuilderSteps saveModalDialog() {
        page.saveModalDialog();
        return this;
    }

    @Step
    public RuleBuilderSteps closeModalDialog() {
        page.closeModalDialog();
        return this;
    }

    @Step
    public RuleBuilderSteps openOperationsList() {
        page.clickOperationSelect();
        return this;
    }

    @Step
    public RuleBuilderSteps clickAddCondition() {
        page.clickAddConditionButton();
        return this;
    }

    @Step
    public RuleBuilderSteps clickRuleConditionForEdit() {
        page.clickRuleCondition();
        return this;
    }

    @Step
    public RuleBuilderSteps clickRuleConditionForEdit(int index) {
        if (index == 0) {
            page.clickRuleCondition();
        }else {
            page.clickRuleCondition(index-1);
        }
        return this;
    }

    @Step
    public RuleBuilderSteps validateRuleCondition(int index, boolean negated, String arg, String operation, String value, String relation) {
        WebElementFacade rule = index == 0 ? page.getRuleCondition() : page.getRuleCondition(index-1);
        page.validateRuleCondition(rule, negated, arg, operation, value, relation);
        return this;
    }

    @Step
    public RuleBuilderSteps selectOperation(Operation value) {
        page.selectOperation(value.toString());
        return this;
    }

    @Step
    public RuleBuilderSteps clickOnTypeaheadItem(String name) {
        page.clickTypeaheadItem(name);
        return this;
    }

    @Step
    public RuleBuilderSteps addCondition(String freeArg, Operation operation, Object fixedArg) {
        typeFreeArg(freeArg).clickOnTypeaheadItem(freeArg).selectOperation(operation).typeFixedArg(fixedArg).clickAddCondition();
        return this;
    }

    @Step
    public RuleBuilderSteps addConditionCustomFreeArg(String freeArg, Operation operation, Object fixedArg) {
        typeFreeArg(freeArg).selectOperation(operation).typeFixedArg(fixedArg).clickAddCondition();
        return this;
    }

    @Step
    public RuleBuilderSteps prepareCondition(boolean negated, String freeArg, Operation operation, String fixedArg, String relation) {
        if (relation != null) {
            if (relation.toLowerCase().equals("and")) {
                clickAndRelation();
            } else if (relation.toLowerCase().equals("or")) {
                clickOrRelation();
            }
        }

        if (negated != page.isSelectedNegateRelation()) {
            checkNegateRelation();
        }

        typeFreeArg(freeArg).clickOnTypeaheadItem(freeArg).selectOperation(operation);
        if (StandardOperation.IS.equals(operation) || StandardOperation.GTE.equals(operation) || StandardOperation.LTE.equals(operation)) {
            typeFixedArg(fixedArg);
        } else if (StandardOperation.IN.equals(operation)) {
            clickFixedArgIn().waitModalDialog().typeSearchText(fixedArg)
                    .selectSuggestion().saveModalDialog();
        } else {
            logger.warn("No action for 'fixedArg' input, because the operations were not found");
        }
        return this;
    }

    @Step
    public RuleBuilderSteps addAndValidateCondition(boolean negated, String freeArg, Operation operation, String fixedArg, String relation) {
        prepareCondition(negated, freeArg, operation, fixedArg, relation);
        String freeArgInput = page.getFreeArgValue();
        String fixedArgInput = page.getFixedArgValue(operation);
        clickAddCondition().validateRuleCondition(page.getRuleConditionList().size(), negated, freeArgInput, operation.toString(), fixedArgInput, relation);
        return this;
    }

    @Step
    public RuleBuilderSteps editAndValidateCondition(int conditionIndex, boolean negated, String freeArg, Operation operation, String fixedArg, String relation) {
        if (conditionIndex == 0) {
            relation = null;
        }
        clickRuleConditionForEdit(conditionIndex);
        prepareCondition(negated, freeArg, operation, fixedArg, relation);
        String freeArgInput = page.getFreeArgValue();
        String fixedArgInput = page.getFixedArgValue(operation);
        clickAddCondition().validateRuleCondition(conditionIndex, negated, freeArgInput, operation.toString(), fixedArgInput, relation);
        return this;
    }

    @Step
    public RuleBuilderSteps clickAndRelation() {
        page.clickAndRelation();
        return this;
    }

    @Step
    public RuleBuilderSteps clickOrRelation() {
        page.clickOrRelation();
        return this;
    }
}
