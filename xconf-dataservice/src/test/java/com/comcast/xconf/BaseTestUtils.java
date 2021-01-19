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
 *  Author: mdolina
 *  Created: 7:52 PM
 */
package com.comcast.xconf;

import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;

public class BaseTestUtils {

    public static Rule createRule(FreeArg freeArg, Operation operation, FixedArg fixedArg, Relation relation) {
        Rule rule = new Rule();
        rule.setCondition(createCondition(freeArg, operation, fixedArg));
        rule.setRelation(relation);
        return rule;
    }

    public static Condition createCondition(FreeArg freeArg, Operation operation, FixedArg fixedArg) {
        Condition condition = new Condition();
        condition.setFreeArg(freeArg);
        condition.setOperation(operation);
        condition.setFixedArg(fixedArg);
        return condition;
    }
}
