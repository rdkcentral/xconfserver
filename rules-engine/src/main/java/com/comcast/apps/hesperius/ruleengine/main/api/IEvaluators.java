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
package com.comcast.apps.hesperius.ruleengine.main.api;

/**
 * Holder of condition evaluators {@link IConditionEvaluator}. may be passed to the constructor of RuleProcessor
 * {@link com.comcast.apps.hesperius.ruleengine.main.impl.RuleProcessor}
 *
 * @see com.comcast.apps.hesperius.ruleengine.main.impl.Evaluators
 */
public interface IEvaluators extends Iterable<IConditionEvaluator> {

    void add(IConditionEvaluator evaluator);

    void add(IEvaluators evaluators);

    IConditionEvaluator getEvaluator(FreeArgType type, Operation operation);
}
