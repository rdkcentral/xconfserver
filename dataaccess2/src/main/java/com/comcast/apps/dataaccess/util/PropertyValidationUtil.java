/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
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
 * @author Maxym Dolina (mdolina@productengine.com)
 */
package com.comcast.apps.dataaccess.util;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;

import java.util.Objects;

public class PropertyValidationUtil {

    public static final String PROPERTY_VALIDATION_ERROR_MSG_TEMPLATE = "%s property should be specified";

    public static void validateProperty(Object property, String name) {
        if (Objects.isNull(property)) {
            throw new ValidationRuntimeException(String.format(PROPERTY_VALIDATION_ERROR_MSG_TEMPLATE, name));
        }
    }
}
