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
package com.comcast.apps.dataaccess.config;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;


@Configuration
public class CassandraConfiguration {

    protected CassandraSettings cassandraSettings;

    protected HostStateListener hostStateListener;

    protected ClusterLatencyListener clusterLatencyListener;

    @Autowired
    public CassandraConfiguration(final CassandraSettings cassandraSettings, HostStateListener hostStateListener, ClusterLatencyListener clusterLatencyListener) {
        this.cassandraSettings = cassandraSettings;
        this.hostStateListener = hostStateListener;
        this.clusterLatencyListener = clusterLatencyListener;
    }

    @Bean
    public Cluster cluster() {
        return cluster(cassandraSettings.getUsername(), cassandraSettings.getPassword());
    }

    protected Cluster cluster(String username, String password) {
        Cluster.Builder builder = Cluster.builder()
                .addContactPoints(cassandraSettings.getContactPoints())
                .withPort(cassandraSettings.getPort())
                .withInitialListeners(Collections.singleton(hostStateListener))
                .withCredentials(username, password)
                .withQueryOptions(new QueryOptions().setConsistencyLevel(
                        ConsistencyLevel.valueOf(cassandraSettings.getConsistencyLevel())));
        if (StringUtils.isNotBlank(cassandraSettings.getLocalDataCenter())) {
            DCAwareRoundRobinPolicy roundRobinPolicy = DCAwareRoundRobinPolicy.builder()
                    .withLocalDc(cassandraSettings.getLocalDataCenter())
                    .build();
            builder.withLoadBalancingPolicy(roundRobinPolicy);
        }
        Cluster cluster = builder.build();
        cluster.register(clusterLatencyListener);
        return cluster;

    }

    @Bean
    public Session session() {
        return cluster().connect(cassandraSettings.getKeyspaceName());
    }


}
