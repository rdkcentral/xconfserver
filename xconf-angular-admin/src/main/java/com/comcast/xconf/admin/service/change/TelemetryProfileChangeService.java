/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.admin.service.change;

import com.comcast.xconf.admin.service.telemetry.PermanentTelemetryProfileService;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.service.change.AbstractChangeService;
import com.comcast.xconf.service.change.TelemetryProfileChangeUtils;
import com.comcast.xconf.shared.service.CrudService;
import com.comcast.xconf.util.ChangeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TelemetryProfileChangeService extends AbstractChangeService<PermanentTelemetryProfile> {

    @Autowired
    private PermanentTelemetryProfileService profileService;

    @Override
    public CrudService<PermanentTelemetryProfile> getEntityService() {
        return profileService;
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
}