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

import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.dao.impl.ListingDaoImpl;
import com.comcast.apps.dataaccess.test.domain.User;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;


public class ListingDaoTest {
    private Session session;
    private ListingDao<String, String, User> dao;
    private String testKey = "firstName";
    private String testKey2 = "lastName";
    private int age = 20;

    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra(50000L);
        EmbeddedCassandraServerHelper.getCluster().getConfiguration().getSocketOptions().setReadTimeoutMillis(50000);
        final Cluster cluster = new Cluster.Builder().addContactPoints("127.0.0.1").withPort(9142).build();
        cluster.connect();
    }

    @Before
    public void setUp() {
        session = EmbeddedCassandraServerHelper.getSession();
        dao = new ListingDaoImpl<>(session, User.class);
        CQLDataLoader cqlDataLoader = new CQLDataLoader(session);
        cqlDataLoader.load(new ClassPathCQLDataSet("demo.cql", true, true, "demo"));
    }

    @Test
    public void setOne() {
        final User user = new User(testKey, testKey2, age);
        dao.setOne(user.getFirstName(), user);
    }

    @Test
    public void getOne() {
        setOne();
        final User user = dao.getOne(testKey, testKey2);

        Assert.assertEquals(testKey, user.getFirstName());
    }

    @Test
    public void getAll() {
        final int count = 20;
        fillData(count);
        final List<User> all = dao.getAll(testKey);

        Assert.assertEquals(count, all.size());
    }

    @Test
    public void deleteOne() {
        setOne();
        dao.deleteOne(testKey, testKey2);

        Assert.assertTrue(dao.getOne(testKey, testKey2) == null);
    }

    @Test
    public void deleteAll() {
        final int count = 20;
        fillData(count);
        dao.deleteAll(testKey);

        Assert.assertEquals(0, dao.getAll().size());
    }

    private List<User> fillData(int count) {
        final List<User> result = new ArrayList<>();
        int age = this.age;
        for (int i = 0; i < count; i++) {
            final String key2 = testKey2 + "_" + i;
            final User user = new User(testKey, key2, age++);
            result.add(user);
            dao.setOne(user.getFirstName(), user);
        }
        return result;
    }

    
}
