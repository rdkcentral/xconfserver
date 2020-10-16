/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
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
 * Author: Maxym Dolina
 * Created: 13.05.2019
 */
package com.comcast.xconf.change;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.Objects;

@CF(cfName = "XconfChange")
public class Change<T extends IPersistable> extends XMLPersistable implements Comparable<Change<T>>, Applicationable {

    private String id;

    private String entityId;

    private EntityType entityType;

    private String applicationType;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
    @JsonSubTypes({@JsonSubTypes.Type(value = PermanentTelemetryProfile.class)})
    private T newEntity;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
    @JsonSubTypes({@JsonSubTypes.Type(value = PermanentTelemetryProfile.class)})
    private T oldEntity;

    private ChangeOperation operation;

    private String author;

    private String approvedUser;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public T getNewEntity() {
        return newEntity;
    }

    public void setNewEntity(T newEntity) {
        this.newEntity = newEntity;
    }

    public T getOldEntity() {
        return oldEntity;
    }

    public void setOldEntity(T oldEntity) {
        this.oldEntity = oldEntity;
    }

    public ChangeOperation getOperation() {
        return operation;
    }

    public void setOperation(ChangeOperation operation) {
        this.operation = operation;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getApprovedUser() {
        return approvedUser;
    }

    public void setApprovedUser(String approvedUser) {
        this.approvedUser = approvedUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Change<?> change = (Change<?>) o;
        return Objects.equals(id, change.id) &&
                Objects.equals(entityId, change.entityId) &&
                entityType == change.entityType &&
                Objects.equals(applicationType, change.applicationType) &&
                Objects.equals(newEntity, change.newEntity) &&
                Objects.equals(oldEntity, change.oldEntity) &&
                operation == change.operation &&
                Objects.equals(author, change.author) &&
                Objects.equals(approvedUser, change.approvedUser);
    }

    public boolean equalChangeData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Change<?> change = (Change<?>) o;
        return entityType == change.entityType &&
                Objects.equals(applicationType, change.applicationType) &&
                Objects.equals(newEntity, change.newEntity) &&
                Objects.equals(oldEntity, change.oldEntity) &&
                operation == change.operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entityId, entityType, applicationType, newEntity, oldEntity, operation, author, approvedUser);
    }

    @Override
    public int compareTo(Change o) {
        return new NullComparator().compare(this.getUpdated(), o.getUpdated());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Change{");
        sb.append("id='").append(id).append('\'');
        sb.append(", entityId='").append(entityId).append('\'');
        sb.append(", entityType=").append(entityType);
        sb.append(", applicationType='").append(applicationType).append('\'');
        sb.append(", newEntity=").append(newEntity);
        sb.append(", oldEntity=").append(oldEntity);
        sb.append(", operation=").append(operation);
        sb.append(", author='").append(author).append('\'');
        sb.append(", approvedUser='").append(approvedUser).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static class Builder<T extends IPersistable> {
        private Change<T> newChange;

        public Builder() {
            newChange = new Change<>();
        }

        public Builder<T> setId(String id) {
            newChange.id = id;
            return this;
        }

        public Builder<T> setEntityId(String entityId) {
            newChange.entityId = entityId;
            return this;
        }

        public Builder<T> setEntityType(EntityType entityType) {
            newChange.entityType = entityType;
            return this;
        }

        public Builder<T> setApplicationType(String applicationType) {
            newChange.setApplicationType(applicationType);
            return this;
        }

        public Builder<T> setNewEntity(T newEntity) {
            newChange.newEntity = newEntity;
            return this;
        }

        public Builder<T> setOldEntity(T oldEntity) {
            newChange.oldEntity = oldEntity;
            return this;
        }

        public Builder<T> setOperation(ChangeOperation operation) {
            newChange.operation = operation;
            return this;
        }

        public Builder<T> setAuthor(String author) {
            newChange.author = author;
            return this;
        }

        public Builder<T> setApprovedUser(String approvedUser) {
            newChange.approvedUser = approvedUser;
            return this;
        }

        public Change<T> build() {
            return newChange;
        }
    }
}
