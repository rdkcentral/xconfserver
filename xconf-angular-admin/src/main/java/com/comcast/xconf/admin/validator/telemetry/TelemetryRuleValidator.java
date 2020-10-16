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
 * Created: 3/24/2016
*/
package com.comcast.xconf.admin.validator.telemetry;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.admin.service.telemetry.PermanentTelemetryProfileService;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.validators.BaseRuleValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TelemetryRuleValidator extends BaseRuleValidator<TelemetryRule> {

    @Autowired
    private PermanentTelemetryProfileService permanentProfileService;

    @Autowired
    private TelemetryPermissionService permissionService;

    @Override
    public void validate(TelemetryRule entity) {
        validateApplicationType(entity);
        String msg = validateProperties(entity);
        if (msg != null) {
            throw new ValidationRuntimeException(msg);
        }
        super.validate(entity);
    }

    private String validateProperties(TelemetryRule entity) {
        if (StringUtils.isBlank(entity.getName())) {
            return "Name is empty";
        }
        if (StringUtils.isBlank(entity.getBoundTelemetryId())) {
            return "Bound profile is not set";
        }
        if (permanentProfileService.getEntityDAO().getOne(entity.getBoundTelemetryId(), false) == null) {
            throw new EntityNotFoundException("Telemetry profile with id: " + entity.getBoundTelemetryId() + " does not exist");
        }
        return null;
    }

    private void validateApplicationType(TelemetryRule telemetryRule) {
        PermissionHelper.validateWrite(permissionService, telemetryRule.getApplicationType());
    }

}