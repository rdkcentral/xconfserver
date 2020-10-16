/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.validators.firmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ActivationVersionValidator implements IValidator<ActivationVersion> {

    @Autowired
    private CachedSimpleDao<String, Model> modelDAO;

    @Autowired
    private FirmwarePermissionService firmwarePermissionService;

    @Override
    public void validate(ActivationVersion entity) {
        if (entity == null) {
            throw new ValidationRuntimeException("Activation minimum version should be specified");
        }
        if (StringUtils.isBlank(entity.getId())) {
            throw new ValidationRuntimeException("Id is required");
        }
        if (StringUtils.isBlank(entity.getDescription())) {
            throw new ValidationRuntimeException("Description is required");
        }
        if (StringUtils.isBlank(entity.getModel())) {
            throw new ValidationRuntimeException("Model is required");
        }
        Model model = modelDAO.getOne(entity.getModel());
        if (model == null) {
            throw new EntityNotFoundException("Model with id " + entity.getModel() + " does not exist");
        }
        if (CollectionUtils.isEmpty(entity.getFirmwareVersions()) && CollectionUtils.isEmpty(entity.getRegularExpressions())) {
            throw new ValidationRuntimeException("FirmwareVersion or regular expression should be specified");
        }
        validateApplicationType(entity);
    }

    @Override
    public void validateAll(ActivationVersion entity, Iterable<ActivationVersion> existingEntities) {
        for (ActivationVersion existingEntity : existingEntities) {
            if (!StringUtils.equals(entity.getId(), existingEntity.getId())
                    && StringUtils.equalsIgnoreCase(entity.getDescription(), existingEntity.getDescription())) {
                throw new EntityConflictException("Activation firmware versions with description " + entity.getDescription() + " already exists");
            }
            if (!StringUtils.equals(entity.getId(), existingEntity.getId())
                    && StringUtils.equalsIgnoreCase(entity.getModel(), existingEntity.getModel())
                    && StringUtils.equalsIgnoreCase(entity.getPartnerId(), existingEntity.getPartnerId())) {
                throw new EntityConflictException("ActivationVersion with the following model/partnerId already exists");
            }
        }
    }

    private boolean firmwareVersionsOverlapEachOther(Set<String> versionsToSave, Set<String> existingVersions) {
        if (CollectionUtils.isEmpty(versionsToSave) || CollectionUtils.isEmpty(existingVersions)) {
            return false;
        }
        if (versionsToSave.size() > existingVersions.size()) {
            return CollectionUtils.isSubCollection(existingVersions, versionsToSave);
        }
        return CollectionUtils.isSubCollection(versionsToSave, existingVersions);
    }

    protected void validateApplicationType(ActivationVersion activationVersion) {
        PermissionHelper.validateWrite(firmwarePermissionService, activationVersion.getApplicationType());
    }
}
