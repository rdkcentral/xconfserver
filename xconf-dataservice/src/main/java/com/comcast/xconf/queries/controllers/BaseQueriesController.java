/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.exception.EntityExistsException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class BaseQueriesController {

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @ExceptionHandler
    protected ResponseEntity handleValidationException(final ValidationRuntimeException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity handleValidationException(final RuleValidationException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity handleEntityNotFoundException(final EntityNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    protected ResponseEntity handleEntityConflictException(final EntityConflictException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    protected ResponseEntity handleEntityExistsException(final EntityExistsException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    protected void validateApplicationType(String applicationType) {
        if (StringUtils.isNotBlank(applicationType) && !ApplicationType.isValid(applicationType)) {
            throw new ValidationRuntimeException("ApplicationType " + applicationType + " is not valid");
        }
    }

    protected void validateRuleName(String id, String name) {
        for (FirmwareRule rule : firmwareRuleDao.getAll()) {
            if (!StringUtils.equals(id, rule.getId()) && StringUtils.equals(name, rule.getName())) {
                throw new ValidationRuntimeException("Name is already used");
            }
        }
    }
}
