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
 * <p>
 * Author: mdolina
 * Created: 6/21/17  2:50 PM
 */
package com.comcast.xconf.firmware;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class ApplicationType {
    public static final String STB = "stb";
    public static final String XHOME = "xhome";
    public static final String RDKCLOUD = "rdkcloud";
    public static final String ALL = "all";

    public static String get(String applicationType) {
        if (StringUtils.isBlank(applicationType) || ApplicationType.STB.equals(applicationType)) {
            return STB;
        } else if (ApplicationType.XHOME.equals(applicationType)) {
            return XHOME;
        } else if (ApplicationType.RDKCLOUD.equals(applicationType)) {
            return RDKCLOUD;
        }
        return applicationType;
    }

    public static boolean equals(String type1, String type2) {
        return StringUtils.equals(get(type1), get(type2));
    }

    public static boolean isValid(String applicationType) {
        return getAll().contains(applicationType);
    }

    private static List<String> getAll() {
        return Lists.newArrayList(STB, XHOME, RDKCLOUD);
    }
}
