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

package com.comcast.xconf.admin.service.telemetrytwochange;

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.xconf.admin.service.telemetry.TelemetryTwoProfileService;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.shared.service.CrudService;
import com.comcast.xconf.shared.utils.TelemetryTwoChangeUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TelemetryTwoProfileChangeService extends AbstractTelemetryTwoChangeService<TelemetryTwoProfile>{

    @Autowired
    private TelemetryTwoProfileService profileService;

    @Override
    public CrudService<TelemetryTwoProfile> getEntityService() {
        return profileService;
    }

    @Override
    public boolean equalPendingEntities(TelemetryTwoProfile oldEntity, TelemetryTwoProfile newEntity) {
        return true;
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
    
    

    private boolean isNewElement(TelemetryTwoProfile telemetryElement) {
        return StringUtils.isBlank(telemetryElement.getId()) && telemetryElement != null;
    }

    private boolean removedBefore(TelemetryTwoProfile old, TelemetryTwoProfile updated, TelemetryTwoProfile merged) {
        return !Objects.equals(old, updated) && merged == null;
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


    private List<String> getRemovedTelemetryElementIds(List<TelemetryTwoProfile> oldElements, List<TelemetryTwoProfile> newElements) {
        List<String> removedElements = new ArrayList<>();
        for (TelemetryTwoProfile oldElement : oldElements) {
            if (findTelemetryElementById(oldElement.getId(), newElements) == null) {
                removedElements.add(oldElement.getId());
            }
        }
        return removedElements;
    }

    private TelemetryTwoProfile findTelemetryElementById(String id, List<TelemetryTwoProfile> telemetryElements) {
        for (TelemetryTwoProfile telemetryElement : telemetryElements) {
            if (StringUtils.equals(id, telemetryElement.getId())) {
                return telemetryElement;
            }
        }
        return null;
    }
}