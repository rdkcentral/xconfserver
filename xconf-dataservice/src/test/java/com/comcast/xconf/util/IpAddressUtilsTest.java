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
 * Created: 12/15/15  11:58 AM
 */
package com.comcast.xconf.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IpAddressUtilsTest {

    @Test
    public void isValidipAddress() throws Exception {
        assertTrue(IpAddressUtils.isValidIpAddress("1.1.1.1"));
        assertTrue(IpAddressUtils.isValidIpAddress("::1"));
        assertTrue(IpAddressUtils.isValidIpAddress("12::ff"));
        assertTrue(IpAddressUtils.isValidIpAddress("255.255.255.0"));
        assertTrue(IpAddressUtils.isValidIpAddress("12.34.56.78/31"));

        assertFalse(IpAddressUtils.isValidIpAddress("1.1.1.1/35"));
        assertFalse(IpAddressUtils.isValidIpAddress("24"));
        assertFalse(IpAddressUtils.isValidIpAddress("8.8.8"));
        assertFalse(IpAddressUtils.isValidIpAddress("12.12.12.a"));
        assertFalse(IpAddressUtils.isValidIpAddress("12.12.12.256"));
        assertFalse(IpAddressUtils.isValidIpAddress(null));
    }

    @Test
    public void isValidIpv4Address() throws Exception {
        assertTrue(IpAddressUtils.isValidIpv4Address("1.1.1.1"));
        assertTrue(IpAddressUtils.isValidIpv4Address("255.255.255.0"));
        assertTrue(IpAddressUtils.isValidIpv4Address("12.34.56.78/31"));

        assertFalse(IpAddressUtils.isValidIpv4Address("12.12.12.256"));
        assertFalse(IpAddressUtils.isValidIpv4Address("::1"));
        assertFalse(IpAddressUtils.isValidIpv4Address("12::ff"));
        assertFalse(IpAddressUtils.isValidIpv4Address(null));
    }

    @Test
    public void isValidIpv6Address() throws Exception {
        assertTrue(IpAddressUtils.isValidIpv6Address("::1"));
        assertTrue(IpAddressUtils.isValidIpv6Address("12::ff"));

        assertFalse(IpAddressUtils.isValidIpv6Address("1.1.1.1"));
        assertFalse(IpAddressUtils.isValidIpv6Address("255.255.255.0"));
        assertFalse(IpAddressUtils.isValidIpv6Address("12.34.56.78/31"));
        assertFalse(IpAddressUtils.isValidIpv6Address("12.12.12.256"));
        assertFalse(IpAddressUtils.isValidIpv6Address(null));
    }
}
