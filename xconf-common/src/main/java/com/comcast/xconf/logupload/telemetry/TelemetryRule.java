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
 * Author: phoenix
 * Created: 18/03/2015  16:05
 */
package com.comcast.xconf.logupload.telemetry;

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

@CF(cfName = "TelemetryRules", keyType = String.class)
public class TelemetryRule extends Rule implements IPersistable, Comparable<TelemetryRule>, XRule, Applicationable {
    private String boundTelemetryId;

    private String id;

    private String name;

    private String applicationType = ApplicationType.STB;


    public String getBoundTelemetryId() {
        return boundTelemetryId;
    }

    public void setBoundTelemetryId(String boundTelemetryId) {
        this.boundTelemetryId = boundTelemetryId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (!super.equals(o)) return false;

        TelemetryRule that = (TelemetryRule) o;

        if (boundTelemetryId != null ? !boundTelemetryId.equals(that.boundTelemetryId) : that.boundTelemetryId != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return applicationType != null ? applicationType.equals(that.applicationType) : that.applicationType == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (boundTelemetryId != null ? boundTelemetryId.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(TelemetryRule o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
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
        return this.getClass().getSimpleName();
    }

    public static class PermanentTelemetryRuleDescriptor {
        private String ruleId;
        private String ruleName;

        public PermanentTelemetryRuleDescriptor(String ruleId, String ruleName) {
            this.ruleId = ruleId;
            this.ruleName = ruleName;
        }

        public String getRuleId() {
            return ruleId;
        }

        public void setRuleId(String ruleId) {
            this.ruleId = ruleId;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }
    }
}
