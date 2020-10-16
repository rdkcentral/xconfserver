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
 * Created: 1/27/16  12:56 PM
 */
package com.comcast.xconf.logupload.settings;

import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.XRule;
import com.comcast.xconf.firmware.ApplicationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.comparators.NullComparator;

import javax.naming.OperationNotSupportedException;
import java.util.Date;

@CF(cfName = "SettingRules", keyType = String.class)
public class SettingRule implements IPersistable, Comparable<SettingRule>, XRule, Applicationable {

    private String id;

    private String name;

    private Rule rule;

    private String boundSettingId;

    private String applicationType = ApplicationType.STB;

    @Override
    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
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
    public String getName() {
        return name;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getBoundSettingId() {
        return boundSettingId;
    }

    public void setBoundSettingId(String boundSettingId) {
        this.boundSettingId = boundSettingId;
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
    public Date getUpdated() {
        return null;
    }

    @JsonIgnore
    @Override
    public void setUpdated(Date timestamp) {

    }

    @Override
    public int getTTL(String column) {
        return 0;
    }

    @JsonIgnore
    @Override
    public void setTTL(String column, int value) {

    }

    @JsonIgnore
    @Override
    public void clearTTL() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettingRule that = (SettingRule) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (rule != null ? !rule.equals(that.rule) : that.rule != null) return false;
        if (boundSettingId != null ? !boundSettingId.equals(that.boundSettingId) : that.boundSettingId != null)
            return false;
        return applicationType != null ? applicationType.equals(that.applicationType) : that.applicationType == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (rule != null ? rule.hashCode() : 0);
        result = 31 * result + (boundSettingId != null ? boundSettingId.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(SettingRule o) {
        String id1 = (name != null) ? name.toLowerCase() : null;
        String id2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(id1, id2);
    }
}
