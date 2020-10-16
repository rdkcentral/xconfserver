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

package com.comcast.xconf.search;

import com.comcast.xconf.Applicationable;
import com.comcast.xconf.firmware.ApplicationType;

import java.util.Objects;
import java.util.function.Predicate;

public class ApplicationTypePredicate {

    public static <T extends Applicationable> Predicate<T> byApplication(String application) {
        return entity -> Objects.nonNull(entity)
                && (ApplicationType.ALL.equals(application)
                    || ApplicationType.equals(entity.getApplicationType(), application));
    }
}
