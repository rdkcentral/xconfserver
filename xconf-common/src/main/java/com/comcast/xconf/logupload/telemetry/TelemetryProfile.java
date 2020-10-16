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
 * Created: 04/03/2015  14:39
 */
package com.comcast.xconf.logupload.telemetry;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.apps.dataaccess.annotation.DataType;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.UploadProtocol;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.comparators.NullComparator;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@CF(cfName = "Telemetry", keyIs = DataType.JSON, keyType = TimestampedRule.class)
public class TelemetryProfile extends XMLPersistable implements Comparable<TelemetryProfile>, Applicationable{

    @JsonIgnore
    protected String id;

    @NotBlank
    @JsonProperty("telemetryProfile:name")
    private String name;

    @Valid
    private List<TelemetryElement> telemetryProfile;

    @NotBlank
    private String schedule;

    private long expires;

    @NotBlank
    @JsonProperty("uploadRepository:URL")
    private String uploadRepository;

    @NotNull
    @JsonProperty("uploadRepository:uploadProtocol")
    private UploadProtocol uploadProtocol;

    private String applicationType = ApplicationType.STB;

    public static class TelemetryElement {

        private String id;

        @NotBlank
        String header;

        @NotBlank
        String content;

        @NotBlank
        String type;

        @NotBlank
        String pollingFrequency = "0";

        private String component;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPollingFrequency() {
            return pollingFrequency;
        }

        public void setPollingFrequency(String pollingFrequency) {
            this.pollingFrequency = pollingFrequency;
        }

        public String getComponent() {
            return component;
        }

        public void setComponent(String component) {
            this.component = component;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TelemetryElement that = (TelemetryElement) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(header, that.header) &&
                    Objects.equals(content, that.content) &&
                    Objects.equals(type, that.type) &&
                    Objects.equals(pollingFrequency, that.pollingFrequency) &&
                    Objects.equals(component, that.component);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, header, content, type, pollingFrequency, component);
        }

        @Override
        public String toString() {
            return "TelemetryElement{" +
                    "id='" + id + '\'' +
                    ", header='" + header + '\'' +
                    ", content='" + content + '\'' +
                    ", type='" + type + '\'' +
                    ", pollingFrequency='" + pollingFrequency + '\'' +
                    ", component='" + component + '\'' +
                    '}';
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TelemetryElement> getTelemetryProfile() {
        return telemetryProfile;
    }

    public void setTelemetryProfile(List<TelemetryElement> elements) {
        this.telemetryProfile = elements;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getUploadRepository() {
        return uploadRepository;
    }

    public void setUploadRepository(String uploadRepository) {
        this.uploadRepository = uploadRepository;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public UploadProtocol getUploadProtocol() {

        return uploadProtocol;
    }

    public void setUploadProtocol(UploadProtocol uploadProtocol) {
        this.uploadProtocol = uploadProtocol;
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

        TelemetryProfile that = (TelemetryProfile) o;

        if (expires != that.expires) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (schedule != null ? !schedule.equals(that.schedule) : that.schedule != null) return false;
        if (telemetryProfile != null ? !telemetryProfile.equals(that.telemetryProfile) : that.telemetryProfile != null)
            return false;
        if (uploadProtocol != that.uploadProtocol) return false;
        if (uploadRepository != null ? !uploadRepository.equals(that.uploadRepository) : that.uploadRepository != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (telemetryProfile != null ? telemetryProfile.hashCode() : 0);
        result = 31 * result + (schedule != null ? schedule.hashCode() : 0);
        result = 31 * result + (int) (expires ^ (expires >>> 32));
        result = 31 * result + (uploadRepository != null ? uploadRepository.hashCode() : 0);
        result = 31 * result + (uploadProtocol != null ? uploadProtocol.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(TelemetryProfile o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TelemetryProfile{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", telemetryProfile=").append(telemetryProfile);
        sb.append(", schedule='").append(schedule).append('\'');
        sb.append(", expires=").append(expires);
        sb.append(", uploadRepository='").append(uploadRepository).append('\'');
        sb.append(", uploadProtocol=").append(uploadProtocol);
        sb.append(", applicationType='").append(applicationType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
