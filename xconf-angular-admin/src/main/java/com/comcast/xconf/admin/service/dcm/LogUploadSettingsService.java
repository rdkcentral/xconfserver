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
 * Created: 20.10.15  17:43
 */
package com.comcast.xconf.admin.service.dcm;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.admin.validator.dcm.LogUploadSettingsValidator;
import com.comcast.xconf.dcm.ruleengine.LogFileService;
import com.comcast.xconf.logupload.LogUploadSettings;
import com.comcast.xconf.logupload.UploadRepository;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.dcm.LogUploadSettingPredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class LogUploadSettingsService extends AbstractApplicationTypeAwareService<LogUploadSettings> {

    @Autowired
    private CachedSimpleDao<String, LogUploadSettings> logUploadSettingsDAO;

    @Autowired
    private LogFileService logFileService;

    @Autowired
    private CachedSimpleDao<String, UploadRepository> uploadRepositoryDAO;

    @Autowired
    private LogUploadSettingsValidator logUploadSettingsValidator;

    @Autowired
    private LogUploadSettingPredicates logUploadSettingPredicates;

    @Autowired
    private DcmPermissionService permissionService;

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    public List<String> getLogUploadSettingsNames() {
        List<String> result = new ArrayList<>();
        for (LogUploadSettings entity : getAll()) {
            result.add(entity.getName());
        }

        return result;
    }

    @Override
    public LogUploadSettings delete(final String id) {
        LogUploadSettings logUploadSettings = super.delete(id);
        logFileService.deleteAll(id);
        return logUploadSettings;
    }

    @Override
    public void validateOnSave(LogUploadSettings entity) {
        super.validateOnSave(entity);

        String uploadRepositoryId = entity.getUploadRepositoryId();
        if (uploadRepositoryDAO.getOne(uploadRepositoryId) == null) {
            throw new ValidationRuntimeException("Upload repository with id " + uploadRepositoryId + " does not exist");
        }
    }

    @Override
    protected List<Predicate<LogUploadSettings>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());
        return logUploadSettingPredicates.getPredicates(contextOptional);
    }

    @Override
    public CachedSimpleDao<String, LogUploadSettings> getEntityDAO() {
        return logUploadSettingsDAO;
    }

    @Override
    public IValidator<LogUploadSettings> getValidator() {
        return logUploadSettingsValidator;
    }
}
