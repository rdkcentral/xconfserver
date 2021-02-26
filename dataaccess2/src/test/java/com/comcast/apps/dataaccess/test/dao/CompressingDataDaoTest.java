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
import com.comcast.apps.dataaccess.test.config.AppConfig;
import com.comcast.apps.dataaccess.test.legacy.NamespacedList;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.junit.Assert;
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
public class CompressingDataDaoTest {

    @Autowired
    private CompressingDataDao<String, NamespacedList> namespacedListDao;

    @Test
    public void getOne() throws Exception {
        String id = "abc";
        NamespacedList list = createAndSaveEnitty(id);

        NamespacedList listFromDB = namespacedListDao.getOne(id);

        Assert.assertEquals(list, listFromDB);
    }

    @Test
    public void getAll() throws Exception {
        int count = 20;
        fillData(count);

        List<NamespacedList> all = namespacedListDao.getAll();

        Assert.assertEquals(count, all.size());
    }

    @Test
    public void deleteOne() throws Exception {
        String id = "abc";
        createAndSaveEnitty(id);

        namespacedListDao.deleteOne(id);

        Assert.assertNull(namespacedListDao.getOne(id));
    }

    @Test
    public void getAllAsMap() {
        final int count = 20;
        fillData(count);
        final Set<String> keys = new HashSet<>();
        for (int i = 0; i < (count - 10); i++) {
            keys.add("" + i);
        }
        final Map<String, Optional<NamespacedList>> allAsMap = namespacedListDao.getAllAsMap(keys);

        Assert.assertEquals((count - 10), allAsMap.size());
    }

    @Test
    public void getKeys() {
        final int count = 20;
        fillData(count);
        final Iterable<String> keys = namespacedListDao.getKeys();

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
        namespacedListDao.setOne(id, entity);
        return entity;
    }

    private NamespacedList createEntity(String id) {
        NamespacedList list = new NamespacedList();
        list.setId(id);
        list.setData(Sets.newHashSet("data_" + id));
        return list;
    }
}
