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
 * Created: 17.10.15  12:12
 */
package com.comcast.xconf.admin.service.dcm;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.admin.validator.dcm.DeviceSettingsValidator;
import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.search.dcm.DeviceSettingPredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;


@Service
public class DeviceSettingsService extends AbstractApplicationTypeAwareService<DeviceSettings> {

    @Autowired
    private CachedSimpleDao<String, DeviceSettings> deviceSettingsDAO;

    @Autowired
    private DeviceSettingsValidator deviceSettingsValidator;

    @Autowired
    private DeviceSettingPredicates deviceSettingPredicates;

    @Autowired
    private DcmPermissionService permissionService;

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    public CachedSimpleDao<String, DeviceSettings> getEntityDAO() {
        return deviceSettingsDAO;
    }

    @Override
    public IValidator<DeviceSettings> getValidator() {
        return deviceSettingsValidator;
    }

    @Override
    protected List<Predicate<DeviceSettings>> getPredicatesByContext(Map<String, String> context) {
        if (Objects.isNull(context)) {
            return new ArrayList<>();
        }
        if (StringUtils.isBlank(context.get(SearchFields.APPLICATION_TYPE))) {
            context.put(SearchFields.APPLICATION_TYPE, permissionService.getReadApplication());
        }
        List<Predicate<DeviceSettings>> predicates = deviceSettingPredicates.getPredicates(new ContextOptional(context));

        return predicates;
    }

    public List<String> getDeviceSettingsNames() {
        List<String> result = new ArrayList<>();
        for (DeviceSettings entity : getAll()) {
            result.add(entity.getName());
        }

        return result;
    }
}
