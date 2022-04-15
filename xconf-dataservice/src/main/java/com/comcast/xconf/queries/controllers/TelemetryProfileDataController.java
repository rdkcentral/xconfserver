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
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.service.telemetry.TelemetryProfileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static com.comcast.xconf.queries.controllers.TelemetryProfileDataController.TELEMETRY_PROFILE_URL;

@Controller
@RequestMapping(TELEMETRY_PROFILE_URL)
public class TelemetryProfileDataController extends BaseQueriesController {

    public static final String TELEMETRY_PROFILE_URL = "/telemetry/profile";

    @Autowired
    private TelemetryProfileDataService telemetryProfileDataService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getAll() {
        List<PermanentTelemetryProfile> profiles = telemetryProfileDataService.getAll();
        return new ResponseEntity(profiles, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity get(@PathVariable String id) {
        PermanentTelemetryProfile profile = telemetryProfileDataService.getOne(id);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody PermanentTelemetryProfile profile) {
        PermanentTelemetryProfile createdProfile = telemetryProfileDataService.create(profile);
        return new ResponseEntity<>(createdProfile, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity update(@RequestBody PermanentTelemetryProfile profile) {
        PermanentTelemetryProfile updatedProfile = telemetryProfileDataService.update(profile);
        return new ResponseEntity<>(updatedProfile, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity delete(@PathVariable String id) {
        telemetryProfileDataService.delete(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/entry/add/{id}")
    public ResponseEntity addTelemetryEntry(@PathVariable String id, @RequestBody List<TelemetryProfile.TelemetryElement> entries) {
        PermanentTelemetryProfile profile = telemetryProfileDataService.addEntry(id, entries);
        return new ResponseEntity(profile, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/entry/remove/{id}")
    public ResponseEntity removeTelemetryEntry(@PathVariable String id, @RequestBody List<TelemetryProfile.TelemetryElement> entries) {
        telemetryProfileDataService.removeEntry(id, entries);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/change")
    public ResponseEntity createWithApproval(@RequestBody PermanentTelemetryProfile profile) {
        Change<PermanentTelemetryProfile> createChange = telemetryProfileDataService.writeCreateChange(profile);
        return new ResponseEntity<>(createChange, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/change")
    public ResponseEntity updateWithApproval(@RequestBody PermanentTelemetryProfile profile) {
        Change<PermanentTelemetryProfile> updateChange = telemetryProfileDataService.writeUpdateChange(profile);
        return new ResponseEntity<>(updateChange, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/change/{id}")
    public ResponseEntity deleteWithApproval(@PathVariable String id) {
        Change<PermanentTelemetryProfile> deleteChange = telemetryProfileDataService.writeDeleteChange(id);
        return new ResponseEntity<>(deleteChange, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/change/entry/add/{id}")
    public ResponseEntity addTelemetryEntriesWithApproval(@PathVariable String id, @RequestBody List<TelemetryProfile.TelemetryElement> entries) {
        Change<PermanentTelemetryProfile> updateChange = telemetryProfileDataService.addEntriesWithApproval(id, entries);
        return new ResponseEntity(updateChange, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/change/entry/remove/{id}")
    public ResponseEntity removeTelemetryEntriesWithApproval(@PathVariable String id, @RequestBody List<TelemetryProfile.TelemetryElement> entries) {
        Change<PermanentTelemetryProfile> updateChange = telemetryProfileDataService.removeEntriesWithApproval(id, entries);
        return new ResponseEntity(updateChange, HttpStatus.OK);
    }
}
