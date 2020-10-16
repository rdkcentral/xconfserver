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
package com.comcast.apps.dataaccess.support.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Filter that logs each request. Keep it fast.
 */
public class AccessLoggingFilter extends OncePerRequestFilter {
    public static final int SAT_EXPOSURE_TAIL_SIZE = 12;
    private static final int MAX_PAYLOAD_LENGTH = 500;

    protected static Logger log = LoggerFactory.getLogger(AccessLoggingFilter.class);

    public static final String START_TIME = "request-start-time";
    private static final String X_AUTHORIZATION = "X-Authorization";

    protected static final ArrayList<String> NOT_LOGGED_HTTP_HEADERS = new ArrayList<>();
    static {
        NOT_LOGGED_HTTP_HEADERS.addAll(Arrays.asList("X-CodeBig-Principal", "Accept-Encoding", "Accept", "Content-Type", "HA-Forwarded-For", "Content-Length"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getAttribute(START_TIME) == null) {
            request.setAttribute(START_TIME, System.currentTimeMillis()); // save request time
        } else {
            log.warn("{} is already set -- possible configuration error", START_TIME);
        }
        boolean isFirstRequest = !isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;

        if (isFirstRequest && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }
        try {
            filterChain.doFilter(requestToUse, response);
        } finally {
            log(requestToUse, response);
        }
    }

    protected void log(HttpServletRequest request, HttpServletResponse response) {
        String duration = "unknown";
        try {
            Long requestStartTime = (Long) request.getAttribute(START_TIME);
            if (requestStartTime != null && requestStartTime > 0) {
                duration = Long.toString(System.currentTimeMillis() - requestStartTime);
            }
        } catch (Exception x) {
            log.warn("Could not get request start time {}", x);
        }

        final Map<String, String> valuesToLog = new LinkedHashMap<>();
        valuesToLog.put("method", request.getMethod());
        valuesToLog.put("url", getFullUrl(request));
        valuesToLog.put("status", String.valueOf(response.getStatus()));
        valuesToLog.put("sourceIP", request.getRemoteAddr());
        valuesToLog.put("duration", duration);

        if (HttpMethod.GET.equalsIgnoreCase(request.getMethod()) ) {
            Object numResultsObj = request.getAttribute("num_results");
            String numResults = (numResultsObj != null) ? String.valueOf(numResultsObj) : "0";
            valuesToLog.put("num_results", numResults);
        }

        addLogEntries(request, valuesToLog);
        valuesToLog.put("headers", getHttpHeaders(request));

        String str = "";
        for (Map.Entry<String, String> entry : valuesToLog.entrySet()) {
            if (entry.getKey().equals("url")) {
                str = str + entry.getValue() + " ";
                continue;
            }

            if (entry.getKey().equals("headers")) {
                str = str + "HttpHeaders: " + entry.getValue();
                break;
            }

            str = str + entry.getKey() + "=" + entry.getValue() + " ";
        }

        log.info(str);
    }

    protected void addLogEntries(HttpServletRequest request, Map<String, String> additionParams) {}

    protected String readPayload(HttpServletRequest request) {
        String payload = "none";
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH);
                    payload = new String(buf, 0, length, wrapper.getCharacterEncoding());
                    payload = "\n" + URLDecoder.decode(cutTrailingEscapePattern(payload), wrapper.getCharacterEncoding());
                } catch (Exception e) {
                    log.error("Can't decode payload", e);
                    payload = "[unknown]";
                }
            }
        }
        return payload;
    }

    private String cutTrailingEscapePattern(String str) {
        int indexOfPercent = str.lastIndexOf("%");
        if (indexOfPercent < 0) {
            return str;
        }
        return str.substring(0, indexOfPercent);
    }

    private String getHttpHeaders(HttpServletRequest request) {
        StringBuilder httpHeaders = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (NOT_LOGGED_HTTP_HEADERS.contains(headerName)) {
                continue;
            }

            String headerValue = request.getHeader(headerName);
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(headerName)) {
                headerValue = StringUtils.right(headerValue, SAT_EXPOSURE_TAIL_SIZE);
            } else if (X_AUTHORIZATION.equalsIgnoreCase(headerName)) {
                headerValue = "scrubbed-value";
            }

            if (HttpHeaders.HOST.equalsIgnoreCase(headerName)) {
                continue;
            }

            httpHeaders.append(headerName).append("=\"").append(headerValue).append("\"");

            if (headerNames.hasMoreElements()) {
                httpHeaders.append(" ");
            }
        }

        return httpHeaders.toString();
    }

    private String getFullUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        return (StringUtils.isEmpty(queryString)) ?
            requestURL.toString() : requestURL.append('?').append(queryString).toString();
    }
}
