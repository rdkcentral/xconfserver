/* 
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
 *
 * Author: Stanislav Menshykov
 * Created: 2/17/16  11:24 AM
 */
package com.comcast.xconf.admin.service.firmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.admin.validator.firmware.FirmwareConfigValidator;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.FirmwareConfigData;
import com.comcast.xconf.estbfirmware.FirmwareConfigQueriesService;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.firmware.FirmwareConfigPredicates;
import com.comcast.xconf.service.firmware.ActivationVersionService;
import com.comcast.xconf.service.firmware.FirmwareRuleService;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;

@Service
public class FirmwareConfigService extends AbstractApplicationTypeAwareService<FirmwareConfig> {

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private FirmwareConfigValidator firmwareConfigValidator;

    @Autowired
    private FirmwareRuleService firmwareRuleService;

    @Autowired
    private FirmwareConfigPredicates firmwareConfigPredicates;

    @Autowired
    private FirmwarePermissionService permissionService;

    @Autowired
    private ActivationVersionService activationVersionService;

    @Autowired
    private FirmwareConfigQueriesService firmwareConfigQueriesService;

    @Override
    public CachedSimpleDao getEntityDAO() {
        return firmwareConfigDAO;
    }

    @Override
    public IValidator getValidator() {
        return firmwareConfigValidator;
    }

    @Override
    public void normalizeOnSave(FirmwareConfig firmwareConfig) {
        normalizeModelIds(firmwareConfig);
    }

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    public List<FirmwareConfig> getFirmwareConfigsByModel(String modelId) {
        List<FirmwareConfig> result = new ArrayList<>();
        if (!StringUtils.isBlank(modelId)) {
            for (FirmwareConfig firmware : getAll()) {
                if (firmware.getSupportedModelIds().contains(modelId)) {
                    result.add(firmware);
                }
            }
        }

        return result;
    }

    public Set<FirmwareConfig> getSupportedConfigsByEnvModelRuleName(String envModelName) {
        Set<FirmwareConfig> versions = new HashSet<>();
        String model = null;
        List<FirmwareRule> firmwareRules = firmwareRuleService.getAll();
        for (FirmwareRule rule : firmwareRules) {
            if (TemplateNames.ENV_MODEL_RULE.equals(rule.getType()) && rule.getName().equals(envModelName)) {
                model = extractModel(rule);
            }
        }

        for (FirmwareConfig config : getAll()) {
            Set<String> supportedModels = config.getSupportedModelIds();
            if (model != null && supportedModels != null && supportedModels.contains(model)) {
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

    private void normalizeModelIds(FirmwareConfig config) {
        Set<String> normalizedModelIds = new HashSet<>();
        for (String modelId : config.getSupportedModelIds()) {
            normalizedModelIds.add(modelId.toUpperCase());
        }

        config.setSupportedModelIds(normalizedModelIds);
    }

    @Override
    public void validateUsage(String id) {
        if (StringUtils.isNotBlank(id)) {
            FirmwareConfig firmwareConfig = getOne(id);
            for (FirmwareRule entity : firmwareRuleService.getAll()) {
                if (configUsedInAction(firmwareConfig, entity)) {
                    throw new EntityConflictException("FirmwareConfig is used by " + entity.getName() + " firmware rule");
                }
            }
            for (ActivationVersion activationVersion : activationVersionService.getAll()) {
                if (activationVersion.getFirmwareVersions().contains(firmwareConfig.getFirmwareVersion())) {
                    throw new EntityConflictException("FirmwareConfig is used by " + activationVersion.getDescription() + " Activation Version");
                }
            }
        }
    }

    public boolean configUsedInAction(FirmwareConfig firmwareConfig, FirmwareRule rule) {
        String id = firmwareConfig.getId();
        if (rule != null && rule.getApplicableAction() != null && (rule.getApplicableAction() instanceof RuleAction)) {
            RuleAction action = (RuleAction) rule.getApplicableAction();
            if (id.equals(action.getConfigId())) {
                return true;
            }
            List<RuleAction.ConfigEntry> configEntries = action.getConfigEntries();
            if (configEntries != null) {
                for (RuleAction.ConfigEntry entry : configEntries) {
                    if (id.equals(entry.getConfigId())) {
                        return true;
                    }
                }
            }
            if (StringUtils.equals(id, action.getIntermediateVersion())
                    || CollectionUtils.isNotEmpty(action.getFirmwareVersions()) && action.getFirmwareVersions().contains(firmwareConfig.getFirmwareVersion())) {
                return true;
            }
        }
        return false;
    }

    public FirmwareConfig getFirmwareConfigByEnvModelRuleName(String envModelRuleName) {
        List<FirmwareRule> firmwareRules = firmwareRuleService.getAll();
        for (FirmwareRule rule : firmwareRules) {
            if (TemplateNames.ENV_MODEL_RULE.equals(rule.getType())
                    && rule.getName().equals(envModelRuleName)
                    && rule.getApplicableAction() instanceof RuleAction) {
                RuleAction ruleAction = (RuleAction) rule.getApplicableAction();
                if (StringUtils.isNotBlank(ruleAction.getConfigId())) {
                    return firmwareConfigDAO.getOne(ruleAction.getConfigId());
                }
            }
        }
        return null;
    }

    @Override
    protected List<Predicate<FirmwareConfig>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());
        return firmwareConfigPredicates.getPredicates(contextOptional);
    }

    public Map<String, FirmwareConfig> getFirmwareConfigMap() {
        Map<String, FirmwareConfig> firmwareConfigs = new HashMap<>();
        for (FirmwareConfig firmwareConfig : firmwareConfigDAO.getAll()) {
            firmwareConfigs.put(firmwareConfig.getId(), firmwareConfig);
        }
        return firmwareConfigs;
    }

    public List<FirmwareConfig> getFirmwareConfigsBySupportedModels(Set<String> modelIds) {
        return firmwareConfigQueriesService.getFirmwareConfigsByModelIdsAndApplication(modelIds, permissionService.getReadApplication());
    }

    public Map<String, Set<String>> getSortedFirmwareVersionsIfDoesExistOrNot(FirmwareConfigData firmwareConfigData) {
        return firmwareConfigQueriesService.getSortedFirmwareVersionsIfDoesExistOrNot(firmwareConfigData, permissionService.getReadApplication());
    }
}
