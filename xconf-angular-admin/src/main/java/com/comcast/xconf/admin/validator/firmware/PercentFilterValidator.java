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
 *  Created: 2:43 PM
 */
package com.comcast.xconf.admin.validator.firmware;

import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.admin.service.firmware.FirmwareConfigService;
import com.comcast.xconf.estbfirmware.EnvModelPercentage;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.PercentFilterValue;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PercentFilterValidator {

    @Autowired
    private FirmwareConfigService firwmareConfigService;

    public void validate(final PercentFilterValue filter, final String envModelKey, String rootApplicationType) {
        if (String.valueOf(filter.getPercentage()).contains("-")) {
            throw new ValidationRuntimeException("Percent filter contains negative value");
        }
        if (StringUtils.isNotBlank(envModelKey) && filter.getEnvModelPercentages() != null) {
            EnvModelPercentage percentage = filter.getEnvModelPercentages().get(envModelKey);
            if (percentage != null) {
                Set<String> firmwareVersions = percentage.getFirmwareVersions();
                if (percentage.isFirmwareCheckRequired() && CollectionUtils.isEmpty(firmwareVersions)) {
                    throw new ValidationRuntimeException("Please select at least one version or disable firmware check");
                }
                if (String.valueOf(percentage.getPercentage()).contains("-")) {
                    throw new ValidationRuntimeException("Percent filter contains negative value");
                }
            }


            Set<String> firmwareVersions = percentage.getFirmwareVersions();
            if (percentage.isFirmwareCheckRequired() && (firmwareVersions == null || firmwareVersions.isEmpty())) {
                throw  new ValidationRuntimeException("FirmwareVersion is required");
            }

            FirmwareConfig firmwareConfigByEnvModelRuleName = firwmareConfigService.getFirmwareConfigByEnvModelRuleName(envModelKey);
            if (firmwareConfigByEnvModelRuleName != null && !firmwareVersions.contains(firmwareConfigByEnvModelRuleName.getFirmwareVersion())) {
                throw new ValidationRuntimeException("Firmware Version " + firmwareConfigByEnvModelRuleName.getFirmwareVersion() + " should be selected in min check list");
            }
            String lastKnownGoodConfigId = percentage.getLastKnownGood();
            if (StringUtils.isNotBlank(lastKnownGoodConfigId)) {
                FirmwareConfig lkgConfig = firwmareConfigService.getOne(lastKnownGoodConfigId);
                if (lkgConfig == null) {
                    throw new EntityNotFoundException("FirmwareConfig with id " + lastKnownGoodConfigId + " does not exist");
                }
                if (!percentage.getFirmwareVersions().contains(lkgConfig.getFirmwareVersion())) {
                    throw new ValidationRuntimeException("Last Know Good Version should be selected in min check list");
                }
                if (percentage.getPercentage() == 100.0) {
                    throw new ValidationRuntimeException("Can't set LastKnownGood when percentage=100");
                }
                if (!percentage.isActive()) {
                    throw new ValidationRuntimeException("Can't set LastKnownGood when filter is not active");
                }
            }

            String intermediateVersionConfigId = percentage.getIntermediateVersion();
            if (StringUtils.isNotBlank(intermediateVersionConfigId)) {
                if (firwmareConfigService.getOne(intermediateVersionConfigId) == null) {
                    throw new EntityNotFoundException("FirmwareConfig with id " + intermediateVersionConfigId + " does not exist");
                }
                if (!percentage.isFirmwareCheckRequired()) {
                    throw new ValidationRuntimeException("Can't set IntermediateVersion when firmware check is disabled");
                }
            }
        }
    }
}
