/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.admin.controller.dcm;

import com.comcast.xconf.MacAddressUtil;
import com.comcast.xconf.dcm.ruleengine.LogUploadRuleBase;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.comcast.xconf.logupload.Settings;
import com.comcast.xconf.permissions.DcmPermissionService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(TestPageController.URL_MAPPING)
public class TestPageController {

    public static final String URL_MAPPING = "api/dcm/testpage";

    @Autowired
    private LogUploadRuleBase ruleBase;

    @Autowired
    private DcmPermissionService permissionService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity matchRule(@RequestBody Map<String, String> params) {
        LogUploaderContext context = new LogUploaderContext(params);
        if (context.getModel() != null) {
            context.setModel(context.getModel().toUpperCase());
        }
        if (context.getEstbMacAddress() != null) {
            context.setEstbMacAddress(MacAddressUtil.normalizeMacAddress(context.getEstbMacAddress()));
        }
        if (context.getEcmMacAddress() != null) {
            context.setEcmMacAddress(MacAddressUtil.normalizeMacAddress(context.getEcmMacAddress()));
        }

        context.setApplication(permissionService.getReadApplication());

        Settings eval = ruleBase.eval(context);
        Map<String, Object> allSettings = new HashMap<>();
        allSettings.put("context", context.getProperties());
        if (eval == null || CollectionUtils.isEmpty(eval.getRuleIDs())) {
            return new ResponseEntity<>(allSettings, HttpStatus.OK);
        }
        allSettings.put("settings", eval);
        allSettings.put("ruleIds", eval.getRuleIDs());
        allSettings.put("ruleType", DCMGenericRule.class.getSimpleName());
        return new ResponseEntity<>(allSettings, HttpStatus.OK);
    }
}
