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

package com.comcast.xconf.admin.controller.firmware;

import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.service.firmware.ActivationVersionService;
import com.comcast.xconf.shared.controller.AbstractController;
import com.comcast.xconf.shared.service.AbstractService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.comcast.xconf.admin.controller.firmware.ActivationVersionController.URL_MAPPING;

@RestController
@RequestMapping(URL_MAPPING)
public class ActivationVersionController extends AbstractController<ActivationVersion> {

    public static final String URL_MAPPING = "api/activationMinimumVersion";

    @Autowired
    private ActivationVersionService activationVersionService;

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.ACTIVATION_MINIMUM_VERSION.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_ACTIVATION_MINIMUM_VERSIONS.getName();
    }

    @Override
    public AbstractService<ActivationVersion> getService() {
        return activationVersionService;
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", params = "export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportOne(@PathVariable String id) {
        ActivationVersion activationVersion = activationVersionService.getOne(id);
        HttpHeaders headers = Utils.createContentDispositionHeader(getOneEntityExportName() + activationVersion.getId() + activationVersionService.getApplicationTypeSuffix());

        return new ResponseEntity<>(Lists.newArrayList(activationVersion), headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, params = "exportAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportAll() {
        HttpHeaders headers = Utils.createContentDispositionHeader(getAllEntitiesExportName() + activationVersionService.getApplicationTypeSuffix());

        List<ActivationVersion> activationVersions = activationVersionService.getAll();
        return new ResponseEntity<>(activationVersions, headers, HttpStatus.OK);
    }
}
