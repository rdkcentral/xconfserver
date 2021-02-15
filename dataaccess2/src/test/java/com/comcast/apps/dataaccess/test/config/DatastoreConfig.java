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
package com.comcast.apps.dataaccess.test.config;

import com.comcast.apps.dataaccess.acl.AccessControlInfo;
import com.comcast.apps.dataaccess.cache.DaoFactory;
import com.comcast.apps.dataaccess.cache.dao.CachedListingDao;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.dao.impl.CompressingDataDao;
import com.comcast.apps.dataaccess.dao.impl.ListingDaoImpl;
import com.comcast.apps.dataaccess.test.domain.CompressedUser;
import com.comcast.apps.dataaccess.test.domain.User;
import com.comcast.apps.dataaccess.test.legacy.NamespacedList;
import com.datastax.driver.core.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DatastoreConfig {
    @Autowired
    private Session session;

    @Autowired
    private DaoFactory daoFactory;

    @Bean
    public ListingDao<String, String, CompressedUser> compressedUserDao() {
        return new ListingDaoImpl<>(session, CompressedUser.class);
    }

    @Bean
    public CompressingDataDao<String, NamespacedList> namespacedListDao() {
        return new CompressingDataDao<>(session, NamespacedList.class);
    }

    @Bean
    public ListingDao<String, String, User> userDao() {
        return new ListingDaoImpl<>(session, User.class);
    }

    @Bean
    public CachedListingDao<String, String, User> cachedUserDao() {
        ListingDao<String, String, User> userDao = userDao();
        return daoFactory.createCachedListingDao(userDao);
    }

    @Bean
    public CachedSimpleDao<Long, AccessControlInfo> aclDao() {
        return daoFactory.createAclDao("Users");
    }

}
