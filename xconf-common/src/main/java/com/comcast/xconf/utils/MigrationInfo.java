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
 *  Created: 2:03 PM
 */
package com.comcast.xconf.utils;

public class MigrationInfo {
    private String oldEntity;
    private String newEntity;
    private String migrationURL;
    private Integer oldEntitiesCount;
    private Integer newEntitiesCount;
    private boolean enableMigrationButton;

    public MigrationInfo() {}

    public MigrationInfo(String oldEntity, String newEntity, String migrationURL, Integer oldEntitiesCount, Integer newEntitiesCount, boolean enableMigrationButton) {
        this.oldEntity = oldEntity;
        this.newEntity = newEntity;
        this.migrationURL = migrationURL;
        this.oldEntitiesCount = oldEntitiesCount;
        this.newEntitiesCount = newEntitiesCount;
        this.enableMigrationButton = enableMigrationButton;
    }

    public String getOldEntity() {
        return oldEntity;
    }

    public void setOldEntity(String oldEntity) {
        this.oldEntity = oldEntity;
    }

    public String getNewEntity() {
        return newEntity;
    }

    public void setNewEntity(String newEntity) {
        this.newEntity = newEntity;
    }

    public String getMigrationURL() {
        return migrationURL;
    }

    public void setMigrationURL(String migrationURL) {
        this.migrationURL = migrationURL;
    }

    public Integer getOldEntitiesCount() {
        return oldEntitiesCount;
    }

    public void setOldEntitiesCount(Integer oldEntitiesCount) {
        this.oldEntitiesCount = oldEntitiesCount;
    }

    public Integer getNewEntitiesCount() {
        return newEntitiesCount;
    }

    public void setNewEntitiesCount(Integer newEntitiesCount) {
        this.newEntitiesCount = newEntitiesCount;
    }

    public boolean isEnableMigrationButton() {
        return enableMigrationButton;
    }

    public void setEnableMigrationButton(boolean enableMigrationButton) {
        this.enableMigrationButton = enableMigrationButton;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigrationInfo that = (MigrationInfo) o;

        if (enableMigrationButton != that.enableMigrationButton) return false;
        if (oldEntity != null ? !oldEntity.equals(that.oldEntity) : that.oldEntity != null) return false;
        if (newEntity != null ? !newEntity.equals(that.newEntity) : that.newEntity != null) return false;
        if (migrationURL != null ? !migrationURL.equals(that.migrationURL) : that.migrationURL != null) return false;
        if (oldEntitiesCount != null ? !oldEntitiesCount.equals(that.oldEntitiesCount) : that.oldEntitiesCount != null)
            return false;
        return newEntitiesCount != null ? newEntitiesCount.equals(that.newEntitiesCount) : that.newEntitiesCount == null;
    }

    @Override
    public int hashCode() {
        int result = oldEntity != null ? oldEntity.hashCode() : 0;
        result = 31 * result + (newEntity != null ? newEntity.hashCode() : 0);
        result = 31 * result + (migrationURL != null ? migrationURL.hashCode() : 0);
        result = 31 * result + (oldEntitiesCount != null ? oldEntitiesCount.hashCode() : 0);
        result = 31 * result + (newEntitiesCount != null ? newEntitiesCount.hashCode() : 0);
        result = 31 * result + (enableMigrationButton ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MigrationInfo{");
        sb.append("oldEntity='").append(oldEntity).append('\'');
        sb.append(", newEntity='").append(newEntity).append('\'');
        sb.append(", migrationURL='").append(migrationURL).append('\'');
        sb.append(", oldEntitiesCount=").append(oldEntitiesCount);
        sb.append(", newEntitiesCount=").append(newEntitiesCount);
        sb.append(", enableMigrationButton=").append(enableMigrationButton);
        sb.append('}');
        return sb.toString();
    }
}
