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
 *  Created: 6:50 PM
 */
package com.comcast.xconf.evaluators;

import com.comcast.apps.hesperius.ruleengine.domain.standard.BaseEvaluator;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

@Component
public class MatchOperationEvaluator extends BaseEvaluator {
    public MatchOperationEvaluator() {
        super(StandardFreeArgType.STRING, RuleFactory.MATCH, String.class);
    }

    @Override
    protected boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
        return FilenameUtils.wildcardMatch(freeArgValue, (String) fixedArgValue);
    }
}
