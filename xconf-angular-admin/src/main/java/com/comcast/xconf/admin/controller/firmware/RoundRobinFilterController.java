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
 *  Created: 12:39 PM
 */
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.admin.service.firmware.RoundRobinFilterService;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.estbfirmware.DownloadLocationRoundRobinFilterValue;
import com.comcast.xconf.estbfirmware.SingletonFilterValue;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.shared.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;

@RestController
@RequestMapping(RoundRobinFilterController.URL_MAPPING)
public class RoundRobinFilterController {
    public static final String URL_MAPPING = "api/roundrobinfilter";

    @Autowired
    private RoundRobinFilterService roundRobinFilterService;

    @Autowired
    private AuthService authService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/{applicationType}", method = RequestMethod.GET, params = "export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity export(@PathVariable String applicationType) {
        SingletonFilterValue singletonFilterValue = roundRobinFilterService.getOne(applicationType);
        singletonFilterValue = QueriesHelper.nullifyUnwantedFields(singletonFilterValue);
        HttpHeaders headers = Utils.createContentDispositionHeader(ExportFileNames.ROUND_ROBIN_FILTER.getName() + "_" + ApplicationType.get(((DownloadLocationRoundRobinFilterValue) singletonFilterValue).getApplicationType()));
        return new ResponseEntity<>(singletonFilterValue, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{applicationType}", method = RequestMethod.GET)
    public ResponseEntity getDownloadLocationRoundRobinFilter(@PathVariable String applicationType) {
        SingletonFilterValue singletonFilterValue = roundRobinFilterService.getOne(applicationType);
        singletonFilterValue = QueriesHelper.nullifyUnwantedFields(singletonFilterValue);
        return new ResponseEntity<>(singletonFilterValue, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity updateDownloadLocationRoundRobinFilter(@RequestBody SingletonFilterValue filter) {
        roundRobinFilterService.save(filter);
        LoggingUtils.log(logger, Level.INFO, "Successfully updated {} with info: {}", authService.getUserName(), filter.getClass().getSimpleName(), JsonUtil.toJson(filter));

        filter = QueriesHelper.nullifyUnwantedFields(filter);
        return new ResponseEntity<>(filter, HttpStatus.OK);
    }

}
