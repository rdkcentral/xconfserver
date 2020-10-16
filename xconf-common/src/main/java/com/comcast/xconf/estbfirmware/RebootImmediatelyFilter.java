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
 * Author: obaturynskyi
 * Created: 10.07.2014  14:22
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import org.apache.commons.collections.comparators.NullComparator;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

/**
 * Allows us to set RebootImmediately output flag based in MAC addresses, IP address
 * range, environments and models.
 * <p>
 * NOTE: This is NOT a blocking filter like the others. It simply modifies the
 * output.
 * <p>
 */
@XmlRootElement
public class RebootImmediatelyFilter implements Comparable<RebootImmediatelyFilter> {

    private static final Logger log = LoggerFactory
            .getLogger(RebootImmediatelyFilter.class);

    protected String macAddresses;

    private Set<IpAddressGroup> ipAddressGroups;

    protected Set<String> environments = new HashSet<String>();

    protected Set<String> models = new HashSet<String>();

    public String getId() {
        return id;
    }

    private String id;

    private String name;


    @NotBlank
    public String getName() {
        return name;
    }

    public String getMacAddresses() {
        return macAddresses;
    }

    public void setMacAddresses(String macAddresses) {
        this.macAddresses = macAddresses;
    }

    public Set<IpAddressGroup> getIpAddressGroups() {
        return ipAddressGroups;
    }

    public void setIpAddressGroups(Set<IpAddressGroup> ipAddressGroups) {
        this.ipAddressGroups = ipAddressGroups;
    }

    public Set<String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<String> environments) {
        this.environments = environments;
    }

    public Set<String> getModels() {
        return models;
    }

    public void setModels(Set<String> models) {
        this.models = models;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RebootImmediatelyFilter that = (RebootImmediatelyFilter) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(RebootImmediatelyFilter o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }
}

