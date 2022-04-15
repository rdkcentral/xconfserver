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
package com.comcast.xconf.admin.service.telemetry;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.change.EntityType;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.telemetry.PermanentProfilePredicates;
import com.comcast.xconf.service.change.ApprovedChangeCrudService;
import com.comcast.xconf.service.change.ChangeCrudService;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.telemetry.TelemetryProfileValidator;
import com.google.common.base.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static com.comcast.xconf.service.change.ChangeBuilders.*;

@Service
public class PermanentTelemetryProfileService extends AbstractApplicationTypeAwareService<PermanentTelemetryProfile> {

    @Autowired
    private CachedSimpleDao<String, PermanentTelemetryProfile> permanentTelemetryDAO;

    @Autowired
    private CachedSimpleDao<String, TelemetryRule> telemetryRuleDAO;

    @Autowired
    private TelemetryProfileValidator telemetryProfileValidator;

    @Autowired
    private PermanentProfilePredicates permanentProfilePredicates;

    @Autowired
    private TelemetryPermissionService permissionService;

    @Autowired
    private ChangeCrudService<PermanentTelemetryProfile> pendingChangesService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ApprovedChangeCrudService<PermanentTelemetryProfile> approvedChangeCrudService;

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    public CachedSimpleDao<String, PermanentTelemetryProfile> getEntityDAO() {
        return permanentTelemetryDAO;
    }

    @Override
    public IValidator<PermanentTelemetryProfile> getValidator() {
        return telemetryProfileValidator;
    }

    @Override
    protected void validateUsage(String id) {
        Iterable<TelemetryRule> all = Optional.presentInstances(telemetryRuleDAO.asLoadingCache().asMap().values());
        for (TelemetryRule rule : all) {
            if (StringUtils.equals(rule.getBoundTelemetryId(), id)) {
                throw new EntityConflictException("Can't delete profile as it's used in telemetry rule: " + rule.getName());
            }
        }
    }

    @Override
    protected List<Predicate<PermanentTelemetryProfile>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());
        return permanentProfilePredicates.getPredicates(contextOptional);
    }

    public Change<PermanentTelemetryProfile> writeCreateChange(PermanentTelemetryProfile profile) {
        beforeCreating(profile);
        beforeSaving(profile);
        return pendingChangesService.create(buildToCreate(profile, EntityType.TELEMETRY_PROFILE, getPermissionService().getWriteApplication(), authService.getUserNameOrUnknown()));
    }

    public Change<PermanentTelemetryProfile> writeUpdateChange(PermanentTelemetryProfile newProfile) {
        beforeUpdating(newProfile);
        beforeSaving(newProfile);
        PermanentTelemetryProfile oldProfile = getOne(newProfile.getId());
        return pendingChangesService.create(buildToUpdate(oldProfile, newProfile, EntityType.TELEMETRY_PROFILE, getPermissionService().getWriteApplication(), authService.getUserNameOrUnknown()));
    }

    public boolean writeUpdateChangeOrSave(PermanentTelemetryProfile newProfile) {
        beforeUpdating(newProfile);
        beforeSaving(newProfile);
        PermanentTelemetryProfile oldProfile = getOne(newProfile.getId());
        if (newProfile.equals(oldProfile)) {
            update(newProfile);
            return false;
        } else {
            pendingChangesService.create(buildToUpdate(oldProfile, newProfile, EntityType.TELEMETRY_PROFILE, getPermissionService().getWriteApplication(), authService.getUserNameOrUnknown()));
            return true;
        }
    }

    public PermanentTelemetryProfile writeDeleteChange(String id) {
        beforeRemoving(id);
        PermanentTelemetryProfile profile = getOne(id);
        pendingChangesService.create(buildToDelete(profile, EntityType.TELEMETRY_PROFILE, getPermissionService().getWriteApplication(), authService.getUserNameOrUnknown()));
        return profile;
    }

    @Override
    public PermanentTelemetryProfile update(PermanentTelemetryProfile updatedProfile) {
        normalizeOnSaveAfterApproving(updatedProfile);
        return super.update(updatedProfile);
    }

    @Override
    public PermanentTelemetryProfile create(PermanentTelemetryProfile profile) {
        normalizeOnSaveAfterApproving(profile);
        return super.create(profile);
    }

    @Override
    public PermanentTelemetryProfile delete(String id) {
        PermanentTelemetryProfile delete = super.delete(id);
        return delete;
    }

    public void normalizeOnSaveAfterApproving(PermanentTelemetryProfile profile) {
        if (profile != null && CollectionUtils.isEmpty(profile.getTelemetryProfile())) {
            return;
        }
        for (TelemetryProfile.TelemetryElement telemetryElement : profile.getTelemetryProfile()) {
            if (StringUtils.isBlank(telemetryElement.getId())) {
                telemetryElement.setId(UUID.randomUUID().toString());
            }
        }
    }
}
