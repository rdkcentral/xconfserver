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

import static com.comcast.xconf.queries.controllers.ChangeDataController.CHANGE_CONTROLLER;

@Controller
@RequestMapping(CHANGE_CONTROLLER)
public class ChangeDataController {

    public static final String CHANGE_CONTROLLER = "/change";

    @Autowired
    private TelemetryProfileChangeDataService telemetryProfileChangeDataService;

    @RequestMapping(method = RequestMethod.POST, value = "/approve/byChangeIds")
    public ResponseEntity approveByChangeId(@RequestBody List<String> changeIds) {
        Map<String, String> approvedChanges = telemetryProfileChangeDataService.approveChanges(changeIds);
        return new ResponseEntity<>(approvedChanges, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/approve/byEntity/{entityId}")
    public ResponseEntity approveByEntityId(@PathVariable String entityId) {
        telemetryProfileChangeDataService.approveByEntityId(entityId);
        return new ResponseEntity<>(HttpStatus.OK);
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
