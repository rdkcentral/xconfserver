/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.evaluators;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.BaseTestUtils;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchOperationEvaluatorTest {

    @Test
    public void evaluateInternalPositiveCases() throws Exception {
        Condition condition = BaseTestUtils.createCondition(
                RuleFactory.MODEL,
                RuleFactory.MATCH,
                FixedArg.from("AAA?A"));

        Map<String, String> context = new HashMap<>();
        context.put("model", "AAABA");
        MatchOperationEvaluator evaluator = new MatchOperationEvaluator();

        assertTrue(evaluator.evaluate(condition, context));

        context.put("model", "AAABAA");

        assertFalse(evaluator.evaluate(condition, context));

        condition = BaseTestUtils.createCondition(
                new FreeArg(StandardFreeArgType.STRING, "firmwareVersion"),
                RuleFactory.MATCH,
                FixedArg.from("version.aa.*"));
        context.put("firmwareVersion", "version.aa.aa.dddd");

        assertTrue(evaluator.evaluate(condition, context));

        context.put("firmwareVersion", "version.a");

        assertFalse(evaluator.evaluate(condition, context));
    }

}