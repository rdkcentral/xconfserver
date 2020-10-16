/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.apps.healthcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class StatusCodeFilter extends OncePerRequestFilter {

    private static Logger log = LoggerFactory.getLogger(StatusCodeFilter.class);

    private static final String REQUEST_START_TIME = "REQUEST_START_TIME";

    public IMetricManager getMetricManager() {
        return metricManager;
    }

    public void setMetricManager(IMetricManager metricManager) {
        this.metricManager = metricManager;
    }

    @Autowired
    private IMetricManager metricManager;

    @Autowired(required = false)
    private IPathMatcher pathMatcher;


    public IPathMatcher getPathMatcher() {
        return pathMatcher;
    }

    public void setPathMatcher(IPathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @Override
    protected void initFilterBean() throws ServletException {
        if (pathMatcher == null) {
            pathMatcher = new PathMatcher();
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (pathMatcher.matches(request)) {
            if (request.getAttribute(REQUEST_START_TIME) == null) {
                request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
            }

            try {
                filterChain.doFilter(request, response);
            } finally {
                int duration = 0;
                try {
                    Long requestStartTime = (Long) request.getAttribute(REQUEST_START_TIME);
                    request.getRequestURI();
                    if (requestStartTime != null)
                        duration = (int) (System.currentTimeMillis() - requestStartTime);
                } catch (Exception e) {
                    log.warn("Could not get request start time", e);
                }
                metricManager.addMetric(response.getStatus(), duration);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("ignoring request for path = {}", request.getPathInfo());

            }
            filterChain.doFilter(request, response);
        }
    }
}
