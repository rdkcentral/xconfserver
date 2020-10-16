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
package com.comcast.apps.hesperius.ruleengine.main.impl;

import com.comcast.apps.hesperius.ruleengine.main.api.IRule;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * @see @link com.comcast.apps.hesperius.ruleengine.main.api.IRule
 */
public class GenericRule<T extends Condition, U extends GenericRule<T, U>> implements IRule<T, U> {
    protected boolean negated;
    protected Relation relation;
    protected T condition;
    protected List<U> compoundParts;

    @Override
    public boolean isNegated() {
        return negated;
    }

    @Override
    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    @Override
    public Relation getRelation() {
        return relation;
    }

    @Override
    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    @Override
    public T getCondition() {
        return condition;
    }

    @Override
    public void setCondition(T condition) {
        this.condition = condition;
    }

    @Override
    public List<U> getCompoundParts() {
        return compoundParts;
    }

    @Override
    public void setCompoundParts(List<U> compoundParts) {
        this.compoundParts = compoundParts;
    }

    @JsonIgnore
    @Override
    public boolean isCompound() {
        return condition == null; // also must be compoundParts != null && !compoundParts.isEmpty(). validated in RuleProcessor.validate()
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenericRule)) return false;

        GenericRule that = (GenericRule) o;

        if (negated != that.negated) return false;
        if (compoundParts != null ? !compoundParts.equals(that.compoundParts) : that.compoundParts != null)
            return false;
        if (condition != null ? !condition.equals(that.condition) : that.condition != null) return false;
        if (relation != that.relation) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (negated ? 1 : 0);
        result = 31 * result + (relation != null ? relation.ordinal() : 0);
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (compoundParts != null ? compoundParts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder();
        res.append("(");
        if (isCompound()) {
            res.append(getCompoundParts().get(0))
                    .append(" ")
                    .append(getCompoundParts().get(1).getRelation())
                    .append(" ")
                    .append("(");
            if (getCompoundParts().size() > 2) {
                U head = getCompoundParts().get(1);
                List<U> tail = getCompoundParts().subList(1, getCompoundParts().size());
                res.append("(");
                while (tail.size() > 1) {
                    res.append(head)
                            .append(" ");
                    final Relation rel = tail.get(1).getRelation();
                    res.append(rel != null ? rel : head.getRelation())
                            .append(" ");

                    tail = tail.subList(1, tail.size());
                    head = tail.get(0);
                }
                res.append(head);
                res.append(")");

            } else {
                res.append(getCompoundParts().get(1));
            }
            res.append(")");
        } else {
            res.append(isNegated() ? " NOT " : "")
                    .append(getCondition());
        }
        res.append(")");

        return res.toString();
    }
}
