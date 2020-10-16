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
 * <p>
 * Author: mdolina
 * Created: 9/6/17  13:42 PM
 */
package com.comcast.xconf.permissions;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.firmware.ApplicationType;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.comcast.xconf.permissions.Permissions.*;

public abstract class PermissionService {

    @Autowired
    private Environment environment;

    @Autowired
    private AuthService authService;

    private static String APPLICATION_TYPE = "applicationType";

    private static final String DEV_PROFILE = "dev";

    abstract EntityPermission getEntityPermission();

    public String getReadApplication() {
        String cookieApplication = getApplicationFromCookies();
        if (StringUtils.isNotBlank(cookieApplication)) {
            return cookieApplication;
        }
        Set<String> permissions = getPermissions();
        if (permissions.contains(getEntityPermission().getReadAll())
                || permissions.contains(getEntityPermission().getReadStb()) && permissions.contains(getEntityPermission().getReadXhome())) {
            throw new ValidationRuntimeException("User has permissions for multiple applications and no applicationType cookie found.");
        } else if (permissions.contains(getEntityPermission().getReadXhome())) {
            return ApplicationType.XHOME;
        } else if (permissions.contains(getEntityPermission().getReadStb())) {
            return ApplicationType.STB;
        } else if (permissions.contains(getEntityPermission().getReadRdkcloud())) {
            return ApplicationType.RDKCLOUD;
        }
        return null;
    }

    public String getWriteApplication() {
        String cookieApplication = getApplicationFromCookies();
        if (StringUtils.isNotBlank(cookieApplication)) {
            return cookieApplication;
        }
        Set<String> permissions = getPermissions();
        if (permissions.contains(getEntityPermission().getWriteAll())
                || permissions.contains(getEntityPermission().getWriteStb()) && permissions.contains(getEntityPermission().getWriteXhome())) {
            throw new ValidationRuntimeException("Permissions for multiple application types are available and no applicationType cookie setup.");
        }
        if (permissions.contains(getEntityPermission().getWriteXhome())) {
            return ApplicationType.XHOME;
        } else if (permissions.contains(getEntityPermission().getWriteStb())) {
            return ApplicationType.STB;
        } else if (permissions.contains(getEntityPermission().getWriteRdkcloud())) {
            return ApplicationType.RDKCLOUD;
        }
        return null;
    }

    public boolean canWrite() {
        String applicationType = getWriteApplication();
        Set<String> permissions = getPermissions();
        if (permissions.contains(getEntityPermission().getWriteAll())) {
            return true;
        }
        if (ApplicationType.STB.equals(applicationType)) {
            return permissions.contains(getEntityPermission().getWriteStb());
        } else if (ApplicationType.XHOME.equals(applicationType)) {
            return permissions.contains(getEntityPermission().getWriteXhome());
        } else if (ApplicationType.RDKCLOUD.equals(applicationType)) {
            return permissions.contains(getEntityPermission().getWriteRdkcloud());
        }
        return false;
    }

    public boolean canRead() {
        String applicationType = getReadApplication();
        Set<String> permissions = getPermissions();
        if (permissions.contains(getEntityPermission().getReadAll())) {
            return true;
        }
        if (ApplicationType.STB.equals(applicationType)) {
            return permissions.contains(getEntityPermission().getReadStb());
        } else if (ApplicationType.XHOME.equals(applicationType)) {
            return permissions.contains(getEntityPermission().getReadXhome());
        } else if (ApplicationType.RDKCLOUD.equals(applicationType)) {
            return permissions.contains(getEntityPermission().getReadRdkcloud());
        }
        return false;
    }

    public String getApplicationFromCookies() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        List<Cookie> cookies = new ArrayList<>();
        if (request.getCookies() != null) {
            cookies = Lists.newArrayList(request.getCookies());
        }
        for (Cookie cookie : cookies) {
            if (APPLICATION_TYPE.equals(cookie.getName()) && StringUtils.isNotBlank(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public Set<String> getPermissions() {
        if (isDevProfile()) {
            return getDevPermissions();
        }

    	return authService.getPermissions();
    }

    private boolean isDevProfile() {
        final String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles != null && activeProfiles.length > 0) {
            return DEV_PROFILE.equals(activeProfiles[0]);
        }
        return DEV_PROFILE.equals(environment.getDefaultProfiles()[0]);
    }

    private Set<String> getDevPermissions() {
        return Sets.newHashSet(WRITE_FIRMWARE_ALL, READ_FIRMWARE_ALL,
                WRITE_DCM_ALL, READ_DCM_ALL,
                WRITE_TELEMETRY_ALL, READ_TELEMETRY_ALL,
                READ_CHANGES_ALL, WRITE_CHANGES_ALL);
    }
}
