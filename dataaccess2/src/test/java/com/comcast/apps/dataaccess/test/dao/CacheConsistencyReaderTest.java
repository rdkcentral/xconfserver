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
 */
package com.comcast.apps.dataaccess.test.dao;

import com.comcast.apps.dataaccess.cache.CacheConsistencyReader;
import com.comcast.apps.dataaccess.cache.CacheManager;
import com.comcast.apps.dataaccess.cache.dao.ChangedKeysProcessingDaoImpl;
import com.comcast.apps.dataaccess.cache.data.ChangedData;
import com.comcast.apps.dataaccess.config.CacheSettings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CacheConsistencyReaderTest {
    private CacheConsistencyReader cacheConsistencyReader;

    @Mock
    private ChangedKeysProcessingDaoImpl changedKeysProcessingDao;
    @Mock
    private CacheManager cacheManager;

    @Before
    public void before() {
        CacheSettings cacheSettings = new CacheSettings();
        cacheSettings.setTickDuration(10);
        cacheSettings.setRetryCountUntilFullRefresh(2);
        cacheConsistencyReader = new CacheConsistencyReader(cacheSettings);

        ReflectionTestUtils.setField(cacheConsistencyReader, CacheConsistencyReader.class, "changedKeysProcessingDao", changedKeysProcessingDao, ChangedKeysProcessingDaoImpl.class);
        ReflectionTestUtils.setField(cacheConsistencyReader, CacheConsistencyReader.class, "cacheManager", cacheManager, CacheManager.class);
    }

    @Test
    public void readerIsRecoveringFromCassandraIssuesByRefreshAll() throws Exception {
        when(changedKeysProcessingDao.getIteratedChangedKeysForTick(anyLong(), anyLong())).thenThrow(Exception.class);

        ReflectionTestUtils.invokeMethod(cacheConsistencyReader, "init");
        TimeUnit.MILLISECONDS.sleep(30);

        verify(cacheManager, atLeastOnce()).refreshAll();
    }

    private class Counter {
        int count = 0;

        void inc() {
            count+=1;
        }

        int getCount() {
            return count;
        }
    }

    @Test
    public void readerIsRecoveringFromCassandraIssuesOnRefreshAll() throws Exception {
        final Counter cntr = new Counter();
        Answer<Iterator<ChangedData>> answer = invocationOnMock -> {
            cntr.inc();
            if (cntr.getCount() < 3) {
                throw new Exception();
            } else {
                return Collections.emptyIterator();
            }
        };

        Answer<Iterator<ChangedData>> cacheManagerAnswer = invocationOnMock -> {
            cntr.inc();
            if (cntr.getCount() < 5) {
                throw new Exception();
            } else {
                return null;
            }
        };

        when(changedKeysProcessingDao.getIteratedChangedKeysForTick(anyLong(), anyLong())).thenAnswer(answer);
        when(cacheManager.refreshAll()).thenAnswer(cacheManagerAnswer);

        ReflectionTestUtils.invokeMethod(cacheConsistencyReader, "init");
        TimeUnit.MILLISECONDS.sleep(100);

        int refreshAttemptsLeft = (Integer) ReflectionTestUtils.getField(cacheConsistencyReader, "refreshAttemptsLeft");
        int retryUntilFullReload = (Integer) ReflectionTestUtils.getField(cacheConsistencyReader, "retryUntilFullReload");

        Assert.assertEquals(retryUntilFullReload, refreshAttemptsLeft);
    }
}
