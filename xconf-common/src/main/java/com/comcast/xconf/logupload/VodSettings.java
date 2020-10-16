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
import com.comcast.xconf.firmware.ApplicationType;
import org.apache.commons.collections.comparators.NullComparator;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.util.List;
import java.util.Map;

@CF(cfName = CfNames.LogUpload.VOD_SETTINGS)
public class VodSettings extends XMLPersistable implements Comparable<VodSettings>, Applicationable {
    @NotBlank
    private String name;
    @URL
    private String locationsURL;

    private List<String> ipNames;

    private List<String> ipList;

    private Map<String, String> srmIPList;

    private String applicationType = ApplicationType.STB;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationsURL() {
        return locationsURL;
    }

    public void setLocationsURL(String locationsURL) {
        this.locationsURL = locationsURL;
    }

    public List<String> getIpNames() {
        return ipNames;
    }

    public void setIpNames(List<String> ipNames) {
        this.ipNames = ipNames;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public Map<String, String> getSrmIPList() {
        return srmIPList;
    }

    public void setSrmIPList(Map<String, String> srmIPList) {
        this.srmIPList = srmIPList;
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

        VodSettings that = (VodSettings) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (locationsURL != null ? !locationsURL.equals(that.locationsURL) : that.locationsURL != null) return false;
        if (ipNames != null ? !ipNames.equals(that.ipNames) : that.ipNames != null) return false;
        if (ipList != null ? !ipList.equals(that.ipList) : that.ipList != null) return false;
        if (srmIPList != null ? !srmIPList.equals(that.srmIPList) : that.srmIPList != null) return false;
        return applicationType != null ? applicationType.equals(that.applicationType) : that.applicationType == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (locationsURL != null ? locationsURL.hashCode() : 0);
        result = 31 * result + (ipNames != null ? ipNames.hashCode() : 0);
        result = 31 * result + (ipList != null ? ipList.hashCode() : 0);
        result = 31 * result + (srmIPList != null ? srmIPList.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(VodSettings o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("VodSettings{");
        sb.append("name='").append(name).append('\'');
        sb.append(", locationsURL='").append(locationsURL).append('\'');
        sb.append(", ipNames=").append(ipNames);
        sb.append(", ipList=").append(ipList);
        sb.append(", srmIPList=").append(srmIPList);
        sb.append(", applicationType='").append(applicationType).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
