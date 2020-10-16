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
 * Created: 07.07.2014  18:01
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.LocalTime;

import javax.validation.constraints.NotNull;

/**
 * Mutable bean for creating TimeFilterRules.
 */
public class TimeFilter implements Comparable<TimeFilter> {

    private String id;

    @NotBlank
    private String name;

    private boolean isLocalTime;

    @NotNull
    private LocalTime start;

    @NotNull
    private LocalTime end;

    private IpAddressGroup ipWhitelist;

    private EnvModelRuleBean envModelWhitelist;

    private boolean neverBlockRebootDecoupled = true;

    private boolean neverBlockHttpDownload = true;

    /**
     * Defensive copy constructor.
     */
    public TimeFilter(TimeFilter o) {
        id = o.id;
        isLocalTime = o.isLocalTime;
        name = o.name;
        start = o.start;
        end = o.end;
        if (o.ipWhitelist != null) {
            ipWhitelist = new IpAddressGroup(o.ipWhitelist);
        }
        if (o.envModelWhitelist != null) {
            envModelWhitelist = o.envModelWhitelist;
        }
        neverBlockRebootDecoupled = o.neverBlockRebootDecoupled;
        neverBlockHttpDownload = o.neverBlockHttpDownload;
    }

    public boolean isBetweenStartAndEnd(LocalTime input) {

        if (start.isAfter(end)) {
            if (input.isBefore(end) || input.isAfter(start)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (!input.isBefore(start) && !input.isAfter(end)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public TimeFilter() {

    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    public IpAddressGroup getIpWhitelist() {
        return ipWhitelist;
    }

    public void setIpWhitelist(IpAddressGroup whitelist) {
        this.ipWhitelist = whitelist;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        if (!(obj instanceof TimeFilter)) {
            return false;
        }
        TimeFilter other = (TimeFilter) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(TimeFilter o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * True if this filter is expressed in local time, false if not.
     */
    public boolean isLocalTime() {
        return isLocalTime;
    }

    public void setLocalTime(boolean isLocalTime) {
        this.isLocalTime = isLocalTime;
    }

    /**
     * If request matches this rule, we don't filter.
     */
    public EnvModelRuleBean getEnvModelWhitelist() {
        return envModelWhitelist;
    }

    public void setEnvModelWhitelist(EnvModelRuleBean envModelWhitelist) {
        this.envModelWhitelist = envModelWhitelist;
    }

    public boolean isNeverBlockRebootDecoupled() {
        return neverBlockRebootDecoupled;
    }

    public void setNeverBlockRebootDecoupled(boolean neverBlockRebootDecoupled) {
        this.neverBlockRebootDecoupled = neverBlockRebootDecoupled;
    }

    public boolean isNeverBlockHttpDownload() {
        return neverBlockHttpDownload;
    }

    public void setNeverBlockHttpDownload(boolean neverBlockHttpDownload) {
        this.neverBlockHttpDownload = neverBlockHttpDownload;
    }
}

