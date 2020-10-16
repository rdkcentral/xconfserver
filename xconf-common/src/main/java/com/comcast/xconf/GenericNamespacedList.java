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
 * Created: 14.10.15  11:00
 */
package com.comcast.xconf;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

@CF(
        cfName = CfNames.Common.GENERIC_NS_LIST,
        defaultColumnName = "NamedListData",
        compressionPolicy = CF.CompressionPolicy.COMPRESS_AND_SPLIT,
        compressionChunkSize = 64
)
public class GenericNamespacedList extends XMLPersistable implements Comparable<GenericNamespacedList> {

    private Set<String> data;

    private String typeName;

    public GenericNamespacedList() {
        typeName = GenericNamespacedListTypes.STRING;
    }

    public GenericNamespacedList(String typeName) {
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

    public static GenericNamespacedList newMacList() {
        return new GenericNamespacedList(GenericNamespacedListTypes.MAC_LIST);
    }

    public static GenericNamespacedList newIpList() {
        return new GenericNamespacedList(GenericNamespacedListTypes.IP_LIST);
    }

    @Override
    public int compareTo(GenericNamespacedList o) {
        String name1 = (id != null) ? id.toLowerCase() : null;
        String name2 = (o != null && o.id != null) ? o.id.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericNamespacedList that = (GenericNamespacedList) o;

        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        return typeName != null ? typeName.equals(that.typeName) : that.typeName == null;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
        return result;
    }

    public static boolean isMacList(GenericNamespacedList namespacedList) {
        return GenericNamespacedListTypes.MAC_LIST.equals(namespacedList.getTypeName());
    }

    public static boolean isIpList(GenericNamespacedList namespacedList) {
        return GenericNamespacedListTypes.IP_LIST.equals(namespacedList.getTypeName());
    }
}
