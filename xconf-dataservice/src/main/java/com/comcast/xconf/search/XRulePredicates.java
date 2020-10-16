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

package com.comcast.xconf.search;

import com.comcast.xconf.XRule;
import com.comcast.xconf.util.RuleUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class XRulePredicates<T extends XRule> {

    public Predicate<T> byName(String name) {
        return xRule -> Objects.nonNull(xRule)
                && StringUtils.containsIgnoreCase(xRule.getName(), name);
    }

    public Predicate<T> byKey(String key) {
        return xRule -> Objects.nonNull(xRule)
                && RuleUtil.isExistConditionByFreeArgName(xRule.getRule(), key);
    }

    public Predicate<T> byValue(String value) {
        return xRule -> Objects.nonNull(xRule)
                && RuleUtil.isExistConditionByFixedArgValue(xRule.getRule(), value);
    }

    protected List<Predicate<T>> getBaseRulePredicates(ContextOptional context) {
        List<Predicate<T>> predicates = new ArrayList<>();

        context.getName().ifPresent(name -> predicates.add(byName(name)));
        context.getKey().ifPresent(key -> predicates.add(byKey(key)));
        context.getValue().ifPresent(value -> predicates.add(byValue(value)));

        return predicates;
    }
}
