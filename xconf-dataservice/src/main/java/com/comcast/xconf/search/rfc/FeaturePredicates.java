/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
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

package com.comcast.xconf.search.rfc;

import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.search.ContextOptional;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class FeaturePredicates {

    public Predicate<Feature> byInstance(String instance) {
        return feature -> Objects.nonNull(feature)
                && StringUtils.containsIgnoreCase(feature.getFeatureName(), instance);
    }

    public Predicate<Feature> byName(String name) {
        return feature -> Objects.nonNull(feature)
                && StringUtils.containsIgnoreCase(feature.getName(), name);
    }

    public Predicate<Feature> byKey(String key) {
        return feature -> {
            if (Objects.nonNull(feature) && MapUtils.isNotEmpty(feature.getConfigData())) {
                for (String existingKey : feature.getConfigData().keySet()) {
                    if (StringUtils.containsIgnoreCase(existingKey, key)) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    public Predicate<Feature> byValue(String value) {
        return feature -> {
            if (Objects.nonNull(feature) && MapUtils.isNotEmpty(feature.getConfigData())) {
                for (String existingValue : feature.getConfigData().values()) {
                    if (StringUtils.containsIgnoreCase(existingValue, value)) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    public List<Predicate<Feature>> getPredicates(ContextOptional context) {
        List<Predicate<Feature>> predicates = new ArrayList<>();

        context.getApplicationType().ifPresent(application -> predicates.add(byApplication(application)));
        context.getFeatureInstance().ifPresent(featureInstance -> predicates.add(byInstance(featureInstance)));
        context.getName().ifPresent(name -> predicates.add(byName(name)));
        context.getKey().ifPresent(key -> predicates.add(byKey(key)));
        context.getValue().ifPresent(value -> predicates.add(byValue(value)));

        return predicates;
    }
}
