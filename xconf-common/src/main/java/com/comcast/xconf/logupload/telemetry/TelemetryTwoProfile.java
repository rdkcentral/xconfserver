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
 * Author: Jeyabala Murugan
 * Created: 13/07/2020
 */
package com.comcast.xconf.logupload.telemetry;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.firmware.ApplicationType;
import org.apache.commons.collections.comparators.NullComparator;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Objects;

@CF(cfName = "TelemetryTwoProfiles", keyType = String.class)
public class TelemetryTwoProfile extends XMLPersistable implements Comparable<TelemetryTwoProfile>, Applicationable {

    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String jsonconfig;

    private String applicationType = ApplicationType.STB;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJsonconfig() {
        return jsonconfig;
    }

    public void setJsonconfig(String jsonconfig) {
        this.jsonconfig = jsonconfig;
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
        TelemetryTwoProfile that = (TelemetryTwoProfile) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(jsonconfig, that.jsonconfig) && Objects.equals(applicationType, that.applicationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, jsonconfig, applicationType);
    }

    @Override
    public int compareTo(TelemetryTwoProfile o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }

    @Override
    public String toString() {
        return "TelemetryTwoProfile [id=" + id + ", name=" + name + ", jsonconfig=" + jsonconfig + ", applicationType="
                + applicationType + "]";
    }
}