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
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.firmware.ApplicationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

public class DownloadLocationRoundRobinFilterValue extends SingletonFilterValue implements Applicationable {


    public DownloadLocationRoundRobinFilterValue() {}

    public DownloadLocationRoundRobinFilterValue(String applicationType) {
        this.applicationType = applicationType;
    }

    public static final String SINGLETON_ID = "DOWNLOAD_LOCATION_ROUND_ROBIN_FILTER_VALUE";

    private String applicationType = ApplicationType.STB;

    @Valid
    private List<Location> locations = new ArrayList<Location>();

    @Valid
    private List<Location> ipv6locations = new ArrayList<Location>();

    @NotBlank
    @Pattern(regexp = "(?=^.{1,254}$)(^(?:(?!\\d+\\.|-)[a-zA-Z0-9_\\-]{1,63}(?<!-)\\.)+(?:[a-zA-Z]{2,})$)")
    private String httpLocation;

    @NotBlank
    @URL
    private String httpFullUrlLocation;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public List<Location> getIpv6locations() {
        return ipv6locations;
    }

    public void setIpv6locations(List<Location> ipv6locations) {
        this.ipv6locations = ipv6locations;
    }

    public String getHttpLocation() {
        return httpLocation;
    }

    public void setHttpLocation(String httpLocation) {
        this.httpLocation = httpLocation;
    }

    public String getHttpFullUrlLocation() {
        return httpFullUrlLocation;
    }

    public void setHttpFullUrlLocation(String httpFullUrlLocation) {
        this.httpFullUrlLocation = httpFullUrlLocation;
    }

    @Override
    public String getApplicationType() {
        return applicationType;
    }

    @Override
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public static class Location {
        @NotNull
        private IpAddress locationIp;

        @Min(0)
        @Max(100)
        private double percentage;

        public IpAddress getLocationIp() {
            return locationIp;
        }

        public void setLocationIp(IpAddress locationIp) {
            this.locationIp = locationIp;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Location location = (Location) o;

            if (Double.compare(location.percentage, percentage) != 0) return false;
            return locationIp != null ? locationIp.equals(location.locationIp) : location.locationIp == null;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = locationIp != null ? locationIp.hashCode() : 0;
            temp = Double.doubleToLongBits(percentage);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    /**
     * Returns download location
     * @return String array: index# 0 - IPv4 location ip, index# 1 - IPv6 location ip
     */
    // TODO move from this bean?
    @JsonIgnore
    public String[] getDownloadLocations() {
        String[] result = new String[2];
        double random = Math.random();
        if (ipv6locations != null) {
            double scale = 0;
            for (Location location : ipv6locations) {
                scale += location.getPercentage() / 100.00;
                if (random < scale) {
                    result[1] = location.getLocationIp().toString();
                    break;
                }
            }
        }

        double scale = 0;
        for (Location location : locations) {
            scale += location.getPercentage() / 100.00;
            if (random < scale) {
                result[0] = location.getLocationIp().toString();
                break;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "DownloadLocationRoundRobinFilterValue{" +
                "locations=" + locations +
                ", ipv6locations=" + ipv6locations +
                ", httpLocation='" + httpLocation + '\'' +
                ", httpFullUrlLocation='" + httpFullUrlLocation + '\'' +
                ", applicationType=" + applicationType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadLocationRoundRobinFilterValue that = (DownloadLocationRoundRobinFilterValue) o;
        if (id != that.id) return false;
        if (applicationType != null ? !applicationType.equals(that.applicationType) : that.applicationType != null)
            return false;
        if (locations != null ? !locations.equals(that.locations) : that.locations != null) return false;
        if (ipv6locations != null ? !ipv6locations.equals(that.ipv6locations) : that.ipv6locations != null)
            return false;
        if (httpLocation != null ? !httpLocation.equals(that.httpLocation) : that.httpLocation != null) return false;
        return  (httpFullUrlLocation != null ? !httpFullUrlLocation.equals(that.httpFullUrlLocation) : that.httpFullUrlLocation != null);
    }

    @Override
    public int hashCode() {

        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        result = 31 * result + (locations != null ? locations.hashCode() : 0);
        result = 31 * result + (ipv6locations != null ? ipv6locations.hashCode() : 0);
        result = 31 * result + (httpLocation != null ? httpLocation.hashCode() : 0);
        result = 31 * result + (httpFullUrlLocation != null ? httpFullUrlLocation.hashCode() : 0);
        return result;
    }
}
