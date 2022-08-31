/**
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2022 RDK Management
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
 * Author: Maksym Dolina
 */
package com.comcast.xconf.service.telemetry;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.change.EntityType;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.telemetry.TelemetryTwoProfilePredicates;
import com.comcast.xconf.service.telemetrytwochange.TelemetryTwoChangeCrudService;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.telemetry.TelemetryTwoProfileDataValidator;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.comcast.xconf.service.telemetrytwochange.TelemetryTwoChangeBuilders.*;

@Service
public class TelemetryProfileTwoDataService extends AbstractApplicationTypeAwareService<TelemetryTwoProfile> {

    @Autowired
    private TelemetryPermissionService telemetryPermissionService;

    @Autowired
    private CachedSimpleDao<String, TelemetryTwoProfile> telemetryTwoProfileDAO;

    @Autowired
    private TelemetryTwoProfilePredicates telemetryTwoProfilePredicates;

    @Autowired
    private TelemetryTwoProfileDataValidator telemetryTwoProfileDataValidator;

    @Autowired
    private TelemetryTwoChangeCrudService<TelemetryTwoProfile> pendingChangesService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CachedSimpleDao<String, TelemetryTwoRule> telemetryTwoRuleDAO;

    @Override
    protected PermissionService getPermissionService() {
        return telemetryPermissionService;
    }

    @Override
    protected List<Predicate<TelemetryTwoProfile>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        return telemetryTwoProfilePredicates.getPredicates(contextOptional);
    }

    @Override
    public CachedSimpleDao<String, TelemetryTwoProfile> getEntityDAO() {
        return telemetryTwoProfileDAO;
    }

    @Override
    public IValidator<TelemetryTwoProfile> getValidator() {
        return telemetryTwoProfileDataValidator;
    }

    @Override
    protected String getWriteApplicationType(TelemetryTwoProfile telemetryTwoProfile) {
        return ApplicationType.get(telemetryTwoProfile.getApplicationType());
    }

    @Override
    public List<TelemetryTwoProfile> getAll() {
        return getEntityDAO().getAll()
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    protected void validateUsage(String id) {
        Iterable<TelemetryTwoRule> all = Optional.presentInstances(telemetryTwoRuleDAO.asLoadingCache().asMap().values());
        for (TelemetryTwoRule rule : all) {
            if (rule.getBoundTelemetryIds().contains(id)) {
                throw new EntityConflictException("Can't delete profile as it's used in telemetry rule: " + rule.getName());
            }
        }
    }

    @Override
    protected void beforeRemoving(String id) {
        getOne(id); //to cause EntityNotFound exception if profile does not exist

        validateUsage(id);
    }

    public TelemetryTwoChange<TelemetryTwoProfile> writeCreateChange(TelemetryTwoProfile profile) {
        beforeCreating(profile);
        beforeSaving(profile);
        return pendingChangesService.create(buildToCreate(profile, EntityType.TELEMETRY_TWO_PROFILE, getWriteApplicationType(profile), authService.getUserNameOrUnknown()));
    }

    public TelemetryTwoChange<TelemetryTwoProfile> writeUpdateChange(TelemetryTwoProfile newProfile) {
        beforeUpdating(newProfile);
        beforeSaving(newProfile);
        TelemetryTwoProfile oldProfile = getOne(newProfile.getId());
        return pendingChangesService.create(buildToUpdate(oldProfile, newProfile, EntityType.TELEMETRY_TWO_PROFILE, getWriteApplicationType(oldProfile), authService.getUserNameOrUnknown()));
    }

    public TelemetryTwoChange<TelemetryTwoProfile> writeDeleteChange(String id) {
        beforeRemoving(id);
        TelemetryTwoProfile profileToRemove = getOne(id);
        return pendingChangesService.create(buildToDelete(profileToRemove, EntityType.TELEMETRY_TWO_PROFILE, getWriteApplicationType(profileToRemove), authService.getUserNameOrUnknown()));
    }
}
