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

package com.comcast.xconf.validators;

import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.ConditionInfo;

import java.util.HashSet;
import java.util.Set;

public class CommonRuleValidator {

    public static void checkFreeArgExists(final Set<ConditionInfo> conditionInfos, final FreeArg freeArg, final Operation operation) {
        if (!freeArgExists(conditionInfos, freeArg, operation)) {
            throw new RuleValidationException(freeArg.getName() + " with " + operation +  " operation is required");
        }
    }

    public static void checkFreeArgExists(final Set<ConditionInfo> conditionInfos, final FreeArg freeArg) {
        if (!freeArgExists(conditionInfos, freeArg)) {
            throw new RuleValidationException(freeArg.getName() + " does not exist");
        }
    }

    public static Set<ConditionInfo> getConditionInfos(final Iterable<Condition> conditions) {
        final Set<ConditionInfo> result = new HashSet<>();
        for (final Condition condition : conditions) {
            result.add(new ConditionInfo(condition.getFreeArg(), condition.getOperation()));
        }
        return result;
    }

    public static boolean freeArgExists(final Set<ConditionInfo> conditionInfos, final FreeArg freeArg) {
        for (final ConditionInfo conditionInfo : conditionInfos) {
            if (conditionInfo.getFreeArg().equals(freeArg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean freeArgExists(final Set<ConditionInfo> conditionInfos, final FreeArg freeArg, final Operation operation) {
        for (final ConditionInfo conditionInfo : conditionInfos) {
            if (conditionInfo.getFreeArg().equals(freeArg) && conditionInfo.getOperation().equals(operation)) {
                return true;
            }
        }
        return false;
    }


}
