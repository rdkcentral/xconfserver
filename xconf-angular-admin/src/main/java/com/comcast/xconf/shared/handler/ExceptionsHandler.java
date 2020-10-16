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
 * Author: rdolomansky
 * Created: 3/12/15  1:29 PM
 */
package com.comcast.xconf.shared.handler;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.exception.EntityExistsException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.shared.domain.RestErrorMessage;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionsHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleInvalidRequest(final Exception e, final ServletWebRequest request) {
        final HttpStatus status = resolveHttpStatus(e);
        final Object error;

        error = new RestErrorMessage(status.value(), e.getClass().getSimpleName(), e.getMessage());

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        logger.error(ExceptionUtils.getStackTrace(e));
        return handleExceptionInternal(e, error, headers, status, request);
    }

    private HttpStatus resolveHttpStatus(final Exception e) {
        if (e instanceof EntityExistsException || e instanceof EntityConflictException) {
            return HttpStatus.CONFLICT;
        } else if (e instanceof EntityNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (e instanceof IllegalArgumentException ||
                e instanceof ValidationRuntimeException ||
                e instanceof RuleValidationException) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {

        final RestErrorMessage obj = new RestErrorMessage(status.value(),
                HttpMessageNotReadableException.class.getSimpleName(),ex.getMessage());
        return new ResponseEntity<Object>(obj, headers, status);
    }

}
