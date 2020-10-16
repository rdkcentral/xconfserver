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

package com.comcast.xconf.auth;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private static final String UNKNOWN_USER = "UNKNOWN_USER";

    public String getUserName() {
        AuthResponse authResponse = getCurrentAuthResponse();
        if (authResponse != null) {
            return authResponse.getUsername();
        }
        return null;
    }

    public String getUserNameOrUnknown() {
        String userName = getUserName();
        return StringUtils.isBlank(userName) ? UNKNOWN_USER : userName;
    }

    private AuthResponse getCurrentAuthResponse() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return AuthUtils.getAuthResponse(request);
    }

    public Set<String> getPermissions() {
        AuthResponse authResponse = getCurrentAuthResponse();
        if (authResponse != null) {
            return authResponse.getPermissions();
        }
        return new HashSet<>();
    }
}