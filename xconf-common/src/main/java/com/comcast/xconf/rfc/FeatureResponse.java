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
 * <p>
 * Author: mdolina
 * Created: 5/10/17  7:08 PM
 */
package com.comcast.xconf.rfc;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties({"ttl", "updated", "whitelisted"})
public class FeatureResponse extends Feature {

    private Map<String, Object> properties = new HashMap<>();

    private String listType;

    private Integer listSize;

    public FeatureResponse() {}

    public FeatureResponse(Feature feature) {
        this.setId(feature.getId());
        this.setName(feature.getName());
        this.setFeatureName(feature.getFeatureName());
        this.setEnable(feature.isEnable());
        this.setEffectiveImmediate(feature.isEffectiveImmediate());
        this.setConfigData(feature.getConfigData());
    }

    @JsonProperty("featureInstance")
    public String getFeatureName() {
        return super.getFeatureName();
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public Integer getListSize() {
        return listSize;
    }

    public void setListSize(Integer listSize) {
        this.listSize = listSize;
    }
}
