/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */
package com.comcast.apps.dataaccess.support.controlleradvise;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.apps.dataaccess.support.exception.WebAppException;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Component
@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(WebAppException.class)
    public ResponseEntity<String> handleWebAppException(final WebAppException e) {
        final String errorMessage = e.getMessage();
        if (StringUtils.isNotBlank(errorMessage)) {
            return new ResponseEntity<>(e.getMessage(), e.getHttpStatus());
        } else {
            return new ResponseEntity<>(e.getHttpStatus());
        }
    }

    @ExceptionHandler(ValidationRuntimeException.class)
    public ResponseEntity<String> handleValidationException(final ValidationRuntimeException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
            HttpHeaders headers, HttpStatus status, WebRequest request) {

        logger.error(e.getMessage());
        return new ResponseEntity<Object>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
