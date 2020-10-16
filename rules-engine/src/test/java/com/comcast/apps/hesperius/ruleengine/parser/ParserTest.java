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
 * Author: Alexander Binkovsky
 * Created: 3/26/2015  5:28 AM
 */
package com.comcast.apps.hesperius.ruleengine.parser;

import com.comcast.apps.hesperius.ruleengine.RuleEngine;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.IRuleProcessor;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class ParserTest {
    @Test
    public void testSimpleRule_IS() throws Exception {
        Parser testee = new Parser("A IS B");
        Rule result = testee.getRule();

        Assert.assertFalse(result.isCompound());
        Assert.assertEquals(StandardOperation.IS, result.getCondition().getOperation());
        Assert.assertEquals("A", result.getCondition().getFreeArg().getName());
        Assert.assertEquals("B", result.getCondition().getFixedArg().getValue());
    }

    @Test
    public void testComplexExpression() throws Exception {
        String expectedExpression = "((((A IS sprint 44) AND (( NOT status IS open question))) OR (((storyPoints IN [1, 3, 5, 7, 13]) OR ((label IS HydraCore))))) AND ((assignee IS engineer)))";
        String input = "(((A IS \"sprint 44\")and NOT status is \"open question\" )or ( ( storyPoints IN [1,3,5 , 7, \"13\"] ) OR (((( label IS HydraCore))) ) ) ) AND assignee IS engineer";

        Rule result = new Parser(input).getRule();

        Assert.assertEquals(expectedExpression, result.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidExpressionBracketsIssue() throws Exception {
        String input = "(A IS B";
        new Parser(input).getRule();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidComplexExpressionBracketsIssue() throws Exception {
        String input = "(A IS B)) AND (C IS D)";
        new Parser(input).getRule();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidExpressionOperatorNotSupported() throws Exception {
        String input = "A + B";
        new Parser(input).getRule();
    }

    @Test
    public void testSimpleRule_LIKE() throws Exception {
        Parser testee = new Parser("User-Agent LIKE Mozilla.*");
        Rule result = testee.getRule();
        IRuleProcessor<Condition, Rule> processor = RuleEngine.getRuleProcessor();

        Assert.assertTrue(processor.evaluate(result, Collections.singletonMap("User-Agent", "Mozilla/5.0 some text here")));
        Assert.assertTrue(processor.evaluate(result, Collections.singletonMap("User-Agent", "Mozilla")));
        Assert.assertFalse(processor.evaluate(result, Collections.<String, String>emptyMap()));
        Assert.assertFalse(processor.evaluate(result, Collections.singletonMap("User-Agent", "test")));
    }

    @Test
    public void testRelationsPriority() throws Exception {
        Assert.assertEquals("((A IS B) OR (((C IS D) AND ((Z IS Y)))))", new Parser("A IS B OR C IS D AND Z IS Y").getRule().toString());
        Assert.assertEquals("(((A IS B) OR ((C IS D))) AND ((Z IS Y)))", new Parser("(A IS B OR C IS D) AND Z IS Y").getRule().toString());
    }
}
