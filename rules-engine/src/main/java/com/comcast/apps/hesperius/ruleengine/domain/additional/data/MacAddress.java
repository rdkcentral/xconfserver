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
 * Created: 5/27/14
 */
package com.comcast.apps.hesperius.ruleengine.domain.additional.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Simple value class for mac addresses that makes sure address is valid and
 * normalizes to colon separated all uppercase format: 00:21:E9:E3:20:EE
 *
 * ToStringSerializer + constructor with string argument allows serialization / deserialization as macAddress string.
 */
@JsonSerialize(using = ToStringSerializer.class)
public final class MacAddress {

    private static final String MAC_ADDRESS_PATTERN = "^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$";

    private String macAddress;

    /**
     * @throws IllegalArgumentException if {@link #isValid(String)} returns false
     */
    public MacAddress(String macAddress) throws IllegalArgumentException {
        this.macAddress = normalize(macAddress);
        if (!isValid(macAddress)) {
            throw new IllegalArgumentException("invalid mac address: " + macAddress);
        }
    }

    public static MacAddress parse(String macAddress) {
        return new MacAddress(macAddress);
    }

    /**
     * normalizes to colon separated all uppercase format: 00:21:E9:E3:20:EE.
     * DOES NOT check if string is valid mac address, just takes whatever you
     * send and changes it to uppercase colon-separated format.
     */
    public static String normalize(String macAddress) {
        if (macAddress == null) {
            return "";
        }
        macAddress = macAddress.replaceAll(":", "").replaceAll("-", "").replaceAll("\\.", "").toUpperCase().trim();
        final StringBuilder result = new StringBuilder();
        for (int i = 0, l = macAddress.length(); i < l; i++) {
            if (i % 2 == 1 && i < l - 1) {
                result.append(macAddress.substring(i, i + 1) + ":");
            } else {
                result.append(macAddress.substring(i, i + 1));
            }
        }
        return result.toString();
    }

    /**
     * Returns true if macAddress is a valid mac address in format
     * 3D:F2:C9:A6:B3:4F or 3D-F2-C9-A6-B3-4F or 3DF2C9A6B34F.
     */
    public static boolean isValid(String macAddress) {
        if (macAddress == null || macAddress.trim().length() == 0) {
            return false;
        }
        return normalize(macAddress).trim().toUpperCase().matches(MAC_ADDRESS_PATTERN);
    }

    public boolean matches(String regex) {
        return macAddress.matches(regex);
    }

    @Override
    public int hashCode() {
        return macAddress.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MacAddress)) {
            return false;
        }
        return macAddress.equals(((MacAddress) obj).macAddress);
    }

    @Override
    public String toString() {
        return macAddress;
    }
}
