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
 *  Created: 5:37 PM
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.xconf.Applicationable;
import com.comcast.xconf.firmware.ApplicationType;

public class GlobalPercentage implements Applicationable {
    private String whitelist;
    private Double percentage = 100.0;
    private String applicationType = ApplicationType.STB;

    public GlobalPercentage() { }

    public GlobalPercentage(String whitelist, Double percentage) {
        this.whitelist = whitelist;
        this.percentage = percentage;
    }

    public String getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(String whitelist) {
        this.whitelist = whitelist;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
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

        GlobalPercentage that = (GlobalPercentage) o;

        if (whitelist != null ? !whitelist.equals(that.whitelist) : that.whitelist != null) return false;
        if (percentage != null ? !percentage.equals(that.percentage) : that.percentage != null) return false;
        return ApplicationType.equals(applicationType, that.applicationType);
    }

    @Override
    public int hashCode() {
        int result = whitelist != null ? whitelist.hashCode() : 0;
        result = 31 * result + (percentage != null ? percentage.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GlobalPercentage{");
        sb.append("whitelist='").append(whitelist).append('\'');
        sb.append(", percentage=").append(percentage);
        sb.append(", applicationType='").append(applicationType).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static GlobalPercentage forApplication(String applicationType) {
        GlobalPercentage percentage = new GlobalPercentage();
        percentage.setApplicationType(applicationType);
        return percentage;
    }
}
