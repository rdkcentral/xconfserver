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

import com.comcast.apps.dataaccess.config.CacheConfiguration;
import com.comcast.apps.dataaccess.config.CacheSettings;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.springframework.context.annotation.*;


@Configuration
@ComponentScan(
        basePackages = "com.comcast.apps.dataaccess.cache"

)
@Import({CacheConfiguration.class, DatastoreConfig.class})
@PropertySource(value = {"classpath:service.properties"})
public class AppConfig {

    {
        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(50000L);
            EmbeddedCassandraServerHelper.getCluster().getConfiguration().getSocketOptions().setReadTimeoutMillis(50000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CQLDataLoader cqlDataLoader = new CQLDataLoader(EmbeddedCassandraServerHelper.getSession());
        cqlDataLoader.load(new ClassPathCQLDataSet("demo.cql", true, true, "demo"));
    }

    @Bean
    public Cluster cluster() {
        final Cluster cluster = new Cluster.Builder().addContactPoints("127.0.0.1").withPort(9142).build();
        cluster.connect();
        return cluster;
    }

    @Bean
    public Session session() {
        return EmbeddedCassandraServerHelper.getSession();
    }

    @Bean
    public CacheSettings cacheSettings() {
        return new CacheSettings();
    }

}
