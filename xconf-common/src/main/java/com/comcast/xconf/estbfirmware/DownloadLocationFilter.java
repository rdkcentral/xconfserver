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
 * Author: obaturynskyi
 * Created: 08.07.2014  18:42
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * Allows us to override download location and protocol based in IP address
 * range.
 * <p>
 * NOTE: This is NOT a blocking filter like the others. It simply modifies the
 * output.
 * <p>
 * I am going to try making this a mutable bean and skipping the duplication.
 * The only real concern here is if other rules in the rule base would be
 * misbehaved and modify other rules. Not a huge concern. No one outside the
 * rule base will have reference to the rules.
 */
public class DownloadLocationFilter implements Comparable<DownloadLocationFilter> {

    private static final Logger log = LoggerFactory
            .getLogger(DownloadLocationFilter.class);

    private IpAddressGroup ipAddressGroup;

    private Set<String> environments = new HashSet<String>();

    private Set<String> models = new HashSet<String>();

    @NotNull
    private FirmwareConfig.DownloadProtocol firmwareDownloadProtocol = FirmwareConfig.DownloadProtocol.tftp;

    // tftp location
    private IpAddress firmwareLocation;
    private IpAddress ipv6FirmwareLocation;

    private String httpLocation;

    private Boolean forceHttp;

    private String id;

    private String name;

    private String boundConfigId;

    /**
     * Quick and dirty way to tell if this filter is tied to a warehouse or not.
     * If it is we don't want to allow deleting. We want to warn against
     * editing.
     */
    public boolean isWarehouse() {
        return StringUtils.isAlpha(id) && StringUtils.isAllLowerCase(id);
    }

    /**
     * Returns location for given address
     * @param address ip address
     * @return String array: index# 0 - download protocol, index# 1 - download location. Null - if address is not in range
     */
    public String[] getLocation(IpAddress address) {
        if (ipAddressGroup != null && ipAddressGroup.isInRange(address)) {
            log.debug("ip is in range");
            String[] result = new String[3];
            result[0] = firmwareDownloadProtocol.name();
            result[1] = firmwareLocation.toString();
            if (ipv6FirmwareLocation != null) {
                result[2] = ipv6FirmwareLocation.toString();
            }
            return result;
        } else {
            return null;
        }
    }

    public IpAddressGroup getIpAddressGroup() {
        return ipAddressGroup;
    }

    public void setIpAddressGroup(IpAddressGroup ipAddressGroup) {
        this.ipAddressGroup = ipAddressGroup;
    }

    public Set<String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<String> environments) {
        this.environments = environments;
    }

    public Set<String> getModels() {
        return models;
    }

    public void setModels(Set<String> models) {
        this.models = models;
    }

    public FirmwareConfig.DownloadProtocol getFirmwareDownloadProtocol() {
        return firmwareDownloadProtocol;
    }

//	public void setFirmwareDownloadProtocol(
//			DownloadProtocol firmwareDownloadProtocol) {
//		this.firmwareDownloadProtocol = firmwareDownloadProtocol;
//	}

    public IpAddress getFirmwareLocation() {
        return firmwareLocation;
    }

    public void setFirmwareLocation(IpAddress firmwareLocation) {
        this.firmwareLocation = firmwareLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NotBlank
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBoundConfigId() {
        return boundConfigId;
    }

    public void setBoundConfigId(String boundConfigId) {
        this.boundConfigId = boundConfigId;
    }

    public IpAddress getIpv6FirmwareLocation() {
        return ipv6FirmwareLocation;
    }

    public void setIpv6FirmwareLocation(IpAddress ipv6FirmwareLocation) {
        this.ipv6FirmwareLocation = ipv6FirmwareLocation;
    }

    public String getHttpLocation() {
        return httpLocation;
    }

    public void setHttpLocation(String httpLocation) {
        this.httpLocation = httpLocation;
    }

    public Boolean getForceHttp() {
        return forceHttp;
    }

    public void setForceHttp(Boolean forceHttp) {
        this.forceHttp = forceHttp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadLocationFilter that = (DownloadLocationFilter) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(DownloadLocationFilter o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }
}

