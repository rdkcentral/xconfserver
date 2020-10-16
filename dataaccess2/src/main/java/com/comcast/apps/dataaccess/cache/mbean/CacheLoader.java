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
package com.comcast.apps.dataaccess.cache.mbean;

import com.comcast.apps.dataaccess.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.stereotype.Component;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

//@ManagedResource
//@Component
public class CacheLoader implements SelfNaming {
    private static final Logger log = LoggerFactory.getLogger(CacheLoader.class);
    public static final String NAME = "AllTables";

    @Autowired
    private CacheManager cacheManager;

    @ManagedOperation(description="Refresh ALL caches")
    public void refreshAll() {
        try {
            cacheManager.refreshAll();
        } catch (Exception e) {
            log.error("Failed to perform full cache refresh", e);
        }
    }

    @Override
    public ObjectName getObjectName() throws MalformedObjectNameException {
        String domainName = CacheManager.class.getPackage().getName();
        return new ObjectName(domainName + ":name=" + NAME);
    }
}
