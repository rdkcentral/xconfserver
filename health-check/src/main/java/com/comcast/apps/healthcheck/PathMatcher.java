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
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created with IntelliJ IDEA.
 * User: elochs200
 * Date: 9/16/13
 * Time: 11:26 AM
 * To change this template use File | Settings | File Templates.
 */
@ManagedResource
public class PathMatcher implements IPathMatcher {
    private static Logger log = LoggerFactory.getLogger(PathMatcher.class);

    @ManagedAttribute
    public String getExcludePaths() {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> it = excludePaths.iterator();
        if (it.hasNext()) {
            stringBuilder.append(it.next());
            while (it.hasNext()) {
                stringBuilder.append(",").append(it.next());
            }
        }
        return stringBuilder.toString();
    }

    @ManagedAttribute
    public void setExcludePaths(String paths) {
        excludePaths.clear();
        if (paths != null) {
            for(String value : paths.split(",")) {
                value = value.trim();
                if (!value.isEmpty()) {
                    excludePaths.add(value);
                }
            }
        }
    }

    private LinkedHashSet<String> excludePaths;

    public PathMatcher() {
        excludePaths = new LinkedHashSet<String>();
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        if (request != null && excludePaths != null) {
            String[] path = getPath(request).split("/");
            for (String exclude: excludePaths) {
                if (exclude.equals(path[1])) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String getPath(HttpServletRequest request) {
        return (request.getPathInfo() != null)
                ? request.getServletPath() + request.getPathInfo()
                : request.getServletPath();
    }

}
