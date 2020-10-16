/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */
package com.comcast.apps.dataaccess.dao;

import com.comcast.apps.dataaccess.cache.dao.impl.TwoKeys;
import com.comcast.apps.dataaccess.dao.query.RangeInfo;
import com.comcast.apps.dataaccess.data.Persistable;
import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface ListingDao<K, N, T> extends BaseDao<K, T> {

    T setOne(K rowKey, T entity);

    default T setOne(K rowKey, N key2, T entity){
        return setOne(rowKey, entity);
    }

    void deleteAll(K rowKey);

    void deleteOne(K rowKey, N key2);

    T getOne(K rowKey, N key2);

    List<T> getAll(K rowKey);

    List<T> getAll();

    List<T> getRange(K rowKey, RangeInfo range);

    List<T> getRange(Map<K, RangeInfo<N>> ranges);

    Iterable<TwoKeys<K, N>> getKeys();

    Iterable<N> getKeys(K rowKey);

    Map<N, Optional<T>> getAllAsMap(K rowKey, Set<N> keys2);

    Class<N> getKey2Class();

}
