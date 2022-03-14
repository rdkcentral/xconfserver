package com.comcast.xconf.queries.controllers;

import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.service.telemetry.TelemetryProfileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.comcast.xconf.queries.controllers.TelemetryProfileDataController.TELEMETRY_PROFILE_URL;

@RestController(TELEMETRY_PROFILE_URL)
public class TelemetryProfileDataController extends BaseQueriesController {

    public static final String TELEMETRY_PROFILE_URL = "/telemetry/profile";

    @Autowired
    private TelemetryProfileDataService telemetryProfileDataService;

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity get(@PathVariable String id) {
        PermanentTelemetryProfile profile = telemetryProfileDataService.getOne(id);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody PermanentTelemetryProfile profile) {
        PermanentTelemetryProfile createdProfile = telemetryProfileDataService.create(profile);
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity update(@RequestBody PermanentTelemetryProfile profile) {
        telemetryProfileDataService.update(profile);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity delete(@PathVariable String id) {
        telemetryProfileDataService.delete(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/entry/add")
    public ResponseEntity addTelemetryEntry(@RequestBody TelemetryProfile.TelemetryElement entry) {
        telemetryProfileDataService.addEntry(entry);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/entry/remove")
    public ResponseEntity removeTelemetryEntry() {
        return new ResponseEntity(HttpStatus.OK);
    }


}
