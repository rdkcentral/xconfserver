/* 
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
 *
 *  Author: mdolina
 *  Created: 1:45 PM
 */
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.admin.service.firmware.GlobalPercentageService;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.estbfirmware.GlobalPercentage;
import com.comcast.xconf.estbfirmware.PercentFilterVo;
import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.estbfirmware.PercentageBeanService;
import com.comcast.xconf.shared.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

@RestController
@RequestMapping(PercentFilterController.URL_MAPPING)
public class PercentFilterController {

    public static final String URL_MAPPING = "api/percentfilter";

    @Autowired
    private GlobalPercentageService globalPercentageService;

    @Autowired
    private PercentageBeanService percentageBeanService;

    @Autowired
    private AuthService authService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getPercentFilter() {
        return new ResponseEntity<>(globalPercentageService.getGlobalPercentage(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity savePercentFilter(@RequestBody GlobalPercentage globalPercentage) {

        globalPercentageService.updateGlobalPercentage(globalPercentage);
        LoggingUtils.log(logger, Level.INFO, "Successfully updated {} with info: {}", authService.getUserName(), globalPercentage.getClass().getSimpleName(), JsonUtil.toJson(globalPercentage));
        return new ResponseEntity<>(globalPercentage, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "globalPercentage", params = {"export"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity exportFilter() {
        HttpHeaders headers = Utils.createContentDispositionHeader(ExportFileNames.GLOBAL_PERCENT.getName() +  percentageBeanService.getApplicationTypeSuffix());
        return new ResponseEntity<>(new PercentFilterVo(globalPercentageService.getGlobalPercentage(), new ArrayList<PercentageBean>()), headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "globalPercentage/asRule", params = {"export"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity exportGlobalPercentageAsRule() {
        HttpHeaders headers = Utils.createContentDispositionHeader(ExportFileNames.GLOBAL_PERCENT_AS_RULE.getName() + percentageBeanService.getApplicationTypeSuffix());
        return new ResponseEntity<>(Collections.singletonList(globalPercentageService.getGlobalPercentageAsFirmwareRule()), headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, params = {"export"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity exportWholeFilter() {
        HttpHeaders headers = Utils.createContentDispositionHeader(ExportFileNames.PERCENT_FILTER.getName() + percentageBeanService.getApplicationTypeSuffix());
        return new ResponseEntity<>(new PercentFilterVo(globalPercentageService.getGlobalPercentage(), percentageBeanService.getAll()), headers, HttpStatus.OK);
    }

}
