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
 * Created: 10/08/2015  1:30 PM
 */
package com.comcast.xconf.dcm.converter;

import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.StringTokenizer;

@Component
public class RuleExpressionParser {

    @Autowired
    private FormulaRuleBuilder builder;

    public Rule getRule(String str, FormulaDataObject dataObject) {
        LinkedList<Token> infixQueue = tokenize(str);

        return evaluate(infixQueue, str, dataObject);
    }

    private LinkedList<Token> tokenize(String str) {
        LinkedList<Token> infixQueue = new LinkedList<>();
        StringTokenizer tokenizer = new StringTokenizer(str, Separator.getTokenizerSeparatorsCharset(), true);
        while (tokenizer.hasMoreTokens()) {
            String current = tokenizer.nextToken();
            if (Separator.SPACE.value.equals(current)
                    || Separator.ROUND_BRACKET_LEFT.value.equals(current)
                    || Separator.ROUND_BRACKET_RIGHT.value.equals(current)
                    || Separator.SQUARE_BRACKET_LEFT.value.equals(current))
                continue;

            if (Separator.QUOTE.value.equals(current)) {
                infixQueue.addLast(new Token(getPhrase(tokenizer, Separator.QUOTE.value), Token.TokenType.ARG));
            } else if (Token.OperationToken.isOperation(current)) {
                Token.OperationToken token = new Token.OperationToken(current);
                infixQueue.addLast(token);
            } else if (Token.RelationToken.isRelation(current)) {
                infixQueue.addLast(new Token.RelationToken(current));
            } else {
                infixQueue.addLast(new Token(current, Token.TokenType.ARG));
            }
        }
        return infixQueue;
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

    private Rule evaluate(LinkedList<Token> postfixQueue, String str, FormulaDataObject dataObject) {
        LinkedList<Token> queue = new LinkedList<>(postfixQueue);
        Token token;
        LinkedList<Rule> compoundParts = new LinkedList();
        while (queue.size() > 0) {
            token = queue.removeFirst();
            if (token.getType() == Token.TokenType.ARG) {
                addRule(compoundParts, token, dataObject);
            } else if (token.getType() == Token.TokenType.RELATION) {
                Rule rule = new Rule();
                rule.setRelation(Relation.valueOf(token.getValue()));
                compoundParts.addLast(rule);
            } else if (token.getType() == Token.TokenType.OPERATION && Token.OperationToken.NOT_OPERATOR.equals(token.getValue())) {
                if (!compoundParts.isEmpty() && compoundParts.getLast().getRelation() != null) {
                    compoundParts.getLast().setNegated(true);
                } else {
                    Rule rule = new Rule();
                    rule.setNegated(true);
                    compoundParts.add(rule);
                }
            }
        }

        if (compoundParts.size() == 0) {
            throw new IllegalStateException("Failed to parse expression " + str);
        }

        if (compoundParts.size() > 1) {
            Rule resultRule = new Rule();
            resultRule.setCompoundParts(compoundParts);
            return resultRule;
        } else {
            Rule resultRule = compoundParts.getLast();
            resultRule.setRelation(null);
            return resultRule;
        }
    }

    private Rule buildRule(String arg, FormulaDataObject dataObject) {
        return builder.buildRule(dataObject, arg);
    }

    private void addRule(LinkedList<Rule> compoundParts, Token token, FormulaDataObject dataObject) {
        if (compoundParts.isEmpty()) {
            compoundParts.add(buildRule(token.getValue(), dataObject));
        } else {
            Rule last = compoundParts.removeLast();
            last.setCondition(buildRule(token.getValue(), dataObject).getCondition());
            compoundParts.addLast(last);
        }
    }
    private enum Separator {
        ROUND_BRACKET_LEFT("("), ROUND_BRACKET_RIGHT(")"), QUOTE("\""), SQUARE_BRACKET_LEFT("["), SQUARE_BRACKET_RIGHT("]"), SPACE(" ");

        private String value;
        Separator(String value) {
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

