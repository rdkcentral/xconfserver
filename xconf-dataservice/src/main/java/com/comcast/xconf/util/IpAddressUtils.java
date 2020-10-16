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
 * Author: Stanislav Menshykov
 * Created: 12/18/15  9:49 AM
 */
package com.comcast.xconf.util;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;

public class IpAddressUtils {

    public static Boolean isValidIpAddress(String stringIp) {
        try {
            final IpAddress ipAddress = new IpAddress(stringIp);
            if (!ipAddress.isIpv6()) {
                String[] parts = stringIp.split("\\.", -1);
                if (parts.length != 4)
                    return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    public static Boolean isValidIpv6Address(String stringIp) {
        if (isValidIpAddress(stringIp)) {
            return new IpAddress(stringIp).isIpv6();
        }

        return false;
    }

    public static Boolean isValidIpv4Address(String stringIp) {
        if (isValidIpAddress(stringIp)) {
            return !(new IpAddress(stringIp).isIpv6());
        }

        return false;
    }
}
