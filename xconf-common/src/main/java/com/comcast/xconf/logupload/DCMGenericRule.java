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
 * Author: ikostrov
 * Created: 25.05.15 14:48
*/
package com.comcast.xconf.logupload;

import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.CfNames;
import com.comcast.xconf.XRule;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.priority.Prioritizable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.naming.OperationNotSupportedException;
import java.util.Date;

@CF(cfName = CfNames.LogUpload.DCM_RULE)
public class DCMGenericRule extends Rule implements IPersistable, Comparable<DCMGenericRule>, Prioritizable, XRule, Applicationable {

    private String id;

    private String name;

    private String description;

    private Integer priority;

    private String ruleExpression;

    private Integer percentage = 100;

    private Integer percentageL1 = 0;

    private Integer percentageL2 = 0;

    private Integer percentageL3 = 0;

    private String applicationType = ApplicationType.STB;

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
    public void setUpdated(Date date) {

    }

    @Override
    public int getTTL(String s) {
        return 0;
    }

    @Override
    public void setTTL(String s, int i) {

    }

    @Override
    public void clearTTL() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getRuleExpression() {
        return ruleExpression;
    }

    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public Integer getPercentageL1() {
        return percentageL1;
    }

    public void setPercentageL1(Integer percentageL1) {
        this.percentageL1 = percentageL1;
    }

    public Integer getPercentageL2() {
        return percentageL2;
    }

    public void setPercentageL2(Integer percentageL2) {
        this.percentageL2 = percentageL2;
    }

    public Integer getPercentageL3() {
        return percentageL3;
    }

    public void setPercentageL3(Integer percentageL3) {
        this.percentageL3 = percentageL3;
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
    public int compareTo(DCMGenericRule dcmGenericRule) {
        int thisPriority = (this.getPriority() != null) ? this.getPriority() : 0;
        int dcmGenericRulePriority = (dcmGenericRule != null && dcmGenericRule.getPriority() != null) ? dcmGenericRule.getPriority() : 0;

        return thisPriority - dcmGenericRulePriority;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DCMGenericRule{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", priority=").append(priority);
        sb.append(", ruleExpression='").append(ruleExpression).append('\'');
        sb.append(", percentage=").append(percentage);
        sb.append(", percentageL1=").append(percentageL1);
        sb.append(", percentageL2=").append(percentageL2);
        sb.append(", percentageL3=").append(percentageL3);
        sb.append(", applicationType='").append(applicationType).append('\'');
        sb.append(", negated=").append(negated);
        sb.append(", relation=").append(relation);
        sb.append(", condition=").append(condition);
        sb.append(", compoundParts=").append(compoundParts);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DCMGenericRule that = (DCMGenericRule) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;
        if (ruleExpression != null ? !ruleExpression.equals(that.ruleExpression) : that.ruleExpression != null)
            return false;
        if (percentage != null ? !percentage.equals(that.percentage) : that.percentage != null) return false;
        if (percentageL1 != null ? !percentageL1.equals(that.percentageL1) : that.percentageL1 != null) return false;
        if (percentageL2 != null ? !percentageL2.equals(that.percentageL2) : that.percentageL2 != null) return false;
        if (percentageL3 != null ? !percentageL3.equals(that.percentageL3) : that.percentageL3 != null) return false;
        return ApplicationType.equals(applicationType, that.applicationType);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (ruleExpression != null ? ruleExpression.hashCode() : 0);
        result = 31 * result + (percentage != null ? percentage.hashCode() : 0);
        result = 31 * result + (percentageL1 != null ? percentageL1.hashCode() : 0);
        result = 31 * result + (percentageL2 != null ? percentageL2.hashCode() : 0);
        result = 31 * result + (percentageL3 != null ? percentageL3.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    @JsonIgnore
    public Rule getRule() {
        return this;
    }

    @Override
    @JsonIgnore
    public String getTemplateId() throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    @Override
    @JsonIgnore
    public String getRuleType() {
        return "Formula";
    }

    // todo fix, should be like estbIP='1.1.1', see Formula class
    public String toStringOnlyBaseProperties() {
        if (isCompound()) {
            return compoundParts.toString();
        }
        return condition.toString();
    }
}
