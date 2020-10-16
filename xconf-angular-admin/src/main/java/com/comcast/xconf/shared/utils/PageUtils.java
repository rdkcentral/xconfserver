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
 * Author: Stanislav Menshykov
 * Created: 04.11.15  15:35
 */
package com.comcast.xconf.shared.utils;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.hydra.astyanax.data.IPersistable;

import java.util.*;

public class PageUtils {

    public static <K, T extends IPersistable & Comparable> Map<K, T> getPageAsMap(final CachedSimpleDao<K, T> dao, final int page, final int pageSize) {
        final Map<K, T> result = new LinkedHashMap<>();
        final int size = page * pageSize;
        final Map<K, T> data = dao.getRowsAsMap(null, size, false);
        final int startIndex = size - pageSize;
        int i = 0;
        for (final Map.Entry<K, T> entry : data.entrySet()) {
            if (i++ < startIndex) {
                continue;
            }
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static <K, T extends IPersistable & Comparable> List<T> getPageAsList(final CachedSimpleDao<K, T> dao, final int page, final int pageSize) {
        return new ArrayList<>(getPageAsMap(dao, page, pageSize).values());
    }

    public static <T extends IPersistable & Comparable> List<T> getPage(final List<T> list, final int page, final int pageSize) {
        Collections.sort(list);
        return generatePage(list, page, pageSize);
    }

    public static <K, Object> Map<K, List<Object>> getPage(final Map<K, List<Object>> map, final int page, final int pageSize) {
        return generatePage(map, page, pageSize);
    }

    public static <T extends IPersistable & Comparable> List<T> getPage(final List<T> list, final int page, final int pageSize, Comparator comparator) {
        Collections.sort(list, comparator);
        return generatePage(list, page, pageSize);
    }

    private static <T extends IPersistable & Comparable> List<T> generatePage(final List<T> list, final int page, final int pageSize) {
        final List<T> result = new ArrayList<>();
        final int startIndex = page * pageSize - pageSize;
        if (page < 1 || startIndex > list.size()) {
            return result;
        }
        final int lastIndex = page * pageSize < list.size() ? page * pageSize : list.size();

        return list.subList(startIndex, lastIndex);
    }

    private static<K, Object> Map<K, List<Object>> generatePage(final Map<K, List<Object>> map, final int page, final int pageSize) {
        final Map<K, List<Object>> result = new LinkedHashMap<>();
        final int startIndex = page * pageSize - pageSize;
        if (page < 1 || startIndex > map.size()) {
            return result;
        }
        final int lastIndex = page * pageSize < map.size() ? page * pageSize : map.size();
        int i = 0;
        for (Map.Entry<K, List<Object>> entry : map.entrySet()) {
            if (i >= startIndex && i < lastIndex) {
                result.put(entry.getKey(), entry.getValue());
            }
            i++;
        }
        return result;
    }
}
