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

import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.change.TelemetryTwoChange;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.permissions.ChangePermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.changes.TelemetryTwoChangePredicates;
import com.comcast.xconf.validators.change.TelemetryTwoChangeValidator;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

public abstract class AbstractTelemetryTwoChangeCrudService<T extends TelemetryTwoChange> {

    protected static Logger log = LoggerFactory.getLogger(AbstractTelemetryTwoChangeCrudService.class);

    @Autowired
    private TelemetryTwoChangeValidator<T> validator;

    @Autowired
    private ChangePermissionService permissionService;

    @Autowired
    private TelemetryTwoChangePredicates changePredicates;

    public abstract SimpleDao<String, T> getEntityDao();

    public String getEntityType() {
        return T.Factory.class.getSimpleName();
    }

    public TelemetryTwoChangeValidator<T> getValidator() {
        return validator;
    }

    protected PermissionService getPermissionService() {
        return permissionService;
    }

    public T create(T change) {
        beforeSaving(change);
        getEntityDao().setOne(change.getId(), change);
        log.info(getEntityType() + " saved: {}", JsonUtil.toJson(change));
        return change;
    }

    public T delete(String changeId) {
        beforeDelete(changeId);
        T change = getOne(changeId);
        getEntityDao().deleteOne(changeId);
        return change;
    }

    public T getOne(String id) {
        getValidator().validateId(id);
        T change = getEntityDao().getOne(id);
        if (change == null) {
            throw new EntityNotFoundException(getEntityType() + " with " + id + " id does not exist");
        }
        return change;
    }

    public List<T> getAll() {
        return getEntityDao().getAll()
                .stream()
                .filter(byApplication(permissionService.getReadApplication()))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<T> getChangesByEntityId(String entityId) {
        List<T> result = new ArrayList<>();
        for (T change : getEntityDao().getAll()) {
            if (StringUtils.equals(change.getEntityId(), entityId)) {
                result.add(change);
            }
        }
        return result;
    }

    public List<T> getChangesByEntityIds(List<String> changeIds) {
        List<T> changes = new ArrayList<>();
        for (String changeId : changeIds) {
            changes.add(getOne(changeId));
        }
        Collections.sort(changes);
        return changes;
    }

    public Map<String, List<T>> groupChanges(List<T> changes) {
        Map<String, List<T>> groupedChanges = new LinkedHashMap<>();
        for (T change : changes) {
            groupChange(change, groupedChanges);
        }
        return groupedChanges;
    }

    public List<String> getChangedEntityIds() {
        List<String> ids = new ArrayList<>();
        for (T change : getEntityDao().getAll()) {
            ids.add(change.getEntityId());
        }
        return ids;
    }

    private void groupChange(T change, Map<String, List<T>> groupedChanges) {
        if (CollectionUtils.isNotEmpty(groupedChanges.get(change.getEntityId()))) {
            groupedChanges.get(change.getEntityId()).add(change);
        } else {
            groupedChanges.put(change.getEntityId(), Lists.newArrayList(change));
        }
    }

    protected void beforeDelete(String id) {
        getValidator().validateId(id);
        if (getEntityDao().getOne(id) == null) {
            throw new EntityNotFoundException(getEntityType() + " with " + id + " id does not exist");
        }
    }

    protected void beforeSaving(T change) {
        if (change != null && StringUtils.isBlank(change.getApplicationType())) {
            change.setApplicationType(permissionService.getWriteApplication());
        }
        if (change != null && StringUtils.isBlank(change.getId())) {
            change.setId(UUID.randomUUID().toString());
        }
        getValidator().validate(change);
    }


    public List<T> findByContext(Map<String, String> searchContext) {
        List<Predicate<T>> predicates = getPredicatesByContext(searchContext);

        return getEntityDao().getAll()
                .stream()
                .filter(predicates.stream().reduce(x -> true, Predicate::and))
                .collect(Collectors.toList());
    }

    protected List<Predicate<T>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());
        return changePredicates.getPredicates(contextOptional);
    }
}
