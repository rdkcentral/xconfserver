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
 * Created: 6/13/14
 */
package com.comcast.xconf.estbfirmware.evaluation;

import com.comcast.xconf.estbfirmware.Capabilities;
import com.comcast.xconf.estbfirmware.DownloadLocationRoundRobinFilterValue;
import com.comcast.xconf.estbfirmware.EstbFirmwareContext;
import com.comcast.xconf.estbfirmware.FirmwareConfigFacade;
import org.apache.commons.lang.StringUtils;

import java.util.Random;

import static com.comcast.xconf.estbfirmware.FirmwareConfig.DownloadProtocol.http;
import static com.comcast.xconf.estbfirmware.FirmwareConfig.DownloadProtocol.https;

public class DownloadLocationRoundRobinFilter {

    private static final Random random = new Random();

    /**
     * @return true if filter is applied, false otherwise
     */
    public static boolean filter(FirmwareConfigFacade firmwareConfig, DownloadLocationRoundRobinFilterValue filterValue, EstbFirmwareContext.Converted context) {
        boolean supportsFullHttpUrl = context.getCapabilities().contains(Capabilities.supportsFullHttpUrl);

        firmwareConfig.setFirmwareDownloadProtocol(http);

        if (!StringUtils.isBlank(filterValue.getHttpLocation())
                && !StringUtils.isBlank(filterValue.getHttpFullUrlLocation())) {
            boolean secureConnection = StringUtils.isBlank(context.getXconfHttpHeader());
            if (supportsFullHttpUrl) {
                setLocationByConnectionType(secureConnection, firmwareConfig, filterValue.getHttpFullUrlLocation());
            } else {
                firmwareConfig.setFirmwareLocation(filterValue.getHttpLocation());
            }
            return true;
        }

        boolean isIPv4LocationApplied = setupIPv4Location(firmwareConfig, filterValue);
        boolean isIPv6LocationApplied = setupIPv6Location(firmwareConfig, filterValue);

        return isIPv4LocationApplied || isIPv6LocationApplied;
    }

    private static void setLocationByConnectionType(boolean secureConnection, FirmwareConfigFacade firmwareConfig, String fullHttpLocation) {
        if (!secureConnection) {
            fullHttpLocation = fullHttpLocation.replace(https.name(), http.name());
        } else if (!fullHttpLocation.startsWith(https.name())) {
            fullHttpLocation = fullHttpLocation.replace(http.name(), https.name());
        }
        firmwareConfig.setFirmwareLocation(fullHttpLocation);
    }

    public static boolean setupIPv6Location(FirmwareConfigFacade firmwareConfig, DownloadLocationRoundRobinFilterValue filterValue) {
        double rand = random.nextDouble();
        double limit = 0.0;
        boolean isApplied = false;
        if (filterValue.getIpv6locations() != null) {
            for (DownloadLocationRoundRobinFilterValue.Location location : filterValue.getIpv6locations()) {
                limit += location.getPercentage() / 100.00;
                if (rand < limit) {
                    firmwareConfig.setIpv6FirmwareLocation(location.getLocationIp().toString());
                    isApplied = true;
                    break;
                }
            }
        }
        return isApplied;
    }

    public static boolean setupIPv4Location(FirmwareConfigFacade firmwareConfig, DownloadLocationRoundRobinFilterValue filterValue) {
        double rand = random.nextDouble();
        double limit = 0.0;
        boolean isApplied = false;
        for (DownloadLocationRoundRobinFilterValue.Location location : filterValue.getLocations()) {
            limit += location.getPercentage() / 100.00;
            if (rand < limit) {
                firmwareConfig.setFirmwareLocation(location.getLocationIp().toString());
                isApplied = true;
                break;
            }
        }
        return isApplied;
    }

    public static boolean containsVersion(String firmwareVersions, String contextVersion) {
        String[] split = firmwareVersions.split("\\s+");
        for (String s : split) {
            if (contextVersion.equals(s)) {
                return true;
            }
        }
        return false;
    }

}
