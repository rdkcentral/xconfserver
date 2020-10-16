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
package com.comcast.apps.hesperius.ruleengine.domain.standard;

import com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.IConditionEvaluator;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.api.internal.IReadonlyCondition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @see IConditionEvaluator
 */
public abstract class BaseEvaluator implements IConditionEvaluator {

    protected final FreeArgType freeArgType;

    protected final Operation operation;

    protected final Collection<Class<?>> fixedArgClasses;

    protected BaseEvaluator(FreeArgType freeArgType, Operation operation, Class<?>...fixedArgClasses) {
        this.freeArgType = freeArgType;
        this.operation = operation;
        this.fixedArgClasses = Collections.unmodifiableCollection(Arrays.asList(fixedArgClasses));
    }

    protected abstract boolean evaluateInternal(String freeArgValue, Object fixedArgValue);

    @Override
    public boolean evaluate(IReadonlyCondition condition, Map<String, String> context) {
        String freeArgValue = null;
        if (!freeArgType.equals(StandardFreeArgType.VOID)) {
            freeArgValue = context.get(condition.getFreeArg().getName());
            if (freeArgValue == null || (!freeArgType.equals(StandardFreeArgType.ANY) && freeArgValue.isEmpty())) {
                return false;
            }
        }
        return evaluateInternal(freeArgValue, condition.getFixedArg() != null ? condition.getFixedArg().getValue() : null);
    }

    @Override
    public FreeArgType getFreeArgType() {
        return freeArgType;
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public Collection<Class<?>> getFixedArgClasses() {
        return fixedArgClasses;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(Object fixedArgValue) throws RuleValidationException {
        if (fixedArgValue == null) {
            throw new RuleValidationException("fixedArgValue is null");
        }
        Class<?> actual = fixedArgValue.getClass();
        for (Class<?> expected : fixedArgClasses) {
            if (expected.isAssignableFrom(actual)) {
                return;
            }
        }
        throw new RuleValidationException(String.format("Unsupported fixedArgClass: " + actual.getName()));
    }
}
