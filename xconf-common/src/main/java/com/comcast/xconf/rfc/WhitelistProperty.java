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
 * <p>
 * Author: mdolina
 * Created: 5/8/17  5:09 PM
 */
package com.comcast.xconf.rfc;

public class WhitelistProperty {

    private String key;

    private String value;

    private String namespacedListType;

    private String typeName;

    public WhitelistProperty() {}

    public WhitelistProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getNamespacedListType() {
        return namespacedListType;
    }

    public void setNamespacedListType(String namespacedListType) {
        this.namespacedListType = namespacedListType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhitelistProperty that = (WhitelistProperty) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (namespacedListType != null ? !namespacedListType.equals(that.namespacedListType) : that.namespacedListType != null)
            return false;
        return typeName != null ? typeName.equals(that.typeName) : that.typeName == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (namespacedListType != null ? namespacedListType.hashCode() : 0);
        result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WhitelistProperty{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", namespacedListType='").append(namespacedListType).append('\'');
        sb.append(", typeName='").append(typeName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
