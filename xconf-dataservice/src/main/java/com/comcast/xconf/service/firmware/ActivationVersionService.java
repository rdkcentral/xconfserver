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

package com.comcast.xconf.service.firmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.FirmwareConfigData;
import com.comcast.xconf.estbfirmware.FirmwareConfigQueriesService;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.converter.ActivationVersionConverter;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.firmware.ActivationVersionPredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;

import static com.comcast.xconf.estbfirmware.FirmwareConfigQueriesService.NOT_EXISTED_VERSIONS;

@Service
public class ActivationVersionService extends AbstractApplicationTypeAwareService<ActivationVersion> {

    @Autowired
    private FirmwarePermissionService permissionService;

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private IValidator<ActivationVersion> activationVersionValidator;

    @Autowired
    private ActivationVersionConverter converter;

    @Autowired
    private FirmwareConfigQueriesService firmwareConfigQueriesService;

    @Autowired
    private ActivationVersionPredicates activationVersionPredicates;

    @Override
    protected PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    public CachedSimpleDao getEntityDAO() {
        return firmwareRuleDao;
    }

    @Override
    public IValidator<ActivationVersion> getValidator() {
        return activationVersionValidator;
    }

    @Override
    public ActivationVersion getOne(String id) {
        FirmwareRule one = firmwareRuleDao.getOne(id);
        if (one == null) {
            throw new EntityNotFoundException("Entity with id: " + id + " does not exist");
        }
        return converter.convertIntoActivationVersion(one);
    }

    @Override
    protected ActivationVersion save(ActivationVersion entity) {
        beforeSaving(entity);
        FirmwareRule firmwareRule = converter.convertIntoRule(entity);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);
        return entity;
    }

    @Override
    protected void normalizeOnSave(ActivationVersion activationVersion) {
        if (StringUtils.isNotBlank(activationVersion.getModel())) {
            String upperCaseModel = activationVersion.getModel().toUpperCase();
            activationVersion.setModel(upperCaseModel);
        }
        if (StringUtils.isNotBlank(activationVersion.getPartnerId())) {
            String upperCasePartner = activationVersion.getPartnerId().toUpperCase();
            activationVersion.setPartnerId(upperCasePartner);
        } else {
            activationVersion.setPartnerId(null);
        }
    }

    @Override
    public List<ActivationVersion> getAll() {
        List<ActivationVersion> result = new ArrayList<>();
        Iterable<FirmwareRule> firmwareRules = Optional.presentInstances(firmwareRuleDao.asLoadingCache().asMap().values());
        String firmwareReadApplication = permissionService.getReadApplication();
        for (FirmwareRule rule : firmwareRules) {
            if (TemplateNames.ACTIVATION_VERSION.equals(rule.getType()) && ApplicationType.equals(firmwareReadApplication, rule.getApplicationType())) {
                result.add(converter.convertIntoActivationVersion(rule));
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
    protected List<Predicate<ActivationVersion>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());
        return activationVersionPredicates.getPredicates(contextOptional);
    }

    protected void beforeSaving(ActivationVersion activationVersion) {
        if (activationVersion != null && CollectionUtils.isNotEmpty(activationVersion.getFirmwareVersions()) && StringUtils.isNotBlank(activationVersion.getModel())) {
            Set<String> models = Sets.newHashSet(activationVersion.getModel());
            FirmwareConfigData firmwareConfigData = new FirmwareConfigData(Lists.newArrayList(activationVersion.getFirmwareVersions()), models);
            Map <String, Set <String>> sortedFirmwareVersions = firmwareConfigQueriesService.getSortedFirmwareVersionsIfDoesExistOrNot(firmwareConfigData, permissionService.getReadApplication());
            Set<String> notExistedVersions = sortedFirmwareVersions.get(NOT_EXISTED_VERSIONS);
            activationVersion.getFirmwareVersions().removeAll(notExistedVersions);
        }
        super.beforeSaving(activationVersion);
    }
}
