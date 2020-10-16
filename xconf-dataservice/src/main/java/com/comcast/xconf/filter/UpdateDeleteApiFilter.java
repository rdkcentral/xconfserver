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
 * Author: ikostrov
 * Created: 7/6/16  12:28 PM
 */
package com.comcast.xconf.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@Component
public class UpdateDeleteApiFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(UpdateDeleteApiFilter.class);

    private static final String REST_DELETE_URL = "/delete/";
    private static final String REST_UPDATES_URL = "/updates/";
    public static final String ENABLE_UPDATE_DELETE_API_PROPERTY_NAME = "specific.enableUpdateDeleteAPI";

    private boolean enableUpdateDeleteAPI = true;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        enableUpdateDeleteAPI = readProperty();
    }

    private boolean readProperty() {
        String appConfig = System.getProperty("appConfig");
        if (StringUtils.isNotBlank(appConfig)) {
            try {
                Properties properties = new Properties();
                properties.load(new FileReader(appConfig));
                String enableApiProperty = properties.getProperty(ENABLE_UPDATE_DELETE_API_PROPERTY_NAME);
                if (StringUtils.isNotBlank(enableApiProperty)) {
                    return Boolean.parseBoolean(enableApiProperty);
                }
            } catch (IOException e) {
                log.error("Can't read properties from appConfig=" + appConfig + " Will fallback to enableUpdateDeleteAPI=true");
            }
        }
        return true;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!enableUpdateDeleteAPI) {
            String uri = httpRequest.getRequestURI();
            if (uri.contains(REST_DELETE_URL) || uri.contains(REST_UPDATES_URL)) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}
