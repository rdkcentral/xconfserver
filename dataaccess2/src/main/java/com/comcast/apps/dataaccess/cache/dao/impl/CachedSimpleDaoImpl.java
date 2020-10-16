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
package com.comcast.apps.dataaccess.cache.dao.impl;

import com.comcast.apps.dataaccess.cache.CacheConsistencyWriter;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.cache.data.ChangedData;
import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.util.CloneUtil;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class CachedSimpleDaoImpl<K, T> extends CachedBaseDaoImpl<K, T> implements CachedSimpleDao<K, T> {

    private final SimpleDao<K, T> targetDao;
    private final LoadingCache<K, Optional<T>> cache;
    private final CacheConsistencyWriter cacheConsistencyWriter;

    public CachedSimpleDaoImpl(final SimpleDao<K, T> targetDao,
                               LoadingCache<K, Optional<T>> cache,
                               CacheConsistencyWriter cacheConsistencyWriter) {
        super(targetDao);
        this.targetDao = targetDao;
        this.cache = cache;
        this.cacheConsistencyWriter = cacheConsistencyWriter;
    }

    @Override
    public T getOne(K rowKey) {
        try {
            final T res = cache.get(rowKey).orNull();
            return res != null ? CloneUtil.clone(res) : null;
        } catch (ExecutionException e) {
            return targetDao.getOne(rowKey);
        }
    }

    @Override
    public T getOne(K rowKey, boolean clone) {
        try {
            final T res = cache.get(rowKey).orNull();
            return (clone && res != null) ? CloneUtil.clone(res) : res;
        } catch (ExecutionException e) {
            return targetDao.getOne(rowKey);
        }
    }

    @Override
    public T setOne(K rowKey, T entity) {
        boolean successful = true;
        try {
            return targetDao.setOne(rowKey, entity);
        } catch (Exception e) {
            successful = false;
            throw e;
        } finally {
            if (successful) {
                cache.put(rowKey, Optional.fromNullable(entity));
                cacheConsistencyWriter.writeCacheLog(targetDao.getCfName(), rowKey, ChangedData.Operation.CREATE, super.id(), Iterables.size(Optional.presentInstances(cache.asMap().values())));
            }
        }
    }

    @Override
    public void deleteOne(K rowKey) {
        boolean successful = true;
        try {
            targetDao.deleteOne(rowKey);
        } catch (Exception e) {
            successful = false;
            throw new RuntimeException(e);
        } finally {
            if (successful) {
                cache.invalidate(rowKey);
                cacheConsistencyWriter.writeCacheLog(targetDao.getCfName(), rowKey, ChangedData.Operation.DELETE, super.id(), Iterables.size(Optional.presentInstances(cache.asMap().values())));
            }
        }
    }

    @Override
    public List<T> getAll() {
        return CloneUtil.clone(
                Lists.newArrayList(
                        Optional.presentInstances(cache.asMap().values())
                )
        );
    }

    @Override
    public List<T> getAll(int maxResults) {
        if (maxResults <= 0) {
            return new ArrayList<>();
        }
        List<T> result = Lists.newArrayList(Optional.presentInstances(cache.asMap().values()));
        result = result.size() <= maxResults ? result : new ArrayList<>(result.subList(0, maxResults));
        return CloneUtil.clone(result);
    }

    @Override
    public List<T> getAll(Predicate<T> filter) {
        Iterable<T> allValues = Optional.presentInstances(cache.asMap().values());
        return CloneUtil.clone(Lists.newArrayList(Iterables.filter(allValues, filter)));
    }

    @Override
    public List<T> getAll(Set<K> keys) {
        try {
            final List<T> res = Lists.newArrayList(Optional.presentInstances(cache.getAll(keys).values()));
            return CloneUtil.clone(res);
        } catch (ExecutionException e) {
            getLogger().debug("exception while trying to return cached falling back to noncached implementation");
            return targetDao.getAll(keys);
        }
    }


    @Override
    public Map<K, Optional<T>> getAllAsMap(Set<K> keys) {
        try {
            final Map<K, Optional<T>> filtered = Maps.newHashMap(
                    Maps.filterValues(cache.getAll(keys), input -> input.isPresent())
            );
            return CloneUtil.clone(filtered);
        } catch (ExecutionException e) {
            getLogger().error("could not get values as map", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<K, T> getRowsAsMap(K from, int size, boolean reversed) {
        final Map<K, T> result = new LinkedHashMap<K, T>();
        final Map<K, Optional<T>> allEntities = asLoadingCache().asMap();
        Iterator<Map.Entry<K, Optional<T>>> iterator = allEntities.entrySet().iterator();
        boolean count = false;
        if (from == null && iterator.hasNext()) {
            count = true;
        }
        while (iterator.hasNext() && result.size() < size) {
            final Map.Entry<K, Optional<T>> entry = iterator.next();
            final K key = entry.getKey();
            if (!count) {
                if (!key.equals(from)) {
                    continue;
                } else {
                    count = true;
                }
            }
            final Optional<T> value = entry.getValue();
            if (!value.isPresent()) {
                continue;
            }
            result.put(CloneUtil.clone(key), CloneUtil.clone(value.get()));
        }
        return result;
    }

    @Override
    public void refreshAll() throws Exception {
        final Set<K> allKeys = Sets.newHashSet(targetDao.getKeys());
        allKeys.addAll(cache.asMap().keySet());
        for (final K key : allKeys) {
            cache.refresh(key);
        }
    }

    @Override
    public LoadingCache<K, Optional<T>> asLoadingCache() {
        return cache;
    }

    @Override
    public SimpleDao<K, T> getTargetDao() {
        return targetDao;
    }

    @Override
    public Iterable<K> getKeys() {
        return CloneUtil.clone(
                Sets.newHashSet(cache.asMap().keySet())
        );
    }
}
