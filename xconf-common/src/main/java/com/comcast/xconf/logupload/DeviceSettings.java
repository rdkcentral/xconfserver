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
 * Created: 4/29/14
 */
package com.comcast.xconf.logupload;


import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.CfNames;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.collections.comparators.NullComparator;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@CF(cfName = CfNames.LogUpload.DEVICE_SETTINGS)
public class DeviceSettings extends XMLPersistable implements Comparable<DeviceSettings>, Applicationable {
    @NotBlank
    private String name;
    @NotNull
    private Boolean checkOnReboot;

    //@NotNull
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
    @Deprecated
    public ConfigurationServiceURL configurationServiceURL;

    @NotNull
    private Boolean settingsAreActive;
    @NotNull
    @Valid
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
    public Schedule schedule = new Schedule();

    public String applicationType;

    public DeviceSettings() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getCheckOnReboot() {
        return checkOnReboot;
    }

    public void setCheckOnReboot(Boolean checkOnReboot) {
        this.checkOnReboot = checkOnReboot;
    }

    public ConfigurationServiceURL getConfigurationServiceURL() {
        return configurationServiceURL;
    }

    public void setConfigurationServiceURL(ConfigurationServiceURL configurationServiceURL) {
        this.configurationServiceURL = configurationServiceURL;
    }

    public Boolean getSettingsAreActive() {
        return settingsAreActive;
    }

    public void setSettingsAreActive(Boolean settingsAreActive) {
        this.settingsAreActive = settingsAreActive;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
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

        DeviceSettings that = (DeviceSettings) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (checkOnReboot != null ? !checkOnReboot.equals(that.checkOnReboot) : that.checkOnReboot != null)
            return false;
        if (configurationServiceURL != null ? !configurationServiceURL.equals(that.configurationServiceURL) : that.configurationServiceURL != null)
            return false;
        if (settingsAreActive != null ? !settingsAreActive.equals(that.settingsAreActive) : that.settingsAreActive != null)
            return false;
        if (schedule != null ? !schedule.equals(that.schedule) : that.schedule != null) return false;
        return applicationType != null ? applicationType.equals(that.applicationType) : that.applicationType == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (checkOnReboot != null ? checkOnReboot.hashCode() : 0);
        result = 31 * result + (configurationServiceURL != null ? configurationServiceURL.hashCode() : 0);
        result = 31 * result + (settingsAreActive != null ? settingsAreActive.hashCode() : 0);
        result = 31 * result + (schedule != null ? schedule.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(DeviceSettings o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DeviceSettings{");
        sb.append("name='").append(name).append('\'');
        sb.append(", checkOnReboot=").append(checkOnReboot);
        sb.append(", configurationServiceURL=").append(configurationServiceURL);
        sb.append(", settingsAreActive=").append(settingsAreActive);
        sb.append(", schedule=").append(schedule);
        sb.append(", applicationType='").append(applicationType).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
