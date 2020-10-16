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
 * Created: 5/29/14
 */
package com.comcast.apps.hesperius.ruleengine.main.api;

import com.comcast.apps.hesperius.ruleengine.main.api.internal.IReadonlyCondition;

import java.util.Collection;
import java.util.Map;

/**
 * Each {@link IConditionEvaluator} works wih particular type {@link FreeArgType} and operation {@link Operation}.
 * For example, there is separate evaluator for operation IS and type STRING.
 *
 * Entry point for extension. Must be placed in Evaluators {@link IEvaluators} as inner class or via {@link IEvaluators#add(IConditionEvaluator)}.
 * After that Evaluators should be passed to the constructor of RuleProcessor {@link com.comcast.apps.hesperius.ruleengine.main.impl.RuleProcessor}
 * and then used when processing rules.
 *
 * Evaluates condition {@link #evaluate(IReadonlyCondition, Map)}. Also can validate if fixedArg satisfy this evaluator
 * {@link #validate(Object)}, generally type must be validated but fixedArg may be null.
 *
 * {@link IRuleProcessor#validate(com.comcast.apps.hesperius.ruleengine.main.api.internal.IReadonlyRule)} provides
 * addition validation and also delegates to {@link #validate(Object)}.
 *
 * @see com.comcast.apps.hesperius.ruleengine.domain.standard.BaseEvaluator
 */
public interface IConditionEvaluator {

    /**
     * freeArgValue may be null, which means particular evaluator doesn't need free arg to evaluate the rule
     * (or rule doesn't need free arg to be evaluated)
     */
    boolean evaluate(IReadonlyCondition condition, Map<String, String> context);

    FreeArgType getFreeArgType();

    Operation getOperation();

    Collection<Class<?>> getFixedArgClasses();

    void validate(Object fixedArgValue) throws RuleValidationException;
}
