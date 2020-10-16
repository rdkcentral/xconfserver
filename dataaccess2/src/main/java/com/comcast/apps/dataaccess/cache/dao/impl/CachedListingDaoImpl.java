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

import com.comcast.apps.dataaccess.annotation.ListingCF;
import com.comcast.apps.dataaccess.annotation.ListingCFDefinition;
import com.comcast.apps.dataaccess.cache.CacheConsistencyWriter;
import com.comcast.apps.dataaccess.cache.dao.CachedListingDao;
import com.comcast.apps.dataaccess.cache.data.ChangedData;
import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.util.CloneUtil;
import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;



public class CachedListingDaoImpl<K, N, T> extends CachedBaseDaoImpl<K, T> implements CachedListingDao<K, N, T> {
    private final ListingDao<K, N, T> targetDao;
    private final LoadingCache<TwoKeys<K, N>, Optional<T>> cache;
    private final CacheConsistencyWriter cacheConsistencyWriter;

    private final String key2FieldName;

    public CachedListingDaoImpl(final ListingDao<K, N, T> targetDao,
                                LoadingCache<TwoKeys<K, N>, Optional<T>> cache,
                                CacheConsistencyWriter cacheConsistencyWriter) {
        super(targetDao);
        this.targetDao = targetDao;
        this.cache = cache;
        this.cacheConsistencyWriter = cacheConsistencyWriter;

        ListingCF listingCf = targetDao.getValueClass().getAnnotation(ListingCF.class);
        key2FieldName = listingCf.key2FieldName();
    }

    public CachedListingDaoImpl(final ListingDao<K, N, T> targetDao,
                                LoadingCache<TwoKeys<K, N>, Optional<T>> cache,
                                CacheConsistencyWriter cacheConsistencyWriter,
                                ListingCFDefinition listingCFDefinition) {
        super(targetDao);
        this.targetDao = targetDao;
        this.cache = cache;
        this.cacheConsistencyWriter = cacheConsistencyWriter;

        key2FieldName = listingCFDefinition.key2FieldName();
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
                final TwoKeys<K, N> twoKeys = new TwoKeys<>(rowKey, readKeys2(entity));
                cache.put(twoKeys, Optional.fromNullable(entity));
                cacheConsistencyWriter.writeCacheLog(targetDao.getCfName(), twoKeys, ChangedData.Operation.CREATE, super.id(), Iterables.size(Optional.presentInstances(cache.asMap().values())));
            }
        }
    }

    @Override
    public T setOne(K rowKey, N key2, T entity) {
        boolean successful = true;
        try {
            return targetDao.setOne(rowKey, key2, entity);
        } catch (Exception e) {
            successful = false;
            throw e;
        } finally {
            if (successful) {
                final TwoKeys<K, N> twoKeys = new TwoKeys<>(rowKey, key2);
                cache.put(twoKeys, Optional.fromNullable(entity));
                cacheConsistencyWriter.writeCacheLog(targetDao.getCfName(), twoKeys, ChangedData.Operation.CREATE, super.id(), Iterables.size(Optional.presentInstances(cache.asMap().values())));
            }
        }
    }

    private N readKeys2(T entity) {
        try {
            return (N) FieldUtils.readDeclaredField(entity, key2FieldName,true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteOne(K rowKey, N key2) {
        boolean successful = true;
        try {
            targetDao.deleteOne(rowKey, key2);
        } catch (Exception e) {
            successful = false;
            throw new RuntimeException(e);
        } finally {
            if (successful) {
                final TwoKeys<K, N> twoKeys = new TwoKeys<>(rowKey, key2);
                cache.invalidate(twoKeys);
                cacheConsistencyWriter.writeCacheLog(targetDao.getCfName(), twoKeys, ChangedData.Operation.DELETE, super.id(), Iterables.size(Optional.presentInstances(cache.asMap().values())));
            }
        }
    }

    @Override
    public T getOne(K rowKey, N key2) {
        try {
            final T res = cache.get(new TwoKeys<>(rowKey, key2)).orNull();
            return res != null ? CloneUtil.clone(res) : null;
        } catch (ExecutionException e) {
            return targetDao.getOne(rowKey, key2);
        }
    }

    @Override
    public List<T> getAll(K rowKey) {
        List<T> result = cache.asMap().entrySet().stream()
                .filter(entry -> {
                    TwoKeys twoKeys = entry.getKey();
                    Object currentRowKey = twoKeys.getKey();
                    return rowKey.equals(currentRowKey);
                })
                .map(Map.Entry::getValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return CloneUtil.clone(result);
    }

    @Override
    public Iterable<N> getKeys(K rowKey) {
        return CloneUtil.clone(
                cache.asMap().keySet().stream()
                        .filter(twoKeys -> twoKeys.getKey().equals(rowKey))
                        .map(TwoKeys::getKey2)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Iterable<TwoKeys<K,N>> getKeys() {
        return CloneUtil.clone(
                new ArrayList<>(cache.asMap().keySet())
        );
    }

    @Override
    public Map<N, Optional<T>> getAllAsMap(K rowKey, Set<N> keys2) {
        List<TwoKeys<K, N>> allKeys = getTwoKeys(rowKey, keys2);
        try {
            Map<TwoKeys<K, N>, Optional<T>> resultWithTwoKeys = cache.getAll(allKeys);
            Map<N, Optional<T>> result = new HashMap<>();
            resultWithTwoKeys.entrySet().forEach(entry -> result.put(entry.getKey().getKey2(), entry.getValue()));

            return CloneUtil.clone(result);
        } catch (ExecutionException e) {
            getLogger().error("could not get values as map", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public LoadingCache<TwoKeys<K, N>, Optional<T>> asLoadingCache() {
        return cache;
    }

    @Override
    public void refreshAll() throws Exception {
        final Set<TwoKeys<K, N>> allKeys = Sets.newHashSet(targetDao.getKeys());
        allKeys.addAll(cache.asMap().keySet());
        for (final TwoKeys<K, N> key : allKeys) {
            cache.invalidate(key);
            cache.get(key);
        }
    }

    @Override
    public ListingDao<K, N, T> getTargetDao() {
        return this.targetDao;
    }

    private List<TwoKeys<K, N>> getTwoKeys(K rowKey, Set<N> keys2) {
        List<TwoKeys<K, N>> result = new ArrayList<>();
        keys2.forEach(key2 -> result.add(new TwoKeys<>(rowKey, key2)));

        return result;
    }
}
