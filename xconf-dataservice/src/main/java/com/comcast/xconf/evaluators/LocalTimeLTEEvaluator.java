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
 * Author: obaturynskyi
 * Created: 06.08.2014  16:03
 */
package com.comcast.xconf.evaluators;

import com.comcast.apps.hesperius.ruleengine.domain.standard.BaseEvaluator;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.google.common.base.Function;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class LocalTimeLTEEvaluator extends BaseEvaluator {

    protected static final  Function<String, LocalTime> freeArgConverter = new Function<String, LocalTime>() {
        @Nullable
        @Override
        public LocalTime apply(@Nullable String s) {
            return ISODateTimeFormat.dateTimeParser().parseDateTime(s).toLocalTime();
        }
    };

    protected static final Function<Integer, Boolean> evaluation = new Function<Integer, Boolean>() {
        @Override
        public Boolean apply(Integer integer) {
            return (integer.intValue() <= 0);
        }
    };

    public LocalTimeLTEEvaluator(){
        super(FreeArgType.forName("LOCAL_TIME"), StandardOperation.LTE, String.class);
    }

    /**
     * for operation IS, GT, GTE, LT, LTE fixedArg type is expected to be the same as actual freeArg type, i.e. T
     * except VOID IS, where freeArgType = VOID, operation = IS, fixedArgClass = Boolean means "IS TRUE" or "IS FALSE"
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean evaluateInternal(String freeArgValue, Object fixedArgValue) {
        LocalTime freeArgActualValue = freeArgConverter.apply(freeArgValue);
        int comparisonResult = freeArgActualValue.compareTo(LocalTime.parse((String)fixedArgValue));
        return evaluation.apply(comparisonResult);
    }
}
