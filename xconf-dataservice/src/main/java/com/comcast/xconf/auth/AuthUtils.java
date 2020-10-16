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
 * Created: 12/10/2018
 */
package com.comcast.xconf.auth;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.util.Base58;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.http.QueryStringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthUtils {

    public static final String AUTH_TOKEN = "token";
    public static final String AUTH_RESPONSE = "authResponse";
    public static final String ALL_PERMISSIONS = "permitAll";
    private static final Logger log = LoggerFactory.getLogger(AuthUtils.class);

    public static AuthResponse getAuthResponse(final HttpServletRequest httpRequest) {
        final AuthResponse authResponse = (AuthResponse) httpRequest.getAttribute(AUTH_RESPONSE);
        if (authResponse == null) {
            final HttpSession httpSession = httpRequest.getSession(false);
            return httpSession != null ? (AuthResponse) httpSession.getAttribute(AUTH_RESPONSE) : null;
        }
        return authResponse;
    }

    public static String addParametersToUrl(final String url, final Map<String, String> parameters) {
        String uriWithQuery = null;
        try {
            QueryStringDecoder decoder = new QueryStringDecoder(url);
            Map<String, String> baseParams = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : decoder.getParameters().entrySet()) {
                String key = entry.getKey();
                if (!parameters.containsKey(key)) {
                    String value = "";
                    if (entry.getValue().size() > 0) {
                        value = entry.getValue().get(0);
                    }
                    baseParams.put(key, value);
                }
            }

            QueryStringEncoder encoder = new QueryStringEncoder(decoder.getPath());
            addQueryToEncoder(encoder, parameters);
            addQueryToEncoder(encoder, baseParams);

            uriWithQuery = encoder.toString();
        } catch (Exception e) {
            log.error("Could not build URI with parameters", e);
        }

        return uriWithQuery;
    }

    public static void addQueryToEncoder(QueryStringEncoder encoder, Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            encoder.addParam(entry.getKey(), entry.getValue());
        }
    }

    public static void eraseCookie(final String name, final String path, final HttpServletResponse response) {
        final Cookie c = new Cookie(name, "");
        c.setMaxAge(0);

        // for empty context path
        if (StringUtils.isBlank(path)) {
            c.setPath("/");
        } else {
            c.setPath(path);
        }
        response.addCookie(c);
    }

    public static String getTokenFromRequest(final HttpServletRequest request) {
        String authToken = request.getHeader(AUTH_TOKEN);
        if (authToken == null) {
            authToken = getCookieValue(AUTH_TOKEN, request);
            if (authToken == null) {
                authToken = request.getParameter(AUTH_TOKEN);
            }
        }
        return authToken;
    }

    public static String getCookieValue(final String name, final HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (final Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static AuthResponse parseToken(String token) {
        try {
            String json = new String(Base58.decode(token));
            return JsonUtil.fromJson(AuthResponse.class, json);
        } catch (Exception e) {
            log.error("Can't deserialize auth response token. Json: " + token);
            return null;
        }
    }

    public static String convertIntoToken(AuthResponse authResponse) {
        return Base58.encode(JsonUtil.toJson(authResponse).getBytes());
    }

}
