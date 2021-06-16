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
 * Created: 2/17/16  11:51 AM
 */
package com.comcast.xconf.admin.validator.firmware;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.ModelQueriesService;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FirmwareConfigValidator implements IValidator<FirmwareConfig> {

    public static final String MAX_ALLOWED_NUMBER_OF_PROPERTIES_ERR_MSG_TEMPLATE = "Max allowed number of properties is %s";
    public static final int MAX_ALLOWED_NUMBER_OF_PROPERTIES = 20;

    @Autowired
    private FirmwarePermissionService permissionService;

    @Autowired
    private ModelQueriesService modelService;

    @Override
    public void validate(FirmwareConfig firmwareConfig) {
        if(firmwareConfig == null) {
            throw new ValidationRuntimeException("Firmware config is not present");
        }
        if (StringUtils.isBlank(firmwareConfig.getDescription())) {
            throw new ValidationRuntimeException("Description is empty");
        }
        if (StringUtils.isBlank(firmwareConfig.getFirmwareFilename())) {
            throw new ValidationRuntimeException("File name is empty");
        }
        if (StringUtils.isBlank(firmwareConfig.getFirmwareVersion())) {
            throw new ValidationRuntimeException("Version is empty");
        }
        if (CollectionUtils.isEmpty(firmwareConfig.getSupportedModelIds())) {
            throw new ValidationRuntimeException("Supported model list is empty");
        }
        if (StringUtils.isBlank(firmwareConfig.getApplicationType())) {
            throw new ValidationRuntimeException("Application type is empty");
        }

        validatePropertyKeys(firmwareConfig.getProperties());
        validatePropertiesSize(firmwareConfig.getProperties());

        PermissionHelper.validateWrite(permissionService, firmwareConfig.getApplicationType());

        for (String modelId : firmwareConfig.getSupportedModelIds()) {
            if (!modelService.isExistModel(modelId)) {
                throw new ValidationRuntimeException("Model: " + modelId + " does not exist");
            }
        }
    }

    @Override
    public void validateAll(FirmwareConfig firmwareConfig, Iterable<FirmwareConfig> existingEntities) {
        for (final FirmwareConfig entity : existingEntities) {
            if (ApplicationType.equals(firmwareConfig.getApplicationType(), entity.getApplicationType()) && !entity.getId().equals(firmwareConfig.getId()) && entity.getDescription().equalsIgnoreCase(firmwareConfig.getDescription())) {
                throw new EntityConflictException("This description " + firmwareConfig.getDescription() + " is already used");
            }
        }
    }

    private void validatePropertyKeys(Map<String, String> parameters) {
        if (MapUtils.isNotEmpty(parameters)) {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                if (StringUtils.isBlank(parameter.getKey())) {
                    throw new ValidationRuntimeException("Key is empty");
                }
            }
        }
    }

    private void validatePropertiesSize(Map<String, String> properties) {
        if (MapUtils.isNotEmpty(properties) && properties.size() > MAX_ALLOWED_NUMBER_OF_PROPERTIES) {
            throw new ValidationRuntimeException(String.format(MAX_ALLOWED_NUMBER_OF_PROPERTIES_ERR_MSG_TEMPLATE, MAX_ALLOWED_NUMBER_OF_PROPERTIES));
        }
    }
}
