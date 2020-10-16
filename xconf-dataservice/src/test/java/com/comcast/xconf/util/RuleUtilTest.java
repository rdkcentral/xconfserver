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
 * Created: 1/26/16  11:58 AM
 */
package com.comcast.xconf.util;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.*;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.estbfirmware.logupload.LogUploadArgs;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class RuleUtilTest {

    @Test
    public void toConditionsNotCompoundRule() throws Exception {
        Condition condition = createCondition("fixedArg");
        Rule rule = createRule(condition);

        List<Condition> actualResult = Lists.newArrayList(RuleUtil.toConditions(rule));

        assertEquals(1, actualResult.size());
        assertEquals(Collections.singletonList(condition), actualResult);
    }

    @Test
    public void toConditionsCheckNPE() throws Exception {
        List<Condition> conditionList = Lists.newArrayList(RuleUtil.toConditions(null));

        assertEquals(0, conditionList.size());
    }

    @Test
    public void toConditionsCompoundRule() throws Exception {
        Condition condition1 = createCondition("fixedArg1");
        Condition condition2 = null;
        Condition condition3 = createCondition("fixedArg2");
        Rule rule = new Rule();
        rule.setCompoundParts(Lists.newArrayList(createRule(condition1), createRule(condition2), createRule(condition3)));

        List<Condition> actualResult = Lists.newArrayList(RuleUtil.toConditions(rule));

        List<Condition> expectedResult = Lists.newArrayList(condition1, condition3);
        assertEquals(2, actualResult.size());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void toConditionsComplexRule() throws Exception {
        Condition condition1 = createCondition("fixedArg1");
        Condition condition2 = createCondition("fixedArg2");
        Rule rule = createRule(condition1);
        rule.setCompoundParts(Collections.singletonList(createRule(condition2)));

        List<Condition> actualResult = Lists.newArrayList(RuleUtil.toConditions(rule));

        List<Condition> expectedResult = Lists.newArrayList(condition1, condition2);
        assertEquals(2, actualResult.size());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void getFixedArgFromConditionByFreeArgNameAndOperation() throws Exception {
        String fixedArgToBeFound = "fixedArgToBeFound";
        Operation operationForSearch = RuleFactory.IN_LIST;
        FreeArg freeArgForSearch = RuleFactory.MAC;
        Condition condition = createCondition(freeArgForSearch, operationForSearch, fixedArgToBeFound);

        Object actualResult = RuleUtil.getFixedArgFromConditionByFreeArgAndOperation(condition, freeArgForSearch, operationForSearch);

        assertEquals(fixedArgToBeFound, actualResult);
    }

    @Test
    public void getFixedArgFromConditionByOperation() throws Exception {
        String fixedArgToBeFound = "fixedArgToBeFound";
        Operation operationForSearch = RuleFactory.IN_LIST;
        Condition condition = createCondition(RuleFactory.MODEL, operationForSearch, fixedArgToBeFound);

        Object actualResult = RuleUtil.getFixedArgFromConditionByOperation(condition, operationForSearch);

        assertEquals(fixedArgToBeFound, actualResult);
    }

    @Test
    public void getFixedArgFromConditionByFreeArgNameAndOperation_CheckThereIsNoNPE() throws Exception {
        Condition condition = createCondition(null, null, null);

        Object actualResult = RuleUtil.getFixedArgFromConditionByFreeArgAndOperation(condition, RuleFactory.IP, RuleFactory.IN_LIST);

        assertNull(actualResult);
    }

    @Test
    public void getFixedArgFromConditionByOperation_CheckThereIsNoNPE() throws Exception {
        Condition condition = createCondition(null, null, null);

        Object actualResult = RuleUtil.getFixedArgFromConditionByOperation(condition, RuleFactory.IN_LIST);

        assertNull(actualResult);
    }

    @Test
    public void getFixedArgsFromRuleByFreeArgNameAndOperation() throws Exception {
        String fixedArgToBeFound1 = "fixedArgToBeFound1";
        String fixedArgToBeFound2 = "fixedArgToBeFound2";
        Operation operationForSearch = RuleFactory.IN_LIST;
        FreeArg freeArgForSearch = RuleFactory.MAC;
        FreeArg anotherFreeArg = RuleFactory.IP;
        Rule rule = new Rule();
        rule.setCompoundParts(Lists.newArrayList(
                createRule(freeArgForSearch, operationForSearch, fixedArgToBeFound1, null),
                createRule(anotherFreeArg, operationForSearch, "someOtherFixedArg", Relation.AND)
        ));
        rule.setCondition(createCondition(freeArgForSearch, operationForSearch, fixedArgToBeFound2));

        List<String> actualResult = RuleUtil.getFixedArgsFromRuleByFreeArgAndOperation(rule, freeArgForSearch, operationForSearch);

        List<String> expectedResult = Lists.newArrayList(fixedArgToBeFound2, fixedArgToBeFound1);
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void getFixedArgsFromRuleByOperation() throws Exception {
        String fixedArgToBeFound1 = "fixedArgToBeFound1";
        String fixedArgToBeFound2 = "fixedArgToBeFound2";
        Operation operationForSearch = RuleFactory.IN_LIST;
        Operation otherOperation = StandardOperation.EXISTS;
        Rule rule = new Rule();
        rule.setCompoundParts(Lists.newArrayList(
                createRule(RuleFactory.MAC, operationForSearch, fixedArgToBeFound1, null),
                createRule(RuleFactory.MODEL, otherOperation, "someOtherFixedArg", Relation.AND)
        ));
        rule.setCondition(createCondition(RuleFactory.IP, operationForSearch, fixedArgToBeFound2));

        List<String> actualResult = RuleUtil.getFixedArgsFromRuleByOperation(rule, operationForSearch);

        List<String> expectedResult = Lists.newArrayList(fixedArgToBeFound2, fixedArgToBeFound1);
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void getFixedArgsFromRuleByFreeArgNameAndOperation_CheckThereIsNoNPE() throws Exception {
        String fixedArgToBeFound = "fixedArgToBeFound";
        Operation operationForSearch = RuleFactory.IN_LIST;
        FreeArg freeArgForSearch = RuleFactory.MAC;
        Rule rule = new Rule();
        rule.setCompoundParts(Lists.newArrayList(
                null,
                createRule(null, null, null, null),
                createRule(freeArgForSearch, operationForSearch, fixedArgToBeFound, Relation.AND)
        ));
        rule.setCondition(createCondition(null, null, null));

        List<String> actualResult = RuleUtil.getFixedArgsFromRuleByFreeArgAndOperation(rule, freeArgForSearch, operationForSearch);

        List<String> expectedResult = Collections.singletonList(fixedArgToBeFound);
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void getFixedArgsFromRuleByOperation_CheckThereIsNoNPE() throws Exception {
        String fixedArgToBeFound = "fixedArgToBeFound";
        Operation operationForSearch = RuleFactory.IN_LIST;
        Rule rule = new Rule();
        rule.setCompoundParts(Lists.newArrayList(
                null,
                createRule(null, null, null, null),
                createRule(RuleFactory.LOCAL_TIME, operationForSearch, fixedArgToBeFound, Relation.AND)
        ));
        rule.setCondition(createCondition(null, null, null));

        List<String> actualResult = RuleUtil.getFixedArgsFromRuleByOperation(rule, operationForSearch);

        List<String> expectedResult = Collections.singletonList(fixedArgToBeFound);
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void getFixedArgsFromRuleByFreeArgNameAndOperation_ReturnsEmptyList() throws Exception {
        Rule rule = new Rule();
        Operation operationInRule = StandardOperation.IN;
        Operation operationForSearch = StandardOperation.EXISTS;
        rule.setCompoundParts(Lists.newArrayList(
                createRule(RuleFactory.IP, operationInRule, "fixedArg", Relation.AND)
        ));

        List<String> actualResult = RuleUtil.getFixedArgsFromRuleByFreeArgAndOperation(rule, RuleFactory.MAC, operationForSearch);

        assertThat(new ArrayList<String>(), is(actualResult));
    }

    @Test
    public void getFixedArgsFromRuleByOperation_ReturnsEmptyList() throws Exception {
        Rule rule = new Rule();
        Operation operationInRule = StandardOperation.IN;
        Operation operationForSearch = StandardOperation.EXISTS;
        rule.setCompoundParts(Lists.newArrayList(
                createRule(RuleFactory.IP, operationInRule, "fixedArg", Relation.AND)
        ));

        List<String> actualResult = RuleUtil.getFixedArgsFromRuleByOperation(rule, operationForSearch);

        assertThat(new ArrayList<String>(), is(actualResult));
    }

    @Test
    public void normalizeFixedArgValue_FreeArgAndOperationNotImportant() throws Exception {
        String expectedResult = "someString";
        String fixedArgValue = "  " + expectedResult + "     ";

        assertEquals(expectedResult, RuleUtil.normalizeFixedArgValue(fixedArgValue, RuleFactory.IP, StandardOperation.GTE));
    }

    @Test
    public void normalizeFixedArgListValues_FreeArgAndOperationNotImportant() throws Exception {
        String expectedResultString = "someString";
        List<String> expectedResult = Collections.singletonList(expectedResultString);
        List<String> fixedArgValue = Lists.newArrayList(null, "    ", " " + expectedResultString + "  ", "");

        assertEquals(expectedResult, RuleUtil.normalizeFixedArgValue(fixedArgValue, RuleFactory.FIRMWARE_DOWNLOAD_PROTOCOL, StandardOperation.EXISTS));
    }

    @Test
    public void normalizeFixedArgValue_MODELFreeArgAndISOperation() throws Exception {
        String expectedResult = "SOMESTRING";
        String fixedArgValue = "  someString     ";

        assertEquals(expectedResult, RuleUtil.normalizeFixedArgValue(fixedArgValue, RuleFactory.MODEL, StandardOperation.IS));
    }

    @Test
    public void normalizeFixedArgListValues_ENVFreeArgAndINOperation() throws Exception {
        List<String> expectedResult = Collections.singletonList("SOMESTRING");
        List<String> fixedArgValue = Lists.newArrayList(null, "    ", " somESTrinG  ", "");

        assertEquals(expectedResult, RuleUtil.normalizeFixedArgValue(fixedArgValue, RuleFactory.ENV, StandardOperation.IN));
    }

    @Test
    public void normalizeFixedArgValue_EstbFirmwareMACFreeArgAndISOperation() throws Exception {
        String expectedResult = "12:22:23:34:34:34";
        String fixedArgValue = "  12:22-23.34.34-34     ";

        assertEquals(expectedResult, RuleUtil.normalizeFixedArgValue(fixedArgValue, RuleFactory.MAC, StandardOperation.IS));
    }

    @Test
    public void normalizeFixedArgListValue_LogUploadMACFreeArgAndINOperation() throws Exception {
        List<String> expectedResult = Collections.singletonList("12:22:23:34:34:34");
        List<String> fixedArgValue = Lists.newArrayList(null, "    ", "  12:22-23.34.34-34     ", "");

        assertEquals(expectedResult, RuleUtil.normalizeFixedArgValue(fixedArgValue, LogUploadArgs.ESTB_MAC, StandardOperation.IN));
    }

    @Test
    public void normalizeCondition() throws Exception {
        Condition condition = createCondition(new FreeArg(StandardFreeArgType.STRING, "  freeArg  "), StandardOperation.GTE, "   some string   ");

        RuleUtil.normalizeCondition(condition);

        assertEquals("some string", condition.getFixedArg().getValue().toString());
        assertEquals("freeArg", condition.getFreeArg().getName());
    }

    @Test
    public void normalizeConditionTestForNPE() throws Exception {
        Condition condition = createCondition(new FreeArg(StandardFreeArgType.STRING, null), StandardOperation.GTE, null);

        RuleUtil.normalizeCondition(condition);

        assertNull(condition.getFixedArg().getValue());
        assertNull(condition.getFreeArg().getName());
    }

    @Test
    public void normalizeConditions() throws Exception {
        Rule rule = createRule(new FreeArg(StandardFreeArgType.STRING, "  freeArg  "), StandardOperation.EXISTS, "   fixedArgValue ", null);
        rule.setCompoundParts(new ArrayList<Rule>(){{
            add(createRule(new FreeArg(StandardFreeArgType.STRING, "  model  "), StandardOperation.IS, " model  ", null));
        }});

        RuleUtil.normalizeConditions(rule);

        String fixedArgValueFromCompoundParts = rule.getCompoundParts().get(0).getCondition().getFixedArg().getValue().toString();
        String freeArgNameFromCompoundParts = rule.getCompoundParts().get(0).getCondition().getFreeArg().getName();
        assertEquals("fixedArgValue", rule.getCondition().getFixedArg().getValue().toString());
        assertEquals("freeArg", rule.getCondition().getFreeArg().getName());
        assertEquals("MODEL", fixedArgValueFromCompoundParts);
        assertEquals("model", freeArgNameFromCompoundParts);
    }

    @Test
    public void equalFixedArgStringValues() throws Exception {
        String stringValue1 = "fixedArg1";
        String stringValue2 = "fixedArg2";

        assertTrue(RuleUtil.equalFixedArgValues(stringValue1, stringValue1));
        assertFalse(RuleUtil.equalFixedArgValues(stringValue1, stringValue2));
        assertFalse(RuleUtil.equalFixedArgValues(null, stringValue2));
        assertFalse(RuleUtil.equalFixedArgValues(stringValue1, null));
        assertTrue(RuleUtil.equalFixedArgValues(null, null));
    }

    @Test
    public void equalFixedArgCollectionValue1() throws Exception {
        List<String> listValue1 = Lists.newArrayList("2", "1");
        List<String> listValue2 = Lists.newArrayList("1", "2");

        assertTrue(RuleUtil.equalFixedArgValues(listValue1, listValue2));
    }

    @Test
    public void equalFixedArgCollectionValue2() throws Exception {
        List<String> listValue1 = Lists.newArrayList("1", "2", "1");
        List<String> listValue2 = Lists.newArrayList("1", "2", "2");

        assertFalse(RuleUtil.equalFixedArgValues(listValue1, listValue2));
    }

    @Test
    public void equalConditions_CheckNPE() throws Exception {
        assertTrue(RuleUtil.equalConditions(null, null));
        assertTrue(RuleUtil.equalConditions(createCondition(null, null, null), createCondition(null, null, null)));
        assertTrue(RuleUtil.equalConditions(createCondition(null, null, ""), createCondition(null, null, null)));
        assertFalse(RuleUtil.equalConditions(null, createCondition(null, null, "someFixedArg")));
    }

    @Test
    public void equalConditions() throws Exception {
        FreeArg equalFreeArg = RuleFactory.IP;
        Operation equalOperation = StandardOperation.EXISTS;
        String equalFixedArg = "fixedArg";

        assertTrue(RuleUtil.equalConditions(createCondition(equalFreeArg, equalOperation, equalFixedArg),
                createCondition(equalFreeArg, equalOperation, equalFixedArg)));
    }

    @Test
    public void equalConditions_DifferentOperations() throws Exception {
        FreeArg equalFreeArg = RuleFactory.IP;
        String equalFixedArg = "fixedArg";

        assertFalse(RuleUtil.equalConditions(createCondition(equalFreeArg, StandardOperation.IS, equalFixedArg),
                createCondition(equalFreeArg, StandardOperation.IN, equalFixedArg)));
    }

    @Test
    public void equalConditions_DifferentFreeArgs() throws Exception {
        Operation equalOperation = StandardOperation.EXISTS;
        String equalFixedArg = "fixedArg";

        assertFalse(RuleUtil.equalConditions(createCondition(RuleFactory.MODEL, equalOperation, equalFixedArg),
                createCondition(RuleFactory.IP, equalOperation, equalFixedArg)));
    }

    @Test
    public void equalConditions_DifferentFixedArgs() throws Exception {
        FreeArg equalFreeArg = RuleFactory.IP;
        Operation equalOperation = StandardOperation.EXISTS;

        assertFalse(RuleUtil.equalConditions(createCondition(equalFreeArg, equalOperation, "1"),
                createCondition(equalFreeArg, equalOperation, "2")));
    }

    @Test
    public void getDuplicateConditions_AllConditionUnique() throws Exception {
        Rule rootRule = createRule(null, false);
        rootRule.setCompoundParts(Lists.newArrayList(
                createRule(Relation.AND, false),
                createRule(Relation.OR, false),
                createRule(Relation.AND, true)
        ));

        assertEquals(1, RuleUtil.getDuplicateConditions(rootRule).size());
    }

    @Test
    public void getDuplicateConditions_TwoConditionsEqual() throws Exception {
        Rule rootRule = createRule(null, false);
        rootRule.setCompoundParts(Lists.newArrayList(
                createRule(Relation.AND, false),
                createRule(Relation.AND, true)
        ));

        Collection<Condition> actualResult = RuleUtil.getDuplicateConditions(rootRule);

        Collection<Condition> expectedResult = Lists.newArrayList(createRule(Relation.AND, false).getCondition());
        assertEquals(expectedResult, actualResult);
        assertEquals(1, actualResult.size());
    }

    @Test
    public void getDuplicateConditionsBetweenOR_AllConditionUnique() throws Exception {
        Rule rule = Rule.Builder.of(createRule(null, false))
                .or(createCondition("model1"))
                .and(createCondition("model2"))
                .build();

        assertEquals(0, RuleUtil.getDuplicateConditionsBetweenOR(rule).size());
    }

    @Test
    public void getDuplicateConditionsBetweenOR_TwoConditionsEqual() throws Exception {
        Rule rule = Rule.Builder.of(createRule(null, false))
                .or(createCondition("model1"))
                .and(createCondition("model1"))
                .build();

        assertEquals(1, RuleUtil.getDuplicateConditionsBetweenOR(rule).size());
    }

    @Test
    public void getDuplicateConditionsBetweenOR_TwoSameConditionsSeparatedByOR() throws Exception {
        String model = "model1";
        Rule rule = Rule.Builder.of(createCondition(model))
                .or(createCondition(RuleFactory.IP, StandardOperation.IS, "1"))
                .and(createCondition(model))
                .build();

        assertEquals(0, RuleUtil.getDuplicateConditionsBetweenOR(rule).size());
    }

    @Test
    public void hasEqualRules_NonCompoundRules() throws Exception {
        Condition defaultCondition = createCondition(RuleFactory.ENV, StandardOperation.IN, "env");
        Rule equalRule1 = createAndRule(defaultCondition);
        Rule equalRule2 = createAndRule(defaultCondition);
        Rule anotherRule = createAndRule(defaultCondition);
        anotherRule.setNegated(true);

        assertTrue(RuleUtil.equalComplexRules(equalRule1, equalRule2));
        assertFalse(RuleUtil.equalComplexRules(equalRule1, anotherRule));
    }

    @Test
    public void hasEqualRules_CompoundRules() throws Exception {
        Rule equalRule1 = new Rule();
        equalRule1.setCompoundParts(Lists.newArrayList(createRule(null, false), createRule(Relation.AND, true)));
        Rule equalRule2 = new Rule();
        equalRule2.setCompoundParts(Lists.newArrayList(createRule(null, true), createRule(Relation.AND, false)));
        Rule anotherRule = new Rule();
        anotherRule.setCompoundParts(Lists.newArrayList(createRule(null, true), createRule(Relation.OR, false)));

        assertTrue(RuleUtil.equalComplexRules(equalRule1, equalRule2));
        assertFalse(RuleUtil.equalComplexRules(equalRule1, anotherRule));
    }

    @Test
    public void hasEqualRules_ComplexRules() throws Exception {
        Condition condition1 = createCondition(RuleFactory.TIME, StandardOperation.EXISTS, "time");
        Condition condition2 = createCondition(RuleFactory.ENV, StandardOperation.IS, "env");
        Condition condition3 = createCondition(RuleFactory.MODEL, StandardOperation.IN, "model");
        Condition condition4 = createCondition(RuleFactory.IP, StandardOperation.LIKE, "ip");
        Condition anotherCondition = createCondition(RuleFactory.IP, StandardOperation.LTE, "someString");

        Rule equalRule1 = constructComplexRule(Lists.newArrayList(condition1, condition2, condition3, condition4));
        Rule equalRule2 = constructComplexRule(Lists.newArrayList(condition4, condition2, condition3, condition1));
        Rule anotherRule = constructComplexRule(Lists.newArrayList(anotherCondition, condition1, condition2, condition3));

        assertTrue(RuleUtil.equalComplexRules(equalRule1, equalRule2));
        assertFalse(RuleUtil.equalComplexRules(equalRule1, anotherRule));
    }

    @Test
    public void getDuplicateFixedArgListItems() throws Exception {
        String notAListValue = "string";
        List<String> listWithUniqueValues = Arrays.asList("1", "2", "3");
        List<String> listWithDuplicateValues = Arrays.asList("1", "1", "3", "2", "2");

        assertTrue(RuleUtil.getDuplicateFixedArgListItems(notAListValue).isEmpty());
        assertTrue(RuleUtil.getDuplicateFixedArgListItems(listWithUniqueValues).isEmpty());
        assertTrue(equalCollections(RuleUtil.getDuplicateFixedArgListItems(listWithDuplicateValues), Arrays.asList("1", "2")));
    }

    private Rule createRule(Relation relation, boolean negated) {
        Rule result = createRule(RuleFactory.IP, StandardOperation.IS, "1", null);
        result.setRelation(relation);
        result.setNegated(negated);

        return result;
    }

    private Rule createRule(FreeArg freeArg, Operation operation, String fixedArg, Relation relation) {
        return createRule(createCondition(freeArg, operation, fixedArg), relation, false);
    }

    private Rule createAndRule(Condition condition) {
        return createRule(condition, Relation.AND, false);
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

    private Rule constructComplexRule(List<Condition> conditions) {
        Rule rootRule = createAndRule(conditions.get(0));
        rootRule.setRelation(null);
        List<Rule> rootRuleCompoundParts = new ArrayList<>();
        Rule innerCompoundRule = createAndRule(conditions.get(1));
        innerCompoundRule.setCompoundParts(Collections.singletonList(createAndRule(conditions.get(2))));
        rootRuleCompoundParts.add(innerCompoundRule);
        rootRuleCompoundParts.add(createAndRule(conditions.get(3)));
        rootRule.setCompoundParts(rootRuleCompoundParts);

        return rootRule;
    }

    private static boolean equalCollections(Collection a, Collection b) {
        if (a.size() == b.size()) {
            return CollectionUtils.intersection(a, b).size() == a.size();
        }

        return false;
    }

    private Rule createRule(Condition condition) {
        Rule result = new Rule();
        result.setCondition(condition);

        return result;
    }

    private Condition createCondition(Object fixedArgValue) {
        return new Condition(RuleFactory.MODEL, StandardOperation.LIKE, FixedArg.from(fixedArgValue));
    }

}
