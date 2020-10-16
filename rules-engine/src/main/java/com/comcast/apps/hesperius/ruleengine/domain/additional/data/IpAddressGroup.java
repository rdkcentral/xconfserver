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

//import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
//import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Just a named list of ip addresses so we can identify warehouses and east coast ips etc. Flat for now. Maybe we will need nesting.
 */
public class IpAddressGroup {

    private String id;

    private String name;

    private Set<IpAddress> ipAddresses = new HashSet<IpAddress>();

    public IpAddressGroup() {
    }

    /**
     * Defensive copy constructor.
     */
    public IpAddressGroup(IpAddressGroup o) {
        id = o.id;
        name = o.name;
        ipAddresses = new HashSet<IpAddress>(o.ipAddresses);
    }

    @Override
    public String toString() {
        return "id=" + id + ", name=" + name + ", ipAddresses=" + ipAddresses;
    }

    /*public String toString() {
        return ReflectionToStringBuilder.toString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }*/

    /**
     * Returns true if any of the given adrs are in range of any of our
     * addresses.
     */
    public boolean isInRange(Collection<IpAddress> adrs) {
        if (adrs == null) {
            return false;
        }
        for (IpAddress ip : ipAddresses) {
            for (IpAddress s : adrs) {
                if (ip.isInRange(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any of the given adrs are in range of any of our
     * addresses.
     */
    public boolean isInRange(IpAddress... adrs) {
        if (adrs == null) {
            return false;
        }
        for (IpAddress ip : ipAddresses) {
            for (IpAddress s : adrs) {
                if (ip.isInRange(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any of the given adrs are in range of any of our
     * addresses.
     */
    public boolean isInRange(String... adrs) {
        if (adrs == null) {
            return false;
        }
        for (IpAddress ip : ipAddresses) {
            for (String s : adrs) {
                if (ip.isInRange(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<IpAddress> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(Set<IpAddress> addresses) {
        this.ipAddresses = addresses;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (!(obj instanceof IpAddressGroup)) {
            return false;
        }
        IpAddressGroup other = (IpAddressGroup) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
}

