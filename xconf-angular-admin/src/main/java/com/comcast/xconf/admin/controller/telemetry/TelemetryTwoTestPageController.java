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
package com.comcast.xconf.admin.controller.telemetry;

import com.comcast.xconf.dcm.ruleengine.TelemetryProfileService;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@RestController
@RequestMapping(TelemetryTwoTestPageController.URL_MAPPING)
public class TelemetryTwoTestPageController {

    public static final String URL_MAPPING = "api/telemetry/v2/testpage";

    @Autowired
    private TelemetryProfileService telemetryProfileService;

    @Autowired
    private TelemetryPermissionService permissionService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity getMatchedRules(@RequestBody Map<String, String> params) {
        LogUploaderContext context = new LogUploaderContext(params);
        context.setApplication(permissionService.getReadApplication());
        Map<String, String> contextProps = context.getProperties();

        List<TelemetryTwoRule> telemetryTwoRules  = telemetryProfileService.processTelemetryTwoRules(contextProps);

        Map<String, Object> result = new HashMap<>();
        result.put("result", Lists.newArrayList(telemetryTwoRules));
        result.put("context", contextProps);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}