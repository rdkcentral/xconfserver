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

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.xconf.admin.service.telemetry.PermanentTelemetryProfileService;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.shared.service.CrudService;
import com.comcast.xconf.shared.utils.ChangeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TelemetryProfileChangeService extends AbstractChangeService<PermanentTelemetryProfile>{

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
        if (mergeResult == null) {
            return CloneUtil.clone(change.getNewEntity());
        }
        PermanentTelemetryProfile oldProfile = change.getOldEntity();
        PermanentTelemetryProfile updatedProfile = change.getNewEntity();
        if (!StringUtils.equals(oldProfile.getName(), updatedProfile.getName())) {
            mergeResult.setName(updatedProfile.getName());
        }
        if (!StringUtils.equals(oldProfile.getSchedule(), updatedProfile.getSchedule())) {
            mergeResult.setSchedule(updatedProfile.getSchedule());
        }
        if (!Objects.equals(oldProfile.getUploadProtocol(), updatedProfile.getUploadProtocol())) {
            mergeResult.setUploadProtocol(updatedProfile.getUploadProtocol());
        }
        if (!StringUtils.equals(oldProfile.getUploadRepository(), updatedProfile.getUploadRepository())) {
            mergeResult.setUploadRepository(updatedProfile.getUploadRepository());
        }
        return applyTelemetryElementChanges(change, mergeResult);
    }
    private PermanentTelemetryProfile applyTelemetryElementChanges(Change<PermanentTelemetryProfile> change, PermanentTelemetryProfile mergeResult) {
        List<TelemetryProfile.TelemetryElement> oldTelemetryElements = change.getOldEntity().getTelemetryProfile();
        List<TelemetryProfile.TelemetryElement> updatedTelemetryElements = change.getNewEntity().getTelemetryProfile();
        for (TelemetryProfile.TelemetryElement updated : updatedTelemetryElements) {
            TelemetryProfile.TelemetryElement old = findTelemetryElementById(updated.getId(), oldTelemetryElements);
            TelemetryProfile.TelemetryElement merged = findTelemetryElementById(updated.getId(), mergeResult.getTelemetryProfile());
            if (isNewElement(updated) || removedBefore(old, updated, merged)) {
                mergeResult.getTelemetryProfile().add(updated);
                continue;
            }
            applyTelemetryElementChange(merged, old, updated);
        }
        removeTelemetryElementsFromMergeResult(getRemovedTelemetryElementIds(oldTelemetryElements, updatedTelemetryElements), mergeResult);
        return mergeResult;
    }

    private boolean isNewElement(TelemetryProfile.TelemetryElement telemetryElement) {
        return StringUtils.isBlank(telemetryElement.getId()) && telemetryElement != null;
    }

    private boolean removedBefore(TelemetryProfile.TelemetryElement old, TelemetryProfile.TelemetryElement updated, TelemetryProfile.TelemetryElement merged) {
        return !Objects.equals(old, updated) && merged == null;
    }

    private void applyTelemetryElementChange(TelemetryProfile.TelemetryElement mergedElement, TelemetryProfile.TelemetryElement oldElement, TelemetryProfile.TelemetryElement newElement) {
        if (oldElement != null && mergedElement != null) {
            if (!StringUtils.equals(oldElement.getHeader(), newElement.getHeader())) {
                mergedElement.setHeader(newElement.getHeader());
            }
            if (!StringUtils.equals(oldElement.getContent(),newElement.getContent())) {
                mergedElement.setContent(newElement.getContent());
            }
            if (!StringUtils.equals(oldElement.getType(), newElement.getType())) {
                mergedElement.setType(newElement.getType());
            }
            if (!StringUtils.equals(oldElement.getPollingFrequency(), newElement.getPollingFrequency())) {
                mergedElement.setPollingFrequency(newElement.getPollingFrequency());
            }
        }
    }

    private void removeTelemetryElementsFromMergeResult(List<String> idsToRemove, PermanentTelemetryProfile mergeResult) {
        for (String id : idsToRemove) {
            TelemetryProfile.TelemetryElement telemetryElementToRemove = findTelemetryElementById(id, mergeResult.getTelemetryProfile());
            mergeResult.getTelemetryProfile().remove(telemetryElementToRemove);
        }
    }

    private List<String> getRemovedTelemetryElementIds(List<TelemetryProfile.TelemetryElement> oldElements, List<TelemetryProfile.TelemetryElement> newElements) {
        List<String> removedElements = new ArrayList<>();
        for (TelemetryProfile.TelemetryElement oldElement : oldElements) {
            if (findTelemetryElementById(oldElement.getId(), newElements) == null) {
                removedElements.add(oldElement.getId());
            }
        }
        return removedElements;
    }

    private TelemetryProfile.TelemetryElement findTelemetryElementById(String id, List<TelemetryProfile.TelemetryElement> telemetryElements) {
        for (TelemetryProfile.TelemetryElement telemetryElement : telemetryElements) {
            if (StringUtils.equals(id, telemetryElement.getId())) {
                return telemetryElement;
            }
        }
        return null;
    }
}