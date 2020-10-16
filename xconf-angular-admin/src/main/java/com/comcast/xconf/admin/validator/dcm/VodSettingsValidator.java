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
 * Created: 25.11.15  15:00
 */
package com.comcast.xconf.admin.validator.dcm;

import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.logupload.VodSettings;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VodSettingsValidator implements IValidator<VodSettings> {

    @Autowired
    private DcmPermissionService permissionService;

    private static final String IP_ADDRESS = "^(([01]?[1-9][0-9]?|2[0-4][0-9]|25[0-5])\\.){3}([01]?[0-9][0-9]?|2[0-4][0-9]|25[0-5])$";

    private static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);

    public String validateProperties(VodSettings vodSettings) {
        if (StringUtils.isBlank(vodSettings.getId())) {
            return "VodSettings must contain id";
        }

        if (StringUtils.isBlank(vodSettings.getName())) {
            return "VodSettings must contain name";
        }

        if (vodSettings.getLocationsURL() != null && !Utils.isValidUrl(vodSettings.getLocationsURL())) {
            return "Url " + vodSettings.getLocationsURL() + " is not valid";
        }

        if (!isValidIpList(vodSettings)) {
            return "Ip list is not valid";
        }

        return null;
    }

    private boolean isValidIpList(VodSettings vodSettings) {
        if ( vodSettings == null || (vodSettings.getIpNames() == null ^ vodSettings.getIpList() == null) ) {return false;}
        if (vodSettings.getIpNames() == null && vodSettings.getIpList() == null) return true;
        if (vodSettings.getIpNames().size() != vodSettings.getIpList().size() ) return false;
        for (String itemIP : vodSettings.getIpList()) {
            if (!validIP(itemIP)) {return false;}
        }

        return true;
    }

    private boolean validIP(String address) {
        if (address == null)
            return false;
        Matcher matcher = addressPattern.matcher(address);
        return matcher.matches();
    }

    @Override
    public void validate(VodSettings entity) {
        validateApplicationType(entity);
        final String validationResult = validateProperties(entity);
        if (validationResult != null) {
            throw new ValidationRuntimeException(validationResult);
        }
    }

    @Override
    public void validateAll(VodSettings entity, Iterable<VodSettings> existingEntities) {
        for (VodSettings vodSettings : existingEntities) {
            if (!vodSettings.getId().equals(entity.getId())
                    && StringUtils.equals(vodSettings.getName(), entity.getName())) {
                throw new EntityConflictException("VodSettings with such name exists: " + entity.getName());
            }
        }
    }

    private void validateApplicationType(VodSettings vodSettings) {
        PermissionHelper.validateWrite(permissionService, vodSettings.getApplicationType());
    }
}
