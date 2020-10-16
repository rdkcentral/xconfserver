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
package com.comcast.apps.hesperius.ruleengine.domain.additional;

import com.comcast.apps.hesperius.ruleengine.domain.RuleUtils;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.Time;
import com.comcast.apps.hesperius.ruleengine.domain.standard.BaseEvaluator;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Evaluators;
import com.google.common.base.Function;

import java.util.Collection;
import java.util.Map;

public class AuxEvaluators extends Evaluators {

    private static enum InstanceHolder {
        INSTANCE;
        AuxEvaluators EVALUATORS = new AuxEvaluators();
    }

    public static AuxEvaluators get() {
        return InstanceHolder.INSTANCE.EVALUATORS;
    }

    {
        // ================================    TimeEvaluators    ==================================================
        add(RuleUtils.generateComparingEvaluators(AuxFreeArgType.TIME, Time.class, new Function<String, Time>() {
            @Override
            public Time apply(String input) {
                return Time.parse(input);
            }
        }));

        // ==============================    IpAddressEvaluators    ===============================================
        add(new BaseEvaluator(AuxFreeArgType.IP_ADDRESS, StandardOperation.IS, IpAddress.class) {
            @Override
            protected boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return IpAddress.parse(freeArgValue).equals(fixedArgValue);
            }
        });

        add(new BaseEvaluator(AuxFreeArgType.IP_ADDRESS, StandardOperation.IN, IpAddressGroup.class) {
            @Override
            protected boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return ((IpAddressGroup)fixedArgValue).isInRange(freeArgValue);
            }
        });

        add(new BaseEvaluator(AuxFreeArgType.IP_ADDRESS, StandardOperation.PERCENT, Double.class) {
            @Override
            protected boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return RuleUtils.fitsPercent(freeArgValue, (Double)fixedArgValue);
            }
        });

        // ==============================    MacAddressEvaluators    ==============================================
        add(new BaseEvaluator(AuxFreeArgType.MAC_ADDRESS, StandardOperation.IS, MacAddress.class) {
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return MacAddress.parse(freeArgValue).equals(fixedArgValue);
            }
        });

        add(new BaseEvaluator(AuxFreeArgType.MAC_ADDRESS, StandardOperation.LIKE, String.class) {
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return MacAddress.parse(freeArgValue).matches((String) fixedArgValue);
            }
        });

        add(new BaseEvaluator(AuxFreeArgType.MAC_ADDRESS, StandardOperation.IN, Collection.class) { // Collection<MacAddress>
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                MacAddress freeArgMac = new MacAddress(freeArgValue);

                for (Object value : (Collection<MacAddress>)fixedArgValue) {
                    if (value instanceof Map) {
                        for (Object macValue : ((Map) value).values()) {
                           if (freeArgMac.equals(macValue)) {
                               return true;
                           }
                        }
                    } else {
                        if (freeArgMac.equals(value)) {
                            return true;
                        }
                    }

                }
                return false;
            }
        });

        add(new BaseEvaluator(AuxFreeArgType.MAC_ADDRESS, StandardOperation.ANY_MATCHED, Collection.class) { // Collection<String>
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                MacAddress freeArgMac = new MacAddress(freeArgValue);
                for (String value : (Collection<String>)fixedArgValue) {
                    if (freeArgMac.matches(value)) {
                        return true;
                    }
                }
                return false;
            }
        });

        add(new BaseEvaluator(AuxFreeArgType.MAC_ADDRESS, StandardOperation.PERCENT, Double.class) {
            @Override
            public boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
                return RuleUtils.fitsPercent(freeArgValue, (Double)fixedArgValue);
            }
        });
    }
}
