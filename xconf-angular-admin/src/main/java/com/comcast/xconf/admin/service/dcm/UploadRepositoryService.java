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
 * Created: 23.11.15  11:48
 */
package com.comcast.xconf.admin.service.dcm;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.admin.validator.dcm.UploadRepositoryValidator;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.logupload.LogUploadSettings;
import com.comcast.xconf.logupload.UploadRepository;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.dcm.UploadRepositoryPredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class UploadRepositoryService extends AbstractApplicationTypeAwareService<UploadRepository> {

    private static final Logger log = LoggerFactory.getLogger(UploadRepositoryService.class);

    @Autowired
    private CachedSimpleDao<String, UploadRepository> uploadRepositoryDAO;

    @Autowired
    private UploadRepositoryValidator uploadRepositoryValidator;

    @Autowired
    private UploadRepositoryPredicates uploadRepositoryPredicates;

    @Autowired
    private DcmPermissionService permissionService;

    @Autowired
    private LogUploadSettingsService logUploadSettingsService;

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    protected List<Predicate<UploadRepository>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());
        return uploadRepositoryPredicates.getPredicates(contextOptional);
    }

    @Override
    public CachedSimpleDao<String, UploadRepository> getEntityDAO() {
        return uploadRepositoryDAO;
    }

    @Override
    public IValidator<UploadRepository> getValidator() {
        return uploadRepositoryValidator;
    }

    @Override
    public void validateUsage(String id) {
        for (LogUploadSettings logUploadSettings : logUploadSettingsService.getAll()) {
            if (StringUtils.isNotBlank(logUploadSettings.getUploadRepositoryId()) && (StringUtils.equals(id, logUploadSettings.getUploadRepositoryId()))) {
                throw new EntityConflictException("UploadRepository is used by LogUploadSettings " + logUploadSettings.getName());
            }
        }
    }
}
