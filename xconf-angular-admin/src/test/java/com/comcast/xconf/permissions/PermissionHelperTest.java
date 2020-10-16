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
 * Created: 10/26/2017
*/
package com.comcast.xconf.permissions;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.firmware.ApplicationType;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PermissionHelperTest {

    @Test
    public void validateException_EmptyApplicationType() throws Exception {
        FirmwarePermissionService permissionService = mock(FirmwarePermissionService.class, CALLS_REAL_METHODS);
        String message = "Application type is empty";

        assertExceptionIsThrownWithMessage(message, permissionService, "");
    }

    @Test
    public void validateException_TypeMismatch() throws Exception {
        FirmwarePermissionService permissionService = mock(FirmwarePermissionService.class);
        String activeApplication = ApplicationType.XHOME;
        when(permissionService.getApplicationFromCookies()).thenReturn(activeApplication);
        String entityApplicationType = ApplicationType.STB;
        String message = "Current application type " + activeApplication + " doesn't match with entity application type: " + entityApplicationType;

        assertExceptionIsThrownWithMessage(message, permissionService, entityApplicationType);
    }

    @Test
    public void validateException_NoPermissions() throws Exception {
        FirmwarePermissionService permissionService = mock(FirmwarePermissionService.class);
        String applicationType = ApplicationType.XHOME;
        when(permissionService.getApplicationFromCookies()).thenReturn(applicationType);
        when(permissionService.canWrite()).thenReturn(false);
        String message = "No write permissions for ApplicationType " + applicationType;

        assertExceptionIsThrownWithMessage(message, permissionService, applicationType);
    }

    @Test
    public void validateNoException() throws Exception {
        FirmwarePermissionService permissionService = mock(FirmwarePermissionService.class);
        String applicationType = ApplicationType.XHOME;
        when(permissionService.getApplicationFromCookies()).thenReturn(applicationType);
        when(permissionService.canWrite()).thenReturn(true);

        PermissionHelper.validateWrite(permissionService, applicationType);
    }

    private void assertExceptionIsThrownWithMessage(String message, FirmwarePermissionService permissionService, String applicationType) {
        try {
            PermissionHelper.validateWrite(permissionService, applicationType);
            Assert.fail();
        } catch (ValidationRuntimeException e) {
            Assert.assertEquals(message, e.getMessage());
        }
    }
}
