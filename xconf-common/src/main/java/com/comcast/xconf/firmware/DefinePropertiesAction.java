/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.firmware;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

public class DefinePropertiesAction extends ApplicableAction {

    public static final String FIRMWARE_VERSIONS = "firmwareVersions";
    public static final String REGULAR_EXPRESSIONS = "regularExpressions";

    @JsonProperty
    private Map<String, String> properties;

    private List<String> byPassFilters;

    private Map<String, Set<String>> activationFirmwareVersions = new HashMap<>();

    public DefinePropertiesAction() {
        super(Type.DEFINE_PROPERTIES);
    }

    public DefinePropertiesAction(Map<String, String> properties) {
        super(Type.DEFINE_PROPERTIES);
        this.properties = properties;
    }

    public DefinePropertiesAction(Map<String, String> properties, List<String> byPassFilters) {
        super(Type.DEFINE_PROPERTIES);
        this.properties = properties;
        this.byPassFilters = byPassFilters;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public List<String> getByPassFilters() {
        return byPassFilters;
    }

    public void setByPassFilters(List<String> byPassFilters) {
        this.byPassFilters = byPassFilters;
    }

    public Map<String, Set<String>> getActivationFirmwareVersions() {
        return activationFirmwareVersions;
    }

    public void setActivationFirmwareVersions(Map<String, Set<String>> activationFirmwareVersions) {
        this.activationFirmwareVersions = activationFirmwareVersions;
    }

    @JsonIgnore
    public Set<String> getFirmwareVersions() {
        if (activationFirmwareVersions.containsKey(FIRMWARE_VERSIONS) && CollectionUtils.isNotEmpty(activationFirmwareVersions.get(FIRMWARE_VERSIONS))) {
            return activationFirmwareVersions.get(FIRMWARE_VERSIONS);
        }
        return new HashSet<>();
    }

    @JsonIgnore
    public Set<String> getFirmwareVersionRegExs() {
        if (activationFirmwareVersions.containsKey(REGULAR_EXPRESSIONS) && CollectionUtils.isNotEmpty(activationFirmwareVersions.get(REGULAR_EXPRESSIONS))) {
            return activationFirmwareVersions.get(REGULAR_EXPRESSIONS);
        }
        return new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefinePropertiesAction that = (DefinePropertiesAction) o;
        return Objects.equals(properties, that.properties) &&
                Objects.equals(byPassFilters, that.byPassFilters) &&
                Objects.equals(activationFirmwareVersions, that.activationFirmwareVersions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), properties, byPassFilters, activationFirmwareVersions);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DefinePropertiesAction{");
        sb.append("properties=").append(properties);
        sb.append(", byPassFilters=").append(byPassFilters);
        sb.append(", firmwareVersions=").append(activationFirmwareVersions);
        sb.append(", actionType=").append(actionType);
        sb.append('}');
        return sb.toString();
    }
}
