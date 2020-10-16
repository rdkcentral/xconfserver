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
 * Created: 18.03.2020
 */
package com.comcast.xconf.rfc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeatureExport extends Feature {

    public FeatureExport(Feature feature) {
        this.setId(feature.getId());
        this.setName(feature.getName());
        this.setFeatureName(feature.getFeatureName());
        this.setApplicationType(feature.getApplicationType());
        this.setConfigData(feature.getConfigData());
        this.setEffectiveImmediate(feature.isEffectiveImmediate());
        this.setEnable(feature.isEnable());
        this.setWhitelisted(feature.isWhitelisted());
        this.setWhitelistProperty(feature.getWhitelistProperty());
    }

    @Override
    @JsonProperty("featureInstance")
    public String getFeatureName() {
        return super.getFeatureName();
    }
}
