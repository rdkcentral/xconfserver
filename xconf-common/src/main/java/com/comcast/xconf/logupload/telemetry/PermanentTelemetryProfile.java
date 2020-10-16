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
 * Author: phoenix
 * Created: 18/03/2015  15:58
 */
package com.comcast.xconf.logupload.telemetry;

import com.comcast.apps.dataaccess.annotation.CF;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@CF(cfName = "PermanentTelemetry", keyType = String.class)
@JsonIgnoreProperties("updated")
public class PermanentTelemetryProfile extends TelemetryProfile {
    protected String id;

    public static final class TelemetryProfileDescriptor {
        private String name;
        private String id;

        public TelemetryProfileDescriptor(String name, String id) {
            this.name = name;
            this.id = id;
        }

        @JsonProperty("telemetryProfile:name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
