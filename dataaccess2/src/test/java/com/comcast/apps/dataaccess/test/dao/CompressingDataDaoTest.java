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
package com.comcast.apps.dataaccess.test.dao;

import com.comcast.apps.dataaccess.dao.impl.CompressingDataDao;
import com.comcast.apps.dataaccess.test.legacy.NamespacedList;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class CompressingDataDaoTest {

    private Session session;
    private CompressingDataDao<String, NamespacedList> dao;

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
        dao = new CompressingDataDao<>(session, NamespacedList.class);
        CQLDataLoader cqlDataLoader = new CQLDataLoader(session);
        cqlDataLoader.load(new ClassPathCQLDataSet("demo.cql", true, true, "demo"));
    }

    @Test
    public void getOne() throws Exception {
        String id = "abc";
        NamespacedList list = createAndSaveEnitty(id);

        NamespacedList listFromDB = dao.getOne(id);

        Assert.assertEquals(list, listFromDB);
    }

    @Test
    public void getAll() throws Exception {
        int count = 20;
        fillData(count);

        List<NamespacedList> all = dao.getAll();

        Assert.assertEquals(count, all.size());
    }

    @Test
    public void deleteOne() throws Exception {
        String id = "abc";
        createAndSaveEnitty(id);

        dao.deleteOne(id);

        Assert.assertNull(dao.getOne(id));
    }

    @Test
    public void getAllAsMap() {
        final int count = 20;
        fillData(count);
        final Set<String> keys = new HashSet<>();
        for (int i = 0; i < (count - 10); i++) {
            keys.add("" + i);
        }
        final Map<String, Optional<NamespacedList>> allAsMap = dao.getAllAsMap(keys);

        Assert.assertEquals((count - 10), allAsMap.size());
    }

    @Test
    public void getKeys() {
        final int count = 20;
        fillData(count);
        final Iterable<String> keys = dao.getKeys();

        Assert.assertEquals(Iterables.size(keys), count);
    }

    private List<NamespacedList> fillData(int count) {
        final List<NamespacedList> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(createAndSaveEnitty("" + i));
        }
        return result;
    }

    private NamespacedList createAndSaveEnitty(String id) {
        NamespacedList entity = createEntity(id);
        dao.setOne(id, entity);
        return entity;
    }

    private NamespacedList createEntity(String id) {
        NamespacedList list = new NamespacedList();
        list.setId(id);
        list.setData(Sets.newHashSet("data_" + id));
        return list;
    }
}
