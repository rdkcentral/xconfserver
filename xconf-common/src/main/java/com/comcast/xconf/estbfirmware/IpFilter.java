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
 * Created: 07.07.2014  14:10
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * Blocking filter based on IP address only. Initial use case was to block
 * requests from boxes in warehouses since they are (will be) serviced by BSE
 * (billing system emulator) which is internal to warehouse.
 * <p/>
 * Again, here we are skipping the intermediary mutable bean and making this
 * rule mutable.
 */
public class IpFilter implements Comparable<IpFilter> {

    private String id;

    @NotBlank
    private String name;

    private static final Logger log = LoggerFactory.getLogger(IpFilter.class);

    @NotNull
    private IpAddressGroup ipAddressGroup;

    /**
     * Quick and dirty way to tell if this filter is tied to a warehouse or not.
     * If it is we don't want to allow editing/deleting
     */
    public boolean isWarehouse() {
        return StringUtils.isAlpha(id) && StringUtils.isAllLowerCase(id);
    }

    public IpAddressGroup getIpAddressGroup() {
        return ipAddressGroup;
    }

    public void setIpAddressGroup(IpAddressGroup ipAddressGroup) {
        this.ipAddressGroup = ipAddressGroup;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IpFilter ipFilter = (IpFilter) o;

        if (id != null ? !id.equals(ipFilter.id) : ipFilter.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(IpFilter o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }
}

