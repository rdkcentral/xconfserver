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

import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.service.dcm.UploadRepositoryService;
import com.comcast.xconf.logupload.UploadRepository;
import com.comcast.xconf.shared.controller.ApplicationTypeAwayController;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = UploadRepositoryController.URL_MAPPING)
public class UploadRepositoryController extends ApplicationTypeAwayController<UploadRepository> {

    public static final String URL_MAPPING = "api/dcm/uploadRepository";

    @Autowired
    private UploadRepositoryService uploadRepositoryService;

    @RequestMapping(value = "/size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getUploadRepositoriesSize() {
        return Integer.toString(getService().getAll().size());
    }

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.UPLOAD_REPOSITORY.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_UPLOAD_REPOSITORIES.getName();
    }

    @Override
    public AbstractApplicationTypeAwareService<UploadRepository> getService() {
        return uploadRepositoryService;
    }
}
