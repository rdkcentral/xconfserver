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

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.dao.impl.SimpleDaoImpl;
import com.comcast.apps.dataaccess.data.Persistable;
import com.comcast.apps.dataaccess.test.config.AppConfig;
import com.datastax.driver.core.Session;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {AppConfig.class})
public class SimpleDaoTest {

    private SimpleDao<String, User> dao;
    private String testLogin = "testLogin";
    private String testFirstName = "testFirstName";
    private String testLastName = "testLastName";

    @Before
    public void setUp() {
        Session session = EmbeddedCassandraServerHelper.getSession();
        dao = new SimpleDaoImpl<>(session, User.class);
    }

    @Test
    public void setOne() {
        final User user = new User(testLogin, testFirstName, testLastName);
        dao.setOne(user.getLogin(), user);
    }

    @Test
    public void getOne() {
        setOne();
        final User user = dao.getOne(testLogin);

        Assert.assertEquals(testLogin, user.getLogin());
    }

    @Test
    public void getAll() {
        final int count = 20;
        fillData(count);
        final List<User> all = dao.getAll();

        Assert.assertEquals(count, all.size());
    }

    @Test
    public void deleteOne() {
        setOne();
        dao.deleteOne(testLogin);

        Assert.assertNull(dao.getOne(testLogin));
    }

    @Test
    public void getAllAsMap() {
        final int count = 20;
        fillData(count);
        final Set<String> keys = new HashSet<>();
        for (int i = 0; i < (count - 10); i++) {
            final String key = testLogin + "_" + i;
            keys.add(key);
        }
        final Map<String, Optional<User>> allAsMap = dao.getAllAsMap(keys);

        Assert.assertEquals((count - 10), allAsMap.size());
    }

    @Test
    public void getKeys() {
        final int count = 20;
        for (int i = 0; i < count; i++) {
            final String key = testLogin + "_" + i;
            final User user = new User(key, testFirstName, testLastName);
            dao.setOne(user.getLogin(), user);
        }
        final Iterable<String> keys = dao.getKeys();

        Assert.assertEquals(Iterables.size(keys), count);
    }

    private List<User> fillData(int count) {
        final List<User> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final String key = testLogin + "_" + i;
            final User user = new User(key, testFirstName, testLastName);
            result.add(user);
            dao.setOne(user.getLogin(), user);
        }
        return result;
    }


    @CF(
            cfName = "SimpleDaoCF"
    )
    public static class User implements Persistable {
        private String login;
        private String firstName;
        private String lastName;

        public User() { }

        public User(String login, String firstName, String lastName) {
            this.login = login;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            User user = (User) o;

            if (!login.equals(user.login)) return false;
            if (!firstName.equals(user.firstName)) return false;
            return lastName.equals(user.lastName);
        }

        @Override
        public int hashCode() {
            int result = login.hashCode();
            result = 31 * result + firstName.hashCode();
            result = 31 * result + lastName.hashCode();
            return result;
        }
    }

}
