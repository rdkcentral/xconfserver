package com.comcast.xconf.service.telemetry;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
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

@Service
public class TelemetryProfileDataService extends AbstractApplicationTypeAwareService<PermanentTelemetryProfile> {

    @Autowired
    private TelemetryPermissionService telemetryPermissionService;

    @Autowired
    private CachedSimpleDao<String, PermanentTelemetryProfile> permanentTelemetryDAO;

    @Autowired
    private TelemetryProfileDataValidator telemetryProfileDataValidator;

    @Autowired
    private PermanentProfilePredicates telemetryProfilePredicates;

    @Autowired
    private ChangeCrudService<PermanentTelemetryProfile> pendingChangesService;

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
        PermanentTelemetryProfile profileToRemove = getOne(id);
        if (CollectionUtils.isNotEmpty(pendingChangesService.getChangesByEntityId(id))) {
            throw new EntityConflictException("There is change for " + profileToRemove.getName() + " telemetry profile");
        }
    }

    public PermanentTelemetryProfile addEntry(String id, TelemetryProfile.TelemetryElement entryToAdd) {
        PermanentTelemetryProfile profile = getOne(id);
        boolean telemetryEntriesAreNotEmpty = CollectionUtils.isNotEmpty(profile.getTelemetryProfile());

        if (telemetryEntriesAreNotEmpty && doesEntryExist(entryToAdd, profile)) {
            throw new EntityExistsException("Telemetry entry already exists");
        }
        profile.getTelemetryProfile().add(entryToAdd);

        return update(profile);
    }

    private boolean doesEntryExist(TelemetryProfile.TelemetryElement entry, PermanentTelemetryProfile profile) {
        return profile.getTelemetryProfile().stream().anyMatch(existingEntry -> Objects.nonNull(existingEntry)
                && Objects.nonNull(entry)
                && existingEntry.equalTelemetryData(entry));
    }

    public PermanentTelemetryProfile removeEntry(String id, final TelemetryProfile.TelemetryElement entryToRemove) {
        PermanentTelemetryProfile profile = getOne(id);
        if (CollectionUtils.isNotEmpty(profile.getTelemetryProfile()) && !doesEntryExist(entryToRemove, profile)) {
            throw new EntityNotFoundException("Telemetry entry does not exist");
        }
        profile.getTelemetryProfile()
                .removeIf(entry -> Objects.nonNull(entry) && entry.equalTelemetryData(entryToRemove));

        return update(profile);
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
}
