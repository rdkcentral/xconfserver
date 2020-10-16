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
package com.comcast.xconf.admin.controller.setting;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.dcm.ruleengine.SettingsProfileService;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.comcast.xconf.permissions.DcmPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(SettingTestPageController.URL_MAPPING)
public class SettingTestPageController {

    public static final String URL_MAPPING = "api/settings/testpage";

    @Autowired
    private SettingsProfileService settingProfileDataService;

    @Autowired
    private DcmPermissionService permissionService;

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getMatchedRules(@RequestParam(value = "settingType", required = false) Set<String> settingTypes,
                                          @RequestBody Map<String, String> context) {

        if (settingTypes == null || settingTypes.isEmpty()) {
            throw new ValidationRuntimeException("Define settings type");
        }
        context.put(LogUploaderContext.APPLICATION, permissionService.getReadApplication());
        Map<String, Object> result = new HashMap<>();
        result.put("result", settingProfileDataService.getSettingRulesWithConfig(settingTypes, context));
        result.put("context", context);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
