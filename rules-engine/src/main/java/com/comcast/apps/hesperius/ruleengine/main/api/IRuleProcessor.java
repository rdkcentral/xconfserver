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
 * Created: 6/2/14
 */
package com.comcast.apps.hesperius.ruleengine.main.api;

import com.comcast.apps.hesperius.ruleengine.main.api.internal.IReadonlyRule;
import com.comcast.apps.hesperius.ruleengine.main.api.internal.IReadonlyCondition;

import java.util.Map;

/**
 * One of the most important interfaces in this project. Provides rules matching and validation.
 */
public interface IRuleProcessor<T extends IReadonlyCondition, U extends IReadonlyRule<T, U>> {

    <U1 extends U> Iterable<U1> filter(Iterable<U1> rules, Map<String, String> context);

    <U1 extends U> U1 find(Iterable<U1> rules, Map<String, String> context);

    <U1 extends U>boolean evaluate(U1 rule, Map<String, String> context);

    <U1 extends U> void validate(U1 rule) throws RuleValidationException;
}
