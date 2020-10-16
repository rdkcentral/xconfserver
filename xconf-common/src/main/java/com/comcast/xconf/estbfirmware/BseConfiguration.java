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
 * Author: fnikon200
 * Created: 3/12/14  5:40 PM
 */
package com.comcast.xconf.estbfirmware;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="bseConfiguration")
public class BseConfiguration {

    private String location;

    private String ipv6Location;

    private String protocol;

    private List<ModelFirmwareConfiguration> modelConfigurations = new ArrayList<ModelFirmwareConfiguration>();

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIpv6Location() {
        return ipv6Location;
    }

    public void setIpv6Location(String ipv6Location) {
        this.ipv6Location = ipv6Location;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<ModelFirmwareConfiguration> getModelConfigurations() {
        return modelConfigurations;
    }

    public void setModelConfigurations(List<ModelFirmwareConfiguration> modelConfigurations) {
        this.modelConfigurations = modelConfigurations;
    }

    @Override
    public String toString() {
        return "BseConfiguration{" +
                "location='" + location + '\'' +
                ", ipv6Location='" + ipv6Location + '\'' +
                ", protocol='" + protocol + '\'' +
                ", modelConfigurations=" + modelConfigurations +
                '}';
    }

    public static class ModelFirmwareConfiguration {
        private String model;
        private String firmwareFilename;
        private String firmwareVersion;

        public String getFirmwareFilename() {
            return firmwareFilename;
        }

        public void setFirmwareFilename(String firmwareFilename) {
            this.firmwareFilename = firmwareFilename;
        }

        public String getFirmwareVersion() {
            return firmwareVersion;
        }

        public void setFirmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public ModelFirmwareConfiguration() {
        }

        public ModelFirmwareConfiguration(String model, String firmwareFilename, String firmwareVersion) {
            this.model = model;
            this.firmwareFilename = firmwareFilename;
            this.firmwareVersion = firmwareVersion;
        }

        @Override
        public String toString() {
            return "ModelFirmwareConfiguration{" +
                    "model='" + model + '\'' +
                    ", firmwareFilename='" + firmwareFilename + '\'' +
                    ", firmwareVersion='" + firmwareVersion + '\'' +
                    '}';
        }
    }

}
