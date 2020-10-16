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
 * Created: 5/29/14
 */
package com.comcast.apps.hesperius.ruleengine.domain.additional.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import sun.net.util.IPAddressUtil;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Models ip address as well as CIDR block notation to denote subnets and allows for checking if another address
 * or subnet is in the range of this subnet.
 *
 * ToStringSerializer + constructor with string argument allows serialization / deserialization as ipAddress string.
 */
@JsonSerialize(using = ToStringSerializer.class)
public final class IpAddress implements Comparable<IpAddress> {

    private String address;
    private InetAddress baseAddress;
    @JsonIgnore
    private InetAddress lowAddress;
    @JsonIgnore
    private InetAddress highAddress;
    @JsonIgnore
    private boolean ipv6 = false;
    @JsonIgnore
    private boolean cidrBlock = false;

    public IpAddress(String ipAddress) {
        if (!parseAndInit(ipAddress)) {
            throw new IllegalArgumentException("bad address: " + ipAddress);
        }
        address = ipAddress;
    }

    public static IpAddress parse(String ipAddress) {
        return new IpAddress(ipAddress);
    }

    public String getLowAddress() {
        return lowAddress.getHostAddress();
    }

    public String getHighAddress() {
        return highAddress.getHostAddress();
    }

    public boolean isIpv6() {
        return ipv6;
    }

    public boolean isCidrBlock() {
        return cidrBlock;
    }

    public String getAddress() {
        return address;
    }

    /**
     * Returns true if the in address is in the range of any of the collection
     * of addresses. False if not.
     */
    public static boolean isInRange(IpAddress in, Collection<IpAddress> addresses) {
        if (addresses == null) {
            return false;
        }
        for (IpAddress aa : addresses) {
            if (aa.isInRange(in)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given ip address is in the range of the subnet
     * denoted by this ip address (or if this is a single addess, true if both
     * addresses are the same).
     */
    public boolean isInRange(String ip) {
        return isValid(ip) && isInRange(new IpAddress(ip));
    }

    /**
     * True if the given ip address is in my address range, false otherwise.
     */
    public boolean isInRange(IpAddress in) {
        if (in == null) {
            return false;
        }
        BigInteger low = new BigInteger(1, lowAddress.getAddress());
        BigInteger hi = new BigInteger(1, highAddress.getAddress());
        BigInteger target_low = new BigInteger(1, in.lowAddress.getAddress());
        BigInteger target_hi = new BigInteger(1, in.highAddress.getAddress());

        return lessOrEquals(low, target_low) && lessOrEquals(target_hi, hi);
    }

    private boolean lessOrEquals(BigInteger first, BigInteger second) {
        return first.compareTo(second) <= 0;
    }

    boolean parseAndInit(String s) {
        if (s == null || s.length() == 0)
            return false;

        int index = s.indexOf("/");
        String baseIpAddress = s;
        if (index != -1) {
            cidrBlock = true;
            baseIpAddress = s.substring(0, index);
        }

        if (!isValid(baseIpAddress))
            return false;

        try {
            baseAddress = InetAddress.getByName(baseIpAddress);
            ipv6 = baseAddress instanceof Inet6Address;
            if (!cidrBlock) {
                lowAddress = baseAddress;
                highAddress = baseAddress;
                return true;
            }
            String prefixPart = s.substring(index + 1);
            int prefixMaxValue = ipv6? 128 : 32;
            int prefix = Integer.parseInt(prefixPart);
            if (prefix < 0 || prefix > prefixMaxValue)
                return false;
            calculateRange(prefix);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void calculateRange(int prefix) throws UnknownHostException {

        ByteBuffer maskBuffer;
        int prefixSize;
        if (baseAddress.getAddress().length == 4) {
            maskBuffer = ByteBuffer.allocate(4).putInt(-1);
            prefixSize = 4;
        } else {
            maskBuffer = ByteBuffer.allocate(16).putLong(-1L).putLong(-1L);
            prefixSize = 16;
        }

        BigInteger mask = (new BigInteger(1, maskBuffer.array())).not().shiftRight(prefix);

        BigInteger ipVal = new BigInteger(1, baseAddress.getAddress());

        BigInteger lowIp = ipVal.and(mask);
        BigInteger hiIp = lowIp.add(mask.not());

        byte[] lowIpArr = modifyArray(lowIp.toByteArray(), prefixSize);
        byte[] hiIpArr = modifyArray(hiIp.toByteArray(), prefixSize);

        lowAddress = InetAddress.getByAddress(lowIpArr);
        highAddress = InetAddress.getByAddress(hiIpArr);

    }

    private byte[] modifyArray(byte[] array, int prefixSize) {
        int counter = 0;
        List<Byte> aList = new ArrayList<Byte>();
        while (counter < prefixSize && (array.length - 1 - counter >= 0)) {
            aList.add(0, array[array.length - 1 - counter]);
            counter++;
        }

        int size = aList.size();
        for (int i = 0; i < (prefixSize - size); i++) {
            aList.add(0, (byte) 0);
        }

        byte[] result = new byte[aList.size()];
        for (int i = 0; i < aList.size(); i++) {
            result[i] = aList.get(i);
        }
        return result;
    }

    /**
     * Returns true if the given address is a valid ip address notation.
     */
    public static boolean isValid(String address) {
        return address != null && address.length() > 0 &&
                (IPAddressUtil.isIPv4LiteralAddress(address) || IPAddressUtil.isIPv6LiteralAddress(address));

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IpAddress)) {
            return false;
        }
        IpAddress other = (IpAddress) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return address;
    }

    @Override
    public int compareTo(IpAddress o) {
        if (o == null) {
            return 1;
        } else {
            BigInteger low = new BigInteger(1, lowAddress.getAddress());
            BigInteger target = new BigInteger(1, o.lowAddress.getAddress());
            return low.compareTo(target);
        }
    }
}

