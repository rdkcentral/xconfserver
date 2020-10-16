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
package com.comcast.apps.dataaccess.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource(
        value = {"classpath:service.properties", "file:${appConfig}"}, ignoreResourceNotFound = true)
public class CacheSettings {

    /**
     * Duration of tick for which we check for changed keys in cassandra
     */
    @Value("${dataaccess.cache.tickDuration:60000}")
    private int tickDuration;

    /**
     * Changed keys retry load count until a full refresh is attempted
     */
    @Value("${dataaccess.cache.retryCountUntilFullRefresh:10}")
    private int retryCountUntilFullRefresh;


    @Value("${dataaccess.cache.changedKeysTimeWindowSize:900000}")
    private long changedKeysTimeWindowSize;

    /**
     * Indicates whether or not cache keys are elapsing
     */
    @Value("${dataaccess.cache.reloadCacheEntries:false}")
    private boolean reloadCacheEntries;

    /**
     * Timeout for cache keys to elapse.
     */
    @Value("${dataaccess.cache.reloadCacheEntriesTimeout:1}")
    private int reloadCacheEntriesTimeout;

    /**
     * Timeunit for cache keys elapsing timeout
     * {NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS}
     */
    @Value("${dataaccess.cache.reloadCacheEntriesTimeUnit:'DAYS'}")
    private String reloadCacheEntriesTimeUnit;

    /**
     * Number of entries that until exceeded will be processed by single thread (namely rules)
     * if exceeded will be processed by Runtime.getAvailableProcessors() threads and though splited into chunks.
     */
    @Value("${dataaccess.cache.numberOfEntriesToProcessSequentially:10000}")
    private int numberOfEntriesToProcessSequentially;

    /**
     * Keys chunk size that is used to load keys during initial cache load.
     */
    @Value("${dataaccess.cache.keysetChunkSizeForMassCacheLoad:500}")
    private int keysetChunkSizeForMassCacheLoad;

    @Value("${dataaccess.cache.changedKeysCfName}")
    private String changedKeysCfName;

    public CacheSettings() {}

    public CacheSettings(final CacheSettings settings) {
        tickDuration = settings.tickDuration;
        retryCountUntilFullRefresh = settings.retryCountUntilFullRefresh;
        changedKeysTimeWindowSize = settings.changedKeysTimeWindowSize;
        reloadCacheEntries = settings.reloadCacheEntries;
        reloadCacheEntriesTimeout = settings.reloadCacheEntriesTimeout;
        reloadCacheEntriesTimeUnit = settings.reloadCacheEntriesTimeUnit;
        numberOfEntriesToProcessSequentially = settings.numberOfEntriesToProcessSequentially;
        keysetChunkSizeForMassCacheLoad = settings.keysetChunkSizeForMassCacheLoad;
        changedKeysCfName = settings.changedKeysCfName;
    }

    public int getTickDuration() {
        return tickDuration;
    }

    public void setTickDuration(int tickDuration) {
        this.tickDuration = tickDuration;
    }

    public int getRetryCountUntilFullRefresh() {
        return retryCountUntilFullRefresh;
    }

    public void setRetryCountUntilFullRefresh(int retryCountUntilFullRefresh) {
        this.retryCountUntilFullRefresh = retryCountUntilFullRefresh;
    }

    public long getChangedKeysTimeWindowSize() {
        return changedKeysTimeWindowSize;
    }

    public void setChangedKeysTimeWindowSize(long changedKeysTimeWindowSize) {
        this.changedKeysTimeWindowSize = changedKeysTimeWindowSize;
    }

    public boolean isReloadCacheEntries() {
        return reloadCacheEntries;
    }

    public void setReloadCacheEntries(boolean reloadCacheEntries) {
        this.reloadCacheEntries = reloadCacheEntries;
    }

    public int getReloadCacheEntriesTimeout() {
        return reloadCacheEntriesTimeout;
    }

    public void setReloadCacheEntriesTimeout(int reloadCacheEntriesTimeout) {
        this.reloadCacheEntriesTimeout = reloadCacheEntriesTimeout;
    }

    public String getReloadCacheEntriesTimeUnit() {
        return reloadCacheEntriesTimeUnit;
    }

    public void setReloadCacheEntriesTimeUnit(String reloadCacheEntriesTimeUnit) {
        this.reloadCacheEntriesTimeUnit = reloadCacheEntriesTimeUnit;
    }

    public int getNumberOfEntriesToProcessSequentially() {
        return numberOfEntriesToProcessSequentially;
    }

    public void setNumberOfEntriesToProcessSequentially(int numberOfEntriesToProcessSequentially) {
        this.numberOfEntriesToProcessSequentially = numberOfEntriesToProcessSequentially;
    }

    public int getKeysetChunkSizeForMassCacheLoad() {
        return keysetChunkSizeForMassCacheLoad;
    }

    public void setKeysetChunkSizeForMassCacheLoad(int keysetChunkSizeForMassCacheLoad) {
        this.keysetChunkSizeForMassCacheLoad = keysetChunkSizeForMassCacheLoad;
    }

    public String getChangedKeysCfName() {
        return changedKeysCfName;
    }

    public void setChangedKeysCfName(String changedKeysCfName) {
        this.changedKeysCfName = changedKeysCfName;
    }
}
