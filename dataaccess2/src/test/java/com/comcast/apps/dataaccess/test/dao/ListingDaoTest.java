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

import com.comcast.apps.dataaccess.cache.dao.impl.TwoKeys;
import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.test.config.AppConfig;
import com.comcast.apps.dataaccess.test.domain.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {AppConfig.class})
public class ListingDaoTest {

    @Autowired
    private ListingDao<String, String, User> userDao;

    private String testKey = "firstName";
    private String testKey2 = "lastName";
    private int age = 20;

    @Before
    public void cleanUp() throws Exception {
        Iterable<TwoKeys<String, String>> all = userDao.getKeys();
        for (TwoKeys<String, String> key : all) {
            userDao.deleteOne(key.getKey(), key.getKey2());
        }
    }

    @Test
    public void setOne() {
        final User user = new User(testKey, testKey2, age);
        userDao.setOne(user.getFirstName(), user);
    }

    @Test
    public void getOne() {
        setOne();
        final User user = userDao.getOne(testKey, testKey2);

        Assert.assertEquals(testKey, user.getFirstName());
    }

    @Test
    public void getAll() {
        final int count = 20;
        fillData(count);
        final List<User> all = userDao.getAll(testKey);

        Assert.assertEquals(count, all.size());
    }

    @Test
    public void deleteOne() {
        setOne();
        userDao.deleteOne(testKey, testKey2);

        Assert.assertTrue(userDao.getOne(testKey, testKey2) == null);
    }

    @Test
    public void deleteAll() {
        final int count = 20;
        fillData(count);
        userDao.deleteAll(testKey);

        Assert.assertEquals(0, userDao.getAll().size());
    }

    private List<User> fillData(int count) {
        final List<User> result = new ArrayList<>();
        int age = this.age;
        for (int i = 0; i < count; i++) {
            final String key2 = testKey2 + "_" + i;
            final User user = new User(testKey, key2, age++);
            result.add(user);
            userDao.setOne(user.getFirstName(), user);
        }
        return result;
    }

    
}
