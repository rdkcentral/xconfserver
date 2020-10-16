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
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.Date;
import java.util.Map;

@CF(cfName = "Feature", keyType = String.class)
public class FeatureLegacy implements IPersistable, Comparable<Feature> {
    private String id;
    private String name;
    private boolean effectiveImmediate;
    private boolean enable;
    private Map<String, String> configData;

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

    @Override
    public int compareTo(Feature o) {
        String id1 = (name != null) ? name.toLowerCase() : null;
        String id2 = (o != null && o.getName() != null) ? o.getName().toLowerCase() : null;
        return new NullComparator().compare(id1, id2);
    }
}
