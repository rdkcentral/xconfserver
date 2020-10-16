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

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.service.firmware.FirmwareRuleDataService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.comcast.xconf.queries.controllers.FirmwareRuleDataController.API_URL;

@RestController
@RequestMapping(API_URL)
public class FirmwareRuleDataController extends BaseQueriesController {

    public static final String API_URL = "/firmwarerule";

    @Autowired
    private FirmwareRuleDataService firmwareRuleDataService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getAll() {
        return new ResponseEntity<>(firmwareRuleDataService.getAll(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/filtered")
    public ResponseEntity getFiltered(@RequestParam Map<String, String> context) {
        validateContext(context);
        context = firmwareRuleDataService.toNormalized(context);

        List<FirmwareRule> firmwareRules = firmwareRuleDataService.findByContext(context);
        return new ResponseEntity<>(firmwareRules, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/importAll")
    public ResponseEntity importAll(@RequestBody List<FirmwareRule> firmwareRules) {
        Map<String, List<String>> importResult = firmwareRuleDataService.importOrUpdateAll(firmwareRules);
        return new ResponseEntity<>(importResult, HttpStatus.OK);
    }

    private void validateContext(Map<String, String> context) {
        if (MapUtils.isEmpty(context) || StringUtils.isBlank(context.get(FirmwareRuleDataService.APPLICATION_TYPE))) {
            throw new ValidationRuntimeException("ApplicationType is not specified");
        }
    }
}