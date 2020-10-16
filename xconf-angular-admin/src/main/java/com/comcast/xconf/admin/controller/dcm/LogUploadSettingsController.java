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
 * Author: Stanislav Menshykov
 * Created: 20.10.15  15:59
 */
package com.comcast.xconf.admin.controller.dcm;

import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.admin.service.dcm.LogUploadSettingsService;
import com.comcast.xconf.logupload.LogUploadSettings;
import com.comcast.xconf.shared.controller.AbstractController;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.shared.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(LogUploadSettingsController.URL_MAPPING)
public class LogUploadSettingsController extends AbstractController<LogUploadSettings> {

    public static final String URL_MAPPING = "api/dcm/logUploadSettings";

    @Autowired
    private LogUploadSettingsService logUploadSettingsService;

    @RequestMapping(value = "/names", method = RequestMethod.GET)
    public ResponseEntity getLogUploadSettingsNames() {
        return new ResponseEntity<>(logUploadSettingsService.getLogUploadSettingsNames(), HttpStatus.OK);
    }

    @RequestMapping(value = "/size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getLogUploadSettingsSize() {
        return Integer.toString(getService().getAll().size());
    }

    @RequestMapping(value = "/filtered", method = RequestMethod.POST)
    public ResponseEntity getFiltered(
            @RequestBody Map<String, String> searchContext,
            @RequestParam final Integer pageSize,
            @RequestParam final Integer pageNumber
    ) {
        List<LogUploadSettings> logUploadSettingsByName = logUploadSettingsService.findByContext(searchContext);
        return new ResponseEntity<>(PageUtils.getPage(logUploadSettingsByName, pageNumber, pageSize),
                Utils.createNumberOfItemsHttpHeaders(logUploadSettingsByName),
                HttpStatus.OK);
    }

    @Override
    public String getOneEntityExportName() {
        return null;
    }

    @Override
    public String getAllEntitiesExportName() {
        return null;
    }

    @Override
    public AbstractService<LogUploadSettings> getService() {
        return logUploadSettingsService;
    }
}
