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
 * Created: 3/17/2016
*/
package com.comcast.xconf.admin.validator.setting;

import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingProfileValidator implements IValidator<SettingProfile> {

    @Autowired
    private DcmPermissionService permissionService;

    @Override
    public void validate(SettingProfile entity) {
        validateApplicationType(entity);
        String msg = validateProperties(entity);
        if (msg != null) {
            throw new ValidationRuntimeException(msg);
        }
    }

    private String validateProperties(SettingProfile entity) {
        if (entity.getSettingType() == null) {
            return "Setting type is empty";
        }
        if (entity.getProperties() == null || entity.getProperties().isEmpty()) {
            return "Property map is empty";
        }
        for (String key : entity.getProperties().keySet()) {
            if (StringUtils.isBlank(key)) {
                return "Key is blank";
            }
            if (StringUtils.isBlank(entity.getProperties().get(key))) {
                return "Value is blank for key: " + key;
            }
        }
        return null;
    }

    @Override
    public void validateAll(SettingProfile entity, Iterable<SettingProfile> existingEntities) {
        for (SettingProfile profile : existingEntities) {
            if (!profile.getId().equals(entity.getId()) &&
                    StringUtils.equals(profile.getSettingProfileId(), entity.getSettingProfileId())) {
                throw new EntityConflictException("SettingProfile with such settingProfileId exists: " + entity.getSettingProfileId());
            }
        }
    }

    private void validateApplicationType(SettingProfile settingProfile) {
        PermissionHelper.validateWrite(permissionService, settingProfile.getApplicationType());
    }
}
