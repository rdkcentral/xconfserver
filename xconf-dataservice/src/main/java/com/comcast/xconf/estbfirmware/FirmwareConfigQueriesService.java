/*******************************************************************************
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
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
 *******************************************************************************/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.search.firmware.FirmwareConfigPredicates;
import com.comcast.xconf.service.firmware.ActivationVersionDataService;
import com.comcast.xconf.service.firmware.FirmwareRuleDataService;
import com.comcast.xconf.util.RuleUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;
import static com.comcast.xconf.validators.firmware.FirmwareConfigValidationUtils.configUsedInAction;


@Service
public class FirmwareConfigQueriesService {

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private FirmwareConfigPredicates firmwareConfigPredicates;

    @Autowired
    private FirmwareRuleDataService firmwareRuleDataService;

    @Autowired
    private ActivationVersionDataService activationVersionService;

    public static final String EXISTED_VERSIONS = "existedVersions";
    public static final String NOT_EXISTED_VERSIONS = "notExistedVersions";

    public List<FirmwareConfig> getAll() {
        return firmwareConfigDAO.getAll();
    }

    public FirmwareConfig getById(String id) {
        FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne(id);
        if (firmwareConfig == null) {
            throw new EntityNotFoundException("FirmwareConfig with id does not exist");
        }
        return firmwareConfig;
    }

    public List<FirmwareConfig> getFirmwareConfigsByModelIdsAndApplication(Set<String> modelIds, String applicationType) {
        if (CollectionUtils.isEmpty(modelIds)) {
            return new ArrayList<>();
        }
        return firmwareConfigDAO.getAll()
                .stream()
                .filter(firmwareConfigPredicates.byModelIds(modelIds))
                .filter(byApplication(applicationType))
                .collect(Collectors.toList());
    }

    public Map<String, Set<String>> getSortedFirmwareVersionsIfDoesExistOrNot(FirmwareConfigData firmwareConfigData,
                                                                              String applicationType) {
        if (CollectionUtils.isEmpty(firmwareConfigData.getFirmwareVersions()) || CollectionUtils.isEmpty(firmwareConfigData.getModels())) {
            return new HashMap<>();
        }
        List<FirmwareConfig> firmwareConfigsByModel = getFirmwareConfigsByModelIdsAndApplication(firmwareConfigData.getModels(), applicationType);
        Set<String> existedVersions = new HashSet<>();
        Set<String> notExistedVersions = new HashSet<>();
        for (String firmwareVersion : firmwareConfigData.getFirmwareVersions()) {
            if(containsVersion(firmwareConfigsByModel, firmwareVersion)) {
                existedVersions.add(firmwareVersion);
            } else {
                notExistedVersions.add(firmwareVersion);
            }
        }
        Map<String, Set<String>> firmwareVersionMap = new HashedMap();
        firmwareVersionMap.put(EXISTED_VERSIONS, existedVersions);
        firmwareVersionMap.put(NOT_EXISTED_VERSIONS, notExistedVersions);
        return firmwareVersionMap;
    }

    public void deleteOne(String id) {
        if(StringUtils.isBlank(id)) {
            throw new ValidationRuntimeException("Id is empty");
        }
        FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne(id);
        if (Objects.isNull(firmwareConfig)) {
            throw new EntityNotFoundException("FirmwareConfig with id does not exist");
        }
        validateUsage(id);
        firmwareConfigDAO.deleteOne(id);
    }

    public void validateUsage(String id) {
        if (StringUtils.isNotBlank(id)) {
            FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne(id);
            for (FirmwareRule entity : firmwareRuleDataService.getAll()) {
                if (configUsedInAction(firmwareConfig, entity)) {
                    throw new EntityConflictException("FirmwareConfig is used by " + entity.getName() + " FirmwareRule");
                }
            }
            for (ActivationVersion activationVersion : activationVersionService.getAll()) {
                if (activationVersion.getFirmwareVersions().contains(firmwareConfig.getFirmwareVersion())) {
                    throw new EntityConflictException("FirmwareConfig is used by " + activationVersion.getDescription() + " Activation Version");
                }
            }
        }
    }

    private boolean containsVersion(List<FirmwareConfig> configs, String version) {
        for (FirmwareConfig config : configs) {
            if (config.getFirmwareVersion().equals(version)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidFirmwareConfigByModelIds(Set<String> modelIds, FirmwareConfig firmwareConfig, String applicationType) {
        return getFirmwareConfigsByModelIdsAndApplication(modelIds, applicationType).contains(firmwareConfig);
    }

    public boolean validateName(FirmwareConfig config, String applicationType) {
        for (FirmwareConfig existedFirmwareConfig : firmwareConfigDAO.getAll()) {
            if (existedFirmwareConfig.getDescription().equalsIgnoreCase(config.getDescription())
                    && ApplicationType.equals(existedFirmwareConfig.getApplicationType(), applicationType)) {
                return false;
            }
        }
        return true;
    }

    public Set<FirmwareConfig> getFirmwareVersions(String envModelName) {
        Set<FirmwareConfig> versions = new HashSet<>();
        String model = null;
        List<FirmwareRule> all = firmwareRuleDao.getAll();
        for (FirmwareRule rule : all) {
            if (TemplateNames.ENV_MODEL_RULE.equals(rule.getType()) && rule.getName().equals(envModelName)) {
                model = extractModel(rule);
            }
        }

        for (FirmwareConfig config : firmwareConfigDAO.getAll()) {
            Set<String> supportedModels = config.getSupportedModelIds();
            if (model != null && supportedModels != null
                    && supportedModels.contains(model)) {
                versions.add(config);
            }
        }
        return versions;
    }

    private String extractModel(FirmwareRule rule) {
        for (Condition condition : RuleUtil.toConditions(rule.getRule())) {
            if (RuleFactory.MODEL.equals(condition.getFreeArg())) {
                return (String) condition.getFixedArg().getValue();
            }
        }
        return null;
    }

    public List<FirmwareConfig> getByApplicationType(final String applicationType) {
        return firmwareConfigDAO.getAll()
                .stream()
                .filter(byApplication(applicationType))
                .collect(Collectors.toList());
    }
}
