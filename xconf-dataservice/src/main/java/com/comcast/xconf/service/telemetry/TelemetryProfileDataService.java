package com.comcast.xconf.service.telemetry;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.telemetry.PermanentProfilePredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.telemetry.TelemetryProfileDataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public PermanentTelemetryProfile addEntry(String id, TelemetryProfile.TelemetryElement entry) {
        PermanentTelemetryProfile profile = getOne(id);
        if (Objects.nonNull(profile) && Objects.nonNull(profile.getTelemetryProfile())) {
            profile.getTelemetryProfile().add(entry);
        }

        return update(profile);

    }

    public PermanentTelemetryProfile removeEntry(String id, final TelemetryProfile.TelemetryElement entryToRemove) {
        PermanentTelemetryProfile profile = getOne(id);
        profile.getTelemetryProfile()
                .removeIf(entry -> Objects.nonNull(entry) && entry.equalTelemetryData(entryToRemove));

        return update(profile);
    }

}
