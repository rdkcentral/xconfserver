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
package com.comcast.apps.hesperius.ruleengine.main.impl;

import com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.IConditionEvaluator;
import com.comcast.apps.hesperius.ruleengine.main.api.IEvaluators;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Evaluators implements IEvaluators {
    private static final Logger LOGGER = LoggerFactory.getLogger(Evaluators.class);

    protected final Map<FreeArgType, Map<Operation, IConditionEvaluator>> evaluators = new HashMap<FreeArgType, Map<Operation, IConditionEvaluator>>();

    public void add(final IConditionEvaluator evaluator, boolean allowOverrides) {
        Map<Operation, IConditionEvaluator> evaluatorsByOperation = evaluators.get(evaluator.getFreeArgType());
        if (evaluatorsByOperation == null) {
            evaluatorsByOperation = new HashMap<Operation, IConditionEvaluator>();
            evaluators.put(evaluator.getFreeArgType(), evaluatorsByOperation);
        }

        final Operation op = evaluator.getOperation();
        if (allowOverrides && evaluatorsByOperation.containsKey(op)) {
            LOGGER.warn("overwriting evaluator for operation {} so that it won't further be available", op);
        }
        evaluatorsByOperation.put(op, evaluator);
    }

    @Override
    public void add(IConditionEvaluator evaluator) {
       add(evaluator, true);
    }

    public void add(final IEvaluators evaluators, boolean allowOverrides) {
        for (IConditionEvaluator evaluator : evaluators) {
            add(evaluator, allowOverrides);
        }
    }

    public void add(final IEvaluators evaluators) {
        add(evaluators, true);
    }

    @Override
    public IConditionEvaluator getEvaluator(FreeArgType type, Operation operation) {
        Map<Operation, IConditionEvaluator> evaluatorsByOperation = evaluators.get(type);
        if (evaluatorsByOperation == null) {
            return null;
        }
        return evaluatorsByOperation.get(operation);
    }

    @Override
    public Iterator<IConditionEvaluator> iterator() {
        return Iterators.concat(Iterators.transform(evaluators.values().iterator(),
                new Function<Map<Operation, IConditionEvaluator>, Iterator<IConditionEvaluator>>() {
                    @Override
                    public Iterator<IConditionEvaluator> apply(Map<Operation, IConditionEvaluator> input) {
                        return input.values().iterator();
                    }
                }));
    }
}
