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
 * Created: 10/25/2017
*/
package com.comcast.xconf.permissions;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.firmware.ApplicationType;
import org.apache.commons.lang.StringUtils;

public class PermissionHelper {

    public static void validateWrite(PermissionService permissionService, String entityApplicationType) {
        if (StringUtils.isBlank(entityApplicationType)) {
            throw new ValidationRuntimeException("Application type is empty");
        }
        if (!ApplicationType.isValid(entityApplicationType)) {
            throw new ValidationRuntimeException("ApplicationType is not valid: notValidApplicationType= " + entityApplicationType);
        }
        String activeApplication = permissionService.getApplicationFromCookies();
        if (StringUtils.isNotBlank(activeApplication) && !StringUtils.equals(activeApplication, entityApplicationType)) {
            throw new ValidationRuntimeException("Current application type " + activeApplication + " doesn't match with entity application type: " + entityApplicationType);
        }
        if (!permissionService.canWrite()) {
            throw new ValidationRuntimeException("No write permissions for ApplicationType " + entityApplicationType);
        }
    }
}
