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
 *  Author: mdolina
 *  Created: 3:04 PM
 */
package com.comcast.xconf.admin.service.firmware;

import com.comcast.xconf.estbfirmware.GlobalPercentage;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.converter.PercentageBeanConverter;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.service.firmware.FirmwareRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GlobalPercentageService {

    @Autowired
    private FirmwareRuleService firmwareRuleService;

    @Autowired
    private FirmwarePermissionService permissionService;

    @Autowired
    private PercentageBeanConverter converter;

    public GlobalPercentage getGlobalPercentage() {
        String applicationType = permissionService.getReadApplication();
        String filterId = getFilterId(applicationType);
        FirmwareRule globalPercentageRule = firmwareRuleService.getEntityDAO().getOne(filterId);
        return globalPercentageRule != null ?
                converter.convertIntoGlobalPercentage(globalPercentageRule) : GlobalPercentage.forApplication(applicationType);
    }

    public GlobalPercentage updateGlobalPercentage(GlobalPercentage globalPercentage) {
        FirmwareRule rule = converter.convertIntoRule(globalPercentage);
        String filterId = getFilterId(permissionService.getWriteApplication());
        rule.setId(filterId);
        if (firmwareRuleService.getEntityDAO().getOne(filterId) != null) {
            firmwareRuleService.update(rule);
        } else {
            firmwareRuleService.create(rule);
        }
        return globalPercentage;
    }

    public FirmwareRule getGlobalPercentageAsFirmwareRule() {
        String applicationType = permissionService.getReadApplication();
        FirmwareRule rule = firmwareRuleService.getEntityDAO().getOne(getFilterId(applicationType));
        return (rule != null) ? rule : converter.convertIntoRule(GlobalPercentage.forApplication(applicationType));
    }

    private String getFilterId(String application) {
        if (ApplicationType.equals(ApplicationType.STB, application)) {
            return TemplateNames.GLOBAL_PERCENT;
        }

        return application.toUpperCase() + "_" + TemplateNames.GLOBAL_PERCENT;
    }
}
