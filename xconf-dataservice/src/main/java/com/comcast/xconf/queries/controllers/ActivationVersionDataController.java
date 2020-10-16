/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
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
 * Author: mdolina
 */
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.service.firmware.ActivationVersionDataService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.comcast.xconf.queries.controllers.ActivationVersionDataController.URL_MAPPING;

@RestController
@RequestMapping(URL_MAPPING)
public class ActivationVersionDataController extends BaseQueriesController {

    public static final String URL_MAPPING = "/amv";

    @Autowired
    private ActivationVersionDataService activationVersionService;

    @RequestMapping(method = RequestMethod.GET)
    public List<ActivationVersion> getAll() {
        return activationVersionService.getAll();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/filtered")
    public List<ActivationVersion> getFiltered(@RequestParam Map<String, String> context) {
        return activationVersionService.findByContext(context);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/importAll")
    public Map<String, List<String>> importAll(@RequestBody List<ActivationVersion> activationVersions) {
        Map<String, List<String>> importResult = activationVersionService.importOrUpdateAll(activationVersions);
        return importResult;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ActivationVersion getOne(@PathVariable String id) {
        validateId(id);
        return activationVersionService.getOne(id);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity<Void> deleteOne(@PathVariable String id) {
        validateId(id);
        activationVersionService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ActivationVersion> create(@RequestBody ActivationVersion activationVersion) {
        activationVersionService.create(activationVersion);
        return new ResponseEntity<>(activationVersion, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ActivationVersion update(@RequestBody ActivationVersion activationVersion) {
        activationVersionService.update(activationVersion);
        return activationVersion;
    }

    private void validateId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ValidationRuntimeException("Id is blank");
        }
    }

}
