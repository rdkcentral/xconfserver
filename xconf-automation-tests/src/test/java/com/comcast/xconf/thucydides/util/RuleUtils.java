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
 * Created: 3/25/16  1:21 PM
 */
package com.comcast.xconf.thucydides.util;

import com.beust.jcommander.internal.Lists;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.Environment;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.thucydides.util.common.EnvironmentUtils;
import com.comcast.xconf.thucydides.util.common.GenericNamespacedListUtils;
import com.comcast.xconf.thucydides.util.common.ModelUtils;

import java.util.ArrayList;
import java.util.List;

public class RuleUtils {

    public static Rule createDefaultRule() {
        return createRule(createDefaultCondition(), null);
    }

    public static Rule createRule(Condition condition, Relation relation) {
        Rule result = new Rule();
        result.setCondition(condition);
        result.setRelation(relation);

        return result;
    }

    public static Rule buildEnvModelRule() throws Exception {
        final Environment environment = EnvironmentUtils.createAndSaveDefaultEnvironment();
        final Model model = ModelUtils.createDefaultModel();

        Rule result = new Rule();
        result.setCompoundParts(new ArrayList<Rule>() {{
            add(createRule(createCondition(RuleFactory.ENV, StandardOperation.IS, environment.getId()), null));
            add(createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, model.getId()), Relation.AND));
        }});

        return result;
    }

    public static Rule buildEnvModelRule(String modelId, String envId) throws Exception {
        ModelUtils.createAndSaveModel(modelId);
        EnvironmentUtils.createAndSaveEnvironment(envId);
        Rule rule = new Rule();
        rule.setCompoundParts(Lists.newArrayList(
                createRule(createCondition(RuleFactory.ENV, StandardOperation.IS, envId), null),
                createRule(createCondition(RuleFactory.MODEL, StandardOperation.IS, modelId), Relation.AND)));
        return rule;
    }

    public static Rule buildMacrule() throws Exception {
        final GenericNamespacedList macList = GenericNamespacedListUtils.createAndSaveDefaultMacList();

        Rule result = new Rule();
        result.setCondition(createCondition(RuleFactory.MAC, RuleFactory.IN_LIST, macList.getId()));

        return result;
    }

    public static Condition createDefaultCondition() {
        return createCondition(RuleFactory.IP, StandardOperation.IS, "1.1.1.1");
    }

    public static Condition createCondition(FreeArg freeArg, Operation operation, Object fixedArg) {
        return new Condition(freeArg, operation, FixedArg.from(fixedArg));
    }

    public static List<Rule> createRule(List<Condition> conditions) {
        List<Rule> compoundParts = new ArrayList<>();
        for (int i = 0; i < conditions.size(); i++) {
            Rule rule1 = new Rule();
            if (i > 0) {
                rule1.setRelation(Relation.OR);
            }
            rule1.setCondition(conditions.get(i));
            compoundParts.add(rule1);
        }
        return compoundParts;
    }
}
