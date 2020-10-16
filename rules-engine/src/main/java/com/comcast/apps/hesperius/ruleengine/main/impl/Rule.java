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
 * Created: 6/6/14
 */
package com.comcast.apps.hesperius.ruleengine.main.impl;

import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;

import java.util.ArrayList;

public class Rule extends GenericRule<Condition, Rule> {

    public static final class Builder {

        private Rule rule;

        public static Builder create() {
            return new Builder(null);
        }

        public static Builder of(final Rule rule) {
            return new Builder(rule);
        }

        public static Builder of(final Condition cond) {
            return new Builder(wrap(cond));
        }

        public Builder and(Condition compound) {
            return and(wrap(compound));
        }

        public Builder and(Rule compound) {
            this.rule = and(this.rule, compound);
            return this;
        }

        public Builder or(Condition compound) {
            return or(wrap(compound));
        }

        public Builder or(Rule compound) {
            this.rule = or(this.rule, compound);
            return this;
        }

        public Rule build() {
            return rule;
        }

        public <R extends Rule> R copyTo(R destination) {
            return Rule.copy(this.rule, destination);
        }

        private Builder(final Rule r) {
            this.rule = copy(r);
        }

        public static Rule not(Condition cond) {
            return not(wrap(cond));
        }

        public static Rule not(Rule rule) {
            rule.setNegated(!rule.isNegated());
            return rule;
        }

        public static Rule copy(Rule rule) {
            if (rule == null) return null;
            Rule result = new Rule();
            result.setNegated(rule.isNegated());
            result.setRelation(rule.getRelation());
            if (!rule.isCompound()) {
                result.setCondition(copy(rule.getCondition()));
                return result;
            }
            result.setCompoundParts(new ArrayList<Rule>());
            for (Rule compoundPart : rule.getCompoundParts()) {
                result.getCompoundParts().add(copy(compoundPart));
            }
            return result;
        }

        public static Condition copy(Condition condition) {
            return new Condition(copy(condition.getFreeArg()), condition.getOperation(), condition.getFixedArg());
        }

        public static FreeArg copy(FreeArg freeArg) {
            return new FreeArg(freeArg.getType(), freeArg.getName());
        }

        private static Rule or(Rule base, Rule compound) {
            return addRelatedCompound(base, compound, Relation.OR);
        }

        private static Rule and(Rule base, Rule compound) {
            return addRelatedCompound(base, compound, Relation.AND);
        }

        private static Rule addRelatedCompound(Rule base, Rule compound, final Relation relation) {
            if(base == null) return compound;

            final Rule result;

            if (!base.isCompound()) {
                result = new Rule();
                result.setCompoundParts(new ArrayList<Rule>());
                result.getCompoundParts().add(copy(base));
            } else {
                result = base;
            }
            compound.setRelation(relation);
            result.getCompoundParts().add(compound);
            return result;
        }


        private static Rule wrap(final Condition cond) {
            final Rule rule = new Rule();
            rule.setCondition(cond);
            return rule;
        }
    }

    /**
     * Copies source into dest overwriting <b>everything {@see Rule} related</b>
     * this method comes handy when dest extends {@see Rule} just to add some specific attributes
     * without altering Rule's structure
     *
     * @param source to copy from
     * @param dest   to copy to
     * @param <R>
     * @return dest populater with rule data from source
     */
    public static <R extends Rule> R copy(Rule source, R dest) {
        final Rule sourceCopy = Builder.copy(source);
        dest.setCompoundParts(null);
        dest.setCondition(null);
        dest.setNegated(source.isNegated());
        if (sourceCopy.isCompound()) {
            dest.setCompoundParts(sourceCopy.getCompoundParts());
        } else {
            dest.setCondition(sourceCopy.getCondition());
        }
        return dest;
    }
}
