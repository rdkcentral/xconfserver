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
 *
 * Author: Igor Kostrov
 * Created: 11/1/2019
 *******************************************************************************/
package com.comcast.xconf.evaluators;

import com.comcast.apps.hesperius.ruleengine.domain.additional.AuxEvaluators;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardEvaluators;
import com.comcast.apps.hesperius.ruleengine.main.api.IConditionEvaluator;
import com.comcast.apps.hesperius.ruleengine.main.api.IRuleProcessor;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Evaluators;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.hesperius.ruleengine.main.impl.RuleProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class RuleProcessorFactory {

    @Autowired
    private List<IConditionEvaluator> customEvaluators;

    private IRuleProcessor<Condition,Rule> ruleProcessor;

    @PostConstruct
    private void init() {
        Evaluators evaluators = StandardEvaluators.get();
        evaluators.add(AuxEvaluators.get());
        for (IConditionEvaluator customEvaluator : customEvaluators) {
            evaluators.add(customEvaluator);
        }
        ruleProcessor = new RuleProcessor<>(evaluators);
    }

    public IRuleProcessor<Condition, Rule> get() {
        return ruleProcessor;
    }

}
