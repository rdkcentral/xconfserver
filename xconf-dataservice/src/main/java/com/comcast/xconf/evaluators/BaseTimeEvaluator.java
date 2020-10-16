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
 * Author: Igor Kostrov
 * Created: 2/7/2016
*/
package com.comcast.xconf.evaluators;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.IConditionEvaluator;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.ValidationException;
import com.comcast.apps.hesperius.ruleengine.main.api.internal.IReadonlyCondition;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.util.TimeUtil;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class BaseTimeEvaluator implements IConditionEvaluator {

    protected final Function<Integer, Boolean> evaluation;

    protected final Operation operation;

    protected BaseTimeEvaluator(Operation operation, Function<Integer, Boolean> evaluation) {
        this.operation = operation;
        this.evaluation = evaluation;
    }

    @Override
    public boolean evaluate(IReadonlyCondition condition, Map<String, String> context) {
        String freeArgValue = context.get(condition.getFreeArg().getName());
        if (StringUtils.isEmpty(freeArgValue)) {
            return false;
        }
        String fixedArg = (String) condition.getFixedArg().getValue();
        if (condition.getFreeArg().getName().equals(StbContext.TIME)) {
            LocalTime freeArgActualValue = TimeUtil.parseDateTime(freeArgValue);
            int comparisonResult = freeArgActualValue.compareTo(LocalTime.parse(fixedArg));
            return evaluation.apply(comparisonResult);
        }
        return evaluation.apply(freeArgValue.compareTo(fixedArg));
    }

    @Override
    public FreeArgType getFreeArgType() {
        return StandardFreeArgType.STRING;
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public Collection<Class<?>> getFixedArgClasses() {
        Class<?> clazz = String.class;
        return Arrays.asList(new Class<?>[]{clazz});
    }

    @Override
    public void validate(Object fixedArgValue) {
        if (fixedArgValue == null) {
            throw new ValidationException("fixedArgValue is null");
        }
        if (! (fixedArgValue instanceof String)) {
            throw new ValidationException("Unsupported fixedArgClass: " + fixedArgValue.getClass().getName());
        }
    }
}
