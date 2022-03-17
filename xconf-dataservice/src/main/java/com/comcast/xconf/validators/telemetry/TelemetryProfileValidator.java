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
package com.comcast.xconf.validators.telemetry;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.dcm.core.Utils;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TelemetryProfileValidator implements IValidator<PermanentTelemetryProfile> {

    @Autowired
    private TelemetryPermissionService permissionService;

    @Override
    public void validate(PermanentTelemetryProfile entity) {
        validateApplicationType(entity);

        String errorMessage = validateProperties(entity);
        if (StringUtils.isNotBlank(errorMessage)) {
            throw new ValidationRuntimeException(errorMessage);
        }
    }

    private String validateProperties(PermanentTelemetryProfile entity) {
        if (StringUtils.isBlank(entity.getName())) {
            return "Name is not present";
        }

        if (!Utils.isValidUrl(entity.getUploadProtocol(), entity.getUploadRepository())) {
            return "URL is not valid";
        }

        List<TelemetryProfile.TelemetryElement> elements = entity.getTelemetryProfile();
        if (elements == null || elements.isEmpty()) {
            return "Should contain at least one profile entry";
        } else {
            return validateElements(elements);
        }
    }

    private String validateElements(List<TelemetryProfile.TelemetryElement> elements) {
        for (int i = 0; i < elements.size(); i++) {
            TelemetryProfile.TelemetryElement element = elements.get(i);

            if (!StringUtils.isNumeric(element.getPollingFrequency())) {
                return "Polling frequency is not a number";
            }

            for (int j = i + 1; j < elements.size(); j++) {
                if (element.equals(elements.get(j))) {
                    return  "Profile entity has duplicate entries";
                }
            }
        }
        return null;
    }

    @Override
    public void validateAll(PermanentTelemetryProfile entity, Iterable<PermanentTelemetryProfile> existingEntities) {
        for (PermanentTelemetryProfile profile : existingEntities) {
            if (!profile.getId().equals(entity.getId()) &&
                    StringUtils.equals(profile.getName(), entity.getName())) {
                throw new EntityConflictException("PermanentProfile with such name exists: " + entity.getName());
            }
        }
    }

    protected void validateApplicationType(PermanentTelemetryProfile telemetryProfile) {
        PermissionHelper.validateWrite(permissionService, telemetryProfile.getApplicationType());
    }
}
