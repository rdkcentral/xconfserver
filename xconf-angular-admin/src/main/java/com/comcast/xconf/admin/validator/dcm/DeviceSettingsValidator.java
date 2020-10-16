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
 * Author: Stanislav Menshykov
 * Created: 27.10.15  17:13
 */
package com.comcast.xconf.admin.validator.dcm;

import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.logupload.Schedule;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.shared.utils.CronValidationUtils;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DeviceSettingsValidator implements IValidator<DeviceSettings> {

    @Autowired
    private DcmPermissionService permissionService;

    @Override
    public void validate(DeviceSettings entity) {
        validateProperties(entity);
        validateApplicationType(entity);
    }

    public void validateProperties(DeviceSettings deviceSettings) {
        if(StringUtils.isBlank(deviceSettings.getId())) {
            throw new ValidationRuntimeException("Device settings must contain id");
        }

        if (StringUtils.isBlank(deviceSettings.getName())) {
            throw new ValidationRuntimeException("Device settings must contain name");
        }

        final Schedule schedule = deviceSettings.getSchedule();
        if (schedule == null) {
            throw new ValidationRuntimeException("Device settings must contain schedule");
        }

        CronValidationUtils.validateCronDayAndMonth(schedule.getExpression());

        if (schedule.getTimeWindowMinutes() == null) {
            throw new ValidationRuntimeException("Schedule must contain time window minutes");
        }

        if (StringUtils.isBlank(schedule.getType())) {
            throw new ValidationRuntimeException("Schedule must contain type");
        }
    }

    @Override
    public void validateAll(DeviceSettings entity, Iterable<DeviceSettings> existingEntities) {
        for (DeviceSettings device : existingEntities) {
            if (!device.getId().equals(entity.getId()) &&
                    StringUtils.equals(device.getName(), entity.getName())) {
                throw new EntityConflictException("DeviceSettings with such name exists: " + entity.getName());
            }
        }
    }

    private void validateApplicationType(DeviceSettings deviceSettings) {
        PermissionHelper.validateWrite(permissionService, deviceSettings.getApplicationType());
    }
}
