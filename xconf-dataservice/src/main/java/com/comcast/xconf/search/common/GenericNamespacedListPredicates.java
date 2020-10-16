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

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.comcast.xconf.GenericNamespacedList.isIpList;
import static com.comcast.xconf.GenericNamespacedList.isMacList;

@Service
public class GenericNamespacedListPredicates {

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    private final Logger logger = LoggerFactory.getLogger(GenericNamespacedListPredicates.class);

    public Predicate<GenericNamespacedList> byId(String id) {
        return namespacedList -> Objects.nonNull(namespacedList)
                && StringUtils.containsIgnoreCase(namespacedList.getId(), id);
    }

    public Predicate<GenericNamespacedList> byType(String type) {
        return genericNamespacedList -> Objects.nonNull(genericNamespacedList)
                && StringUtils.equals(genericNamespacedList.getTypeName(), type);
    }

    public Predicate<GenericNamespacedList> byData(String data) {
        return namespacedList -> {
            if (StringUtils.isBlank(data)) {
                return true;
            }
            try {
                if (isIpList(namespacedList)) {
                    return genericNamespacedListQueriesService.isIpAddressHasIpPart(data, namespacedList.getData());
                } else if (isMacList(namespacedList)) {
                    return genericNamespacedListQueriesService.isMacListHasMacPart(data, namespacedList.getData());
                }
            } catch (Exception e) {
                logger.error("Exception: " + e.getMessage());
            }
            return false;
        };
    }

    public List<Predicate<GenericNamespacedList>> getPredicates(ContextOptional context) {
        List<Predicate<GenericNamespacedList>> predicates = new ArrayList<>();

        context.getName().ifPresent(name -> predicates.add(byId(name)));
        context.getData().ifPresent(data -> predicates.add(byData(data)));
        context.getType().ifPresent(type -> predicates.add(byType(type)));

        return predicates;
    }
}
