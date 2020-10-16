/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.queries.beans;

import com.comcast.xconf.estbfirmware.EnvModelPercentage;
import com.comcast.xconf.estbfirmware.PercentFilterValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "PercentFilterWrapper")
@JsonIgnoreProperties({"percent", "envModelPercentages"})
public class PercentFilterWrapper extends PercentFilterValue {

    @JsonProperty("EnvModelPercentages")
    private List<EnvModelPercentageWrapper> envModelPercentageWrappers = new ArrayList<>();

    public PercentFilterWrapper() { }

    public PercentFilterWrapper(PercentFilterValue percentFilterValue) {
        setId(percentFilterValue.getId());
        setWhitelist(percentFilterValue.getWhitelist());
        setPercentage(percentFilterValue.getPercentage());
        if (percentFilterValue.getEnvModelPercentages() != null) {
            for (String key : percentFilterValue.getEnvModelPercentages().keySet()) {
                EnvModelPercentage envModelPercentage = percentFilterValue.getEnvModelPercentages().get(key);
                if (!envModelPercentage.isFirmwareCheckRequired()) {
                    envModelPercentage.setRebootImmediately(false);
                }
                this.envModelPercentageWrappers.add(new EnvModelPercentageWrapper(key, envModelPercentage));
            }
            super.setEnvModelPercentages(percentFilterValue.getEnvModelPercentages());
        }
    }

    public PercentFilterValue toPercentFilterValue() {
        Map<String, EnvModelPercentage> map = new HashMap<>();
        for (EnvModelPercentageWrapper wrapper : envModelPercentageWrappers) {
            map.put(wrapper.getName(), wrapper.toEnvModelPercentage());
        }
        return new PercentFilterValue(getWhitelist(), getPercentage(), map);

    }

    public List<EnvModelPercentageWrapper> getEnvModelPercentageWrappers() {
        return envModelPercentageWrappers;
    }

    public void setEnvModelPercentageWrappers(List<EnvModelPercentageWrapper> envModelPercentageWrappers) {
        this.envModelPercentageWrappers = envModelPercentageWrappers;
    }
}
