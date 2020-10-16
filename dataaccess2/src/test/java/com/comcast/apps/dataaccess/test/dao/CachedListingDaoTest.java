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
package com.comcast.apps.dataaccess.test.dao;

import com.comcast.apps.dataaccess.cache.dao.CachedListingDao;
import com.comcast.apps.dataaccess.cache.dao.ChangedKeysProcessingDaoImpl;
import com.comcast.apps.dataaccess.test.config.AppConfig;
import com.comcast.apps.dataaccess.test.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {AppConfig.class})
public class CachedListingDaoTest {
    private String testKey = "firstName";
    private String testKey2 = "lastName";
    private int age = 20;

    @Autowired
    private CachedListingDao<String, String, User> cachedListingDao;

    @Autowired
    private ChangedKeysProcessingDaoImpl changedKeysProcessingDao;

    @Test
    public void cache() throws InterruptedException {
        final User user = new User(testKey, testKey2, age);
        cachedListingDao.setOne(user.getFirstName(), user);
    }

}