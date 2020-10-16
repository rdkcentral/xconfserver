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
 * Created: 3/21/2016
*/
package com.comcast.xconf.admin.validator.setting;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.validators.BaseRuleValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingRuleValidator extends BaseRuleValidator<SettingRule> {

    @Autowired
    private DcmPermissionService permissionService;

    @Override
    public void validate(SettingRule entity) {
        validateApplicationType(entity);
        String msg = validateProperties(entity);
        if (msg != null) {
            throw new ValidationRuntimeException(msg);
        }
        super.validate(entity);
    }

    private String validateProperties(SettingRule entity) {
        if (StringUtils.isBlank(entity.getName())) {
            return "Name is empty";
        }
        if (StringUtils.isBlank(entity.getBoundSettingId())) {
            return "Setting profile is not present";
        }
        return null;
    }

    private void validateApplicationType(SettingRule settingRule) {
        PermissionHelper.validateWrite(permissionService, settingRule.getApplicationType());
    }
}

