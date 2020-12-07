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
 * Author: Jeyabala Murugan
 * Created: 05/08/2020
 */
package com.comcast.xconf.admin.validator.telemetry;

import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.admin.service.telemetry.TelemetryTwoProfileService;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.validators.BaseRuleValidator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelemetryTwoRuleValidator extends BaseRuleValidator<TelemetryTwoRule> {

    @Autowired
    private TelemetryTwoProfileService telemetryTwoProfileService;

    @Autowired
    private TelemetryPermissionService permissionService;

    @Override
    public void validate(TelemetryTwoRule entity) {
        validateApplicationType(entity);
        String msg = validateProperties(entity);
        if (msg != null) {
            throw new ValidationRuntimeException(msg);
        }
        super.validate(entity);
    }

    private String validateProperties(TelemetryTwoRule entity) {
        if (StringUtils.isBlank(entity.getName())) {
            return "Name is empty";
        }
        if (CollectionUtils.isEmpty(entity.getBoundTelemetryIds())) {
            return "Bound profile is not set";
        }
        for (String boundTelemetryId : entity.getBoundTelemetryIds()) {
            if (StringUtils.isBlank(boundTelemetryId)) {
                continue;
            }
            if (telemetryTwoProfileService.getEntityDAO().getOne(boundTelemetryId, false) == null) {
                throw new EntityNotFoundException("Telemetry 2.0 profile with id: " + boundTelemetryId + " does not exist");
            }
        }
        return null;
    }

    private void validateApplicationType(TelemetryTwoRule telemetryTwoRule) {
        PermissionHelper.validateWrite(permissionService, telemetryTwoRule.getApplicationType());
    }

}