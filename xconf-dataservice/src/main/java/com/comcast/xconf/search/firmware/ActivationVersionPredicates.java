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

package com.comcast.xconf.search.firmware;

import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.search.ContextOptional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class ActivationVersionPredicates {

    public Predicate<ActivationVersion> byFirmwareVersion(String version) {
        return activationVersion -> {
            if (Objects.isNull(activationVersion) || CollectionUtils.isEmpty(activationVersion.getFirmwareVersions())) {
                return false;
            }
            for (String existingVersion : activationVersion.getFirmwareVersions()) {
                if (StringUtils.containsIgnoreCase(existingVersion, version)) {
                    return true;
                }
            }
            return false;
        };
    }

    public Predicate<ActivationVersion> byRegularExpression(String regularExpression) {
        return activationVersion -> {
            if (Objects.isNull(activationVersion) || CollectionUtils.isEmpty(activationVersion.getRegularExpressions())) {
                return false;
            }
            for (String expression : activationVersion.getRegularExpressions()) {
                if (StringUtils.containsIgnoreCase(expression, regularExpression)) {
                    return true;
                }
            }
            return false;
        };
    }

    public Predicate<ActivationVersion> byPartnerId(String partnerId) {
        return activationVersion -> Objects.nonNull(activationVersion)
                && StringUtils.containsIgnoreCase(activationVersion.getPartnerId(), partnerId);
    }

    public Predicate<ActivationVersion> byModel(String modelId) {
        return activationVersion -> Objects.nonNull(activationVersion)
                && StringUtils.containsIgnoreCase(activationVersion.getModel(), modelId);

    }

    public Predicate<ActivationVersion> byDescription(String description) {
        return activationVersion -> Objects.nonNull(activationVersion)
                && StringUtils.containsIgnoreCase(activationVersion.getDescription(), description);
    }

    public List<Predicate<ActivationVersion>> getPredicates(ContextOptional context) {
        List<Predicate<ActivationVersion>> predicates = new ArrayList<>();

        context.getDescription().ifPresent(description -> predicates.add(byDescription(description)));
        context.getModel().ifPresent(model -> predicates.add(byModel(model)));
        context.getPartnerId().ifPresent(partnerId -> predicates.add(byPartnerId(partnerId)));
        context.getRegExp().ifPresent(regExp -> predicates.add(byRegularExpression(regExp)));
        context.getFirmwareVersion().ifPresent(firmwareVersion -> predicates.add(byFirmwareVersion(firmwareVersion)));
        context.getApplicationType().ifPresent(application -> predicates.add(byApplication(application)));

        return predicates;
    }
}
