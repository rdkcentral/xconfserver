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

package com.comcast.xconf.search.changes;

import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.search.ContextOptional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class ChangePredicates<T extends Change> {

    public Predicate<T> byAuthor(String author) {
        return change -> Objects.nonNull(change)
                && StringUtils.containsIgnoreCase(change.getAuthor(), author);
    }

    public Predicate<T> byTelemetryProfileName(String name) {
        return change -> {
            if (Objects.nonNull(change)) {
                IPersistable entity = Objects.nonNull(change.getNewEntity()) ? change.getNewEntity() : change.getOldEntity();
                PermanentTelemetryProfile profile = (PermanentTelemetryProfile) entity;
                return profile != null && StringUtils.containsIgnoreCase(profile.getName(), name);
            }
            return false;
        };
    }

    public List<Predicate<T>> getPredicates(ContextOptional context) {
        List<Predicate<T>> predicates = new ArrayList<>();

        context.getApplicationType().ifPresent(application -> predicates.add(byApplication(application)));
        context.getAuthor().ifPresent(author -> predicates.add(byAuthor(author)));
        context.getEntity().ifPresent(profileName -> predicates.add(byTelemetryProfileName(profileName)));

        return predicates;
    }
}
