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
 * Created: 3/25/2015  1:30 PM
 */
package com.comcast.apps.hesperius.ruleengine.parser;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Parser {
    private String str;
    private Rule rule;
    private LinkedList<Token> infixQueue = new LinkedList<>();
    private LinkedList<Token> postfixQueue = new LinkedList<>();

    public Parser (String str) {
        this.str = str;
        rule = parse();
    }

    public Rule getRule() {
        return rule;
    }

    private Rule parse() {
        tokenize();
        toPostfix();

        return evaluate();
    }

    private void tokenize() {
        StringTokenizer tokenizer = new StringTokenizer(str, Separator.getTokenizerSeparatorsCharset(), true);
        while (tokenizer.hasMoreTokens()) {
            String current = tokenizer.nextToken();
            if (Separator.SPACE.value.equals(current))
                continue;

            if (Separator.ROUND_BRACKET_LEFT.value.equals(current)) {
                infixQueue.addLast(new Token(Token.TokenType.BRACKET_LEFT));
            } else if (Separator.ROUND_BRACKET_RIGHT.value.equals(current)) {
                infixQueue.addLast(new Token(Token.TokenType.BRACKET_RIGHT));
            } else if (Separator.QUOTE.value.equals(current)) {
                infixQueue.addLast(new Token(getPhrase(tokenizer, Separator.QUOTE.value), Token.TokenType.ARG));
            } else if (Separator.SQUARE_BRACKET_LEFT.value.equals(current)) {
                infixQueue.addLast(new Token(getPhrase(tokenizer, Separator.SQUARE_BRACKET_RIGHT.value), Token.TokenType.ARG));
            } else if (Token.OperationToken.isOperation(current)) {
                Token.OperationToken token = new Token.OperationToken(current);
                infixQueue.addLast(token);
            } else if (Token.RelationToken.isRelation(current)) {
                infixQueue.addLast(new Token.RelationToken(current));
            } else {
                infixQueue.addLast(new Token(current, Token.TokenType.ARG));
            }
        }
    }

    private String getPhrase(StringTokenizer tokenizer, String phraseEnd) {
        StringBuilder value = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String innerToken = tokenizer.nextToken();
            if (phraseEnd.equals(innerToken)) {
                break;
            }
            value.append(innerToken);
        }
        return value.toString();
    }

    private void toPostfix() {
        LinkedList<Token> shuntingStack = new LinkedList<>();
        LinkedList<Token> queue = new LinkedList<>(infixQueue);

        Token t;
        boolean isNegatedOperation = false;
        while (queue.size() > 0) {
            t = queue.removeFirst();
            if (t.getType() == Token.TokenType.ARG) {
                postfixQueue.addLast(t);
            } else if (t.getType() == Token.TokenType.RELATION) {
                while (shuntingStack.size() > 0 && shuntingStack.getLast().getPriority() >= t.getPriority()) {
                    postfixQueue.addLast(shuntingStack.removeLast());
                }
                shuntingStack.addLast(t);
            } else if (t.getType() == Token.TokenType.OPERATION) {
                if (Token.OperationToken.NOT_OPERATOR.equals(t.getValue())) {
                    isNegatedOperation = true;
                } else {
                    if (isNegatedOperation) {
                        ((Token.OperationToken)t).setNegated(true);
                        isNegatedOperation = false;
                    }
                    shuntingStack.addLast(t);
                }
            } else if (t.getType() == Token.TokenType.BRACKET_LEFT) {
                shuntingStack.addLast(t);
            } else if (t.getType() == Token.TokenType.BRACKET_RIGHT) {
                if (shuntingStack.size() == 0) {
                    throw new IllegalStateException("Unexpected right bracket in " + str);
                }
                while (shuntingStack.getLast().getType() != Token.TokenType.BRACKET_LEFT) {
                    postfixQueue.addLast(shuntingStack.removeLast());

                    if (shuntingStack.size() == 0) {
                        throw new IllegalStateException("Unexpected right bracket in " + str);
                    }
                }
                shuntingStack.removeLast();
            }
        }

        while (shuntingStack.size() > 0) {
            if (shuntingStack.getLast().getType() != Token.TokenType.OPERATION
                    && shuntingStack.getLast().getType() != Token.TokenType.RELATION) {
                throw new IllegalStateException("Failed to parse expression " + str);
            } else {
                postfixQueue.addLast(shuntingStack.removeLast());
            }
        }
    }

    private Rule evaluate() {
        LinkedList<Token> queue = new LinkedList<>(postfixQueue);
        LinkedList<String> args = new LinkedList<>();
        LinkedList<Rule> rules = new LinkedList<>();
        Token token;
        while (queue.size() > 0) {
            token = queue.removeFirst();
            if (token.getType() == Token.TokenType.ARG) {
                args.addLast(token.getValue());
            } else if (token.getType() == Token.TokenType.RELATION) {
                Rule right = rules.removeLast();
                Rule left = rules.removeLast();

                rules.addLast(buildCompoundRule(left, right, token));
            } else if (token.getType() == Token.TokenType.OPERATION) {
                String fixedArg = args.removeLast();
                String freeArg = args.removeLast();

                rules.addLast(buildRule(fixedArg, freeArg, (Token.OperationToken)token));
            }
        }

        if (rules.size() != 1) {
            throw new IllegalStateException("Failed to parse rule " + str);
        }

        return rules.getLast();
    }

    private Rule buildRule(String fixedArg, String freeArg, Token.OperationToken operationToken) {
        Rule rule = new Rule();
        Operation operation = Operation.forName(operationToken.getValue());
        FixedArg fixedArgument = (operation.equals(StandardOperation.IN))
                ? FixedArg.from(Splitter.on(",").trimResults(CharMatcher.anyOf(Separator.getTrimCharset())).splitToList(fixedArg))
                : FixedArg.from(fixedArg);
        rule.setCondition(
                new Condition(
                        new FreeArg(StandardFreeArgType.STRING, freeArg), operation, fixedArgument));
        rule.setNegated(operationToken.isNegated());

        return rule;
    }

    private Rule buildCompoundRule(Rule left, Rule right, Token relation) {
        Rule compound = new Rule();
        compound.setCompoundParts(new ArrayList<Rule>());
        compound.getCompoundParts().add(left);
        compound.getCompoundParts().add(right);
        right.setRelation(Relation.valueOf(relation.getValue()));

        return compound;
    }

    private static enum Separator {
        ROUND_BRACKET_LEFT("("), ROUND_BRACKET_RIGHT(")"), QUOTE("\""), SQUARE_BRACKET_LEFT("["), SQUARE_BRACKET_RIGHT("]"), SPACE(" ");

        private String value;
        private Separator(String value) {
            this.value = value;
        }

        private static String getTrimCharset() {
            return SPACE.value + QUOTE.value;
        }

        private static String getTokenizerSeparatorsCharset() {
            StringBuilder sb = new StringBuilder();
            for (Separator item : Separator.values()) {
                sb.append(item);
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

