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
 * Author: Yury Stagit
 * Created: 02/11/16  12:00 PM
 */

package com.comcast.xconf.rfc;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.firmware.ApplicationType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@CF(cfName = "XconfFeature", keyType = String.class)
public class Feature implements IPersistable, Comparable<Feature>, Applicationable {
    private String id;

    private String name;

    @JsonAlias(value = "featureInstance")
    private String featureName;

    private boolean effectiveImmediate;

    private boolean enable;

    private boolean whitelisted;

    private Map<String, String> configData = new HashMap<>();

    private WhitelistProperty whitelistProperty;

    private String applicationType = ApplicationType.STB;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Date getUpdated() {
        return null;
    }

    @JsonIgnore
    @Override
    public void setUpdated(Date timestamp) {

    }

    @Override
    public int getTTL(String column) {
        return 0;
    }

    @JsonIgnore
    @Override
    public void setTTL(String column, int value) {

    }

    @JsonIgnore
    @Override
    public void clearTTL() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getConfigData() {
        return configData;
    }

    public void setConfigData(Map<String, String> configData) {
        this.configData = configData;
    }

    public boolean isEffectiveImmediate() {
        return effectiveImmediate;
    }

    public void setEffectiveImmediate(boolean effectiveImmediate) {
        this.effectiveImmediate = effectiveImmediate;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public boolean isWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    public WhitelistProperty getWhitelistProperty() {
        return whitelistProperty;
    }

    public void setWhitelistProperty(WhitelistProperty whitelistProperty) {
        this.whitelistProperty = whitelistProperty;
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
    public int compareTo(Feature o) {
        String id1 = (name != null) ? name.toLowerCase() : null;
        String id2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(id1, id2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Feature feature = (Feature) o;

        if (effectiveImmediate != feature.effectiveImmediate) return false;
        if (enable != feature.enable) return false;
        if (whitelisted != feature.whitelisted) return false;
        if (id != null ? !id.equals(feature.id) : feature.id != null) return false;
        if (name != null ? !name.equals(feature.name) : feature.name != null) return false;
        if (featureName != null ? !featureName.equals(feature.featureName) : feature.featureName != null) return false;
        if (configData != null ? !configData.equals(feature.configData) : feature.configData != null) return false;
        if (whitelistProperty != null ? !whitelistProperty.equals(feature.whitelistProperty) : feature.whitelistProperty != null)
            return false;
        return applicationType != null ? applicationType.equals(feature.applicationType) : feature.applicationType == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (featureName != null ? featureName.hashCode() : 0);
        result = 31 * result + (effectiveImmediate ? 1 : 0);
        result = 31 * result + (enable ? 1 : 0);
        result = 31 * result + (whitelisted ? 1 : 0);
        result = 31 * result + (configData != null ? configData.hashCode() : 0);
        result = 31 * result + (whitelistProperty != null ? whitelistProperty.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Feature{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", featureName='").append(featureName).append('\'');
        sb.append(", effectiveImmediate=").append(effectiveImmediate);
        sb.append(", enable=").append(enable);
        sb.append(", whitelisted=").append(whitelisted);
        sb.append(", configData=").append(configData);
        sb.append(", whitelistProperty=").append(whitelistProperty);
        sb.append(", applicationType='").append(applicationType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
