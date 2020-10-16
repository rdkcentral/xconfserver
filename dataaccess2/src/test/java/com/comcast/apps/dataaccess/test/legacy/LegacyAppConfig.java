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
package com.comcast.apps.dataaccess.test.legacy;

import com.comcast.apps.dataaccess.acl.AccessControlInfo;
import com.comcast.apps.dataaccess.cache.DaoFactory;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.config.CacheConfiguration;
import com.comcast.apps.dataaccess.config.CacheSettings;
import com.comcast.apps.dataaccess.dao.impl.CompressingDataDao;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(
        basePackages = "com.comcast.apps.dataaccess.cache"

)
@Import({CacheConfiguration.class})
@PropertySource(value = {"classpath:service.properties"})
public class LegacyAppConfig {

    @Bean
    public Cluster cluster() {
        final Cluster cluster = new Cluster.Builder().addContactPoints("127.0.0.1").withPort(9042).build();
        cluster.connect();
        return cluster;
    }

    @Bean
    public Session session() {
        return cluster().connect("\"DevKeyspace\"");
    }

    @Bean
    public CacheSettings cacheSettings() {
        return new CacheSettings();
    }

    @Autowired
    private Session session;

    @Autowired
    private DaoFactory daoFactory;

    @Bean
    public CompressingDataDao<String, NamespacedList> nsListDao() {
        return new CompressingDataDao<>(session, NamespacedList.class);
    }

    @Bean
    public CachedSimpleDao<Long, AccessControlInfo> aclDao() {
        return daoFactory.createAclDao("TSPRules");
    }

}
