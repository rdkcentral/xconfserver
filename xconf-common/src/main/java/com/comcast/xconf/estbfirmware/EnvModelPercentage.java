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
 * Created: 6/5/14
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.util.HashSet;
import java.util.Set;

public class EnvModelPercentage {

    private IpAddressGroup whitelist;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double percentage;

    private boolean active;

    private boolean firmwareCheckRequired;

    private boolean rebootImmediately = false;

    private String lastKnownGood;

    private String intermediateVersion;

    private Set<String> firmwareVersions = new HashSet<>();

    public IpAddressGroup getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(IpAddressGroup whitelist) {
        this.whitelist = whitelist;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnvModelPercentage that = (EnvModelPercentage) o;

        if (Double.compare(that.percentage, percentage) != 0) return false;
        if (active != that.active) return false;
        if (firmwareCheckRequired != that.firmwareCheckRequired) return false;
        if (rebootImmediately != that.rebootImmediately) return false;
        if (whitelist != null ? !whitelist.equals(that.whitelist) : that.whitelist != null) return false;
        return !(firmwareVersions != null ? !firmwareVersions.equals(that.firmwareVersions) : that.firmwareVersions != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = whitelist != null ? whitelist.hashCode() : 0;
        temp = Double.doubleToLongBits(percentage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + (firmwareCheckRequired ? 1 : 0);
        result = 31 * result + (rebootImmediately ? 1 : 0);
        result = 31 * result + (firmwareVersions != null ? firmwareVersions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("EnvModelPercentage{percentage=%s,active=%s,firmwareCheckRequired=%s, lastKnownGood=%s, intermediateVersion=%s, firmwareVersions=%s}",
                percentage, active, firmwareCheckRequired, lastKnownGood, intermediateVersion, firmwareVersions);
    }
}
