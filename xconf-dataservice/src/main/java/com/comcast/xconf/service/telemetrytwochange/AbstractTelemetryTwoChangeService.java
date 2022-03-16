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

package com.comcast.xconf.service.telemetrytwochange;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.change.ApprovedTelemetryTwoChange;
import com.comcast.xconf.change.ChangeOperation;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.shared.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public abstract class AbstractTelemetryTwoChangeService<T extends IPersistable & Comparable> {

    @Autowired
    private TelemetryTwoChangeCrudService<T> changeCrudService;

    @Autowired
    private ApprovedTelemetryTwoChangeCrudService<T> approvedChangeCrudService;

    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AbstractTelemetryTwoChangeService.class);

    public abstract CrudService<T> getEntityService();

    public abstract boolean equalPendingEntities(T oldEntity, T newEntity);

    public abstract List<String> getEntityNames(List<TelemetryTwoChange<T>> changes);

    public abstract T applyUpdateChange(T updateMergeResult, TelemetryTwoChange<T> change);

    public Map<String, String>  approveChanges(List<String> changeIds) {
        List<TelemetryTwoChange<T>> changesToApprove = changeCrudService.getChangesByEntityIds(changeIds);
        Map<String, String> errorMessages = new HashMap<>();
        Map<String, T> mergedUpdateChangesByEntityId = new HashMap<>();
        List<String> entityToByCancelChange = new ArrayList<>();
        for (TelemetryTwoChange<T> change : changesToApprove) {
            try {
                switch(change.getOperation()) {
                    case CREATE:
                        T newEntity = change.getNewEntity();
                        getEntityService().create(newEntity);
                        break;
                    case UPDATE:
                        T mergeResult = applyUpdateChange(mergedUpdateChangesByEntityId.get(change.getEntityId()), change);
                        mergedUpdateChangesByEntityId.put(mergeResult.getId(), mergeResult);
                        getEntityService().update(mergeResult);
                        break;
                    case DELETE:
                        getEntityService().delete(change.getOldEntity().getId());
                        break;
                }
                entityToByCancelChange.add(change.getEntityId());
                saveToApprovedAndCleanUpChange(change);
            } catch (Exception e) {
                logger.error("ApprovingException: ", e);
                errorMessages.put(change.getId(), e.getMessage());
            }
        }
        cancelApprovedChangesByEntityId(entityToByCancelChange, errorMessages.keySet());
        return errorMessages;
    }

    private void cancelApprovedChangesByEntityId(List<String> entityIdsToByCancelChanges, Set<String> changeIdsToBeExcluded) {
        for (String entityId : entityIdsToByCancelChanges) {
            for (TelemetryTwoChange<T> changeByEntityId : changeCrudService.getChangesByEntityId(entityId)) {
                if (!changeIdsToBeExcluded.contains(changeByEntityId.getId())) {
                    changeCrudService.delete(changeByEntityId.getId());
                    logger.info("Automatically canceled change by {}: {}", authService.getUserName(), changeByEntityId);
                }
            }
        }
    }

    private void saveToApprovedAndCleanUpChange(TelemetryTwoChange<T> change) {
        ApprovedTelemetryTwoChange<T> approvedChange = approvedChangeCrudService.saveToApproved(change);
        changeCrudService.delete(change.getId());
        logger.info("Change approved by {}: {}", authService.getUserName(), approvedChange);
    }

    public Map<String, String> revertChanges(List<String> changeIds) {
        List<ApprovedTelemetryTwoChange<T>> changesToRevert = new ArrayList<>();
        for (String changeId : changeIds) {
            changesToRevert.add(approvedChangeCrudService.getOne(changeId));
        }
        Collections.sort(changesToRevert);
        Map<String, String> errorMessages = new HashMap<>();
        for (ApprovedTelemetryTwoChange<T> approvedChange : changesToRevert) {
            try {
                revert(approvedChange.getId());
            } catch(Exception e) {
                logger.error("RevertingException: ", e);
                errorMessages.put(approvedChange.getId(), e.getMessage());
            }
        }
        return errorMessages;
    }

    public ApprovedTelemetryTwoChange<T> approve(String id) {
        TelemetryTwoChange<T> change = changeCrudService.getOne(id);
        if (ChangeOperation.CREATE.equals(change.getOperation())) {
            T newEntity = change.getNewEntity();
            getEntityService().create(newEntity);
            ApprovedTelemetryTwoChange approved = approvedChangeCrudService.saveToApproved(change);
            changeCrudService.delete(id);
            return approved;
        }

        return updateDeleteEntity(change);
    }

    private ApprovedTelemetryTwoChange<T> updateDeleteEntity(TelemetryTwoChange<T> change) {
        T currentEntity = change.getOldEntity();
        T entityToChange = getEntityService().getOne(change.getEntityId());
        if (entityToChange != null && equalPendingEntities(currentEntity, entityToChange)) {
            ApprovedTelemetryTwoChange<T> approvedChange;
            change.setApprovedUser(authService.getUserNameOrUnknown());
            if (ChangeOperation.DELETE.equals(change.getOperation())) {
                getEntityService().delete(change.getOldEntity().getId());
            } else {
                T newEntity = change.getNewEntity();
                getEntityService().update(newEntity);
            }
            approvedChange = approvedChangeCrudService.saveToApproved(change);
            changeCrudService.delete(change.getId());
            return approvedChange;
        } else {
            throw new EntityConflictException("Change could not be approved, " + change.getOldEntity().getClass().getSimpleName() + " have been already changed: " + JsonUtil.toJson(entityToChange));
        }
    }

    public void revert(String approvedId) {
        ApprovedTelemetryTwoChange<T> approvedChange = approvedChangeCrudService.getOne(approvedId);
        if (ChangeOperation.DELETE.equals(approvedChange.getOperation())) {
            revertDelete(approvedId);
        } else {
            revertCreateOrUpdateChange(approvedId, approvedChange.getEntityId());
        }
        logger.info("Change has been reverted by {}: {}", authService.getUserName(), approvedChange);
    }

    public void cancel(String changeId) {
        TelemetryTwoChange canceledChange = changeCrudService.delete(changeId);
        logger.info("Change has been canceled by {}: {}", authService.getUserName(), canceledChange);
    }

    private TelemetryTwoChange<T> revertDelete(String changeId) {
        ApprovedTelemetryTwoChange<T> approvedChange = approvedChangeCrudService.getOne(changeId);
        getEntityService().create(approvedChange.getOldEntity());
        approvedChangeCrudService.delete(changeId);
        return approvedChange;
    }

    private TelemetryTwoChange<T> revertCreateOrUpdateChange(String changeId, String entityId) {
        T entityToRevert = getEntityService().getOne(entityId);
        ApprovedTelemetryTwoChange<T> approvedChange = approvedChangeCrudService.getOne(changeId);
        if (equalPendingEntities(approvedChange.getNewEntity(), entityToRevert)) {
            if (ChangeOperation.CREATE.equals(approvedChange.getOperation())) {
                getEntityService().delete(entityToRevert.getId());
            } else {
                T oldEntity = approvedChange.getOldEntity();
                getEntityService().update(oldEntity);
            }
            approvedChangeCrudService.delete(changeId);
            return approvedChange;
        } else {
            throw new EntityConflictException(T.Factory.class.getSimpleName() + " with id " + approvedChange.getId() + " has been already changed");
        }
    }
}