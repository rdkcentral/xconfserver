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
 *  Created: 11/20/15 7:03 PM
 */

package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.xconf.estbfirmware.ConfigChangeLogService;
import com.comcast.xconf.estbfirmware.LastConfigLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(LogController.URL_MAPPING)
public class LogController {

    public static final String URL_MAPPING = "api/log";

    @Autowired
    private ConfigChangeLogService configChangeLogService;

    @RequestMapping(method = RequestMethod.GET, value = "/{macStr}")
    public ResponseEntity getLogs(@PathVariable String macStr) {
        MacAddress macAddress = MacAddress.parse(macStr);
        Map<String, Object> result = new HashMap<>();
        LastConfigLog last = configChangeLogService.getLastConfigLog(macAddress.toString());
        if (last != null) {
            result.put("lastConfigLog", last);
            result.put("configChangeLog", configChangeLogService.getChangeLogsOnly(macAddress.toString()));
        }
        return ResponseEntity.ok(result);
    }
}
