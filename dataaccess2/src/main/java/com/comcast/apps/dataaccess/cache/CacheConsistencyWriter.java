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

import com.comcast.apps.dataaccess.cache.dao.ChangedKeysProcessingDaoImpl;
import com.comcast.apps.dataaccess.cache.data.ChangedData;
import com.comcast.apps.dataaccess.config.CacheSettings;
import com.comcast.apps.dataaccess.util.AuthUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
public class CacheConsistencyWriter {
    private final Logger logger = LoggerFactory.getLogger(CacheConsistencyWriter.class);

    private final ExecutorService changeWriter = Executors.newFixedThreadPool(4);

    private long changedKeysTimeWindowSize;

    @Autowired
    private ChangedKeysProcessingDaoImpl changedKeysProcessingDao;

    @Autowired
    public CacheConsistencyWriter(final CacheSettings cacheSettings) {
        changedKeysTimeWindowSize = cacheSettings.getChangedKeysTimeWindowSize();
    }

    public void writeCacheLog(final String columnFamilyName, final Object key, final ChangedData.Operation operation, final int daoId, final int daoCacheSize) {
        changeWriter.execute(new WriteTask(columnFamilyName, key, operation, daoId, daoCacheSize));
    }

    private final class WriteTask implements Runnable {
        final String cfName;
        final Object key;
        final ChangedData.Operation operation;
        final String userName;
        final int daoId;
        final int cacheSize;

        public WriteTask(final String columnFamilyName, final Object key, final ChangedData.Operation operation,
                         final int daoId, final int cacheSize) {

            this.cfName = columnFamilyName;
            this.key = key;
            this.operation = operation;
            this.userName = AuthUtil.getUserName();
            this.daoId = daoId;
            this.cacheSize = cacheSize;
        }

        @Override
        public void run() {
            final long now = DateTime.now(DateTimeZone.UTC).getMillis();
            final long rowKey = now - (now % changedKeysTimeWindowSize);
            final ChangedData data = new ChangedData();
            data.setColumnName(generateUuid());
            data.setCfName(cfName);
            data.setChangedKey(JsonUtil.toJson(key));
            data.setOperation(operation);
            data.setUserName(userName);
            data.setDAOid(daoId);
            data.setValidCacheSize(cacheSize);
            logger.debug("writing cache changeLog: " + data.getChangedKey() + " " + operation + " " + daoId + " " + userName);
            changedKeysProcessingDao.setOne(rowKey, data);
        }

        private UUID generateUuid() {
            return UUID.fromString(new com.eaio.uuid.UUID().toString());
        }
    }
}
