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
 * Author: Igor Kostrov
 * Created: 12/12/2018
 */
package com.comcast.xconf.admin.service.login;

import com.comcast.xconf.auth.AuthResponse;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LoginService {

    private static final String ADMIN = "admin";
    private static final String USER = "user";

    private final Set<String> adminPermissions = Sets.newHashSet(
            "read-common", "write-common", "read-firmware-*", "write-firmware-*",
            "read-dcm-*", "write-dcm-*", "read-telemetry-*", "write-telemetry-*",
            "view-tools", "write-tools", "read-firmware-rule-templates", "write-firmware-rule-templates",
            "read-changes-*", "write-changes-*"
    );

    private final Set<String> userPermissions = Sets.newHashSet(
            "read-common", "read-firmware-*", "read-dcm-*", "read-telemetry-*",
            "view-tools", "read-firmware-rule-templates", "read-changes-*"
    );

    public AuthResponse authenticate(String username, String password) {
        if (ADMIN.equalsIgnoreCase(username) && ADMIN.equalsIgnoreCase(password)) {
            return createAdminAuthResponse();
        } else if (USER.equalsIgnoreCase(username) && USER.equalsIgnoreCase(password)) {
            return createUserAuthResponse();
        } else {
            return null;
        }
    }

    private AuthResponse createAdminAuthResponse() {
        AuthResponse response = createBaseAuthResponse(ADMIN);
        response.setPermissions(adminPermissions);
        return response;
    }

    private AuthResponse createUserAuthResponse() {
        AuthResponse response = createBaseAuthResponse(USER);
        response.setPermissions(userPermissions);
        return response;
    }

    private AuthResponse createBaseAuthResponse(String user) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setUsername(user);
        authResponse.setFirstName(user);
        authResponse.setLastName(user);
        authResponse.setServiceName("xconf");
        return authResponse;
    }
}
