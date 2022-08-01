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

package com.comcast.xconf.service.change.telemetrytwo;

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.service.telemetry.TelemetryProfileTwoDataService;
import com.comcast.xconf.service.telemetrytwochange.AbstractTelemetryTwoChangeService;
import com.comcast.xconf.service.telemetrytwochange.TelemetryTwoChangeCrudService;
import com.comcast.xconf.shared.service.CrudService;
import com.comcast.xconf.util.TelemetryTwoChangeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TelemetryTwoProfileChangeDataService extends AbstractTelemetryTwoChangeService<TelemetryTwoProfile> {

    @Autowired
    private TelemetryProfileTwoDataService telemetryProfileTwoDataService;

    @Autowired
    private TelemetryTwoChangeCrudService<TelemetryTwoProfile> changeCrudService;

    @Override
    public CrudService<TelemetryTwoProfile> getEntityService() {
        return telemetryProfileTwoDataService;
    }

    //TODO: fix that, basically some evaluation should be here
    @Override
    public boolean equalPendingEntities(TelemetryTwoProfile oldProfile, TelemetryTwoProfile newProfile) {
        return Objects.equals(oldProfile, newProfile);
    }

    @Override
    public List<String> getEntityNames(List<TelemetryTwoChange<TelemetryTwoProfile>> changes) {
        List<String> names = new ArrayList<>();
        for (TelemetryTwoChange<TelemetryTwoProfile> change : changes) {
            TelemetryTwoProfile profile = TelemetryTwoChangeUtils.getEntity(change);
            if (profile != null) {
                names.add(profile.getName());
            }
        }
        return names;
    }

    @Override
    public TelemetryTwoProfile applyUpdateChange(TelemetryTwoProfile mergeResult, TelemetryTwoChange<TelemetryTwoProfile> change) {
        if (mergeResult == null) {
            return CloneUtil.clone(change.getNewEntity());
        }
        TelemetryTwoProfile oldProfile = change.getOldEntity();
        TelemetryTwoProfile updatedProfile = change.getNewEntity();
        if (!StringUtils.equals(oldProfile.getName(), updatedProfile.getName())) {
            mergeResult.setName(updatedProfile.getName());
        }
        if (!StringUtils.equals(oldProfile.getJsonconfig(), updatedProfile.getJsonconfig())) {
            mergeResult.setJsonconfig(updatedProfile.getJsonconfig());
        }
        applyTelemetryElementChange(mergeResult, oldProfile, updatedProfile);
        return mergeResult;
    }

    public Map<String, String> approveByEntityId(String profileId) {
        List<TelemetryTwoChange<TelemetryTwoProfile>> changesByEntityId = changeCrudService.getChangesByEntityId(profileId);
        List<String> changeIds = changesByEntityId.stream()
                .filter(change -> Objects.nonNull(change) && StringUtils.isNotBlank(change.getId())).map(TelemetryTwoChange::getId)
                .collect(Collectors.toList());

        return approveChanges(changeIds);
    }

    public List<TelemetryTwoChange<TelemetryTwoProfile>> getAllChanges() {
        return changeCrudService.getAll();
    }

    private void applyTelemetryElementChange(TelemetryTwoProfile mergedElement, TelemetryTwoProfile oldElement, TelemetryTwoProfile newElement) {
        if (oldElement != null && mergedElement != null) {
            if (!StringUtils.equals(oldElement.getName(), newElement.getName())) {
                mergedElement.setName(newElement.getName());
            }
            if (!StringUtils.equals(oldElement.getJsonconfig(),newElement.getJsonconfig())) {
                mergedElement.setJsonconfig(newElement.getJsonconfig());
            }
        }
    }
}
