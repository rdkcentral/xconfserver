/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.contextconfig;

import com.comcast.xconf.permissions.FirmwarePermissionService;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({RestWebConfig.class})
public class TestContext {

    static {
        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(50000L);
            EmbeddedCassandraServerHelper.getCluster().getConfiguration().getSocketOptions().setReadTimeoutMillis(50000);
            CQLDataLoader cqlDataLoader = new CQLDataLoader(EmbeddedCassandraServerHelper.getSession());
            cqlDataLoader.load(new ClassPathCQLDataSet("schema.cql", true, true, "demo"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public FirmwarePermissionService firmwarePermissionService() {
        return Mockito.mock(FirmwarePermissionService.class);
    }
}
