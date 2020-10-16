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
 * Author: rdolomansky
 * Created: 3/17/16  7:30 PM
 */
package com.comcast.xconf;

import org.apache.commons.lang3.StringUtils;

public class ApiVersionUtils {

    public static boolean greaterOrEqual(final String version, final float value) {
        if (StringUtils.isNoneBlank(version)) {
            try {
                return Float.valueOf(version).floatValue() >= value;
            } catch (NumberFormatException n) { }
        }
        return false;
    }
}
