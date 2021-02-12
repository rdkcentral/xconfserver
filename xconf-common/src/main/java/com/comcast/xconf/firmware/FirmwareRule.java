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
 * Created: 10/2/15  2:54 PM
 */
package com.comcast.xconf.firmware;

import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.CfNames;
import com.comcast.xconf.XRule;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.StringUtils;

@CF(cfName = CfNames.Firmware.FIRMWARE_RULE)
@JsonIgnoreProperties({"updated", "ttlMap"})
public class FirmwareRule extends XMLPersistable implements Comparable<FirmwareRule>, XRule, Applicationable {
    private String name;
    private Rule rule;
    private ApplicableAction applicableAction;
    private String type;
    private boolean active = true;
    private String applicationType = ApplicationType.STB;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
    @JsonIgnore
    public String getTemplateId() {
        return type;
    }

    @Override
    @JsonIgnore
    public String getRuleType() {
        return this.getClass().getSimpleName();
    }

    @JsonIgnore
    public boolean isNoop() {
        if (applicableAction instanceof RuleAction) {
            RuleAction action = (RuleAction) applicableAction;
            return isNoop(action);
        }
        return true;
    }

    private boolean isNoop(RuleAction action) {
        if (StringUtils.isNotBlank(action.getConfigId())) {
            return false;
        }
        if (action.getConfigEntries() != null) {
            for (RuleAction.ConfigEntry entry : action.getConfigEntries()) {
                if (StringUtils.isNotBlank(entry.getConfigId())) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public int compareTo(FirmwareRule o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FirmwareRule that = (FirmwareRule) o;

        if (active != that.active) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (rule != null ? !rule.equals(that.rule) : that.rule != null) return false;
        if (applicableAction != null ? !applicableAction.equals(that.applicableAction) : that.applicableAction != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return applicationType != null ? applicationType.equals(that.applicationType) : that.applicationType == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (rule != null ? rule.hashCode() : 0);
        result = 31 * result + (applicableAction != null ? applicableAction.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("FirmwareRule{id='%s', name='%s', type=%s}", id, name, type);
    }

}
