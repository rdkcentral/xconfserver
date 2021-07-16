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
 * Author: pbura
 * Created: 1/26/15
 */
package com.comcast.xconf;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * Provides namespaced list data model according to specification
 *
 * @author PBura
 */
public class NamespacedList extends XMLPersistable implements Comparable<NamespacedList> {

    private Set<String> data;

    private String typeName;

    public NamespacedList() {
        typeName = String.class.getName();
    }

    public NamespacedList(String typeName) {
        this.typeName = typeName;
    }

    public Set<String> getData() {
        return data;
    }

    public void setData(Set<String> data) {
        this.data = data;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String toString() {
        return getId() + "type=" + typeName + " data=" + StringUtils.join(getData(), ',');
    }

    public static NamespacedList newMacList() {
        return new NamespacedList(MacAddress.class.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamespacedList)) return false;

        NamespacedList that = (NamespacedList) o;

        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        if (typeName != null ? !typeName.equals(that.typeName) : that.typeName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(NamespacedList o) {
        String name1 = (id != null) ? id.toLowerCase() : null;
        String name2 = (o != null && o.id != null) ? o.id.toLowerCase() : null;

        return new NullComparator().compare(name1, name2);
    }
}