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

import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.MINIMAL_CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RuleAction.class, name = "rule"),
        @JsonSubTypes.Type(value = DefinePropertiesAction.class, name = "define"),
        @JsonSubTypes.Type(value = DefinePropertiesTemplateAction.class, name = "defineTemplate"),
        @JsonSubTypes.Type(value = BlockingFilterAction.class, name = "blocking")
})
@JsonIgnoreProperties({"id", "updated"})
public class ApplicableAction extends XMLPersistable {

    @JsonProperty
    protected Type actionType;

    public ApplicableAction() {
    }

    public ApplicableAction(final Type actionType) {
        this.actionType = actionType;
    }

    public Type getActionType() {
        return actionType;
    }

    public void setActionType(Type actionType) {
        this.actionType = actionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicableAction that = (ApplicableAction) o;

        return actionType == that.actionType;
    }

    @Override
    public int hashCode() {
        return actionType != null ? actionType.hashCode() : 0;
    }

    public enum Type {
        RULE,
        DEFINE_PROPERTIES,
        BLOCKING_FILTER,

        RULE_TEMPLATE,
        DEFINE_PROPERTIES_TEMPLATE,
        BLOCKING_FILTER_TEMPLATE
    }

    public static ApplicableAction.Type readFromString(String type) {
        try {
            return ApplicableAction.Type.valueOf(type);
        } catch (Exception e) {
            return null;
        }
    }
}
