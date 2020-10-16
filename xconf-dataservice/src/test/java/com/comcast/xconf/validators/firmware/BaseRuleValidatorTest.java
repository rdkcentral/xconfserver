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
 * Created: 2/3/16  4:02 PM
 */
package com.comcast.xconf.validators.firmware;


import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.*;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.estbfirmware.logupload.LogUploadArgs;
import com.comcast.xconf.validators.BaseRuleValidator;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BaseRuleValidatorTest {

    @org.junit.Rule
    public ExpectedException expectedException = ExpectedException.none();
    private BaseRuleValidator baseRuleValidator = new BaseRuleValidator(){
        @Override
        protected List<Operation> getAllowedOperations() {
            List<Operation> allowedOperations = super.getAllowedOperations();
            allowedOperations.add(StandardOperation.EXISTS);
            allowedOperations.add(StandardOperation.IN);
            return allowedOperations;
        }
    };

    @Test
    public void runGlobalValidation_NullRule() throws Exception {
        Rule rule = null;

        assertGlobalValidationThrowsException(rule, "Rule is null");
    }

    @Test
    public void runGlobalValidation_RuleIsEmpty() throws Exception {
        Rule rule = createDefaultRule();
        rule.setCondition(null);

        assertGlobalValidationThrowsException(rule, "Rule is empty");
    }


    @Test
    public void runGlobalValidation_DuplicateConditions() throws Exception {
        Rule rule = createDefaultRule();
        rule.setCompoundParts(Arrays.asList(createRule(Relation.AND, false)));
        rule.setCompoundParts(Arrays.asList((createRule(Relation.AND, false))));

        assertGlobalValidationThrowsException(rule, "Please, remove duplicate conditions first: " + Lists.newArrayList(rule.getCondition()));
    }

    @Test
    public void runGlobalValidation_FreeArgIsNull() throws Exception {
        Rule rule = createDefaultRule();
        rule.getCondition().setFreeArg(null);

        assertGlobalValidationThrowsException(rule, "FreeArg is empty");
    }

    @Test
    public void runGlobalValidation_FreeArgNameIsNull() throws Exception {
        Rule rule = createDefaultRule();
        rule.getCondition().setFreeArg(new FreeArg(StandardFreeArgType.STRING, null));

        assertGlobalValidationThrowsException(rule, "FreeArg is empty");
    }

    @Test
    public void runGlobalValidation_OperationIsNull() throws Exception {
        Rule rule = createDefaultRule();
        rule.getCondition().setOperation(null);

        assertGlobalValidationThrowsException(rule, "Operation is null");
    }

    @Test
    public void runGlobalValidation_FixedArgIsNull() throws Exception {
        Rule rule = createDefaultRule();
        rule.getCondition().setFixedArg(null);

        assertGlobalValidationThrowsException(rule, "FixedArg is null");
    }

    @Test
    public void runGlobalValidation_FixedArgValueIsNull() throws Exception {
        Rule rule = createDefaultRule();
        rule.getCondition().setFixedArg(FixedArg.from((String) null));

        assertGlobalValidationThrowsException(rule, "FixedArg is null");
    }

    @Test
    public void runGlobalValidation_FixedArgValueNotACollectionWhenINOperation() throws Exception {
        Rule rule = createRule(RuleFactory.IP, StandardOperation.IN, "string", null);

        assertGlobalValidationThrowsException(rule, rule.getCondition().getFreeArg().getName() + " is not collection");
    }

    @Test
    public void runGlobalValidation_FixedArgValueCollectionIsEmpty() throws Exception {
        Rule rule = createDefaultRule();
        rule.getCondition().setFixedArg(FixedArg.from(Collections.emptyList()));
        rule.getCondition().setOperation(StandardOperation.IN);

        assertGlobalValidationThrowsException(rule, rule.getCondition().getFreeArg().getName() + " is empty");
    }

    @Test
    public void runGlobalValidation_FixedArgIsNullButEXISTSOperation() throws Exception {
        Rule rule = createDefaultRule();
        rule.getCondition().setFixedArg(FixedArg.from(Collections.emptyList()));
        rule.getCondition().setOperation(StandardOperation.EXISTS);

        baseRuleValidator.runGlobalValidation(rule);
    }

    @Test
    public void runGlobalValidation_FixedArgValueContainsDuplicateStrings() throws Exception {
        Rule rule = createDefaultRule();
        rule.getCondition().setFixedArg(FixedArg.from(Arrays.asList("1", "2", "1")));

        assertGlobalValidationThrowsException(rule,
                "FixedArg of condition: " + rule.getCondition() + " contains duplicate items: " + Collections.singletonList("1"));
    }

    @Test
    public void runGlobalValidation_MacAddressIsNotValid1() throws Exception {
        assertInvalidMacAddressException(RuleFactory.MAC, StandardOperation.IS, "42");
    }

    @Test
    public void runGlobalValidation_MacAddressIsNotValid2() throws Exception {
        assertInvalidMacAddressException(RuleFactory.MAC, StandardOperation.IN, Collections.singletonList("42"));
    }

    @Test
    public void runGlobalValidation_MacAddressIsNotValid3() throws Exception {
        assertInvalidMacAddressException(LogUploadArgs.ESTB_MAC, StandardOperation.IS, "42");
    }

    @Test
    public void runGlobalValidation_MacAddressIsNotValid4() throws Exception {
        assertInvalidMacAddressException(LogUploadArgs.ESTB_MAC, StandardOperation.IN, Collections.singletonList("42"));
    }

    @Test
    public void runGlobalValidation_MacAddressIsNotValid5() throws Exception {
        assertInvalidMacAddressException(LogUploadArgs.ECM_MAC, StandardOperation.IS, "42");
    }

    @Test
    public void runGlobalValidation_MacAddressIsNotValid6() throws Exception {
        assertInvalidMacAddressException(LogUploadArgs.ECM_MAC, StandardOperation.IN, Collections.singletonList("42"));
    }

    @Test
    public void runGlobalValidation_IpAddressIsNotValid1() throws Exception {
        assertInvalidIpAddressException(RuleFactory.IP, StandardOperation.IS, "42");
    }

    @Test
    public void runGlobalValidation_IpAddressIsNotValid2() throws Exception {
        assertInvalidIpAddressException(RuleFactory.IP, StandardOperation.IN, Collections.singletonList("42"));
    }

    @Test
    public void runGlobalValidation_IpAddressIsNotValid3() throws Exception {
        assertInvalidIpAddressException(LogUploadArgs.ESTB_IP, StandardOperation.IS, "42");
    }

    @Test
    public void runGlobalValidation_IpAddressIsNotValid4() throws Exception {
        assertInvalidIpAddressException(LogUploadArgs.ESTB_IP, StandardOperation.IN, Collections.singletonList("42"));
    }

    @Test
    public void runGlobalValidation_PercentIsNotValid1() throws Exception {
        assertInvalidPercentException("-1");
    }

    @Test
    public void runGlobalValidation_PercentIsNotValid2() throws Exception {
        assertInvalidPercentException("101");
    }

    @Test
    public void runGlobalValidation_PercentIsNotValid3() throws Exception {
        assertInvalidPercentException("a");
    }

    @Test
    public void runGlobalValidation_PatternIsNotValid() throws Exception {
        Rule rule = createRule(new Condition(RuleFactory.MODEL, StandardOperation.LIKE, FixedArg.from("\\")), null, false);

        assertGlobalValidationThrowsException(rule, rule.getCondition().getFreeArg().getName() + " is not valid; ");
    }

    private void assertGlobalValidationThrowsException(Rule rule, String exceptionMessage) {
        expectedException.expect(RuleValidationException.class);
        expectedException.expectMessage(exceptionMessage);

        baseRuleValidator.runGlobalValidation(rule);
    }

    private void assertInvalidMacAddressException(FreeArg freeArg, Operation operation, Object fixedArgValue) {
        Rule rule = createRule(new Condition(freeArg, operation, FixedArg.from(fixedArgValue)), null, false);

        assertGlobalValidationThrowsException(rule, "Mac Address is not valid: 42");
    }

    private void assertInvalidIpAddressException(FreeArg freeArg, Operation operation, Object fixedArgValue) {
        Rule rule = createRule(new Condition(freeArg, operation, FixedArg.from(fixedArgValue)), null, false);

        assertGlobalValidationThrowsException(rule, "Ip Address is not valid: 42");
    }

    private void assertInvalidPercentException(Object fixedArgValue) {
        Rule rule = createRule(new Condition(RuleFactory.MODEL, StandardOperation.PERCENT, FixedArg.from(fixedArgValue)), null, false);

        assertGlobalValidationThrowsException(rule, rule.getCondition().getFreeArg().getName() + " is not valid; 0.0 < value < 100.0");
    }

    private Rule createRule(Relation relation, boolean negated) {
        Rule result = createRule(RuleFactory.MODEL, StandardOperation.ANY_MATCHED, "1", null);
        result.setRelation(relation);
        result.setNegated(negated);

        return result;
    }

    private Rule createDefaultRule() {
        return createRule(null, false);
    }

    private Rule createRule(FreeArg freeArg, Operation operation, String fixedArg, Relation relation) {
        return createRule(createCondition(freeArg, operation, fixedArg), relation, false);
    }

    private Rule createRule(Condition condition, Relation relation, boolean negated) {
        Rule result = new Rule();
        result.setCondition(condition);
        result.setRelation(relation);
        result.setNegated(negated);

        return result;
    }

    private Condition createCondition(FreeArg freeArg, Operation operation, String fixedArg) {
        return new Condition(freeArg, operation, FixedArg.from(fixedArg));
    }
}
