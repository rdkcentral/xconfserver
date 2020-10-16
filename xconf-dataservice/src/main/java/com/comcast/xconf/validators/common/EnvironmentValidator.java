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

package com.comcast.xconf.validators.common;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.Environment;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentValidator implements IValidator<Environment> {

    @Override
    public void validate(Environment environment) {
        if (environment == null) {
            throw new ValidationRuntimeException("Environment should be specified");
        }
        if (StringUtils.isBlank(environment.getId()) || !environment.getId().matches("^[-a-zA-Z0-9_.' ]+$")) {
            throw new ValidationRuntimeException("Id is invalid");
        }
    }

    @Override
    public void validateAll(Environment environmentToSave, Iterable<Environment> existingEntities) {}
}