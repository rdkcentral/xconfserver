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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.service.telemetry.TelemetryTwoProfileService;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.shared.controller.ApplicationTypeAwayController;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;

@RestController
@RequestMapping(TelemetryTwoProfileController.URL_MAPPING)
public class TelemetryTwoProfileController extends ApplicationTypeAwayController<TelemetryTwoProfile> {

    public static final String URL_MAPPING = "api/telemetry/v2/profile";

    @Autowired
    private TelemetryTwoProfileService telemetryTwoProfileService;

    @Override
    public AbstractApplicationTypeAwareService<TelemetryTwoProfile> getService() {
        return telemetryTwoProfileService;
    }

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.TELEMETRY_TWO_PROFILE.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_TELEMETRY_TWO_PROFILES.getName();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/byIdList")
    public ResponseEntity getTelemetryTwoProfilesByIdList(@RequestBody List<String> telemetryTwoProfileIdList) {
        return new ResponseEntity<>(telemetryTwoProfileService.getTelemetryTwoProfilesByIdList(telemetryTwoProfileIdList), HttpStatus.OK);
    }
}