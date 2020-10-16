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
 * Created: 23.11.15  12:07
 */
package com.comcast.xconf.admin.validator.dcm;

import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.logupload.UploadRepository;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UploadRepositoryValidator implements IValidator<UploadRepository> {

    @Autowired
    private DcmPermissionService permissionService;

    public String validateProperties(UploadRepository uploadRepository) {
        if (StringUtils.isBlank(uploadRepository.getName())) {
            return "Upload repository must contain name";
        }

        if (StringUtils.isBlank(uploadRepository.getDescription())) {
            return "Upload repository must contain description";
        }

        if (StringUtils.isBlank(uploadRepository.getUrl())) {
            return "Upload repository must contain url";
        }

        if (!Utils.isValidUrl(uploadRepository.getProtocol(), uploadRepository.getUrl())) {
            return "Url is not valid";
        }

        return null;
    }

    @Override
    public void validate(UploadRepository entity) {
        validateApplicationType(entity);
        final String validationResult = validateProperties(entity);
        if (validationResult != null) {
            throw new ValidationRuntimeException(validationResult);
        }
    }

    @Override
    public void validateAll(UploadRepository entity, Iterable<UploadRepository> existingEntities) {
        for (UploadRepository uploadRepository : existingEntities) {
            if (!uploadRepository.getId().equals(entity.getId())
                    && StringUtils.equals(uploadRepository.getName(), entity.getName())) {
                throw new EntityConflictException("UploadRepository with such name exists: " + entity.getName());
            }
        }
    }

    private void validateApplicationType(UploadRepository uploadRepository) {
        PermissionHelper.validateWrite(permissionService, uploadRepository.getApplicationType());
    }
}
