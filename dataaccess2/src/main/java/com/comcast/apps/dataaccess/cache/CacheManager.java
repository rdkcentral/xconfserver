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
package com.comcast.apps.dataaccess.cache;

import com.comcast.apps.dataaccess.cache.dao.CachedBaseDao;
import com.comcast.apps.dataaccess.cache.dao.CachedListingDao;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.cache.dao.impl.CachedListingDaoImpl;
import com.comcast.apps.dataaccess.cache.dao.impl.CachedSimpleDaoImpl;
import com.comcast.apps.dataaccess.cache.dao.impl.TwoKeys;
import com.comcast.apps.dataaccess.cache.mbean.CacheInfo;
import com.comcast.apps.dataaccess.config.CacheSettings;
import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.util.ExecutorUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.stereotype.Component;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;


@Component
public class CacheManager {

    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    private static final List<String> ignoreDuringRefresh = Arrays.asList("ApprovedChange", "PendingChange");

    private final Map<String, CacheInfo> cacheMBeans = Maps.newConcurrentMap();

    private final Map<Integer, CachedBaseDao> cachedDaos = new HashMap<>();

    @Autowired
    private CacheSettings cacheSettings;

    @Autowired
    private CacheConsistencyWriter cacheConsistencyWriter;

    @Autowired
    private MBeanExporter mBeanExporter;

    public CacheManager() {
    }

    public <K, T> CachedSimpleDao<K, T> augmentWithCache(SimpleDao<K, T> targetDao) {

        int daoId = targetDao.id();

        if (cachedDaos.containsKey(daoId)) {
            return (CachedSimpleDao<K, T>) cachedDaos.get(daoId);
        }

        final CacheLoader loader = CacheLoader.asyncReloading(
                CacheLoaderFactory.createSimpleCacheLoader(targetDao, targetDao.getKeyClass(), targetDao.getValueClass()),
                ExecutorUtil.getAsyncTaskProcessor()
        );

        LoadingCache<K, Optional<T>> cache = buildLoadingCache(loader);
        initiatePrecaching(targetDao.getValueClass().getSimpleName(), targetDao.getKeys(), cache);

        CachedSimpleDao<K, T> cachedSimpleDao = new CachedSimpleDaoImpl<>(targetDao, cache, cacheConsistencyWriter);

        registerCachedDao(cachedSimpleDao);

        return cachedSimpleDao;
    }

    private <K, T> LoadingCache<K, Optional<T>> buildLoadingCache(CacheLoader loader) {
        CacheBuilder cBuilder = CacheBuilder.newBuilder().recordStats();
        if (cacheSettings.isReloadCacheEntries()) {
            final long refreshTimeout = cacheSettings.getReloadCacheEntriesTimeout();
            final TimeUnit refreshTimeUnit = TimeUnit.valueOf(cacheSettings.getReloadCacheEntriesTimeUnit());
            cBuilder = cBuilder.refreshAfterWrite(refreshTimeout, refreshTimeUnit);
        }

        return cBuilder.build(loader);
    }

    public <K,N,T> CachedListingDao<K,N,T> augmentWithCache(ListingDao<K,N,T> targetDao) {

        int daoId = targetDao.id();

        if (cachedDaos.containsKey(daoId)) {
            return (CachedListingDao<K,N,T>) cachedDaos.get(daoId);
        }

        final CacheLoader loader = CacheLoader.asyncReloading(
                CacheLoaderFactory.createListingCacheLoader(targetDao, targetDao.getValueClass()),
                ExecutorUtil.getAsyncTaskProcessor()
        );
        LoadingCache<TwoKeys<K, N>, Optional<T>> cache = buildLoadingCache(loader);

        initiatePrecaching(targetDao.getValueClass().getSimpleName(), targetDao.getKeys(), cache);

        CachedListingDao<K,N,T> cachedListingDao = new CachedListingDaoImpl<>(targetDao, cache, cacheConsistencyWriter);

        registerCachedDao(cachedListingDao);

        return cachedListingDao;
    }

    private <T> void initiatePrecaching(String valueClassName, Iterable<?> keys, LoadingCache<?, Optional<T>> cache) {
        logger.info("Scheduling precaching for " + valueClassName);
        ExecutorUtil.doAsync(() -> {
            try {
                final long timestamp = System.currentTimeMillis();
                for (final Iterable<?> chunk :
                        Iterables.partition(keys, cacheSettings.getKeysetChunkSizeForMassCacheLoad())) {

                    ((LoadingCache) cache).getAll(chunk);
                }
                final int effectiveSetSize = Iterables.size(Optional.presentInstances(cache.asMap().values()));
                logger.info("{} precached {} entries in {}ms ( {} effective records, {} tombstones )",
                            valueClassName,
                            cache.size(),
                            System.currentTimeMillis() - timestamp,
                            effectiveSetSize,
                            cache.size() - effectiveSetSize
                );
            } catch (Exception e) {
                logger.error("Precaching failed for " + valueClassName, e);
            }
        });
    }

    public void registerCachedDao(final CachedBaseDao dao) {
        cachedDaos.put(dao.id(), dao);
        registerMBean(dao.asLoadingCache(), dao.getTargetDao().getCfName());
    }

    public CachedBaseDao getCachedDao(final int daoId) {
        return cachedDaos.get(daoId);
    }

    public boolean exists(final int daoId) {
        return cachedDaos.get(daoId) != null;
    }

    /**
     * Refresh all caches.
     * @return list with cfNames which were not refreshed
     */
    public List<String> refreshAll() throws Exception {
        final List<String> failedToRefreshCFs = new ArrayList<>();
        for (final Map.Entry<Integer, CachedBaseDao> cachedDao: cachedDaos.entrySet()) {
            final CachedBaseDao dao = cachedDao.getValue();
            String cfName = dao.getTargetDao().getCfName();
            if (!refreshAll(cfName, dao)) {
                failedToRefreshCFs.add(cfName);
            }
        }
        return failedToRefreshCFs;
    }

    /**
     * Refresh all caches.
     * @return list with cfNames which were not refreshed
     */
    public boolean refreshAll(String cf) throws Exception {
        CachedBaseDao cachedDao = getCachedDao(cf);
        return refreshAll(cf, cachedDao);
    }

    private boolean refreshAll(String cfName, CachedBaseDao dao) throws Exception {
        if (dao != null) {
            long timestamp = System.currentTimeMillis();
            dao.refreshAll();
            long daoRefreshTime = System.currentTimeMillis();

            LoadingCache cache = dao.asLoadingCache();
            int effectiveSetSize = Iterables.size(Optional.presentInstances(cache.asMap().values()));
            logger.info("Cache refreshed: {} precached {} entries in {}ms ( {} effective records, {} tombstones )",
                    cfName, cache.size(), daoRefreshTime - timestamp, effectiveSetSize, cache.size() - effectiveSetSize);
            return true;
        } else {
            logger.warn("Couldn't refresh cache. Not found DAO for CF: " + cfName);
            return false;
        }
    }

    public CachedBaseDao getCachedDao(String cf) {
        for (final Map.Entry<Integer, CachedBaseDao> cachedDao: cachedDaos.entrySet()) {
            final CachedBaseDao dao = cachedDao.getValue();
            if (dao.getTargetDao().getCfName().equals(cf)) {
                return dao;
            }
        }
        return null;
    }

    public Map<String, CacheInfo> getCacheMBeans() {
        return cacheMBeans;
    }

    private void registerMBean(LoadingCache cache, String tableName) {

        CacheInfo mbean = new CacheInfo(this, tableName, cache);

        String domainName = CacheManager.class.getPackage().getName();
        try {
            new ObjectName(domainName + ":name=" + tableName);
            mBeanExporter.registerManagedResource(mbean, new ObjectName(domainName + ":name=" + tableName));
        } catch (Exception e) {
            logger.error("Couldn't create mbean: " + e.getMessage());
        }

        cacheMBeans.put(tableName, mbean);
    }

    /**
     * Calculate hash for all objects in cf
     * @return long - hash value
     */
    public long calculateHash(String cfName) {
        if (ignoreDuringRefresh.contains(cfName)) {
            logger.warn("Cache doesn't exist for CF: " + cfName);
            return 0;
        }
        CachedBaseDao cachedDao = getCachedDao(cfName);
        CRC32 globalCrc = new CRC32();
        calculateCrcForCache(cachedDao, globalCrc);
        return globalCrc.getValue();
    }

    /**
     * Calculate hash for a specific object in cf
     * @return long - hash value
     */
    public long calculateHash(String cfName, String itemId) {
        if (ignoreDuringRefresh.contains(cfName)) {
            logger.warn("Cache doesn't exist for CF: " + cfName);
            return 0;
        }
        CachedBaseDao cachedDao = getCachedDao(cfName);
        return calculateCrcForItem(cachedDao, itemId);
    }

    private long calculateCrcForItem(CachedBaseDao cachedDao, String itemId) {
        if (cachedDao == null) {
            return 0;
        }
        Set<Map.Entry> set = cachedDao.asLoadingCache().asMap().entrySet();
        for (Map.Entry entry : set) {
            Object key = entry.getKey();
            Optional<Object> value = (Optional<Object>) entry.getValue();
            if (value.isPresent() && StringUtils.equals(itemId, key.toString())) {
                CRC32 localCrc = new CRC32();
                return countCrcForObject(value.get(), localCrc);
            }
        }
        return 0;
    }

    /**
     * Calculate hash of all objects in service
     * @return long - hash value
     */
    public long calculateHash() {
        try {
            CRC32 globalCrc = new CRC32();

            //Sorting by class name
            TreeMap<Integer, CachedBaseDao> treeMap = new TreeMap<>(cachedDaos);

            for (Map.Entry<Integer, CachedBaseDao> entry: treeMap.entrySet()) {
                CachedBaseDao value = entry.getValue();
                calculateCrcForCache(value, globalCrc);
            }
            return globalCrc.getValue();
        } catch (Exception e) {
            logger.error("Can't calculate cache hash", e);
            return 0;
        }
    }

    private void calculateCrcForCache(CachedBaseDao cachedDao, CRC32 globalCrc) {
        if (cachedDao == null) {
            return;
        }

        //store crc of each object to sorted map
        TreeMap<Long, Long> crcAndItsCount = new TreeMap<>();

        CRC32 localCrc = new CRC32();
        for (Object obj : Optional.presentInstances(cachedDao.asLoadingCache().asMap().values())) {
            if (obj != null) {
                Long crc = countCrcForObject(obj, localCrc);
                addObjectCrcToMap(crc, crcAndItsCount);
                localCrc.reset();
            }
        }

        for (Map.Entry<Long, Long> entryCrc : crcAndItsCount.entrySet()) {
            for (int i = 0; i < entryCrc.getValue(); i++) {
                globalCrc.update(entryCrc.getKey().byteValue());
            }
        }
    }

    private static Long countCrcForObject(Object obj, CRC32 localCrc) {
        String json = JsonUtil.toJson(obj);
        localCrc.update(json.getBytes());
        return localCrc.getValue();
    }

    private static void addObjectCrcToMap(Long crc, TreeMap<Long, Long> crcAndItsCount) {
        Long existingCount = crcAndItsCount.get(crc);
        if (existingCount == null) {
            existingCount = 1L;
        } else {
            existingCount++;
        }
        crcAndItsCount.put(crc, existingCount);
    }

}
