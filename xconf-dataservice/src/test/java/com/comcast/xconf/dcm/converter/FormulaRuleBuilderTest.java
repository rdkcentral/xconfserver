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
package com.comcast.xconf.dcm.converter;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class FormulaRuleBuilderTest {

    FormulaRuleBuilder formulaRuleBuilder = new FormulaRuleBuilder();

    @Test
    public void buildConditionFromCollection() throws Exception {
        List<String> fixedArgValues = Lists.newArrayList("envValue");
        Condition condition = formulaRuleBuilder.buildConditionFromCollection(RuleFactory.ENV, fixedArgValues);

        assertEquals(StandardOperation.IS, condition.getOperation());

        fixedArgValues.add("anotherEnvValue");
        condition = formulaRuleBuilder.buildConditionFromCollection(RuleFactory.ENV, fixedArgValues);

        assertEquals(StandardOperation.IN, condition.getOperation());
    }
}