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
package com.comcast.apps.hesperius.ruleengine.main.impl;

import com.comcast.apps.hesperius.ruleengine.main.api.IConditionEvaluator;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.IEvaluators;
import com.comcast.apps.hesperius.ruleengine.main.api.IRuleProcessor;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.api.internal.IReadonlyRule;
import com.comcast.apps.hesperius.ruleengine.main.api.internal.IReadonlyCondition;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Iterator;
import java.util.Map;

public class RuleProcessor<T extends IReadonlyCondition, U extends IReadonlyRule<T, U>> implements IRuleProcessor<T, U> {

    protected final IEvaluators evaluators;

    public RuleProcessor(IEvaluators evaluators) {
        this.evaluators = evaluators;
    }

    @Override
    public <U1 extends U> Iterable<U1> filter(Iterable<U1> rules, final Map<String, String> context) {
        return Iterables.filter(rules, new Predicate<U>() {
            @Override
            public boolean apply(U input) {
                return evaluate(input, context);
            }
        });
    }

    /**
     * May return null if no rule matched
     */
    @Override
    public <U1 extends U> U1 find(Iterable<U1> rules, final Map<String, String> context) {
        return Iterables.tryFind(rules, new Predicate<U>() {
            @Override
            public boolean apply(U input) {
                return evaluate(input, context);
            }
        }).orNull();
    }

    @Override
    public <U1 extends U> void validate(U1 rule) throws RuleValidationException {
        validateRule(rule);
        if (!rule.isCompound()) {
            validateCondition(rule.getCondition());
            return;
        }
        for (U compoundPart : rule.getCompoundParts()) {
            validate(compoundPart);
        }
    }

    @Override
    public <U1 extends U> boolean evaluate(U1 rule, Map<String, String> context) {
        if (!rule.isCompound()) {
            return evaluate(rule.getCondition(), rule.isNegated(), context);
        }
        Iterator<U> compoundPartsIt = rule.getCompoundParts().iterator();
        boolean result = evaluate(compoundPartsIt.next(), context); // rule.isCompound() so compoundPartsIt.next() should not throw NoSuchElementException
        while (compoundPartsIt.hasNext()) {
            U compoundPart = compoundPartsIt.next();
            Relation relation = compoundPart.getRelation();
            if (result && relation == Relation.OR) {
                continue;
            }
            if (!result && relation == Relation.AND) {
                break;
            }
            result = evaluate(compoundPart, context);
        }
        if (rule.isNegated()) {
            result = !result;
        }
        return result;
    }

    protected boolean evaluate(T rule, boolean negation, Map<String, String> context) {
        FreeArg freeArg = rule.getFreeArg();
        IConditionEvaluator evaluator = evaluators.getEvaluator(freeArg.getType(), rule.getOperation());
        boolean result = evaluator.evaluate(rule, context);
        if (negation) {
            result = !result;
        }
        return result;
    }

    protected void validateRule(U rule) throws RuleValidationException {
        validateNotNull(rule, "rule");
        boolean hasCondition = (rule.getCondition() != null);
        boolean hasCompoundParts = (rule.getCompoundParts() != null && !rule.getCompoundParts().isEmpty());
        if (hasCondition == hasCompoundParts) { // exactly one must be true and the other false. both true or both false are incorrect
            throw new RuleValidationException("Rule must have either condition or compoundParts. condition is "
                + (hasCondition ? "present" : "absent") + ", compoundParts are " + (hasCompoundParts ? "present" : "absent"));
        }
    }

    protected void validateCondition(T condition) throws RuleValidationException {
        validateNotNull(condition, "condition");
        validateNotNull(condition.getFreeArg(), "freeArg");

        FreeArgType freeArgType = validateNotNull(condition.getFreeArg().getType(), "freeArgType");
        Operation operation = validateNotNull(condition.getOperation(), "operation");

        IConditionEvaluator evaluator = evaluators.getEvaluator(freeArgType, operation);
        if (evaluator == null) {
            throw new RuleValidationException("No evaluator found for FreeArgType = " + freeArgType + ", Operation " + operation);
        }
        evaluator.validate(condition.getFixedArg() != null ? condition.getFixedArg().getValue() : null);
    }

    private <V> V validateNotNull(V obj, String objName) throws RuleValidationException {
        if (obj == null) {
            throw new RuleValidationException(objName + " is null");
        }
        return obj;
    }

    public boolean hasEvaluatorFor(final FreeArgType type, final Operation operation) {
        return evaluators.getEvaluator(type, operation) != null;
    }
}
