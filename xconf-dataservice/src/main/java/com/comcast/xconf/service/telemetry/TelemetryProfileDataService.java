package com.comcast.xconf.service.telemetry;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.change.EntityType;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.exception.EntityExistsException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.telemetry.PermanentProfilePredicates;
import com.comcast.xconf.service.change.ChangeCrudService;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.telemetry.TelemetryProfileDataValidator;
import com.google.common.base.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.comcast.xconf.service.change.ChangeBuilders.*;

@Service
public class TelemetryProfileDataService extends AbstractApplicationTypeAwareService<PermanentTelemetryProfile> {

    @Autowired
    private TelemetryPermissionService telemetryPermissionService;

    @Autowired
    private AuthService authService;

    @Autowired
    private TelemetryProfileDataValidator telemetryProfileDataValidator;

    @Autowired
    private PermanentProfilePredicates telemetryProfilePredicates;

    @Autowired
    private ChangeCrudService<PermanentTelemetryProfile> pendingChangesService;

    @Autowired
    private CachedSimpleDao<String, PermanentTelemetryProfile> permanentTelemetryDAO;

    @Autowired
    private CachedSimpleDao<String, TelemetryRule> telemetryRuleDAO;

    @Override
    protected PermissionService getPermissionService() {
        return telemetryPermissionService;
    }

    @Override
    protected List<Predicate<PermanentTelemetryProfile>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(telemetryPermissionService.getReadApplication());
        return telemetryProfilePredicates.getPredicates(contextOptional);
    }

    @Override
    public CachedSimpleDao<String, PermanentTelemetryProfile> getEntityDAO() {
        return permanentTelemetryDAO;
    }

    @Override
    public IValidator<PermanentTelemetryProfile> getValidator() {
        return telemetryProfileDataValidator;
    }

    @Override
    protected String getWriteApplicationType(PermanentTelemetryProfile profile) {
        return ApplicationType.get(profile.getApplicationType());
    }

    @Override
    public List<PermanentTelemetryProfile> getAll() {
        return getEntityDAO().getAll()
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    protected void beforeRemoving(String id) {
        PermanentTelemetryProfile profile = getOne(id);

        validateUsage(id);
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

    public PermanentTelemetryProfile addEntry(String id, List<TelemetryProfile.TelemetryElement> entriesToAdd) {
        PermanentTelemetryProfile profile = addEntriesToProfile(id, entriesToAdd);
        return update(profile);
    }

    public PermanentTelemetryProfile removeEntry(String id, final List<TelemetryProfile.TelemetryElement> entriesToRemove) {
        PermanentTelemetryProfile profile = removeEntriesFromProfile(id, entriesToRemove);

        return update(profile);
    }

    private PermanentTelemetryProfile addEntriesToProfile(String profileId, final List<TelemetryProfile.TelemetryElement> entriesToAdd) {
        PermanentTelemetryProfile profile = getOne(profileId);
        if (CollectionUtils.isEmpty(entriesToAdd)) {
            throw new ValidationRuntimeException("Telemetry entry list is empty");
        }

        for (TelemetryProfile.TelemetryElement entryToAdd : entriesToAdd) {
            if (doesEntryExist(entryToAdd, profile)) {
                throw new EntityExistsException("Telemetry entry already exists");
            }
            profile.getTelemetryProfile().add(entryToAdd);
        }
        return profile;
    }

    private PermanentTelemetryProfile removeEntriesFromProfile(String profileId, final List<TelemetryProfile.TelemetryElement> entriesToRemove) {
        PermanentTelemetryProfile profile = getOne(profileId);
        if (CollectionUtils.isEmpty(entriesToRemove)) {
            throw new ValidationRuntimeException("Telemetry entry list is empty");
        }
        for (TelemetryProfile.TelemetryElement entryToRemove : entriesToRemove) {
            if (CollectionUtils.isNotEmpty(profile.getTelemetryProfile()) && !doesEntryExist(entryToRemove, profile)) {
                throw new EntityNotFoundException("Telemetry entry does not exist");
            }
            profile.getTelemetryProfile()
                    .removeIf(entry -> Objects.nonNull(entry) && entry.equalTelemetryData(entryToRemove));
        }
        return profile;
    }

    private boolean doesEntryExist(TelemetryProfile.TelemetryElement entry, PermanentTelemetryProfile profile) {
        return Objects.nonNull(profile.getTelemetryProfile()) && profile.getTelemetryProfile().stream().anyMatch(existingEntry -> Objects.nonNull(existingEntry)
                && Objects.nonNull(entry)
                && existingEntry.equalTelemetryData(entry));
    }

    @Override
    protected void normalizeOnSave(PermanentTelemetryProfile profile) {
        if (profile != null && CollectionUtils.isEmpty(profile.getTelemetryProfile())) {
            return;
        }
        for (TelemetryProfile.TelemetryElement telemetryElement : profile.getTelemetryProfile()) {
            if (StringUtils.isBlank(telemetryElement.getId())) {
                telemetryElement.setId(UUID.randomUUID().toString());
            }
        }
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

    public Change<PermanentTelemetryProfile> writeDeleteChange(String id) {
        beforeRemoving(id);
        PermanentTelemetryProfile profile = getOne(id);
        Change<PermanentTelemetryProfile> deleteChange = pendingChangesService.create(buildToDelete(profile, EntityType.TELEMETRY_PROFILE, getPermissionService().getWriteApplication(), authService.getUserName()));
        return deleteChange;
    }

    public Change<PermanentTelemetryProfile> addEntriesWithApproval(String id, final List<TelemetryProfile.TelemetryElement> entries) {
        PermanentTelemetryProfile profile = addEntriesToProfile(id, entries);
        return writeUpdateChange(profile);
    }

    public Change<PermanentTelemetryProfile> removeEntriesWithApproval(String id, final List<TelemetryProfile.TelemetryElement> entries) {
        PermanentTelemetryProfile profile = removeEntriesFromProfile(id, entries);
        return writeUpdateChange(profile);
    }
}
