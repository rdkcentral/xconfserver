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
 *  Created: 2:57 PM
 */
package com.comcast.xconf.admin.validator.firmware;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.estbfirmware.DownloadLocationRoundRobinFilterValue;
import com.comcast.xconf.estbfirmware.LocationFilterService;
import com.comcast.xconf.estbfirmware.SingletonFilterValue;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Component
public class RoundRobinFilterValidator {

    @Autowired
    private LocationFilterService locationFilterService;

    @Autowired
    private FirmwarePermissionService permissionService;

    public void validate(SingletonFilterValue singletonFilterValue) {
        if (!(singletonFilterValue instanceof DownloadLocationRoundRobinFilterValue)) {
            throw new ValidationRuntimeException("Filter is not instance of DownloadLocationRoundRobinFilterValue");
        }
        DownloadLocationRoundRobinFilterValue roundRobinFilter = (DownloadLocationRoundRobinFilterValue) singletonFilterValue;

        PermissionHelper.validateWrite(permissionService, roundRobinFilter.getApplicationType());

        if (!locationFilterService.isValidUrl(roundRobinFilter.getHttpFullUrlLocation())) {
            throw new ValidationRuntimeException("Full http location URL is not valid");
        }

        if (roundRobinFilter.getLocations() == null) {
            roundRobinFilter.setLocations(new ArrayList<DownloadLocationRoundRobinFilterValue.Location>());
        }
        if (roundRobinFilter.getIpv6locations() == null) {
            roundRobinFilter.setIpv6locations(new ArrayList<DownloadLocationRoundRobinFilterValue.Location>());
        }

        boolean ipv6InIpv4List = false;
        for (DownloadLocationRoundRobinFilterValue.Location location : roundRobinFilter.getLocations()) {
            if (location.getLocationIp().isIpv6()) {
                ipv6InIpv4List = true;
                break;
            }
        }

        boolean ipv4InIpv6List = false;
        for (DownloadLocationRoundRobinFilterValue.Location location : roundRobinFilter.getIpv6locations()) {
            if (!location.getLocationIp().isIpv6()) {
                ipv4InIpv6List = true;
                break;
            }
        }
        if (ipv4InIpv6List || ipv6InIpv4List) {
            throw new ValidationRuntimeException("IP address has an invalid version");
        }

        int percentage = 0;
        for (DownloadLocationRoundRobinFilterValue.Location location : roundRobinFilter.getLocations()) {
            percentage += location.getPercentage();
        }

        int ipv6Percentage = 0;
        for (DownloadLocationRoundRobinFilterValue.Location location : roundRobinFilter.getIpv6locations()) {
            ipv6Percentage += location.getPercentage();
        }

        if (percentage != 100) {
            throw new ValidationRuntimeException("Summary IPv4 percentage should be 100");
        }

        if (ipv6Percentage != 100 && roundRobinFilter.getIpv6locations() != null && roundRobinFilter.getIpv6locations().size() > 0) {
            throw new ValidationRuntimeException ("Summary IPv6 percentage should be 100");
        }

        Set<String> ipSet = new HashSet<>();
        for (DownloadLocationRoundRobinFilterValue.Location loc : roundRobinFilter.getLocations()) {
            ipSet.add(loc.getLocationIp().toString());
        }

        Set<String> ipv6Set = new HashSet<>();
        for (DownloadLocationRoundRobinFilterValue.Location loc : roundRobinFilter.getIpv6locations()) {
            ipv6Set.add(loc.getLocationIp().toString());
        }

        if (ipSet.size() < roundRobinFilter.getLocations().size() || ipv6Set.size() < roundRobinFilter.getIpv6locations().size()) {
            throw new ValidationRuntimeException("Locations are duplicated");
        }
    }
}
