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
 * @author Igor Kostrov (ikostrov@productengine.com)
 */
package com.comcast.apps.dataaccess.cache.mbean;

import com.comcast.apps.dataaccess.cache.CacheManager;
import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.TimeUnit;

/**
 * JMX implementation for viewing Cache statistics and refreshing cache for particular table.
 */
@XmlRootElement
public class CacheInfo implements CacheInfoMBean {

    private static final Logger log = LoggerFactory.getLogger(CacheInfo.class);

    private CacheManager cacheManager;

    private long daoRefreshTime;
    private String cfName;
    private LoadingCache cache;

    public CacheInfo() {
    }

    public CacheInfo(CacheManager cacheManager, String cfName, LoadingCache cache) {
        this.cacheManager = cacheManager;
        this.cfName = cfName;
        this.cache = cache;
    }

    @XmlElement
    @Override
    public long getTotalLoadTime() {
        return TimeUnit.MILLISECONDS.convert(cache.stats().totalLoadTime(), TimeUnit.NANOSECONDS);
    }

    @XmlElement
    @Override
    public double getHitRate() {
        return cache.stats().hitRate();
    }

    @XmlElement
    @Override
    public double getMissRate() {
        return cache.stats().missRate();
    }

    @XmlElement
    @Override
    public double getRequestCount() {
        return cache.stats().requestCount();
    }

    @XmlElement
    @Override
    public double getEvictionCount() {
        return cache.stats().evictionCount();
    }

    @XmlElement
    @Override
    public long getCacheSize() {
        return cache.size();
    }

    @XmlElement
    @Override
    public long getNonAbsentCount() {
        return Iterables.size(Optional.presentInstances(cache.asMap().values()));
    }

    @XmlElement
    @Override
    public long getDaoRefreshTime() {
        return daoRefreshTime;
    }

    @Override
    public void refreshCache() {
        try {
            cacheManager.refreshAll(cfName);
            daoRefreshTime = System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Failed to perform full cache refresh", e);
        }
    }

}
