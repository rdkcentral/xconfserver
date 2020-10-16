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
package com.comcast.apps.hesperius.ruleengine.domain.standard;

import com.comcast.apps.hesperius.ruleengine.domain.RuleUtils;
import com.comcast.apps.hesperius.ruleengine.main.api.IConditionEvaluator;
import com.comcast.apps.hesperius.ruleengine.main.impl.Evaluators;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;

import com.google.common.base.Function;

import java.util.Collection;
import java.util.Random;

public class StandardEvaluators extends Evaluators {

    private static enum InstanceHolder {
        INSTANCE;
        StandardEvaluators EVALUATORS = new StandardEvaluators();
    }

    public static StandardEvaluators get() {
        return InstanceHolder.INSTANCE.EVALUATORS;
    }

    @Override
    public IConditionEvaluator getEvaluator(FreeArgType type, Operation operation) {
        if (operation.equals(StandardOperation.EXISTS)) {
            if (type.equals(StandardFreeArgType.VOID)) { // illegal usage. return null and expect IRuleProcessor.validate() will throw an exception
                return null;
            }
            type = StandardFreeArgType.ANY;
        }
        return super.getEvaluator(type, operation);
    }

    {
        // =============================    ExistsEvaluator    ======================================================
        add(new BaseEvaluator(StandardFreeArgType.ANY, StandardOperation.EXISTS, Void.class) {
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return freeArgValue != null;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void validate(Object fixedArgValue) throws RuleValidationException {
                if (fixedArgValue != null) {
                    throw new RuleValidationException("fixedArgValue for StandardOperation.EXISTS is expected to be null. found: " + fixedArgValue);
                }
            }
        });

        // ================================    StringEvaluators    ==================================================
        add(new BaseEvaluator(StandardFreeArgType.STRING, StandardOperation.IS, String.class) {
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return freeArgValue.equals(fixedArgValue);
            }
        });

        add(new BaseEvaluator(StandardFreeArgType.STRING, StandardOperation.LIKE, String.class) {
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return freeArgValue.matches((String) fixedArgValue);
            }
        });

        add(new BaseEvaluator(StandardFreeArgType.STRING, StandardOperation.IN, Collection.class) { // Collection<String>
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return ((Collection)fixedArgValue).contains(freeArgValue);
            }
        });

        add(new BaseEvaluator(StandardFreeArgType.STRING, StandardOperation.ANY_MATCHED, Collection.class) { // Collection<String>
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                for (String value : (Collection<String>)fixedArgValue) {
                    if (freeArgValue.matches(value)) {
                        return true;
                    }
                }
                return false;
            }
        });

        add(new BaseEvaluator(StandardFreeArgType.STRING, StandardOperation.PERCENT, Double.class) {
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return RuleUtils.fitsPercent(freeArgValue, Double.valueOf(String.valueOf(fixedArgValue)));
            }
        });

        // ================================    LongEvaluators    ==================================================
        add(RuleUtils.generateComparingEvaluators(StandardFreeArgType.LONG, Long.class, new Function<String, Long>() {
            @Override
            public Long apply(String input) {
                try {
                    return Long.valueOf(input);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }));

        add(new BaseEvaluator(StandardFreeArgType.LONG, StandardOperation.IN, Collection.class) { // Collection<Long>
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                final Long freeArgLong;
                try {
                    freeArgLong = Long.valueOf(freeArgValue);
                } catch (NumberFormatException e) {
                    return false;
                }
                return ((Collection)fixedArgValue).contains(freeArgLong);
            }
        });

        add(new BaseEvaluator(StandardFreeArgType.LONG, StandardOperation.PERCENT, Double.class) {
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                final Long freeArgValueLong;
                try {
                    freeArgValueLong = Long.parseLong(freeArgValue);
                } catch (NumberFormatException e) {
                    return false;
                }
                return RuleUtils.fitsPercent(freeArgValueLong, Double.valueOf(String.valueOf(fixedArgValue)));
            }
        });

        // ================================    VoidEvaluators    ==================================================
        add(new BaseEvaluator(StandardFreeArgType.VOID, StandardOperation.IS, Boolean.class) {
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return (Boolean)fixedArgValue;
            }
        });

        add(new BaseEvaluator(StandardFreeArgType.VOID, StandardOperation.PERCENT, Double.class) {
            private final Random random = new Random();
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return random.nextDouble() * 100 < Double.valueOf(String.valueOf(fixedArgValue));
            }
        });

    }
}
