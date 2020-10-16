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
 */
package com.comcast.apps.dataaccess.acl;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Set;

public class AuthResponse implements Serializable {

    private String serviceName;

    private String username;

    private String firstName;

    private String lastName;

    private Set<String> permissions;

    public static final String AUTH_RESPONSE_ATTRIBUTE_NAME = "authResponse";

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
    private Set<String> groups;

    public AuthResponse() {
    }

    public String getServiceName() {
        return this.serviceName;
    }


    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<String> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getGroups() {
        return this.groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AuthResponse{");
        sb.append("serviceName='").append(serviceName).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append(", permissions=").append(permissions);
        sb.append(", groups=").append(groups);
        sb.append('}');
        return sb.toString();
    }
}
