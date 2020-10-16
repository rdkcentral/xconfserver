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
 * @author Philipp Bura
 */
package com.comcast.apps.dataaccess.cache.data;

import com.comcast.apps.dataaccess.annotation.DataType;
import com.comcast.apps.dataaccess.annotation.ListingCF;
import com.comcast.apps.dataaccess.data.Persistable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;


@JsonIgnoreProperties({"id", "ttlMap"})
@ListingCF(
        cfName = "ChangedKeys",
        keyType = Long.class,
        key2FieldName = "columnName",
        key2Is = DataType.JSON,
        key2Type = UUID.class,
        ttl = 86400 * 7 // one week
)
public final class ChangedData implements Persistable {

    public enum Operation {
        CREATE,
        UPDATE,
        DELETE,
        TRUNCATE_CF
    }

    @JsonProperty
    private UUID columnName;
    @JsonProperty
    private String cfName;
    @JsonProperty
    private String changedKey;
    @JsonProperty
    private Operation operation;
    @JsonProperty
    private Integer DAOid;
    @JsonProperty
    private Integer validCacheSize = 0;
    @JsonProperty
    private String userName;

    public UUID getColumnName() {
        return columnName;
    }

    public void setColumnName(UUID columnName) {
        this.columnName = columnName;
    }

    public String getCfName() {
        return cfName;
    }

    public void setCfName(String cfName) {
        this.cfName = cfName;
    }

    public String getChangedKey() {
        return changedKey;
    }

    public void setChangedKey(String changedKey) {
        this.changedKey = changedKey;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Integer getDAOid() {
        return DAOid;
    }

    public void setDAOid(Integer DAOid) {
        this.DAOid = DAOid;
    }

    public Integer getValidCacheSize() {
        return validCacheSize;
    }

    public void setValidCacheSize(Integer validCacheSize) {
        this.validCacheSize = validCacheSize;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangedData that = (ChangedData) o;
        return Objects.equals(getCfName(), that.getCfName()) &&
                Objects.equals(getChangedKey(), that.getChangedKey()) &&
                Objects.equals(getOperation(), that.getOperation()) &&
                Objects.equals(getDAOid(), that.getDAOid()) &&
                Objects.equals(getValidCacheSize(), that.getValidCacheSize()) &&
                Objects.equals(getUserName(), that.getUserName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCfName(),
                getChangedKey(),
                getOperation(),
                getDAOid(),
                getValidCacheSize(),
                getUserName());
    }
}
