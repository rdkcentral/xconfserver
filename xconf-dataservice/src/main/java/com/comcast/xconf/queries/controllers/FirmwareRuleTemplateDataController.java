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

import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.service.firmware.FirmwareRuleDataService;
import com.comcast.xconf.service.firmware.FirmwareRuleTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.comcast.xconf.queries.controllers.FirmwareRuleTemplateDataController.API_URL;

@RestController
@RequestMapping(API_URL)
public class FirmwareRuleTemplateDataController extends BaseQueriesController {

    public static final String API_URL = "/firmwareruletemplate";

    @Autowired
    private FirmwareRuleTemplateService firmwareRuleTemplateService;

    @Autowired
    private FirmwareRuleDataService firmwareRuleDataService;

    @RequestMapping(method = RequestMethod.POST, value = "/importAll")
    public ResponseEntity importAll(@RequestBody List<FirmwareRuleTemplate> firmwareRuleTemplates) {
        Map<String, List<String>> importResult = firmwareRuleTemplateService.importOrUpdateAll(firmwareRuleTemplates);
        return new ResponseEntity<>(importResult, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/filtered")
    public ResponseEntity getFiltered(@RequestParam Map<String, String> context) {
        context = firmwareRuleDataService.toNormalized(context);
        List<FirmwareRuleTemplate> firmwareRules = firmwareRuleTemplateService.findByContext(context);
        return new ResponseEntity<>(firmwareRules, HttpStatus.OK);
    }
}