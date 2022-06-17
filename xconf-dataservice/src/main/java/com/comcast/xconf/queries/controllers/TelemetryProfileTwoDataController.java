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

import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.service.telemetry.TelemetryProfileTwoDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static com.comcast.xconf.queries.controllers.TelemetryProfileTwoDataController.TELEMETRY_TWO_PROFILE_API;

@Controller
@RequestMapping(TELEMETRY_TWO_PROFILE_API)
public class TelemetryProfileTwoDataController extends BaseQueriesController {

    public static final String TELEMETRY_TWO_PROFILE_API = "/telemetry/v2/profile";

    @Autowired
    private TelemetryProfileTwoDataService telemetryProfileTwoDataService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getAll() {
        List<TelemetryTwoProfile> profiles = telemetryProfileTwoDataService.getAll();
        return new ResponseEntity<>(profiles, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity getById(@PathVariable String id) {
        TelemetryTwoProfile profile = telemetryProfileTwoDataService.getOne(id);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody TelemetryTwoProfile profile) {
        telemetryProfileTwoDataService.create(profile);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity update(@RequestBody TelemetryTwoProfile profile) {
        telemetryProfileTwoDataService.update(profile);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity delete(@PathVariable String id) {
        telemetryProfileTwoDataService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
