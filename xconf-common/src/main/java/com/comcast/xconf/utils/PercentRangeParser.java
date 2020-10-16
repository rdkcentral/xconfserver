/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.utils;

import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.rfc.PercentRange;

public class PercentRangeParser {

    public static boolean isNumeric(String strNum) {
        return strNum.matches("-?\\d+(\\.\\d+)?");
    }

    public static PercentRange parsePercentRangeCondition(Condition condition) {
        String percentRange = (String) condition.getFixedArg().getValue();
        return parsePercentRange(percentRange);
    }

    public static Double convertToRange(String rangePart, String wholePercentRange) {
        if (isNumeric(rangePart)) {
            return Double.valueOf(rangePart);
        }
        throw new RuleValidationException("Percent range " + wholePercentRange + " is not valid");
    }

    public static PercentRange parsePercentRange(String percentRange) {
        String[] splitRange = percentRange.trim().split("-");
        if (splitRange.length < 2) {
            throwRangeFormatException(percentRange);
        }
        PercentRange convertedRange = new PercentRange();
        try {
            convertedRange.setStartRange(convertToRange(splitRange[0], percentRange));
            convertedRange.setEndRange(convertToRange(splitRange[1], percentRange));
            return convertedRange;
        } catch (NumberFormatException | IndexOutOfBoundsException e){
            throwRangeFormatException(percentRange);
        }
        return convertedRange;
    }

    private static void throwRangeFormatException(String wholePercentRange) throws RuleValidationException {
        throw new RuleValidationException("Range format exception " + wholePercentRange + ", format pattern is: startRange-endRange");
    }
}
