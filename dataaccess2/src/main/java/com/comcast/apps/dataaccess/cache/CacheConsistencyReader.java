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
import com.comcast.apps.dataaccess.cache.dao.ChangedKeysProcessingDaoImpl;
import com.comcast.apps.dataaccess.cache.dao.impl.TwoKeys;
import com.comcast.apps.dataaccess.cache.data.ChangedData;
import com.comcast.apps.dataaccess.config.CacheSettings;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;


@Component
public class CacheConsistencyReader {
    private final Logger logger = LoggerFactory.getLogger(CacheConsistencyReader.class);

    private final int retryUntilFullReload;
    private long lastRefreshedTimestamp;
    private int refreshAttemptsLeft;
    private final long delay;
    private final long reloadPeriod;

    @Autowired
    private ChangedKeysProcessingDaoImpl changedKeysProcessingDao;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    public CacheConsistencyReader(final CacheSettings cacheSettings) {
        final long now = DateTime.now(DateTimeZone.UTC).getMillis();
        reloadPeriod = cacheSettings.getTickDuration();
        delay = reloadPeriod - (now % reloadPeriod);

        this.lastRefreshedTimestamp = now + delay;
        this.retryUntilFullReload = cacheSettings.getRetryCountUntilFullRefresh();
        this.refreshAttemptsLeft = retryUntilFullReload;
    }

    @PostConstruct
    private void init() {
        new Timer().scheduleAtFixedRate(new ReadTask(), delay, reloadPeriod);
    }

    private final class ReadTask extends TimerTask {

        /**
         * Loads changed data. Or completely reloads cache if partial load failed retryUntilFullReload times in a row.
         */
        @Override
        public void run() {
            long now = DateTime.now(DateTimeZone.UTC).getMillis();
            logger.info("starting cache update for[{} - {}], system time - {}, cache_hash={}", lastRefreshedTimestamp, now, System.currentTimeMillis(), cacheManager.calculateHash());
            if (refreshAttemptsLeft == 0) {     // load all data
                logger.info("Attempting full refresh");
                try {
                    lastRefreshedTimestamp = now;
                    cacheManager.refreshAll();
                    refreshAttemptsLeft++;
                } catch (Exception e) {
                    logger.warn("Exception caught while trying to sync cache changes", e);
                }
            } else {    // load only changed data
                try {
                    loadChanges(lastRefreshedTimestamp, now);
                    lastRefreshedTimestamp = now;
                    refreshAttemptsLeft = retryUntilFullReload;
                } catch (Exception e) {
                    logger.warn("Exception caught while trying to sync cache changes", e);
                    refreshAttemptsLeft--;
                }
            }
         }

        /**
         * Loads changes for given time-window defined by {@param #start} & {@param #end} parameters
         *
         * @param start lower time-window bound, inclusive
         * @param end   upper time-window bound, exclusive
         */
        private void loadChanges(long start, long end) throws Exception {
            final PeekingIterator<ChangedData> changedKeysIterator = Iterators.peekingIterator(
                    changedKeysProcessingDao.getIteratedChangedKeysForTick(start, end));
            MutableInt maybeUpdated = new MutableInt(0);

            while (changedKeysIterator.hasNext()) {
                final ChangedData data = changedKeysIterator.next();
                final ChangedData nextData = changedKeysIterator.hasNext() ? changedKeysIterator.peek() : null;

                if(data.getChangedKey() == null || data.getDAOid() == null
                        || data.getOperation() == null || data.getValidCacheSize() == null
                        || (nextData != null && nextData.getOperation() == null)) {
                    logger.warn("Unable to load changed data");
                    continue;
                }

                final boolean updateDetected = nextData != null ?
                        data.getChangedKey().equals(nextData.getChangedKey())
                                && data.getDAOid().equals(nextData.getDAOid())
                                && data.getOperation().equals(ChangedData.Operation.DELETE)
                                && nextData.getOperation().equals(ChangedData.Operation.CREATE)
                        : false;

                if (!cacheManager.exists(data.getDAOid())) {
                    logger.warn("Unable to locate cache for {} given {} as id (changed key is {})",
                            data.getCfName(), data.getDAOid(), data.getChangedKey());
                    continue;
                }

//                List<Class<?>> typeParams;
//                try {
//                    typeParams = Lists.newArrayList(cachedDaoRegister.exists((data.getDAOid()));
//                } catch (Exception e) {
//                    log.warn("Unable to get typeParams by daoId: {}", data.getDAOid());
//                    throw e;
//                }

                CachedBaseDao cachedDao;
                try {
                    cachedDao = cacheManager.getCachedDao(data.getDAOid());
                } catch(Exception e) {
                    logger.warn("Unable to get cache by daoId: {}", data.getDAOid());
                    throw e;
                }

                try {
                    refreshCache(data, cachedDao, cachedDao.asLoadingCache(), updateDetected, changedKeysIterator, maybeUpdated);
                } catch(Exception e) {
                    logger.warn("Unable to refresh cache for {} CF", data.getCfName());
                    throw e;
                }
            }

            if (maybeUpdated.toInteger() > 0) {
                logger.info("Expected to refresh {} entries", maybeUpdated);
            }
        }

        private void refreshCache(ChangedData data, CachedBaseDao cachedDao, LoadingCache cache, boolean updateDetected, PeekingIterator<ChangedData> changedKeysIterator, MutableInt maybeUpdated) throws Exception {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Processing ").append(data.getOperation()).append(" for ").append(cachedDao.getTargetDao().getKeyClass().getSimpleName());
            final String changedKeyString = data.getChangedKey();

            Object changedKey = null;

            if (cachedDao instanceof CachedSimpleDao) {
                changedKey = JsonUtil.fromJson(cachedDao.getTargetDao().getKeyClass(), data.getChangedKey());
            } else if (cachedDao instanceof CachedListingDao) {
                final CachedListingDao listingDao = ((CachedListingDao) cachedDao);
                changedKey = JsonUtil.fromJson(data.getChangedKey(), TwoKeys.class, listingDao.getTargetDao().getKeyClass(), listingDao.getTargetDao().getKey2Class());
            }

            stringBuilder.append(" type=").append(cachedDao.getTargetDao().getValueClass().getSimpleName()).append(" key=").append(changedKeyString);
            if(updateDetected) {
                logger.info("detected UPDATE for " + changedKeyString);
                data.setOperation(ChangedData.Operation.UPDATE);
                changedKeysIterator.next();
            }
            switch (data.getOperation()) {
                case CREATE:
                    cache.invalidate(changedKey);   //add to cache
                    cache.get(changedKey);   //add to cache
                case UPDATE: {
                    maybeUpdated.increment();
                    cache.invalidate(changedKey);  // to guarantee key is not stuck Optional::Absent
                    cache.get(changedKey);  // to guarantee key is not stuck Optional::Absent
                    logger.info(stringBuilder.toString());
                    break;
                }
                case DELETE: {
                    maybeUpdated.increment();
                    cache.invalidate(changedKey);    // evict key
                    logger.info(stringBuilder.toString());
                    break;
                }
                case TRUNCATE_CF: {
                    cache.invalidateAll();
                    logger.info(stringBuilder.toString());
                    break;
                }
            }

            final int cachesize = Iterables.size(Optional.presentInstances(cache.asMap().values()));
            if (cachesize < data.getValidCacheSize()) {
                logger.warn("sizes differ for caches, got {} instead of {}, scheduling full refresh for {}",
                        new Object[]{cachesize, data.getValidCacheSize(), cachedDao.getTargetDao().getCfName()});

                cachedDao.refreshAll();
            }
        }

    }
}
