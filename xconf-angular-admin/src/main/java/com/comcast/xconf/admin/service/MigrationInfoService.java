/* 
 * If not stated otherwise in this file or this component's Licenses.txt file the 
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
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
 *  Author: mdolina
 *  Created: 5:47 PM
 */
package com.comcast.xconf.admin.service;

import com.comcast.apps.dataaccess.cache.DaoFactory;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.util.AnnotationScanner;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.NamespacedList;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import com.comcast.xconf.utils.MigrationInfo;
import com.comcast.xconf.utils.annotation.Migration;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class MigrationInfoService {

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    @Autowired
    private DaoFactory daoFactory;

    private static List<Migration> entityInfo = new ArrayList<>();
    private static Map<Class, CachedSimpleDao> oldEntitiesDao = new HashMap<>();
    private static Map<Class, CachedSimpleDao> newEntitiesDao = new HashMap<>();
    private static final List<String> MIGRATION_PACKAGES = Lists.newArrayList("com.comcast.xconf.migration", "com.comcast.xconf.admin.controller.migration");

    @PostConstruct
    public void scanMigrationClasses() {
        for (String migrationPackage : MIGRATION_PACKAGES) {
            Set<Class<?>> annotatedClasses = AnnotationScanner.getAnnotatedClasses(new Class[]{Migration.class}, migrationPackage);
            for (Class<?> annotatedClass : annotatedClasses) {
                for (Method method : annotatedClass.getDeclaredMethods()) {
                    Migration annotation = method.getAnnotation(Migration.class);
                    if (annotation != null) {
                        entityInfo.add(annotation);
                        oldEntitiesDao.put(annotation.oldEntity(), daoFactory.createCachedSimpleDao(annotation.oldKey(), annotation.oldEntity()));
                        newEntitiesDao.put(annotation.newEntity(), daoFactory.createCachedSimpleDao(annotation.newKey(), annotation.newEntity()));
                    }
                }
            }
        }
    }

    public List<MigrationInfo> getMigrationInfo() {
        List<MigrationInfo> entityMigrationInfos = new ArrayList<>();

        for (Migration annotation : entityInfo) {
            MigrationInfo migrationInfo = new MigrationInfo();
            migrationInfo.setOldEntity(annotation.oldEntity().getSimpleName());
            migrationInfo.setOldEntitiesCount(oldEntitiesDao.get(annotation.oldEntity()).getAll().size());
            migrationInfo.setMigrationURL(annotation.migrationURL());
            if(annotation.oldEntity().equals(NamespacedList.class)) {
                migrationInfo.setNewEntity(GenericNamespacedListTypes.MAC_LIST);
                migrationInfo.setNewEntitiesCount(genericNamespacedListQueriesService.getAllByType(GenericNamespacedListTypes.MAC_LIST).size());
            } else if(annotation.oldEntity().equals(IpAddressGroupExtended.class)) {
                migrationInfo.setNewEntity(GenericNamespacedListTypes.IP_LIST);
                migrationInfo.setNewEntitiesCount(genericNamespacedListQueriesService.getAllByType(GenericNamespacedListTypes.IP_LIST).size());
            } else {
                migrationInfo.setNewEntity(annotation.newEntity().getSimpleName());
                migrationInfo.setNewEntitiesCount(newEntitiesDao.get(annotation.newEntity()).getAll().size());
            }
            boolean enableMigrationButton = migrationInfo.getNewEntitiesCount() == 0;
            migrationInfo.setEnableMigrationButton(enableMigrationButton);
            entityMigrationInfos.add(migrationInfo);
        }

        return entityMigrationInfos;
    }
}
