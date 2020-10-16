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
 * Author: slavrenyuk
 * Created: 6/6/14
 */
package com.comcast.apps.hesperius.ruleengine;

import com.comcast.apps.hesperius.ruleengine.domain.additional.AuxFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.Time;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.IRuleProcessor;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class RulesTest {

    private static final Logger log = LoggerFactory.getLogger(RulesTest.class);

    private IRuleProcessor<Condition, Rule> processor = RuleEngine.getRuleProcessor();

    private ObjectReader reader = new ObjectMapper().reader(Rule.class);
    private ObjectWriter writer = new ObjectMapper().writer();

    private FreeArg day = new FreeArg(StandardFreeArgType.STRING, "day");
    private FreeArg age = new FreeArg(StandardFreeArgType.LONG, "age");
    private FreeArg noArg =  new FreeArg(StandardFreeArgType.VOID, "");
    private FreeArg time =  new FreeArg(AuxFreeArgType.TIME, "time");
    private Rule saturday;
    private Rule sunday;
    private Rule afterOnePm;
    private Rule beforeTwoPm;

    {
        saturday = new Rule();
        saturday.setCondition(new Condition(day, StandardOperation.IS, FixedArg.from("Saturday")));
        sunday = new Rule();
        sunday.setCondition(new Condition(day, StandardOperation.IS, FixedArg.from("Sunday")));
        afterOnePm = new Rule();
        afterOnePm.setCondition(new Condition(time, StandardOperation.GT, FixedArg.from(Time.parse("13:00"))));
        beforeTwoPm = new Rule();
        beforeTwoPm.setCondition(new Condition(time, StandardOperation.LT, FixedArg.from(Time.parse("14:00"))));
    }

    @Test
    public void testRuleProcessor() throws IOException {

        Rule weekend = new Rule(); weekend.setCompoundParts(Arrays.asList(saturday, or(sunday)));

        Rule weekday = not(weekend);

        Rule baby = new Rule();
        baby.setCondition(new Condition(age, StandardOperation.LT, FixedArg.from(5L)));

        Rule oldman = new Rule();
        oldman.setCondition(new Condition(age, StandardOperation.GT, FixedArg.from(65L)));

        Rule alwaysTrue = new Rule();
        alwaysTrue.setCondition(new Condition(noArg, StandardOperation.PERCENT, FixedArg.from(100.0)));

        Rule isFalse = new Rule();
        isFalse.setCondition(new Condition(noArg, StandardOperation.IS, FixedArg.from(false)));

        Rule vacation = new Rule();
        vacation.setCondition(new Condition(new FreeArg(StandardFreeArgType.ANY, "vacation"), StandardOperation.EXISTS, null));

        Rule midnight = new Rule();
        midnight.setCondition(new Condition(time, StandardOperation.IS, FixedArg.from(Time.parse("00:00"))));

        Rule dinnerTime = new Rule();
        dinnerTime.setCompoundParts(Arrays.asList(afterOnePm, and(beforeTwoPm)));

        Rule localhost = new Rule();
        localhost.setCondition(new Condition(new FreeArg(AuxFreeArgType.IP_ADDRESS, "ip"), StandardOperation.IS, FixedArg.from(IpAddress.parse("127.0.0.1"))));

        Rule notAtWork = new Rule();
        notAtWork.setCompoundParts(Arrays.asList(vacation, or(weekend), or(dinnerTime), or(baby), or(oldman)));

        Map<Rule, String> rulesToNames = ImmutableMap.<Rule,String>builder()
                .put(weekend, "weekend")
                .put(weekday, "weekday")
                .put(baby, "baby")
                .put(oldman, "oldman")
                .put(alwaysTrue, "alwaysTrue")
                .put(isFalse, "isFalse")
                .put(vacation, "vacation")
                .put(midnight, "midnight")
                .put(dinnerTime, "dinnerTime")
                .put(localhost, "localhost")
                .put(notAtWork, "notAtWork")
                .build();

        Map<String, String> context = ImmutableMap.<String, String>builder()
                .put("day", "Friday")
                .put("age", "2")
                .put("vacation", "")
                .put("time", "13:30")
                .put("ip", "192.168.0.1")
                .build();
        Iterable<Rule> matched = processor.filter(rulesToNames.keySet(), context);
        verify(rulesToNames, matched, weekday, baby, vacation, dinnerTime, alwaysTrue, notAtWork);

        context = ImmutableMap.<String, String>builder()
                .put("day", "Saturday")
                .put("age", "24")
                .put("vacation", "")
                .put("time", "00:00")
                .put("ip", "127.0.0.1")
                .build();
        matched = processor.filter(rulesToNames.keySet(), context);
        verify(rulesToNames, matched, weekend, vacation, midnight, localhost, alwaysTrue, notAtWork);

        context = ImmutableMap.<String, String>builder()
                .put("day", "Monday")
                .put("age", "24")
                .put("time", "16:00")
                .put("ip", "135.5.7.98")
                .build();
        matched = processor.filter(rulesToNames.keySet(), context);
        verify(rulesToNames, matched, weekday, alwaysTrue); // notAtWork must not match

        log.info("testRuleProcessor passed");
    }

    @Test
    public void testRelations() throws Exception {
        Rule complexRule = new Rule();
        List<Rule> compoundParts = new ArrayList<>();
        compoundParts.add(createLikeRule("adsqa27", "test1.adsqa27"));
        compoundParts.add(or(not(createLikeRule("adsqa28", "[asd|qwe].*"))));
        compoundParts.add(and(not(createLikeRule("adsqa29", ".*ads.*qa.*29"))));
        compoundParts.add(or(createLikeRule("adsqa30", "(12|c)*")));
        compoundParts.add(and(createLikeRule("adsqa31", "[hc]at")));
        complexRule.setCompoundParts(compoundParts);

        boolean evaluate = processor.evaluate(complexRule, getNonMatchingContext());
        Assert.assertFalse(evaluate);

        evaluate = processor.evaluate(complexRule, getMatchingContext());
        Assert.assertTrue(evaluate);
    }

    private Rule createLikeRule(String key, String value) {
        Rule rule = new Rule();
        FreeArg freeArg = new FreeArg(StandardFreeArgType.STRING, key);
        rule.setCondition(new Condition(freeArg, StandardOperation.LIKE, FixedArg.from(value)));
        return rule;
    }

    private Map<String, String> getNonMatchingContext() {
        Map<String, String> map = new HashMap<>();
        map.put("adsqa21", "10000");
        map.put("adsqa22", "333334");
        map.put("adsqa23", "666667");
        map.put("adsqa24", "44444445");
        map.put("adsqa25", "23");
        return map;
    }

    private Map<String, String> getMatchingContext() {
        Map<String, String> map = new HashMap<>();
        map.put("adsqa21", "10000");
        map.put("adsqa22", "333334");
        map.put("adsqa23", "666667");
        map.put("adsqa27", "test1.adsqa27");
        map.put("adsqa31", "hat");
        return map;
    }

    @Test
    public void testJsonMapping() throws IOException {
        Rule original = saturday;
        Rule mapped = reader.readValue(writer.writeValueAsString(original));
        Assert.assertTrue(deepEquals(original, mapped));
        log.info(writer.writeValueAsString(original));

        original = new Rule();
        original.setCondition(new Condition(new FreeArg(AuxFreeArgType.MAC_ADDRESS, "mac"), StandardOperation.IN,
                FixedArg.from(Arrays.asList(MacAddress.parse("00:11:22:AA:BB:CC"), MacAddress.parse("66:77:88:DD:EE:FF")))));
        mapped = reader.readValue(writer.writeValueAsString(original));
        Assert.assertTrue(deepEquals(original, mapped));
        log.info(writer.writeValueAsString(original));

        log.info("testJsonMapping passed");
    }

    @Test
    public void testIllegalOperationAndArgumentType() {
        Rule invalidRule = new Rule();
        invalidRule.setCondition(new Condition(noArg, StandardOperation.IN, FixedArg.from(Arrays.asList("A", "B")))); // VOID IN
        verifyValidation(invalidRule);

        invalidRule = new Rule();
        invalidRule.setCondition(new Condition(noArg, StandardOperation.LIKE, FixedArg.from(""))); // VOID LIKE
        verifyValidation(invalidRule);

        invalidRule = new Rule();
        invalidRule.setCondition(new Condition(day, StandardOperation.GT, FixedArg.from(""))); // STRING GT
        verifyValidation(invalidRule);

        invalidRule = new Rule();
        invalidRule.setCondition(new Condition(age, StandardOperation.LIKE, FixedArg.from(""))); // LONG LIKE
        verifyValidation(invalidRule);

        invalidRule.setCondition(new Condition(new FreeArg(StandardFreeArgType.ANY, ""), StandardOperation.IS, FixedArg.from(""))); // ANY fits only EXISTS
        verifyValidation(invalidRule);

        log.info("testIllegalOperationAndArgumentType passed");
    }

    @Test
    public void testIllegalFixedArgType() {
        Rule invalidRule = new Rule();
        invalidRule.setCondition(new Condition(age, StandardOperation.IS, FixedArg.from(12))); // actual Integer required Long ! no int->long auto conversion
        verifyValidation(invalidRule);

        invalidRule.setCondition(new Condition(time, StandardOperation.IS, null)); // actual null required Time
        verifyValidation(invalidRule);

        invalidRule.setCondition(new Condition(age, StandardOperation.IS, FixedArg.from("12"))); // actual String required Long
        verifyValidation(invalidRule);

        invalidRule.setCondition(new Condition(day, StandardOperation.IS, FixedArg.from(123L))); // actual Long required String
        verifyValidation(invalidRule);

        log.info("testIllegalFixedArgType passed");
    }

    @Test
    public void testInvalidCompoundState() {

        verifyValidation(new Rule()); // both condition and compoundParts absent

        Rule invalidRule = new Rule();
        invalidRule.setCondition(new Condition(noArg, StandardOperation.IS, FixedArg.from(false)));
        Rule r = new Rule();
        r.setCondition(new Condition(noArg, StandardOperation.IS, FixedArg.from(true)));
        invalidRule.setCompoundParts(Arrays.asList(r, r, r));
        verifyValidation(invalidRule);

        log.info("testInvalidCompoundState passed");
    }

    @Test
    public void testValidRule() {
        Rule validRule = new Rule();
        validRule.setCondition(new Condition(time, StandardOperation.IS, FixedArg.from(Time.parse("00:00"))));
        try {
            processor.validate(validRule);
        } catch (Exception ex) { // including ValidationException
            Assert.fail("Rule is valid. Unexpected exception while validating: " + ex.toString());
        }
        validRule = new Rule();
        validRule.setCompoundParts(Arrays.asList(afterOnePm, and(beforeTwoPm)));
        try {
            processor.validate(validRule);
        } catch (Exception ex) { // including ValidationException
            Assert.fail("Rule is valid. Unexpected exception while validating: " + ex.toString());
        }
        log.info("testValidRule passed");
    }

    private void verifyValidation(Rule invalidRule) throws AssertionError {
        try {
            processor.validate(invalidRule);
            Assert.fail();
        } catch (RuleValidationException ex) {
            log.info("expected ValidationException: " + ex.toString());
        } catch (Throwable t) {
            Assert.fail(t.toString());
        }
    }

    private void verify(final Map<Rule, String> rulesToNames, final Iterable<Rule> matched, Rule... expected) throws AssertionError {
        List<Rule> actual = Lists.newArrayList(matched);
        Assert.assertEquals(actual.size(), expected.length);
        log.info("matched size is correct: " + actual.size());
        for (Rule expectedRule : expected) {
            Assert.assertTrue(actual.contains(expectedRule));
            log.info("matched rule is correct: " + rulesToNames.get(expectedRule));
        }
    }

    private boolean deepEquals(Rule r1, Rule r2) {
        if (r1.isCompound() != r2.isCompound()) {
            return false;
        }
        if (!r1.isCompound()) {
            return (r1.isNegated() == r2.isNegated())
                    && bothNullOrEqual(r1.getRelation(), r2.getRelation())
                    && areEqual(r1.getCondition(), r2.getCondition());
        }
        if (r1.getCompoundParts().size() != r2.getCompoundParts().size()) {
            return false;
        }
        Iterator<Rule> r1It = r1.getCompoundParts().iterator();
        for (Rule r2Part : r2.getCompoundParts()) {
            Rule r1Part = r1It.next();
            if (!deepEquals(r1Part, r2Part)) {
                return false;
            }
        }
        return true;
    }

    private boolean bothNullOrEqual(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }

    private boolean areEqual(Condition c1, Condition c2) {
        return c1.getOperation().equals(c2.getOperation())
                && c1.getFreeArg().getName().equals(c2.getFreeArg().getName())
                && c1.getFreeArg().getType().equals(c2.getFreeArg().getType())
                && bothNullOrEqual(c1.getFixedArg().getValue(), c2.getFixedArg().getValue());
    }

    private Rule not(Rule rule) {
        Rule result = copy(rule);
        result.setNegated(!rule.isNegated());
        return result;
    }

    private Rule or(Rule rule) {
        Rule result = copy(rule);
        result.setRelation(Relation.OR);
        return result;
    }

    private Rule and(Rule rule) {
        Rule result = copy(rule);
        result.setRelation(Relation.AND);
        return result;
    }


    private Rule copy(Rule rule) {
        Rule result = new Rule();
        result.setNegated(rule.isNegated());
        result.setRelation(rule.getRelation());
        if (!rule.isCompound()) {
            result.setCondition(copy(rule.getCondition()));
            return result;
        }
        result.setCompoundParts(new ArrayList<Rule>());
        for (Rule compoundPart : rule.getCompoundParts()) {
            result.getCompoundParts().add(copy(compoundPart));
        }
        return result;
    }

    private Condition copy(Condition condition) {
        return new Condition(copy(condition.getFreeArg()), condition.getOperation(), condition.getFixedArg());
    }

    private FreeArg copy(FreeArg freeArg) {
        return new FreeArg(freeArg.getType(), freeArg.getName());
    }
}
