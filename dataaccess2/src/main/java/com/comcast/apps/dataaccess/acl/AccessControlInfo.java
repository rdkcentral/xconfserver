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
package com.comcast.apps.dataaccess.acl;

import java.util.Set;

public class AccessControlInfo {

    public static final int PERM_READ = 4;
    public static final int PERM_WRITE = 2;

    private int ownerPermissions;
    private int groupPermissions;
    private int othersPermissions;

    private Set<String> trustedGroups;

    private String ownerId;

    public Integer getOwnerPermissions() {
        return ownerPermissions;
    }

    public void setOwnerPermissions(Integer ownerPermissions) {
        this.ownerPermissions = ownerPermissions;
    }

    public Integer getGroupPermissions() {
        return groupPermissions;
    }

    public void setGroupPermissions(Integer groupPermissions) {
        this.groupPermissions = groupPermissions;
    }

    public Integer getOthersPermissions() {
        return othersPermissions;
    }

    public void setOthersPermissions(Integer othersPermissions) {
        this.othersPermissions = othersPermissions;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Set<String> getTrustedGroups() {
        return trustedGroups;
    }

    public void setTrustedGroups(Set<String> trustedGroups) {
        this.trustedGroups = trustedGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccessControlInfo)) return false;

        AccessControlInfo that = (AccessControlInfo) o;

        if (groupPermissions != that.groupPermissions) return false;
        if (othersPermissions != that.othersPermissions) return false;
        if (ownerPermissions != that.ownerPermissions) return false;
        if (ownerId != null ? !ownerId.equals(that.ownerId) : that.ownerId != null) return false;
        if (trustedGroups != null ? !trustedGroups.equals(that.trustedGroups) : that.trustedGroups != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ownerPermissions;
        result = 31 * result + groupPermissions;
        result = 31 * result + othersPermissions;
        result = 31 * result + (trustedGroups != null ? trustedGroups.hashCode() : 0);
        result = 31 * result + (ownerId != null ? ownerId.hashCode() : 0);
        return result;
    }
}
