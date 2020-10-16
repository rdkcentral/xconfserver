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
 *  Created: 2:33 PM
 */
package com.comcast.xconf.featurecontrol;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

import static com.comcast.xconf.util.RequestUtil.XCONF_HTTP_HEADER;
import static com.comcast.xconf.firmware.ApplicationType.STB;

@RestController
@RequestMapping(FeatureControlSettingsController.URL_MAPPING)
public class FeatureControlSettingsController {

    public static final String URL_MAPPING = "featureControl";
    public static final String ERROR_MSG_KEY = "errorMsg";

    @Resource
    private FeatureControlSettingsService svc;

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity handleRuntimeException(final RuntimeException e) {
        Map<String, String> result = Collections.singletonMap(ERROR_MSG_KEY, e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/getSettings", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getDefaultFeatureSettings(HttpServletRequest request,
                                                    @RequestParam Map<String, String> context,
                                                    @RequestHeader(required = false) String configSetHash,
                                                    @RequestHeader(value = XCONF_HTTP_HEADER, required = false) String xconfHttp) {
        return getFeatureSettings(request, context, STB, configSetHash, xconfHttp);
    }

    @RequestMapping(value = "/getSettings/{applicationType}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getFeatureSettingsByApplication(HttpServletRequest request,
                                                          @RequestParam Map<String, String> context,
                                                          @PathVariable String applicationType,
                                                          @RequestHeader(required = false) String configSetHash,
                                                          @RequestHeader(value = XCONF_HTTP_HEADER, required = false) String xconfHttp) {
        return getFeatureSettings(request, context, applicationType, configSetHash, xconfHttp);
    }

    private ResponseEntity getFeatureSettings(HttpServletRequest request, Map<String, String> context, String applicationType, String configSetHash, String xconfHttpHeader) {
        ResponseEntity response = svc.getFeatureSettings(request, context,applicationType, configSetHash, xconfHttpHeader);
        return response;
    }

}
