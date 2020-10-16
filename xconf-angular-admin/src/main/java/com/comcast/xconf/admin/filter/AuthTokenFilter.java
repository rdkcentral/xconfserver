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
package com.comcast.xconf.admin.filter;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.auth.AuthResponse;
import com.comcast.xconf.auth.AuthUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class AuthTokenFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);

    private static final String LOGIN_URL = "loginUrl";

    private static final String DEV_PROFILE = "devProfile";

    private static final String PATHS_TO_RETURN_401 = "pathsToReturn401Unauthorized";

    private static final String PATHS_TO_INCLUDE = "pathsToInclude";

    private static final String PATHS_TO_EXCLUDE = "pathsToExclude";

    private static final String INIT_PARAM_DELIMITER = ";";

    private static final String METHOD_DELIMITER = ":!";

    private static final String XML_HTTP_REQUEST = "XMLHttpRequest";

    private static final String USER_AGENT = "User-Agent";

    private String loginUrl;

    private String devProfile;

    private final Map<String, Map<String, Set<String>>> pathsToReturn401 = new LinkedHashMap<>();

    private final Map<String, Map<String, Set<String>>> pathsToInclude = new LinkedHashMap<>();

    private final Map<String, Map<String, Set<String>>> pathsToExclude = new LinkedHashMap<String, Map<String, Set<String>>>() {{
        put("/favicon.ico", new HashMap<String, Set<String>>());
        put("/auth", new HashMap<String, Set<String>>());
    }};

    private final Set<String> currentProfiles = new HashSet<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        loginUrl = filterConfig.getInitParameter(LOGIN_URL);
        devProfile = filterConfig.getInitParameter(DEV_PROFILE);

        pathsToReturn401.putAll(
                processParamValue(
                        filterConfig.getInitParameter(PATHS_TO_RETURN_401),
                        INIT_PARAM_DELIMITER,
                        METHOD_DELIMITER
                )
        );
        pathsToInclude.putAll(
                processParamValue(
                        filterConfig.getInitParameter(PATHS_TO_INCLUDE),
                        INIT_PARAM_DELIMITER,
                        METHOD_DELIMITER
                )
        );
        pathsToExclude.putAll(
                processParamValue(
                        filterConfig.getInitParameter(PATHS_TO_EXCLUDE),
                        INIT_PARAM_DELIMITER,
                        METHOD_DELIMITER
                )
        );

        currentProfiles.addAll(getSpringProfiles(filterConfig.getServletContext()));
        log.info("Current Profile(s): {}", currentProfiles);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        final String path = getPath(request);

        if (matchesPath(loginUrl, path)) {
            AuthUtils.eraseCookie(AuthUtils.AUTH_TOKEN, request.getContextPath(), response);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (matches(path, request.getMethod(), request.getScheme(), pathsToExclude)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        } else {
            log.debug("Doing the filter for {}", request.getRequestURL());
        }

        if (currentProfiles.contains(devProfile)) {
            AuthResponse authResponse = new AuthResponse();
            authResponse.setUsername(devProfile);
            authResponse.setPermissions(new HashSet<String>() {{add(AuthUtils.ALL_PERMISSIONS);}});
            authResponse.setGroups(new HashSet<String>());

            addTokenToResponse(JsonUtil.toJson(authResponse), request.getContextPath(), response);
            request.setAttribute(AuthUtils.AUTH_RESPONSE, authResponse);

            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        final boolean isTokenInIncludedPaths = matches(path, request.getMethod(), request.getScheme(), pathsToInclude);
        final boolean isTokenInReturn401Paths = matches(path, request.getMethod(), request.getScheme(), pathsToReturn401);

        if (isTokenInIncludedPaths || isTokenInReturn401Paths) {
            final String authToken = AuthUtils.getTokenFromRequest(request);

            if (StringUtils.isBlank(authToken)) {
                if (isTokenInIncludedPaths) { //if token is blank, we get the request slip through despite pathsToReturn401 if it is not included
                    handleErrorToken(request, response);
                    return;
                }
            } else {

                if (!isValidToken(authToken, request)) {
                    handleErrorToken(request, response);
                    return;
                } else {
                    addTokenToResponse(authToken, request.getContextPath(), response);
                    request.setAttribute(AuthUtils.AUTH_RESPONSE, AuthUtils.parseToken(authToken));
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    protected boolean isValidToken(String token, HttpServletRequest request) {
        return AuthUtils.parseToken(token) != null;
    }

    private boolean matchesPath(String url, String path) {
        return FilenameUtils.wildcardMatch(path, url);
    }

    private boolean matches(final String servletPath, final String method, final String scheme,
                            final Map<String, Map<String, Set<String>>> paths) {
        for (final Map.Entry<String, Map<String, Set<String>>> entry : paths.entrySet()) {
            if ((entry.getValue().isEmpty() || !matchesMethodAndScheme(entry.getValue(), method, scheme))
                    && matchesPath(entry.getKey(), servletPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesMethodAndScheme(
            final Map<String, Set<String>> methods, final String method, final String scheme) {
        for (final Map.Entry<String, Set<String>> entry : methods.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(method)
                    && (entry.getValue().isEmpty() || containsIgnoreCase(entry.getValue(), scheme))) {
                return true;
            }
        }
        return false;
    }

    private void handleErrorToken(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String path = getPath(request);
        if (isAjaxRequest(request) || matches(path, request.getMethod(), request.getScheme(), pathsToReturn401)) {
            int status = HttpServletResponse.SC_UNAUTHORIZED;
            response.setStatus(status);

            log.warn("SecurityLogging library=ACL status=" + status + " path=" + request.getRequestURI()
                    + " agent=" + packStringIntoQuotes(request.getHeader(USER_AGENT)));
        } else {
            response.sendRedirect(request.getContextPath() + loginUrl);
        }
    }

    private static String packStringIntoQuotes(String string) {
        if (string != null) {
            String trimmingString = string.trim();
            if (trimmingString.length() > 0) {
                if (trimmingString.startsWith("[") && trimmingString.endsWith("]")) {
                    trimmingString = trimmingString.substring(1, trimmingString.length() - 1); //remove first and last square brackets
                }

                if (trimmingString.startsWith("\"") && trimmingString.endsWith("\"")) {
                    return trimmingString;
                }

                return "\"" + trimmingString + "\"";
            }
        }

        return "";
    }

    private Set<String> processSimpleParamValue(final String paramValue, final String delimiter) {
        final Set<String> result = new HashSet<>();
        if(StringUtils.isBlank(paramValue)) {
            return result;
        }
        final String[] data = paramValue.split(delimiter);
        for (final String item : data) {
            result.add(item.trim());
        }
        return result;
    }

    private Map<String, Map<String, Set<String>>> processParamValue(
            final String paramValue, final String pathDelimiter, final String methodDelimiter) {
        final Map<String, Map<String, Set<String>>> result = new LinkedHashMap<>();
        if(StringUtils.isBlank(paramValue)) {
            return result;
        }
        final String[] data = paramValue.split(pathDelimiter);
        for (final String item : data) {
            final String trimmedValue = item.trim();
            final String[] values = trimmedValue.split(methodDelimiter);

            final String path;
            if (values.length >= 1) {
                path = values[0];
            } else {
                break;
            }

            final Map<String, Set<String>> methods = new LinkedHashMap<>();
            for (int i = 1; i < values.length; i++) {
                final String[] methodAndSchemes =  values[i].split("@");
                final Set<String> schemes = new HashSet<>();
                for (int j = 1; j < methodAndSchemes.length; j++) {
                    schemes.add(methodAndSchemes[j]);
                }
                methods.put(methodAndSchemes[0], schemes);
            }

            result.put(path, methods);
        }
        return result;
    }

    private String getPath(HttpServletRequest request) {
        return (request.getPathInfo() != null) ? request.getServletPath() + request.getPathInfo() : request.getServletPath();
    }

    private void addTokenToResponse(final String token, final String path, final HttpServletResponse response) {
        response.setHeader(AuthUtils.AUTH_TOKEN, token);

        final Cookie tokenCookie = new Cookie(AuthUtils.AUTH_TOKEN, token);
        tokenCookie.setMaxAge(Integer.MAX_VALUE);

        // for empty context path
        if (StringUtils.isBlank(path)) {
            tokenCookie.setPath("/");
        } else {
            tokenCookie.setPath(path);
        }
        response.addCookie(tokenCookie);
    }

    private Set<String> getSpringProfiles(final ServletContext servletContext) {
        String springProfiles = System.getProperty("spring.profiles.active");
        if (springProfiles == null) {
            springProfiles = servletContext.getInitParameter("spring.profiles.default");
        }
        return processSimpleParamValue(springProfiles, ",");
    }

    private boolean isAjaxRequest(final HttpServletRequest request) {
        return XML_HTTP_REQUEST.equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    private boolean containsIgnoreCase(final Collection<String> collection, final String str) {
        for (final String item : collection) {
            if (item.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {

    }
}
