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
 * Created: 3/3/16  5:05 PM
 */
package com.comcast.xconf.logupload.settings;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.firmware.ApplicationType;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.Date;
import java.util.Map;

@CF(cfName = "SettingProfiles", keyType = String.class)
public class SettingProfile implements IPersistable, Comparable<SettingProfile>, Applicationable {

    private String id;

    private String settingProfileId;

    private SettingType settingType;

    private Map<String, String> properties;

    private String applicationType = ApplicationType.STB;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getSettingProfileId() {
        return settingProfileId;
    }

    public void setSettingProfileId(String settingProfileId) {
        this.settingProfileId = settingProfileId;
    }

    public SettingType getSettingType() {
        return settingType;
    }

    public void setSettingType(SettingType settingType) {
        this.settingType = settingType;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
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

    @Override
    public void setUpdated(Date timestamp) {

    }

    @Override
    public int getTTL(String column) {
        return 0;
    }

    @Override
    public void setTTL(String column, int value) {

    }

    @Override
    public void clearTTL() {

    }

    @Override
    public int compareTo(SettingProfile o) {
        String id1 = (settingProfileId != null) ? settingProfileId.toLowerCase() : null;
        String id2 = (o != null && o.settingProfileId != null) ? o.settingProfileId.toLowerCase() : null;
        return new NullComparator().compare(id1, id2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettingProfile that = (SettingProfile) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (settingProfileId != null ? !settingProfileId.equals(that.settingProfileId) : that.settingProfileId != null)
            return false;
        if (settingType != that.settingType) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;
        return applicationType != null ? applicationType.equals(that.applicationType) : that.applicationType == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (settingProfileId != null ? settingProfileId.hashCode() : 0);
        result = 31 * result + (settingType != null ? settingType.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }
}
