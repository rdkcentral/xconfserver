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
package com.comcast.apps.dataaccess.cache;

import com.comcast.apps.dataaccess.acl.AccessControlInfo;
import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.apps.dataaccess.annotation.SimpleCFDefinition;
import com.comcast.apps.dataaccess.cache.dao.CachedListingDao;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.dao.impl.CompressingDataDao;
import com.comcast.apps.dataaccess.dao.impl.ListingDaoImpl;
import com.comcast.apps.dataaccess.dao.impl.SimpleDaoImpl;
import com.datastax.driver.core.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.comcast.apps.dataaccess.acl.AccessControlManager.ACL_CF_PREFIX;

@Component
public class DaoFactory {

    @Autowired
    private Session session;

    @Autowired
    private CacheManager cacheManager;

    public <K,T> CachedSimpleDao<K, T> createCachedSimpleDao(Class<K> keyClass, Class<T> valueClass) {
        SimpleDao<K, T> simpleDao = createSimpleDao(valueClass);
        return createCachedSimpleDao(simpleDao);
    }

    public <K, T> CachedSimpleDao<K, T> createCachedSimpleDao(SimpleDao<K, T> simpleDao) {
        return cacheManager.augmentWithCache(simpleDao);
    }

    public <K, T> SimpleDao<K, T> createSimpleDao(SimpleCFDefinition cfDefinition, Class<T> valueClass) {
        CF.CompressionPolicy compressionPolicy = cfDefinition.compressionPolicy();
        return compressionPolicy == CF.CompressionPolicy.NONE ?
                new SimpleDaoImpl<>(session, valueClass, cfDefinition) :
                new CompressingDataDao<>(session, valueClass, cfDefinition);
    }

    public <K, T> SimpleDao<K, T> createSimpleDao(Class<T> valueClass) {
        CF cfAnnotation = valueClass.getAnnotation(CF.class);
        CF.CompressionPolicy compressionPolicy = cfAnnotation.compressionPolicy();
        return compressionPolicy == CF.CompressionPolicy.NONE ?
                new SimpleDaoImpl<>(session, valueClass) :
                new CompressingDataDao<>(session, valueClass);
    }

    public <K,N,T> CachedListingDao<K,N,T> createCachedListingDao(Class<T> valueClass) {
        ListingDao<K, N, T> listingDao = createListingDao(valueClass);
        return createCachedListingDao(listingDao);
    }

    public <K,N,T> CachedListingDao<K,N,T> createCachedListingDao(ListingDao<K,N,T> listingDao) {
        return cacheManager.augmentWithCache(listingDao);
    }

    public <K,N,T> ListingDao<K,N,T> createListingDao(Class<T> valueClass) {
        return new ListingDaoImpl<K, N, T>(session, valueClass);
    }

    public CachedSimpleDao<Long, AccessControlInfo> createAclDao(String cfName) {
        String aclCfName = cfName + ACL_CF_PREFIX;
        AclCFDefinition cfDef = new AclCFDefinition(aclCfName);
        SimpleDaoImpl<Long, AccessControlInfo> simpleDao = new SimpleDaoImpl<>(session, AccessControlInfo.class, cfDef);
        return cacheManager.augmentWithCache(simpleDao);
    }

    public static class AclCFDefinition implements SimpleCFDefinition {

        private String cfName;

        public AclCFDefinition(String cfName) {
            this.cfName = cfName;
        }

        @Override
        public String cfName() {
            return cfName;
        }

        @Override
        public Class<?> keyType() {
            return Long.class;
        }
    }
}
