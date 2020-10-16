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
 * Author: Alexander Binkovsky
 * Created: 3/25/2015  1:32 PM
 */
package com.comcast.apps.hesperius.ruleengine.parser;

import com.comcast.apps.hesperius.ruleengine.RuleEngine;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;

import java.util.HashSet;
import java.util.Set;

class Token {
    private String value;
    private TokenType type;
    private byte priority = 0;

    enum TokenType {
        ARG, OPERATION, RELATION, BRACKET_LEFT, BRACKET_RIGHT
    }

    public Token(String value, TokenType type) {
        this.value = value;
        this.type = type;
    }

    public Token(String value, TokenType type, byte priority) {
        this.value = value;
        this.type = type;
        this.priority = priority;
    }

    public Token(TokenType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }

    public byte getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return value != null ? value : type.name();
    }

    static class RelationToken extends Token {

        public RelationToken(String value) {
            super(value.toUpperCase(), TokenType.RELATION, relationPriority(value));
        }

        static boolean isRelation(String str) {
            return Relation.AND.name().equalsIgnoreCase(str) || Relation.OR.name().equalsIgnoreCase(str);
        }

        static byte relationPriority(String relation) {
            return Relation.AND.name().equalsIgnoreCase(relation) ? (byte)20 : (byte)10;
        }
    }

    static class OperationToken extends Token {
        static final String NOT_OPERATOR = "NOT";
        private boolean negated;

        private static Set<Operation> supportedOperations = new HashSet<>(RuleEngine.getSupportedOperations());
        static {
            supportedOperations.add(Operation.forName(NOT_OPERATOR));
        }

        static boolean isOperation(String str) {
            return supportedOperations.contains(Operation.forName(str.toUpperCase()));
        }

        public OperationToken(String value) {
            super(value.toUpperCase(), TokenType.OPERATION, (byte)50);
        }

        public void setNegated(boolean negated) {
            this.negated = negated;
        }

        public boolean isNegated() {
            return negated;
        }
    }
}
