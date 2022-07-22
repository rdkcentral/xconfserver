/**
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2022 RDK Management
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
 * Author: Maksym Dolina
 */
package com.comcast.xconf.queries.controllers;

import com.comcast.xconf.change.Change;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.service.change.TelemetryProfileChangeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

import static com.comcast.xconf.queries.controllers.TelemetryProfileChangeDataController.CHANGE_URL;

@Controller
@RequestMapping(CHANGE_URL)
public class TelemetryProfileChangeDataController {

    public static final String CHANGE_URL = "/change";

    @Autowired
    private TelemetryProfileChangeDataService telemetryProfileChangeDataService;

    @RequestMapping(method = RequestMethod.POST, value = "/approve/byChangeIds")
    public ResponseEntity approveByChangeId(@RequestBody List<String> changeIds) {
        Map<String, String> approvedChanges = telemetryProfileChangeDataService.approveChanges(changeIds);
        return new ResponseEntity<>(approvedChanges, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/approve/byEntity/{entityId}")
    public ResponseEntity approveByEntityId(@PathVariable String entityId) {
        Map<String, String> errorMessages = telemetryProfileChangeDataService.approveByEntityId(entityId);
        return new ResponseEntity<>(errorMessages, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/cancel/{changeId}")
    public ResponseEntity cancelChange(@PathVariable String changeId) {
        telemetryProfileChangeDataService.cancel(changeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public ResponseEntity getAll() {
        List<Change<PermanentTelemetryProfile>> profileChanges = telemetryProfileChangeDataService.getProfileChanges();
        return new ResponseEntity<>(profileChanges, HttpStatus.OK);
    }
}
