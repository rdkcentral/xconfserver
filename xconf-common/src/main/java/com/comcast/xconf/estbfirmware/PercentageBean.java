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
 *  Author: mdolina
 *  Created: 3:47 PM
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.XRule;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.RuleAction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.*;

public class PercentageBean extends XMLPersistable implements Comparable<PercentageBean>, Applicationable, XRule {

    private String name;

    private String whitelist;

    private boolean active = true;

    private boolean firmwareCheckRequired;

    private boolean rebootImmediately = false;

    private String lastKnownGood;

    private String intermediateVersion;

    private Set<String> firmwareVersions = new HashSet<>();

    private List<RuleAction.ConfigEntry> distributions = new ArrayList<>();

    private String applicationType = ApplicationType.STB;

    private String environment;

    private String model;

    private Rule optionalConditions;

    private boolean useAccountIdPercentage = false;

    @Override
    @JsonIgnore
    public Rule getRule() {
        return optionalConditions;
    }

    public String getName() {
        return name;
    }

    @Override
    @JsonIgnore
    public String getTemplateId() {
        return "ENV_MODEL_RULE";
    }

    @Override
    @JsonIgnore
    public String getRuleType() {
        return "PercentFilter";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(String whitelist) {
        this.whitelist = whitelist;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isFirmwareCheckRequired() {
        return firmwareCheckRequired;
    }

    public void setFirmwareCheckRequired(boolean firmwareCheckRequired) {
        this.firmwareCheckRequired = firmwareCheckRequired;
    }

    public String getLastKnownGood() {
        return lastKnownGood;
    }

    public void setLastKnownGood(String lastKnownGood) {
        this.lastKnownGood = lastKnownGood;
    }

    public String getIntermediateVersion() {
        return intermediateVersion;
    }

    public void setIntermediateVersion(String intermediateVersion) {
        this.intermediateVersion = intermediateVersion;
    }

    public boolean isRebootImmediately() {
        return rebootImmediately;
    }

    public void setRebootImmediately(boolean rebootImmediately) {
        this.rebootImmediately = rebootImmediately;
    }

    public Set<String> getFirmwareVersions() {
        return firmwareVersions;
    }

    public void setFirmwareVersions(Set<String> firmwareVersions) {
        this.firmwareVersions = firmwareVersions;
    }

    public List<RuleAction.ConfigEntry> getDistributions() {
        return distributions;
    }

    public void setDistributions(List<RuleAction.ConfigEntry> distributions) {
        this.distributions = distributions;
    }

    public Rule getOptionalConditions() {
        return optionalConditions;
    }

    public void setOptionalConditions(Rule rule) {
        this.optionalConditions = rule;
    }

    public boolean isUseAccountIdPercentage() {
        return useAccountIdPercentage;
    }

    public void setUseAccountIdPercentage(boolean useAccountIdPercentage) {
        this.useAccountIdPercentage = useAccountIdPercentage;
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
        PercentageBean that = (PercentageBean) o;
        return active == that.active &&
                firmwareCheckRequired == that.firmwareCheckRequired &&
                rebootImmediately == that.rebootImmediately &&
                Objects.equals(name, that.name) &&
                Objects.equals(whitelist, that.whitelist) &&
                Objects.equals(lastKnownGood, that.lastKnownGood) &&
                Objects.equals(intermediateVersion, that.intermediateVersion) &&
                Objects.equals(firmwareVersions, that.firmwareVersions) &&
                Objects.equals(distributions, that.distributions) &&
                Objects.equals(applicationType, that.applicationType) &&
                Objects.equals(environment, that.environment) &&
                Objects.equals(model, that.model) &&
                Objects.equals(optionalConditions, that.optionalConditions) &&
                Objects.equals(useAccountIdPercentage, that.useAccountIdPercentage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, whitelist, active, firmwareCheckRequired, rebootImmediately, lastKnownGood,
                intermediateVersion, firmwareVersions, distributions, applicationType, environment, model,
                optionalConditions, useAccountIdPercentage);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PercentageBean{");
        sb.append("name='").append(name).append('\'');
        sb.append(", whitelist='").append(whitelist).append('\'');
        sb.append(", active=").append(active);
        sb.append(", firmwareCheckRequired=").append(firmwareCheckRequired);
        sb.append(", rebootImmediately=").append(rebootImmediately);
        sb.append(", lastKnownGood='").append(lastKnownGood).append('\'');
        sb.append(", intermediateVersion='").append(intermediateVersion).append('\'');
        sb.append(", firmwareVersions=").append(firmwareVersions);
        sb.append(", distributions=").append(distributions);
        sb.append(", applicationType='").append(applicationType).append('\'');
        sb.append(", environment='").append(environment).append('\'');
        sb.append(", model='").append(model).append('\'');
        sb.append(", optionalConditions=").append(optionalConditions).append('\'');
        sb.append(", useAccountIdPercentage=").append(useAccountIdPercentage);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(PercentageBean o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }
}
