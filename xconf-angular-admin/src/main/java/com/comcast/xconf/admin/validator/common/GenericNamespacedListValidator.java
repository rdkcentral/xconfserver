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
 *  Created: 8:03 PM
 */
package com.comcast.xconf.admin.validator.common;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.util.GenericNamespacedListUtils;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;


@Component
public class GenericNamespacedListValidator implements IValidator<GenericNamespacedList> {

    @Override
    public void validate(GenericNamespacedList entity) {
        if (!entity.getId().matches("^[-a-zA-Z0-9_.' ]+$")) {
            throw new ValidationRuntimeException("Name is invalid");
        }
        if (!GenericNamespacedListTypes.isValidType(entity.getTypeName())) {
            throw new ValidationRuntimeException("Type is invalid");
        }
        if (CollectionUtils.isEmpty(entity.getData())) {
            throw new ValidationRuntimeException("Data is not present");
        }

        GenericNamespacedListUtils.validateListData(entity);
    }

    @Override
    public void validateAll(GenericNamespacedList entity, Iterable<GenericNamespacedList> existingEntities) {
        GenericNamespacedListUtils.validateDataIntersection(entity, entity.getData(), existingEntities);
    }
}
