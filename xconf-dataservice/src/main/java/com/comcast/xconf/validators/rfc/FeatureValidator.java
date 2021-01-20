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
 * Author: Yury Stagit
 * Created: 11/02/16  12:00 PM
 */
package com.comcast.xconf.validators.rfc;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.WhitelistProperty;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FeatureValidator implements IValidator<Feature> {

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    @Autowired
    private DcmPermissionService permissionService;

    @Override
    public void validate(Feature entity) {
        validateApplicationType(entity);
        String msg = validateFeature(entity);
        if (msg != null) {
            throw new ValidationRuntimeException(msg);
        }
    }

    private String validateFeature(Feature entity) {
        if (StringUtils.isBlank(entity.getName())) {
            return "Name is blank";
        }
        if (StringUtils.isBlank(entity.getFeatureName())) {
            return "Feature Instance is blank";
        }
        if (entity.getConfigData() != null || !entity.getConfigData().isEmpty()) {
            for (Map.Entry<String, String> configEntry : entity.getConfigData().entrySet()) {
                if (StringUtils.isBlank(configEntry.getKey())) {
                    return "Key is blank";
                }
                if (StringUtils.isBlank(configEntry.getValue())) {
                    return "Value is blank for key: " + configEntry.getKey();
                }
            }
        }

        if (entity.isWhitelisted()) {
            WhitelistProperty whitelistProperty = entity.getWhitelistProperty();
            if (StringUtils.isBlank(whitelistProperty.getKey())) {
                throw new ValidationRuntimeException("Key is required");
            }
            if (StringUtils.isBlank(whitelistProperty.getValue())) {
                throw new ValidationRuntimeException("Value is required");
            }
            GenericNamespacedList namespacedList = genericNamespacedListQueriesService.getOneByType(entity.getWhitelistProperty().getValue(), entity.getWhitelistProperty().getNamespacedListType());
            if (namespacedList == null) {
                throw new EntityNotFoundException(entity.getWhitelistProperty().getNamespacedListType() + " with id " + entity.getWhitelistProperty().getValue() + " does not exist");
            }
            if (StringUtils.isBlank(whitelistProperty.getNamespacedListType())) {
                throw new ValidationRuntimeException("NamespacedList type is required");
            }
            if (StringUtils.isBlank(whitelistProperty.getTypeName())) {
                throw new ValidationRuntimeException("NamespacedList type name is required");
            }
        }

        return null;
    }

    @Override
    public void validateAll(Feature entity, Iterable<Feature> existingEntities) {
        for (Feature feature : existingEntities) {
            if (!feature.getId().equals(entity.getId()) && ApplicationType.equals(feature.getApplicationType(), entity.getApplicationType()) && StringUtils.equals(feature.getFeatureName(), entity.getFeatureName())) {
                throw new EntityConflictException("Feature with such featureInstance already exists: " + entity.getFeatureName());
            }
        }
    }

    protected void validateApplicationType(Feature feature) {
        PermissionHelper.validateWrite(permissionService, feature.getApplicationType());
    }
}
