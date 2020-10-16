/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 */
package com.comcast.apps.dataaccess.test.legacy;

import com.comcast.apps.dataaccess.annotation.CF;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@CF(
        cfName = "NameSpacedList2",
        defaultColumnName = "namespacedListData"
)
@XmlRootElement
public class NamespacedList {

    private String id;

    @XmlElementWrapper
    @XmlElement(name = "entry")
    private Set<String> data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getData() {
        return data;
    }

    public void setData(Set<String> data) {
        this.data = data;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(id);
        stringBuilder.append(" data=").append(StringUtils.join(getData(), ','));
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NamespacedList that = (NamespacedList) o;
        return (id == that.id
                || (id != null
                    && id.equalsIgnoreCase(that.id)))
                && (data == that.data
                    || (data != null
                        && data.equals(that.data)));

    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }
}
