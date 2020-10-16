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
 * Author: Igor Kostrov
 * Created: 12/12/18  1:15 PM
 */
package com.comcast.xconf.admin.controller;

import com.comcast.xconf.auth.AuthResponse;
import com.comcast.xconf.auth.AuthUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(AuthController.URL_MAPPING)
public class AuthController {
    public static final String URL_MAPPING = "api/auth";

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public ResponseEntity<AuthResponse> getAuthInfo(final HttpServletRequest request) {
        final AuthResponse authResponse = AuthUtils.getAuthResponse(request);
        if (authResponse != null) {
            return new ResponseEntity<>(authResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

}