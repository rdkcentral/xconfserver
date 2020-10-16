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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RuleAction extends ApplicableAction {

    public RuleAction() {
        super(ApplicableAction.Type.RULE);
    }

    public RuleAction(String config) {
        this();
        configId = config;
    }

    @JsonProperty
    private String configId;

    @JsonProperty
    private List<ConfigEntry> configEntries;

    @JsonProperty
    private boolean active = true;

    @JsonProperty
    private boolean useAccountPercentage = false;

    @JsonProperty
    private boolean firmwareCheckRequired = false;

    @JsonProperty
    private boolean rebootImmediately = false;

    @JsonProperty
    private String whitelist;

    @JsonProperty
    private String intermediateVersion;

    @JsonProperty
    private Set<String> firmwareVersions;

    public List<ConfigEntry> getConfigEntries() {
        return configEntries;
    }

    public void setConfigEntries(List<ConfigEntry> configEntries) {
        this.configEntries = configEntries;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(String whitelist) {
        this.whitelist = whitelist;
    }

    public boolean isFirmwareCheckRequired() {
        return firmwareCheckRequired;
    }

    public void setFirmwareCheckRequired(boolean firmwareCheckRequired) {
        this.firmwareCheckRequired = firmwareCheckRequired;
    }

    public boolean isRebootImmediately() {
        return rebootImmediately;
    }

    public void setRebootImmediately(boolean rebootImmediately) {
        this.rebootImmediately = rebootImmediately;
    }

    public String getIntermediateVersion() {
        return intermediateVersion;
    }

    public void setIntermediateVersion(String intermediateVersion) {
        this.intermediateVersion = intermediateVersion;
    }

    public Set<String> getFirmwareVersions() {
        return firmwareVersions;
    }

    public boolean isUseAccountPercentage() {
        return useAccountPercentage;
    }

    public void setUseAccountPercentage(boolean useAccountPercentage) {
        this.useAccountPercentage = useAccountPercentage;
    }

    public void setFirmwareVersions(Set<String> firmwareVersions) {
        this.firmwareVersions = firmwareVersions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RuleAction that = (RuleAction) o;
        return active == that.active &&
                useAccountPercentage == that.useAccountPercentage &&
                firmwareCheckRequired == that.firmwareCheckRequired &&
                rebootImmediately == that.rebootImmediately &&
                Objects.equals(configId, that.configId) &&
                Objects.equals(configEntries, that.configEntries) &&
                Objects.equals(whitelist, that.whitelist) &&
                Objects.equals(intermediateVersion, that.intermediateVersion) &&
                Objects.equals(firmwareVersions, that.firmwareVersions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), configId, configEntries, active, useAccountPercentage, firmwareCheckRequired, rebootImmediately, whitelist, intermediateVersion, firmwareVersions);
    }

    @Override
    public String toString() {
        return "RuleAction{" +
                "configId='" + configId + '\'' +
                ", configEntries=" + configEntries +
                ", active=" + active +
                ", useAccountPercentage=" + useAccountPercentage +
                ", firmwareCheckRequired=" + firmwareCheckRequired +
                ", rebootImmediately=" + rebootImmediately +
                ", whitelist='" + whitelist + '\'' +
                ", intermediateVersion='" + intermediateVersion + '\'' +
                ", firmwareVersions=" + firmwareVersions +
                '}';
    }

    public static class ConfigEntry implements Comparable<ConfigEntry> {
        private String configId;
        private Double percentage;
        private Double startPercentRange;
        private Double endPercentRange;

        public ConfigEntry() { }

        public ConfigEntry(String configId, Double startPercentRange, Double endPercentRange) {
            this.configId = configId;
            this.startPercentRange = startPercentRange;
            this.endPercentRange = endPercentRange;
            if(endPercentRange != null && startPercentRange != null) {
                this.percentage = Math.round((endPercentRange - startPercentRange) * 1000d) / 1000d;
            }
        }

        public String getConfigId() {
            return configId;
        }

        public void setConfigId(String configId) {
            this.configId = configId;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }

        public Double getStartPercentRange() {
            return startPercentRange;
        }

        public void setStartPercentRange(Double startPercentRange) {
            this.startPercentRange = startPercentRange;
        }

        public Double getEndPercentRange() {
            return endPercentRange;
        }

        public void setEndPercentRange(Double endPercentRange) {
            this.endPercentRange = endPercentRange;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConfigEntry that = (ConfigEntry) o;

            if (configId != null ? !configId.equals(that.configId) : that.configId != null) return false;
            if (startPercentRange != null ? !startPercentRange.equals(that.startPercentRange) : that.startPercentRange != null) return false;
            if (endPercentRange != null ? !endPercentRange.equals(that.endPercentRange) : that.endPercentRange != null) return false;
            return !(percentage != null ? !percentage.equals(that.percentage) : that.percentage != null);
        }

        @Override
        public int hashCode() {
            int result = configId != null ? configId.hashCode() : 0;
            result = 31 * result + (startPercentRange != null ? startPercentRange.hashCode() : 0);
            result = 31 * result + (endPercentRange != null ? endPercentRange.hashCode() : 0);
            result = 31 * result + (percentage != null ? percentage.hashCode() : 0);
            return result;
        }



        @Override
        public String toString() {
            return "ConfigEntry{" +
                    "configId='" + configId + '\'' +
                    ", percentage=" + percentage +
                    ", startPercentRange=" + startPercentRange +
                    ", endPercentRange=" + endPercentRange +
                    '}';
        }

        @Override
        public int compareTo(ConfigEntry o) {
            if (startPercentRange == null || o == null || o.startPercentRange == null) {
                return 0;
            }
            if (startPercentRange > o.startPercentRange) {
                return 1;
            } else if (startPercentRange < o.startPercentRange) {
                return -1;
            }
            return 0;
        }
    }
}
