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
 * Created: 2/8/2016
*/
package com.comcast.xconf.evaluators;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.google.common.base.Function;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class TimeLTEEvaluator extends BaseTimeEvaluator {

    protected static final Function<Integer, Boolean> evaluation = new Function<Integer, Boolean>() {
        @Nullable
        @Override
        public Boolean apply(@Nullable Integer integer) {
            return (integer.intValue() <= 0);
        }
    };

    public TimeLTEEvaluator() {
        super(StandardOperation.LTE, evaluation);
    }

}
