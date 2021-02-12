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
 * Created: 10/12/14  5:33 AM
 */
package com.comcast.xconf.estbfirmware.util;

import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.estbfirmware.SingletonFilterValue;
import com.comcast.xconf.firmware.RuleAction;
import org.apache.commons.lang.StringUtils;

// TODO: get rid of incompatibility in upcoming releases and remove this util class
public class LogsCompatibilityUtils {
    public static String getRuleTypeInfo(Object ruleOrFilter) {
        // TODO: this cutting of "Value" suffix is a workaround done to make logs compatible with XConf v.1.
        return StringUtils.substringBeforeLast(ruleOrFilter.getClass().getSimpleName(), "Value");
    }

    public static String getRuleIdInfo(Object ruleOrFilter) {
        if (ruleOrFilter instanceof FirmwareRule) {
            return ((FirmwareRule)ruleOrFilter).getId();
        } else if (ruleOrFilter instanceof SingletonFilterValue) {
            return "SINGLETON_" + StringUtils.substringBeforeLast(((SingletonFilterValue) ruleOrFilter).getId(), "_VALUE");
        } else if (ruleOrFilter instanceof RuleAction) {
            return "DistributionPercentInRuleAction";
        }

        return null;
    }
}
