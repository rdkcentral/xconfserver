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
 *  Author: mdolina
 *  Created: 2:06 PM
 */
package com.comcast.xconf.rfc;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FeatureControl {

    private Set<FeatureResponse> features = new HashSet<>();

    public Set<FeatureResponse> getFeatures() {
        return features;
    }

    public void setFeatures(Set<FeatureResponse> features) {
        this.features = features;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureControl that = (FeatureControl) o;
        return Objects.equals(features, that.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(features);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FeatureControl{");
        sb.append("features=").append(features);
        sb.append('}');
        return sb.toString();
    }
}
