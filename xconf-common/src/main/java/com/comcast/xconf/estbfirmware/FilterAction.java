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
 * Created: 6/4/14
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.CfNames;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.StringUtils;

@CF(cfName = CfNames.Firmware.FILTER_ACTION)
public class FilterAction extends XMLPersistable {
    public enum Type {
        BLOCK,
        MODIFY_REBOOT_IMMEDIATELY,
        MODIFY_DOWNLOAD_LOCATION
    }

    protected Type type;
    protected IpAddressGroup ipAddressGroup; // required for MODIFY_DOWNLOAD_LOCATION. is null for BLOCK and MODIFY_REBOOT_IMMEDIATELY

    // tftp location
    protected IpAddress firmwareLocation;     // required for MODIFY_DOWNLOAD_LOCATION. is null for BLOCK and MODIFY_REBOOT_IMMEDIATELY
    protected IpAddress ipv6FirmwareLocation; // required for MODIFY_DOWNLOAD_LOCATION. is null for BLOCK and MODIFY_REBOOT_IMMEDIATELY

    // http location
    protected String httpLocation;     // required for MODIFY_DOWNLOAD_LOCATION. is null for BLOCK and MODIFY_REBOOT_IMMEDIATELY

    protected Boolean forceHttp;     // required for MODIFY_DOWNLOAD_LOCATION. is null for BLOCK and MODIFY_REBOOT_IMMEDIATELY

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public IpAddressGroup getIpAddressGroup() {
        return ipAddressGroup;
    }

    public void setIpAddressGroup(IpAddressGroup ipAddressGroup) {
        this.ipAddressGroup = ipAddressGroup;
    }

    public IpAddress getFirmwareLocation() {
        return firmwareLocation;
    }

    public void setFirmwareLocation(IpAddress firmwareLocation) {
        this.firmwareLocation = firmwareLocation;
    }

    public IpAddress getIpv6FirmwareLocation() {
        return ipv6FirmwareLocation;
    }

    public void setIpv6FirmwareLocation(IpAddress ipv6FirmwareLocation) {
        this.ipv6FirmwareLocation = ipv6FirmwareLocation;
    }

    /**
     * Returns location for given address
     * @param address ip address
     * @return String array: index# 0 - download protocol, index# 1 - download location. Null - if address is not in range
     */
    @JsonIgnore
    public String[] getLocation(IpAddress address) {
        if (ipAddressGroup != null && ipAddressGroup.isInRange(address)) {

            boolean useHttp = StringUtils.isNotBlank(httpLocation)
                    && (Boolean.TRUE.equals(forceHttp) || firmwareLocation == null);

            if (useHttp) {
                String[] result = new String[3];
                result[0] = FirmwareConfig.DownloadProtocol.http.name();
                result[1] = httpLocation;
                if (ipv6FirmwareLocation != null) {
                    result[2] = ipv6FirmwareLocation.toString();
                }
                return result;
            } else if (firmwareLocation != null) {
                String[] result = new String[3];
                result[0] = FirmwareConfig.DownloadProtocol.tftp.name();
                result[1] = firmwareLocation.toString();
                if (ipv6FirmwareLocation != null) {
                    result[2] = ipv6FirmwareLocation.toString();
                }
                return result;
            }
        }
        return null;
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

}
