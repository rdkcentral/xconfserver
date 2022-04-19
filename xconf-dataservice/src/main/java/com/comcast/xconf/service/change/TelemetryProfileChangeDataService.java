package com.comcast.xconf.service.change;

import com.comcast.xconf.change.ApprovedChange;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.service.telemetry.TelemetryProfileDataService;
import com.comcast.xconf.shared.service.CrudService;
import com.comcast.xconf.util.ChangeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TelemetryProfileChangeDataService extends AbstractChangeService<PermanentTelemetryProfile> {

    @Autowired
    private TelemetryProfileDataService telemetryProfileDataService;

    @Autowired
    private ChangeCrudService changeCrudService;

    @Override
    public CrudService<PermanentTelemetryProfile> getEntityService() {
        return telemetryProfileDataService;
    }

    @Override
    public boolean equalPendingEntities(PermanentTelemetryProfile oldEntity, PermanentTelemetryProfile newEntity) {
        return true;
    }

    @Override
    public List<String> getEntityNames(List<Change<PermanentTelemetryProfile>> changes) {
        List<String> names = new ArrayList<>();
        for (Change<PermanentTelemetryProfile> change : changes) {
            PermanentTelemetryProfile profile = ChangeUtils.getEntity(change);
            if (profile != null) {
                names.add(profile.getName());
            }
        }
        return names;
    }

    @Override
    public PermanentTelemetryProfile applyUpdateChange(PermanentTelemetryProfile mergeResult, Change<PermanentTelemetryProfile> change) {
        return TelemetryProfileChangeUtils.mergeTelemetryProfileChange(mergeResult, change);
    }

    public ApprovedChange<PermanentTelemetryProfile> approveByChangeId(String changeId) {
        ApprovedChange<PermanentTelemetryProfile> approvedChange = approve(changeId);
        return approvedChange;
    }

    public Map<String, String> approveByEntityId(String entityId) {
        List<Change<PermanentTelemetryProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(entityId);
        List<String> changeIds = changesByEntityId.stream()
                .filter(change -> Objects.nonNull(change) && StringUtils.isNotBlank(change.getId())).map(Change::getId)
                .collect(Collectors.toList());

        return approveChanges(changeIds);
    }

    public List<Change<PermanentTelemetryProfile>> getProfileChanges() {
        return changeCrudService.getEntityDao().getAll();
    }
}
