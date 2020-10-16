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
 * Author: Igor Kostrov
 * Created: 3/17/2016
*/
package com.comcast.xconf.admin.service.setting;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.admin.validator.setting.SettingProfileValidator;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.setting.SettingProfilePredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class SettingProfileService extends AbstractApplicationTypeAwareService<SettingProfile> {

    @Autowired
    private CachedSimpleDao<String, SettingRule> settingRuleDAO;

    @Autowired
    private CachedSimpleDao<String, SettingProfile> settingProfileDao;

    @Autowired
    private SettingProfileValidator validator;

    @Autowired
    private SettingProfilePredicates settingProfilePredicates;

    @Autowired
    private DcmPermissionService permissionService;

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    public CachedSimpleDao<String, SettingProfile> getEntityDAO() {
        return settingProfileDao;
    }

    @Override
    public IValidator<SettingProfile> getValidator() {
        return validator;
    }

    @Override
    protected void validateUsage(String id) {
        Iterable<SettingRule> all = Optional.presentInstances(settingRuleDAO.asLoadingCache().asMap().values());
        for (SettingRule rule : all) {
            if (StringUtils.equals(rule.getBoundSettingId(), id)) {
                throw new EntityConflictException("Can't delete profile as it's used in setting rule: " + rule.getName());
            }
        }
    }

    @Override
    protected List<Predicate<SettingProfile>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());
        return settingProfilePredicates.getPredicates(contextOptional);
    }
}
