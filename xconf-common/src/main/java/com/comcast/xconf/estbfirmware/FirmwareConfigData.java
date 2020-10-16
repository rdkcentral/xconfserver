/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.estbfirmware;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FirmwareConfigData {

    public FirmwareConfigData() {};

    public FirmwareConfigData(List<String> firmwareVersions, Set<String> models) {
        this.firmwareVersions = firmwareVersions;
        this.models = models;
    }

    private List<String> firmwareVersions = new ArrayList<>();

    private Set<String> models = new HashSet<>();

    public List<String> getFirmwareVersions() {
        return firmwareVersions;
    }

    public void setFirmwareVersions(List<String> firmwareVersions) {
        this.firmwareVersions = firmwareVersions;
    }

    public Set<String> getModels() {
        return models;
    }

    public void setModels(Set<String> models) {
        this.models = models;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FirmwareConfigData that = (FirmwareConfigData) o;

        if (firmwareVersions != null ? !firmwareVersions.equals(that.firmwareVersions) : that.firmwareVersions != null)
            return false;
        return models != null ? models.equals(that.models) : that.models == null;
    }

    @Override
    public int hashCode() {
        int result = firmwareVersions != null ? firmwareVersions.hashCode() : 0;
        result = 31 * result + (models != null ? models.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FirmwareConfigInfo{");
        sb.append("firmwareVersions=").append(firmwareVersions);
        sb.append(", models=").append(models);
        sb.append('}');
        return sb.toString();
    }
}
