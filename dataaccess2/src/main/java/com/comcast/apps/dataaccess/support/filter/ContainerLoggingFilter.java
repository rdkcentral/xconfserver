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

import com.comcast.apps.dataaccess.support.exception.WebAppException;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;

/**
 * This class represents logging filter for http requests/responses and it uses org.slf4j.Logger.
 * And its log level can be adjusted via properties files.
 */
public class ContainerLoggingFilter extends OncePerRequestFilter {
    private static Logger logger = LoggerFactory.getLogger(ContainerLoggingFilter.class);
    private static final int BUFFER_SIZE = 256;
    private static final int[] STATUS_CODES_TO_HANDLE = {500, 503, 400, 412};

    private long requestID;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
        final FilterChain filterChain) throws ServletException, IOException {

        final HttpServletRequest bufferedRequest = new HttpServletRequestWrapper(request);

        beforeRequest(bufferedRequest);
        try {
            doFilter(bufferedRequest, response, filterChain);
        } finally {
            afterRequest(bufferedRequest, response);
        }
    }

    private void beforeRequest(final HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            createDebugInfo(request);
        }
    }

    private void afterRequest(final HttpServletRequest request, final HttpServletResponse response) {
        if (!logger.isDebugEnabled() && !logger.isErrorEnabled()) {
            return;
        }

        if (logger.isErrorEnabled()) {
            if (!handleStatusCode(response.getStatus())) {
                return;
            }
        }

        setRequestID(request);

        logResponse(response, request);
    }


    private void logResponse(final HttpServletResponse response, final HttpServletRequest request) {
        final StringBuilder responseBuilder = new StringBuilder();

        final String id = request.getAttribute("request-id").toString();

        if (handleStatusCode(response.getStatus())) {
            responseBuilder.append(requestToString(request));
        }

        // build response headers
        Collection<String> headerNames = response.getHeaderNames();
        for (final String headerName : headerNames) {
            responseBuilder.append(id).append(" <- ");
            responseBuilder.append(headerName).append(" : ").append(response.getHeaders(headerName)).append('\n');
        }
        responseBuilder.append('\n');

    }

    /* Logs the request details and returns the request. */
    private void createDebugInfo(final HttpServletRequest request) {
        setRequestID(request);
        StringBuilder requestBuilder = requestToString(request);

        try {
            // build request entity
            InputStream in = request.getInputStream();

            int availableBytesNumber = in.available();
            if (availableBytesNumber > 0) {
                ByteArrayOutputStream out = new ByteArrayOutputStream(availableBytesNumber);
                ByteStreams.copy(in,out);

                byte[] requestEntity = out.toByteArray();
                if (requestEntity.length != 0) {
                    requestBuilder.append(new String(requestEntity));
                }
            }
        } catch (IOException ex) {
            throw new WebAppException(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log(requestBuilder.toString());
        }
    }

    protected void log(String logMessage) {
        if (logger.isDebugEnabled()) {
            logger.debug(logMessage);
        } else if (logger.isErrorEnabled()) {
            logger.error(logMessage);
        }
    }

    protected StringBuilder requestToString(final HttpServletRequest request) {
        final StringBuilder requestBuilder = new StringBuilder(BUFFER_SIZE);
        int requestEntitySize = 0;
        try {
            requestEntitySize = request.getInputStream().available();
        } catch (IOException ex) {
            log("Can not get available number of bytes from request entity stream.");
        } finally {
            requestBuilder.ensureCapacity(BUFFER_SIZE + requestEntitySize);
        }

        String id = request.getAttribute("request-id").toString();

        // build request line
        requestBuilder.append("Server inbound request:").append('\n');
        requestBuilder.append(id).append(" -> ").append(request.getMethod()).append(" ").
                append(request.getRequestURI()).append('\n');

        // build request headers
        Enumeration<String> headerNamesEnumeration = request.getHeaderNames();
        while (headerNamesEnumeration.hasMoreElements()) {
            final String headerName = headerNamesEnumeration.nextElement(); // get request header name

            // read request values, for header information that have multiple values.
            final Enumeration<String> values = request.getHeaders(headerName);
            while (values.hasMoreElements()) {
                final String headerValue = values.nextElement();
                requestBuilder.append(id).append(" -> ").append(headerName).append(" : ").append(headerValue).append('\n');
            }
        }
        requestBuilder.append('\n');
        return requestBuilder;
    }

    /** Do we handle the given status code? */
    public boolean handleStatusCode(int statusCode) {
        for (int i = 0; i < STATUS_CODES_TO_HANDLE.length; i++) {
            if (STATUS_CODES_TO_HANDLE[i] == statusCode) {
                return true;
            }
        }
        return false;
    }

    /** Attach an ID to the request to correlate it with the response and label the log messages. */
    private synchronized void setRequestID(final HttpServletRequest request) {
        if (request.getAttribute("request-id") == null) {
            request.setAttribute("request-id", Long.toString(++requestID));
        }
    }
}
