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
package com.comcast.xconf.queries.beans;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.xconf.estbfirmware.DownloadLocationFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"warehouse", "firmwareDownloadProtocol"})
public class DownloadLocationFilterWrapper extends DownloadLocationFilter {

    @JsonProperty("ipv4FirmwareLocation")
    @Override
    public IpAddress getFirmwareLocation() {
        return super.getFirmwareLocation();
    }

    public DownloadLocationFilterWrapper() {
    }

    public DownloadLocationFilterWrapper(DownloadLocationFilter downloadLocationFilter) {
        setId(downloadLocationFilter.getId());
        setName(downloadLocationFilter.getName());
        setEnvironments(downloadLocationFilter.getEnvironments());
        setModels(downloadLocationFilter.getModels());
        setBoundConfigId(downloadLocationFilter.getBoundConfigId());
        setIpAddressGroup(downloadLocationFilter.getIpAddressGroup());
        setFirmwareLocation(downloadLocationFilter.getFirmwareLocation());
        setIpv6FirmwareLocation(downloadLocationFilter.getIpv6FirmwareLocation());
        setHttpLocation(downloadLocationFilter.getHttpLocation());
        setForceHttp(downloadLocationFilter.getForceHttp());
    }
}
