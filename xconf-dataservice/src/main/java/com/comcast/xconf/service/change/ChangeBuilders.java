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

package com.comcast.xconf.service.change;

import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.change.ChangeOperation;
import com.comcast.xconf.change.EntityType;

import java.util.UUID;

public class ChangeBuilders {

    public static <T extends IPersistable>Change<T> buildToCreate(T newEntity, EntityType entityType, String applicationType, String userName) {
        return new Change.Builder<T>()
                .setId(UUID.randomUUID().toString())
                .setEntityId(newEntity.getId())
                .setEntityType(entityType)
                .setApplicationType(applicationType)
                .setNewEntity(newEntity)
                .setOperation(ChangeOperation.CREATE)
                .setAuthor(userName)
                .build();
    }

    public static <T extends IPersistable>Change<T> buildToUpdate(T oldEntity, T newEntity, EntityType entityType, String applicationType, String userName) {
        return new Change.Builder<T>()
                .setId(UUID.randomUUID().toString())
                .setEntityId(oldEntity.getId())
                .setEntityType(entityType)
                .setApplicationType(applicationType)
                .setOldEntity(oldEntity)
                .setNewEntity(newEntity)
                .setAuthor(userName)
                .setOperation(ChangeOperation.UPDATE)
                .build();
    }

    public static <T extends IPersistable>Change<T> buildToDelete(T oldEntity, EntityType entityType, String applicationType, String userName) {
        return new Change.Builder<T>()
                .setId(UUID.randomUUID().toString())
                .setEntityId(oldEntity.getId())
                .setEntityType(entityType)
                .setApplicationType(applicationType)
                .setOldEntity(oldEntity)
                .setOperation(ChangeOperation.DELETE)
                .setAuthor(userName)
                .build();
    }
}