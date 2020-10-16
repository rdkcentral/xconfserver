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
 * Author: Yury Stagit
 * Created: 06/11/16  12:00 PM
 */

package com.comcast.xconf.rfc;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.XRule;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.priority.Prioritizable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CF(cfName = "FeatureControlRule2", keyType = String.class)
public class FeatureRule implements IPersistable, Comparable<FeatureRule>, XRule, Prioritizable, Applicationable {

    private String id;

    private String name;

    private Rule rule;

    private Integer priority;

    private List<String> featureIds = new ArrayList<>();

    private String applicationType = ApplicationType.STB;

    @Override
    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    @JsonIgnore
    public String getTemplateId() throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    @Override
    @JsonIgnore
    public String getRuleType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Date getUpdated() {
        return null;
    }

    @Override
    @JsonIgnore
    public void setUpdated(Date date) {

    }

    @Override
    public int getTTL(String s) {
        return 0;
    }

    @Override
    @JsonIgnore
    public void setTTL(String s, int i) {

    }

    @Override
    @JsonIgnore
    public void clearTTL() {

    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<String> getFeatureIds() {
        return featureIds;
    }

    public void setFeatureIds(List<String> featureIds) {
        this.featureIds = featureIds;
    }

    @Override
    public String getApplicationType() {
        return applicationType;
    }

    @Override
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeatureRule that = (FeatureRule) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (rule != null ? !rule.equals(that.rule) : that.rule != null) return false;
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;
        if (featureIds != null ? !featureIds.equals(that.featureIds) : that.featureIds != null) return false;
        return applicationType != null ? applicationType.equals(that.applicationType) : that.applicationType == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (rule != null ? rule.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (featureIds != null ? featureIds.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(FeatureRule o) {
        int priority1 = (this.getPriority() != null) ? this.getPriority() : 0;
        int priority2 = (o != null && o.getPriority() != null) ? o.getPriority() : 0;
        return priority1 - priority2;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FeatureRule{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", rule=").append(rule);
        sb.append(", priority=").append(priority);
        sb.append(", featureIds=").append(featureIds);
        sb.append(", applicationType='").append(applicationType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
