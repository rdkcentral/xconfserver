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

package com.comcast.xconf.validators.change;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.change.ApprovedTelemetryTwoChange;
import com.comcast.xconf.change.ChangeOperation;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.exception.EntityExistsException;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TelemetryTwoChangeValidator<T extends TelemetryTwoChange> implements IValidator<T> {

    @Override
    public void validate(TelemetryTwoChange change) {
        if (change == null) {
            throw new ValidationRuntimeException("Change is empty");
        }
        validateId(change.getId());
        if (StringUtils.isBlank(change.getAuthor())) {
            throw new ValidationRuntimeException("Author is empty");
        }
        if (StringUtils.isBlank(change.getEntityId())) {
            throw new ValidationRuntimeException("Entity id is empty");
        }
        if (change.getOperation() == null) {
            throw new ValidationRuntimeException("Operation is empty");
        }
        if ((ChangeOperation.CREATE.equals(change.getOperation()) || ChangeOperation.UPDATE.equals(change.getOperation()))
            && change.getNewEntity() == null) {
            throw new ValidationRuntimeException("New entity is empty");
        }
        if ((ChangeOperation.UPDATE.equals(change.getOperation()) || ChangeOperation.DELETE.equals(change.getOperation()))
                && change.getOldEntity() == null) {
            throw new ValidationRuntimeException("Old entity is empty");
        }
        if (change instanceof ApprovedTelemetryTwoChange && StringUtils.isBlank(change.getApprovedUser())) {
            throw new ValidationRuntimeException("Approved user is empty");
        }
    }

    public void validateId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ValidationRuntimeException("Id is blank");
        }
    }

    @Override
    public void validateAll(T entity, Iterable<T> existingEntities) {
        for (T existingEntity : existingEntities) {
            if (existingEntity.equalChangeData(entity)) {
                throw new EntityExistsException("The same change already exists");
            }
        }
    }
}