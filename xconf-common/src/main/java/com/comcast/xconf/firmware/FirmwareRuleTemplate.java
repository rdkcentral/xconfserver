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
 * Created: 10/13/15  10:34 AM
 */
package com.comcast.xconf.firmware;

import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.XRule;
import com.comcast.xconf.priority.Prioritizable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.ArrayList;
import java.util.List;

@CF(cfName = "FirmwareRuleTemplate")
@JsonIgnoreProperties({"updated", "ttlMap"})
public class FirmwareRuleTemplate extends XMLPersistable implements Prioritizable, Comparable<FirmwareRuleTemplate>, XRule {
    private Rule rule;
    private ApplicableAction applicableAction;
    private Integer priority;
    private List<String> requiredFields = new ArrayList<>();
    private List<String> byPassFilters = new ArrayList<>();
    private String validationExpression;
    private boolean editable = true;

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public ApplicableAction getApplicableAction() {
        return applicableAction;
    }

    public void setApplicableAction(ApplicableAction applicableAction) {
        this.applicableAction = applicableAction;
    }

    @Override
    public Integer getPriority() {
        return priority;
    }

    @Override
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    @JsonIgnore
    public String getTemplateId() {
        return id;
    }

    @Override
    @JsonIgnore
    public String getRuleType() {
        return this.getClass().getSimpleName();
    }

    @Override
    @JsonIgnore
    public String getName() {
        return id;
    }

    public List<String> getRequiredFields() {
        return requiredFields;
    }

    public void setRequiredFields(List<String> requiredFields) {
        this.requiredFields = requiredFields;
    }

    public List<String> getByPassFilters() {
        return byPassFilters;
    }

    public void setByPassFilters(List<String> byPassFilters) {
        this.byPassFilters = byPassFilters;
    }

    public String getValidationExpression() {
        return validationExpression;
    }

    public void setValidationExpression(String validationExpression) {
        this.validationExpression = validationExpression;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FirmwareRuleTemplate template = (FirmwareRuleTemplate) o;

        if (editable != template.editable) return false;
        if (rule != null ? !rule.equals(template.rule) : template.rule != null) return false;
        if (applicableAction != null ? !applicableAction.equals(template.applicableAction) : template.applicableAction != null)
            return false;
        if (priority != null ? !priority.equals(template.priority) : template.priority != null) return false;
        if (requiredFields != null ? !requiredFields.equals(template.requiredFields) : template.requiredFields != null)
            return false;
        if (byPassFilters != null ? !byPassFilters.equals(template.byPassFilters) : template.byPassFilters != null)
            return false;
        return !(validationExpression != null ? !validationExpression.equals(template.validationExpression) : template.validationExpression != null);

    }

    @Override
    public int hashCode() {
        int result = rule != null ? rule.hashCode() : 0;
        result = 31 * result + (applicableAction != null ? applicableAction.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (requiredFields != null ? requiredFields.hashCode() : 0);
        result = 31 * result + (byPassFilters != null ? byPassFilters.hashCode() : 0);
        result = 31 * result + (validationExpression != null ? validationExpression.hashCode() : 0);
        result = 31 * result + (editable ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(FirmwareRuleTemplate o) {
        String id1 = (id != null) ? id.toLowerCase() : null;
        String id2 = (o != null && o.id != null) ? o.id.toLowerCase() : null;
        return new NullComparator().compare(id1, id2);
    }
}
