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
 * Author: rdolomansky
 * Created: 10/26/15  1:21 PM
 */
package com.comcast.xconf.firmware;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

import static com.comcast.xconf.firmware.DefinePropertiesAction.FIRMWARE_VERSIONS;
import static com.comcast.xconf.firmware.DefinePropertiesAction.REGULAR_EXPRESSIONS;

public class DefinePropertiesTemplateAction extends ApplicableAction {

    private Map<String, PropertyValue> properties;

    private List<String> byPassFilters;

    private Map<String, Set<String>> activationFirmwareVersions = new HashMap<>();


    public DefinePropertiesTemplateAction() {
        super(Type.DEFINE_PROPERTIES_TEMPLATE);
    }

    public DefinePropertiesTemplateAction(Map<String, PropertyValue> map) {
        this();
        this.properties = map;
    }

    public DefinePropertiesTemplateAction(Map<String, PropertyValue> map, List<String> byPassFilters) {
        this(map);
        this.byPassFilters = byPassFilters;
    }

    public Map<String, PropertyValue> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, PropertyValue> properties) {
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

    @JsonIgnore
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
        DefinePropertiesTemplateAction that = (DefinePropertiesTemplateAction) o;
        return Objects.equals(properties, that.properties) &&
                Objects.equals(byPassFilters, that.byPassFilters) &&
                Objects.equals(activationFirmwareVersions, that.activationFirmwareVersions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), properties, byPassFilters, activationFirmwareVersions);
    }

    public static class PropertyValue {
        private String value;
        private boolean optional;
        private List<ValidationType> validationTypes;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isOptional() {
            return optional;
        }

        public void setOptional(boolean optional) {
            this.optional = optional;
        }

        public List<ValidationType> getValidationTypes() {
            return validationTypes;
        }

        public void setValidationTypes(List<ValidationType> validationTypes) {
            this.validationTypes = validationTypes;
        }

        public static PropertyValue create(String value, boolean optional, ValidationType type) {
            PropertyValue propertyValue = new PropertyValue();
            propertyValue.setValue(value);
            propertyValue.setOptional(optional);
            propertyValue.setValidationTypes(Lists.newArrayList(type));
            return propertyValue;
        }
    }

    public enum ValidationType {
        STRING,
        BOOLEAN,
        NUMBER,
        PERCENT,
        PORT,
        URL,
        IPV4,
        IPV6
    }
}
