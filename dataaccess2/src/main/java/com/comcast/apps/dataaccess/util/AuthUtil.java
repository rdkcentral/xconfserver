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
package com.comcast.apps.dataaccess.util;

import com.comcast.apps.dataaccess.acl.AuthResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DataBindingException;

import static com.comcast.apps.dataaccess.acl.AuthResponse.AUTH_RESPONSE_ATTRIBUTE_NAME;
import static com.comcast.apps.dataaccess.util.UtilConstants.*;

public class AuthUtil {

    private static Logger logger = LoggerFactory.getLogger(AuthUtil.class);

    public static String getUserName() {
        if (isDevProfile()) {
            return DEV_PROFILE;
        }
        try {
            AuthResponse authResponse = getAuthResponse(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest());
            if (authResponse != null && StringUtils.isNotBlank(authResponse.getUsername())) {
                return authResponse.getUsername();
            }
        } catch (IllegalStateException e) {
            logger.error("Method is called not by the HTTP request. Exception: ", e);
            return DATA_SERVICE;
        }
        return DATA_SERVICE;
    }

    public static final AuthResponse getAuthResponse(HttpServletRequest httpRequest) {
        AuthResponse authResponse = readAuthResponse(httpRequest.getAttribute(AUTH_RESPONSE_ATTRIBUTE_NAME));
        if (authResponse == null) {
            HttpSession httpSession = httpRequest.getSession(false);
            return httpSession != null ? readAuthResponse(httpSession.getAttribute(AUTH_RESPONSE_ATTRIBUTE_NAME)) : null;
        } else {
            return authResponse;
        }
    }

    private static AuthResponse readAuthResponse(Object authResponseAttribute) {
        if (authResponseAttribute == null) {
            return null;
        }
        try {
            return JsonUtil.fromJson(new TypeReference<AuthResponse>(){}, JsonUtil.toJson(authResponseAttribute));
        } catch (DataBindingException e) {
            return null;
        }
    }

    private static boolean isDevProfile() {
        final String activeProfiles = System.getenv().get(SPRING_PROFILES_ACTIVE);

        return StringUtils.contains(activeProfiles, DEV_PROFILE);
    }
}
