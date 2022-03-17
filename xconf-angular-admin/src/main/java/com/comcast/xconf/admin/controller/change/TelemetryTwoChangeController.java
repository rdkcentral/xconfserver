/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.admin.controller.change;


import com.comcast.xconf.admin.service.telemetrytwochange.TelemetryTwoProfileChangeService;
import com.comcast.xconf.change.ApprovedTelemetryTwoChange;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.service.telemetrytwochange.ApprovedTelemetryTwoChangeCrudService;
import com.comcast.xconf.service.telemetrytwochange.TelemetryTwoChangeCrudService;
import com.comcast.xconf.shared.utils.PageUtils;
import com.comcast.xconf.shared.utils.TelemetryTwoChangeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.comcast.xconf.admin.controller.change.TelemetryTwoChangeController.URL;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(URL)
public class TelemetryTwoChangeController {

    public static final String URL = "/api/telemetrytwo/change";

    @Autowired
    private TelemetryTwoChangeCrudService changeCrudService;

    @Autowired
    private ApprovedTelemetryTwoChangeCrudService approveChangeCrudService;

    @Autowired
    private TelemetryTwoProfileChangeService telemetryProfileChangeService;

    @RequestMapping(method = RequestMethod.GET, value = "/approved")
    public ResponseEntity approved() {
        return new ResponseEntity<>(approveChangeCrudService.getAll(), OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/approve/{changeId}")
    public ResponseEntity approve(@PathVariable String changeId) {
        telemetryProfileChangeService.approve(changeId);
        return new ResponseEntity<>(createHeadersWithEntitySize(), OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/revert/{approveId}")
    public ResponseEntity revert(@PathVariable String approveId) {
        telemetryProfileChangeService.revert(approveId);
        return new ResponseEntity<>(createHeadersWithEntitySize(), OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/cancel/{changeId}")
    public ResponseEntity cancel(@PathVariable String changeId) {
        telemetryProfileChangeService.cancel(changeId);
        return new ResponseEntity<>(createHeadersWithEntitySize(), OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/changes/grouped/byId")
    public ResponseEntity getGroupedChanges(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) {
        List<TelemetryTwoChange> changesPerPage = PageUtils.getPage(changeCrudService.getAll(), pageNumber, pageSize, TelemetryTwoChangeUtils.ascByDateComparator());
        return new ResponseEntity<>(changeCrudService.groupChanges(changesPerPage), createHeadersWithEntitySize(), OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/approved/grouped/byId")
    public ResponseEntity getGroupedApprovedChanges(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) {
        List<ApprovedTelemetryTwoChange> changesPerPage = PageUtils.getPage(approveChangeCrudService.getAll(), pageNumber, pageSize, TelemetryTwoChangeUtils.ascByDateComparator());
        return new ResponseEntity<>(Collections.singletonMap("changesPerPage", changesPerPage), createHeadersWithEntitySize(), OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/entityIds")
    public ResponseEntity getChangedEntityIds() {
        return new ResponseEntity<>(changeCrudService.getChangedEntityIds(), OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/approveChanges")
    public ResponseEntity approveChanges(@RequestBody List<String> changeIds) {
        return new ResponseEntity<>(telemetryProfileChangeService.approveChanges(changeIds), createHeadersWithEntitySize(), OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/revertChanges")
    public ResponseEntity revertChanges(@RequestBody List<String> changeIds) {
        return new ResponseEntity<>(telemetryProfileChangeService.revertChanges(changeIds), OK);
    }

    @RequestMapping(value = "/approved/filtered", method = RequestMethod.POST)
    public ResponseEntity getApprovedFiltered(@RequestBody(required = false) Map<String, String> searchContext,
                                              @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
                                              @RequestParam(required = false, defaultValue = "50") Integer pageSize) {
        List<TelemetryTwoChange> changes = changeCrudService.findByContext(searchContext);
        List<ApprovedTelemetryTwoChange> approvedChanges = approveChangeCrudService.findByContext(searchContext);
        List<ApprovedTelemetryTwoChange> approvedChangesPerPage = PageUtils.getPage(approvedChanges, pageNumber, pageSize, TelemetryTwoChangeUtils.ascByDateComparator());
        return new ResponseEntity<>(Collections.singletonMap("changesPerPage", approvedChangesPerPage), createHeadersWithEntitySize(changes.size(), approvedChanges.size()), OK);
    }

    @RequestMapping(value = "/changes/filtered", method = RequestMethod.POST)
    public ResponseEntity getChangesFiltered(@RequestBody(required = false) Map<String, String> searchContext,
                                             @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
                                             @RequestParam(required = false, defaultValue = "50") Integer pageSize) {
        List<TelemetryTwoChange> changes = changeCrudService.findByContext(searchContext);
        List<TelemetryTwoChange> changesPerPage = PageUtils.getPage(changes, pageNumber, pageSize, TelemetryTwoChangeUtils.ascByDateComparator());
        List<ApprovedTelemetryTwoChange> approvedChanges = approveChangeCrudService.findByContext(searchContext);
        return new ResponseEntity<>(changeCrudService.groupChanges(changesPerPage), createHeadersWithEntitySize(changes.size(), approvedChanges.size()), OK);
    }

    private HttpHeaders createHeadersWithEntitySize() {
        return createHeadersWithEntitySize(
                changeCrudService.getAll().size(),
                approveChangeCrudService.getAll().size()
        );
    }

    private HttpHeaders createHeadersWithEntitySize(int pendingChangesSize, int approvedChangesSize) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("pendingChangesSize", String.valueOf(pendingChangesSize));
        headers.add("approvedChangesSize", String.valueOf(approvedChangesSize));
        return headers;
    }
}