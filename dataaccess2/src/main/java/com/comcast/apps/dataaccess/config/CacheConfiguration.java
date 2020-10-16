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

import com.comcast.apps.dataaccess.cache.dao.ChangedKeysProcessingDaoImpl;
import com.datastax.driver.core.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;


@Configuration
public class CacheConfiguration {
    private Session session;
    private CacheSettings cacheSettings;

    @Autowired
    public CacheConfiguration(final Session session, final CacheSettings cacheSettings) {
        this.session = session;
        this.cacheSettings = cacheSettings;
    }

    @Bean
    public ChangedKeysProcessingDaoImpl changedKeysProcessingDao() {
        return new ChangedKeysProcessingDaoImpl(session, cacheSettings.getChangedKeysCfName(), cacheSettings.getChangedKeysTimeWindowSize());
    }

    @Bean
    public MBeanExporter mbeanExporter() {
        return new AnnotationMBeanExporter();
    }

}
