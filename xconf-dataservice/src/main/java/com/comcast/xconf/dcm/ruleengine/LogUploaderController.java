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
package com.comcast.xconf.dcm.ruleengine;

import com.comcast.xconf.logupload.LogUploaderContext;
import org.codehaus.jackson.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

import static com.comcast.xconf.firmware.ApplicationType.STB;

@Controller
@RequestMapping(value = "/loguploader")
public class LogUploaderController {

    @Autowired
    private LogUploaderService logUploaderService;

    @RequestMapping(value = "/getT2Settings/{applicationType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getT2Settings(HttpServletRequest request,
                                      @RequestParam(value = "checkNow", required = false) Boolean checkNow,
                                      @RequestParam(value = "version", required = false) String apiVersion,
                                      @RequestParam(value = "settingType", required = false) Set<String> settingTypes,
                                      @RequestParam Map<String, String> params,
                                      @PathVariable String applicationType) {

        final LogUploaderContext context = new LogUploaderContext(params);
        context.setApplication(applicationType);
        return logUploaderService.evaluateSettings(request, checkNow, apiVersion, settingTypes, context, true);
    }

    @RequestMapping(value = "/getT2Settings", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getT2Settings(HttpServletRequest request,
                                      @RequestParam(value = "checkNow", required = false) Boolean checkNow,
                                      @RequestParam(value = "version", required = false) String apiVersion,
                                      @RequestParam(value = "settingType", required = false) Set<String> settingTypes,
                                      @RequestParam Map<String, String> params) {

        final LogUploaderContext context = new LogUploaderContext(params);
        context.setApplication(STB);

        return logUploaderService.evaluateSettings(request, checkNow, apiVersion, settingTypes, context, true);
    }

    @RequestMapping(value = "/getSettings/{applicationType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSettingsByApplicationType(HttpServletRequest request,
                                           @PathVariable String applicationType,
                                           @RequestParam(value = "checkNow", required = false) Boolean checkNow,
                                           @RequestParam(value = "version", required = false) String apiVersion,
                                           @RequestParam(value = "settingType", required = false) Set<String> settingTypes,
                                           @RequestParam Map<String, String> params) {

        final LogUploaderContext context = new LogUploaderContext(params);
        context.setApplication(applicationType);

        return logUploaderService.evaluateSettings(request, checkNow, apiVersion, settingTypes, context, false);
    }

    @RequestMapping(value = "/getSettings", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getSettings(HttpServletRequest request,
                                      @RequestParam(value = "checkNow", required = false) Boolean checkNow,
                                      @RequestParam(value = "version", required = false) String apiVersion,
                                      @RequestParam(value = "settingType", required = false) Set<String> settingTypes,
                                      @RequestParam Map<String, String> params) {

        final LogUploaderContext context = new LogUploaderContext(params);
        context.setApplication(STB);
        return logUploaderService.evaluateSettings(request, checkNow, apiVersion, settingTypes, context, false);
    }

    @RequestMapping(value = "/getTelemetryProfiles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getTelemetryTwoProfiles(HttpServletRequest request,
                                                  @RequestParam Map<String, String> params) throws JsonProcessingException, IOException {

        final LogUploaderContext context = new LogUploaderContext(params);
        context.setApplication(STB);
        return logUploaderService.getTelemetryTwoProfiles(request, context);
    }

    @RequestMapping(value = "/getTelemetryProfiles/{applicationType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getTelemetryTwoProfilesByApplicationType(HttpServletRequest request,
                                                                   @PathVariable String applicationType,
                                                                   @RequestParam Map<String, String> params) throws JsonProcessingException, IOException {

        final LogUploaderContext context = new LogUploaderContext(params);
        context.setApplication(applicationType);
        return logUploaderService.getTelemetryTwoProfiles(request, context);
    }
}
