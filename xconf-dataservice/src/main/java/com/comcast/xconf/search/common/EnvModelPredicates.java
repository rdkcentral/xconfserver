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

package com.comcast.xconf.search.common;

import com.comcast.xconf.XEnvModel;
import com.comcast.xconf.search.ContextOptional;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Service
public class EnvModelPredicates<T extends XEnvModel> {

    public Predicate<T> byId(String id) {
        return entity -> Objects.nonNull(entity)
                && StringUtils.containsIgnoreCase(entity.getId(), id);
    }

    public Predicate<T> byDescription(String description) {
        return entity -> Objects.nonNull(entity)
                && StringUtils.containsIgnoreCase(entity.getDescription(), description);
    }

    public List<Predicate<T>> getPredicates(ContextOptional context) {
        List<Predicate<T>> predicates = new ArrayList<>();

        context.getId().ifPresent(id -> predicates.add(byId(id)));
        context.getDescription().ifPresent(description -> predicates.add(byDescription(description)));

        return predicates;
    }
}
