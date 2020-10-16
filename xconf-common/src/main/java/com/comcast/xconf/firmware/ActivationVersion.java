/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
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
 * Author: Maxym Dolina
 * Created: 25.01.2019
 */

package com.comcast.xconf.firmware;

import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.Applicationable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties({"updated", "ttlMap"})
public class ActivationVersion extends XMLPersistable implements Applicationable, Comparable<ActivationVersion> {

    private String applicationType;

    private String description;

    private Set<String> regularExpressions = new HashSet<>();

    private String model;

    private Set<String> firmwareVersions = new HashSet<>();

    private String partnerId;

    public Set<String> getRegularExpressions() {
        return regularExpressions;
    }

    public void setRegularExpressions(Set<String> regularExpressions) {
        this.regularExpressions = regularExpressions;
    }

    public Set<String> getFirmwareVersions() {
        return firmwareVersions;
    }

    public void setFirmwareVersions(Set<String> firmwareVersions) {
        this.firmwareVersions = firmwareVersions;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getApplicationType() {
        return applicationType;
    }

    @Override
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivationVersion that = (ActivationVersion) o;
        return Objects.equals(id, that.id)
                && Objects.equals(applicationType, that.applicationType)
                && Objects.equals(description, that.description)
                && Objects.equals(model, that.model)
                && Objects.equals(partnerId, that.partnerId)
                && Objects.equals(regularExpressions, that.regularExpressions)
                && Objects.equals(firmwareVersions, that.firmwareVersions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, applicationType, model, regularExpressions, firmwareVersions);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ActivationVersion{");
        sb.append("regexp='").append(regularExpressions).append('\'');
        sb.append(", applicationType=").append(applicationType);
        sb.append(", model=").append(model);
        sb.append(", firmwareVersions=").append(firmwareVersions);
        sb.append(", id='").append(id).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", partnerId='").append(partnerId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(ActivationVersion o) {
        String description1 = (description != null) ? description.toLowerCase() : null;
        String description2 = (o != null && o.description != null) ? o.description.toLowerCase() : null;
        return new NullComparator().compare(description1, description2);
    }
}
