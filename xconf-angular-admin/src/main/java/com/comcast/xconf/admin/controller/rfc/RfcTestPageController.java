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
 *  Created: 4:12 PM
 */
package com.comcast.xconf.admin.controller.rfc;

import com.comcast.xconf.permissions.DcmPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = RfcTestPageController.URL_MAPPING)
public class RfcTestPageController {

    public static final String URL_MAPPING = "api/rfc";

    @Autowired
    private DcmPermissionService permissionService;

    @Autowired
    private RfcTestPageService rfcTestPageService;

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public ResponseEntity matchRule(@RequestBody Map<String, String> context) {

        String application = permissionService.getReadApplication();
        HashMap<String, Object> result = rfcTestPageService.processFeatureRules(context, application);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
