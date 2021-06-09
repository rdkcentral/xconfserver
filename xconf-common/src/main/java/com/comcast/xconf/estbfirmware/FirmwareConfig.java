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
 * Author: slavrenyuk
 * Created: 6/3/14
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.CfNames;
import com.comcast.xconf.firmware.ApplicationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * Firmware configuration for eSTBs.
 * TODO implement : Note, when serializing to the database we need to include id and description, but we don't serialize id and description when sending JSON back to eSTB.
 */
@CF(cfName = CfNames.Firmware.FIRMWARE_CONFIG)
@XmlRootElement
public class FirmwareConfig extends XMLPersistable implements Comparable<FirmwareConfig>, Applicationable {

    public enum DownloadProtocol {
        tftp, http, https
    }

    @NotBlank
    private String description;

    @NotEmpty
    private Set<String> supportedModelIds = new HashSet<>();

    /*
     * The following 5 properties get returned in JSON to eSTB.
     */
    private DownloadProtocol firmwareDownloadProtocol = DownloadProtocol.tftp;

    @NotBlank
    private String firmwareFilename;

    private String firmwareLocation;

    @NotBlank
    private String firmwareVersion;

    private String ipv6FirmwareLocation;

    private Long upgradeDelay;

    private Boolean rebootImmediately = false;

    private String applicationType = ApplicationType.STB;

    private Map<String, String> parameters = new HashMap<>();

    public FirmwareConfig() {
    }

    /**
     * For defensive copies - since this is mutable we need to make a defensive
     * copy before we hand the rule's config to the
     */
    public FirmwareConfig(FirmwareConfig c) {
        id = c.id;
        description = c.description;
        firmwareDownloadProtocol = c.firmwareDownloadProtocol;
        firmwareFilename = c.firmwareFilename;
        firmwareLocation = c.firmwareLocation;
        ipv6FirmwareLocation = c.ipv6FirmwareLocation;
        firmwareVersion = c.firmwareVersion;
        upgradeDelay = c.upgradeDelay;
        rebootImmediately = c.rebootImmediately;
        setSupportedModelIds(new HashSet<String>(c.supportedModelIds));
        parameters = c.getParameters();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getId(){
        return this.id;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Date getUpdated(){
        return this.updated;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DownloadProtocol getFirmwareDownloadProtocol() {
        return firmwareDownloadProtocol;
    }

    public void setFirmwareDownloadProtocol(
            DownloadProtocol firmwareDownloadProtocol) {
        this.firmwareDownloadProtocol = firmwareDownloadProtocol;
    }

    public String getFirmwareFilename() {
        return firmwareFilename;
    }

    public void setFirmwareFilename(String firmwareFilename) {
        this.firmwareFilename = firmwareFilename;
    }

    public String getFirmwareLocation() {
        return firmwareLocation;
    }

    public void setFirmwareLocation(String firmwareLocation) {
        this.firmwareLocation = firmwareLocation;
    }

    public void setIpv6FirmwareLocation(String ipv6FirmwareLocation) {
        this.ipv6FirmwareLocation = ipv6FirmwareLocation;
    }

    public String getIpv6FirmwareLocation() {
        return ipv6FirmwareLocation;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Long getUpgradeDelay() {
        return upgradeDelay;
    }

    public void setUpgradeDelay(Long upgradeDelay) {
        this.upgradeDelay = upgradeDelay;
    }

    public Boolean getRebootImmediately() {
        return rebootImmediately;
    }

    public void setRebootImmediately(Boolean rebootImmediately) {
        this.rebootImmediately = rebootImmediately;
    }

    @Override
    public String getApplicationType() {
        return applicationType;
    }

    @Override
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public int compareTo(FirmwareConfig o) {
        String name1 = (description != null) ? description.toLowerCase() : null;
        String name2 = (o != null && o.description != null) ? o.description.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FirmwareConfig)) {
            return false;
        }
        FirmwareConfig other = (FirmwareConfig) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                        .setExcludeFieldNames(new String[] {"updated", "ttlMap"})
                        .toString();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Set<String> getSupportedModelIds() {
        return supportedModelIds;
    }

    public void setSupportedModelIds(Set<String> supportedModelIds) {
        this.supportedModelIds = supportedModelIds;
    }

    public static class TargetedModelsFilter implements Predicate<FirmwareConfig> {
        private Set<String> targetedModelIds;

        public TargetedModelsFilter(Set<String> targetedModelIds) {
            this.targetedModelIds = targetedModelIds;
        }

        @Override
        public boolean apply(FirmwareConfig input) {
            return  input.getSupportedModelIds() != null &&
                    targetedModelIds != null &&
                    !Sets.intersection(targetedModelIds, input.getSupportedModelIds()).isEmpty();
        }
    }

    public String toLogString() {
        return  "firmwareDownloadProtocol=" + firmwareDownloadProtocol +
                " firmwareFilename=" + firmwareFilename +
                " firmwareLocation=" + firmwareLocation +
                " firmwareVersion=" + firmwareVersion +
                " ipv6FirmwareLocation=" + ipv6FirmwareLocation +
                " rebootImmediately=" + rebootImmediately;
    }
}
