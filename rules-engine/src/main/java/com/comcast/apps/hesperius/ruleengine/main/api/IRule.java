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
 * Created: 6/1/14
 */
package com.comcast.apps.hesperius.ruleengine.main.api;

import com.comcast.apps.hesperius.ruleengine.main.api.internal.IReadonlyRule;

import java.util.List;

/**
 * Please see condition {@link ICondition} javadoc at first.
 *
 * Recursive definition: rule is condition or list of rules.
 * Rule may be negated {@link #isNegated()} (boolean not, java ! operator). If rule is a list of rules
 * (i.e. has compound parts) than each compound part (except the first) has boolean relation (and, or) {@link #getRelation()}
 * with previous rule from the list.
 *
 * This IRule interface provides getter for both condition and compound parts. But their presence is mutually exclusive.
 * See below how this contract may be ensured.
 *
 * Let's define some terms.
 * "compound parts are absent" means {@link #getCompoundParts()} returns null
 * "condition is absent" means {@link #getCondition()} returns null
 *
 * Rule may be in one of four states:
 * - "not initialized" state, when both condition and compound parts are absent
 * - "illegal" state, when both condition and compound parts are present
 * - "single condition" state, when condition is present and compound parts are absent
 * - "compound" state, when condition is absent and compound parts are present
 *
 * "rule has single condition" means the same as "rule in single condition state"
 * "rule is compound" means the same as "rule in compound state"
 *
 * "invalid rule" means rule is not initialized or illegal. If rule is invalid than {@link IRuleProcessor#validate(IReadonlyRule)}
 * throws {@link RuleValidationException}. Result of {@link #isCompound()} on invalid rule is undefined.
 * If invalid rule is passed to RuleProcessor for processing than result is undefined {@link IRuleProcessor#find(Iterable, java.util.Map)},
 * {@link IRuleProcessor#filter(Iterable, java.util.Map)}
 *
 * Examples (for simplicity only "is" operation is used):
 * 1) A is B
 * Rule has single condition "A is B"
 *
 * 2) not A is B
 * Rule has single condition "A is B", {@link #isNegated()} returns true
 *
 * 3) A is B and C is D
 * Rule is compound with two compound parts. Each compound part has single condition. Single condition of the
 * first part is "A is B", of the second "C is D". {@link #getRelation()} of the second part returns {@link Relation#AND}
 *
 * 4) A is B or not D is E and not (F is G or not H is I)
 *
 * Okay, let's do it. We have IRule topLevel = ...       |  IRule thirdPart = topLevel.getCompoundParts().get(2)
 * topLevel.isNegated()        -> false                  |  thirdPart.isNegated()        -> true
 * topLevel.getRelation()      -> null                   |  thirdPart.getRelation()      -> Relation.AND
 * topLevel.getCondition()     -> null                   |  thirdPart.getCondition()     -> null
 * topLevel.getCompoundParts() -> list of three elements |  thirdPart.getCompoundParts() -> list of two elements
 * topLevel.isCompound()       -> true                   |  thirdPart.isCompound()       -> true
 *                                                       |
 * IRule firstPart = topLevel.getCompoundParts().get(0)  |  IRule firstSubPart = thirdPart.getCompoundParts().get(0)
 * firstPart.isNegated()        -> false                 |  firstSubPart.isNegated()        -> false
 * firstPart.getRelation()      -> null                  |  firstSubPart.getRelation()      -> null
 * firstPart.getCondition()     -> A is B                |  firstSubPart.getCondition()     -> F is G
 * firstPart.getCompoundParts() -> null                  |  firstSubPart.getCompoundParts() -> null
 * firstPart.isCompound()       -> false                 |  firstSubPart.isCompound()       -> false
 *                                                       |
 * IRule secondPart = topLevel.getCompoundParts().get(1) |  IRule secondSubPart = thirdPart.getCompoundParts().get(1)
 * secondPart.isNegated()        -> true                 |  secondSubPart.isNegated()        -> true
 * secondPart.getRelation()      -> Relation.OR          |  secondSubPart.getRelation()      -> Relation.OR
 * secondPart.getCondition()     -> D is E               |  secondSubPart.getCondition()     -> H is I
 * secondPart.getCompoundParts() -> null                 |  secondSubPart.getCompoundParts() -> null
 * secondPart.isCompound()       -> false                |  secondSubPart.isCompound()       -> false
 *
 */
public interface IRule<T extends ICondition, U extends IRule<T, U>> extends IReadonlyRule<T, U> {

    /**
     * @return boolean negation
     */
    boolean isNegated();

    void setNegated(boolean negated);

    /**
     * boolean relation with previous rule. previous rule is placed before this rule in list {@link #getCompoundParts()}.
     * null if this rule is a top level or is the first element of rule list {@link #getCompoundParts()}
     */
    Relation getRelation();

    void setRelation(Relation relation);

    /**
     * @return condition if rule has single condition (see interface javadoc), null otherwise
     */
    T getCondition();

    void setCondition(T condition);

    /**
     * @return compound parts if rule is compound (see interface javadoc), null otherwise
     */
    List<U> getCompoundParts();

    void setCompoundParts(List<U> compoundParts);

    /**
     * see interface javadoc, assuming rule is valid
     * if true {@link #getCompoundParts()} returns non empty list, {@link #getCondition()} returns null
     * otherwise {@link #getCondition()} returns non null value, {@link #getCompoundParts()} returns null
     */
    boolean isCompound();
}
