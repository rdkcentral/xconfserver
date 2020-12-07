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
 */
package com.comcast.xconf.admin.validator.telemetry;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class TelemetryTwoProfileValidator implements IValidator<TelemetryTwoProfile> {

    @Autowired
    private TelemetryPermissionService telemetryTwoProfileService;

    @Override
    public void validate(TelemetryTwoProfile entity) {
        validateApplicationType(entity);

        String errorMessage = validateProperties(entity);
        if (StringUtils.isNotBlank(errorMessage)) {
            throw new ValidationRuntimeException(errorMessage);
        }

        validateJSONFormat(entity);
    }

    private String validateProperties(TelemetryTwoProfile entity) {
        if (StringUtils.isBlank(entity.getName())) {
            return "Name is not present";
        }
        return null;
    }

    @Override
    public void validateAll(TelemetryTwoProfile entity, Iterable<TelemetryTwoProfile> existingEntities) {
        for (TelemetryTwoProfile profile : existingEntities) {
            if (!profile.getId().equals(entity.getId()) &&
                    StringUtils.equals(profile.getName(), entity.getName())) {
                throw new EntityConflictException("TelemetryTwo Profile with such name exists: " + entity.getName());
            }
        }
    }

    private void validateApplicationType(TelemetryTwoProfile telemetryProfile) {
        PermissionHelper.validateWrite(telemetryTwoProfileService, telemetryProfile.getApplicationType());
    }

    private void validateJSONFormat(TelemetryTwoProfile entity) {
        String errorMessage = "Please provide the valid Telemetry 2.0 Profile JSON config data.";
        try {
            String resourceName = "/telemetrytwoprofile-schema.json";
            InputStream is = TelemetryTwoProfileValidator.class.getResourceAsStream(resourceName);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            JSONTokener tokener = new JSONTokener(in);
            JSONObject rawSchema = new JSONObject(tokener);
            SchemaLoader loader = SchemaLoader.builder()
                    .schemaJson(rawSchema)
                    .draftV6Support()
                    .build();
            Schema schema = loader.load().build();
            schema.validate(new JSONObject(entity.getJsonconfig()));
        } catch (Exception e) {
            throw new ValidationRuntimeException(errorMessage.concat(e.getLocalizedMessage()));
        }
    }
}