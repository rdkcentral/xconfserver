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

package com.comcast.xconf.search.dcm;

import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.search.ContextOptional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class DeviceSettingPredicates {

    public Predicate<DeviceSettings> byName(String name) {
        return deviceSettings -> Objects.nonNull(deviceSettings)
                && StringUtils.containsIgnoreCase(deviceSettings.getName(), name);
    }

    public List<Predicate<DeviceSettings>> getPredicates(ContextOptional context) {
        List<Predicate<DeviceSettings>> predicates = new ArrayList<>();

        context.getName().ifPresent(name -> predicates.add(byName(name)));
        context.getApplicationType().ifPresent(application -> predicates.add(byApplication(application)));

        return predicates;
    }
}
