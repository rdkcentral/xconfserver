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
 * Created by Yury Stagit on 12/9/2016.
 */

package com.comcast.xconf.util;

import com.comcast.xconf.Applicationable;
import com.comcast.xconf.XRule;
import com.comcast.xconf.evaluators.RuleProcessorFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;

@Component
public class EvaluatorHelper {

    @Autowired
    private RuleProcessorFactory ruleProcessorFactory;

    public <E extends XRule & Applicationable> Iterable<E> processEntityRules(Iterable<E> entityRuleList, final Map<String, String> context) {
        return Iterables.filter(entityRuleList, new Predicate<E>() {
            @Override
            public boolean apply(@Nullable E rule) {
                return ApplicationType.equals(context.get(LogUploaderContext.APPLICATION), rule.getApplicationType())
                        && ruleProcessorFactory.get().evaluate(rule.getRule(), context);
            }
        });
    }

    public <E extends XRule & Applicationable> E getEntityRuleForContext(Iterable<E> entityRuleList, final Map<String, String> context) {
        Iterable<E> presentRules = processEntityRules(entityRuleList, context);
        return getMaxRule(presentRules);
    }

    public <E extends XRule> E getMaxRule(Iterable<E> presentRules) {
        return presentRules.iterator().hasNext() ? Ordering.from(new Comparator<E>() {
            @Override
            public int compare(E r1, E r2) {
                return RuleUtil.compareRules(r1.getRule(), r2.getRule());
            }
        }).max(presentRules) : null;
    }

}
