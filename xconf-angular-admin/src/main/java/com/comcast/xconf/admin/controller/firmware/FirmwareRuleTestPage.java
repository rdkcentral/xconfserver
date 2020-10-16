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

import com.comcast.xconf.estbfirmware.EstbFirmwareContext;
import com.comcast.xconf.estbfirmware.EstbFirmwareRuleBase;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(FirmwareRuleTestPage.URL_MAPPING)
public class FirmwareRuleTestPage {

    public static final String URL_MAPPING = "api/firmwarerule/testpage";

    @Autowired
    private EstbFirmwareRuleBase ruleBase;

    @Autowired
    private FirmwarePermissionService permissionService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getMatchedRules(@RequestParam MultiValueMap<String, String> params) {
        EstbFirmwareContext context = new EstbFirmwareContext(params);

        if (StringUtils.isBlank(context.geteStbMac())) {
            context.seteStbMac("11:11:11:11:11:11");
            context.seteStbMac("aa:aa:aa:aa:aa:aa");
        }
        if (context.getTime() == null) {
            context.setTime(new LocalDateTime());
        }
        if (StringUtils.isBlank(context.getIpAddress())) {
            context.setIpAddress("1.1.1.1");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("result", ruleBase.eval(context, permissionService.getReadApplication()));
        map.put("context", context.getProperties());
        return ResponseEntity.ok(map);
    }
}
