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
 */
package com.comcast.apps.dataaccess.support.exception;


import org.springframework.http.HttpStatus;

public class WebAppException extends RuntimeException {
    final HttpStatus httpStatus;

    public WebAppException() {
        this((Throwable) null, null);
    }

    public WebAppException(final String message) {
        this(message, null, null);
    }

    public WebAppException(final Throwable cause) {
        this(null, cause, null);
    }

    public WebAppException(final HttpStatus httpStatus) {
        this(null, null, httpStatus);
    }

    public WebAppException(final String message, final HttpStatus httpStatus) {
        this(message, null, httpStatus);
    }

    public WebAppException(final Throwable cause, final HttpStatus httpStatus) {
        this(buildExceptionMessage(httpStatus), cause, httpStatus);
    }

    public WebAppException(final String message, final Throwable cause, final HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    private static String buildExceptionMessage(HttpStatus httpStatus) {
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return "HTTP " + httpStatus.value() + ' ' + httpStatus.getReasonPhrase();
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
