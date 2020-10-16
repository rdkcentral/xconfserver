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
 *  Author: mdolina
 *  Created: 4:59 PM
 */
package com.comcast.xconf.admin.service.common;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.XRule;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.common.EnvModelPredicates;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.common.ModelValidator;
import com.google.common.base.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.OperationNotSupportedException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class ModelService extends AbstractService<Model> {

    @Autowired
    private CachedSimpleDao<String, Model> modelDAO;

    @Autowired
    private CachedSimpleDao<String, DCMGenericRule> dcmRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    @Autowired
    protected CachedSimpleDao<String, TelemetryRule> telemetryRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, FeatureRule> featureRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, SettingRule> settingRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private EnvModelPredicates<Model> envModelPredicates;

    @Autowired
    private ModelValidator validator;

    private static Logger logger = LoggerFactory.getLogger(ModelService.class);

    @Override
    public CachedSimpleDao<String, Model> getEntityDAO() {
        return modelDAO;
    }

    @Override
    public IValidator<Model> getValidator() {
        return validator;
    }

    @Override
    protected List<Predicate<Model>> getPredicatesByContext(Map<String, String> context) {
        return envModelPredicates.getPredicates(new ContextOptional(context));
    }

    @Override
    public void validateUsage(String id) {
        validateUsageByXRules(id);
        validateUsageByFirmwareConfig(id);
    }

    private void validateUsageByXRules(String modelId) {
        List<? extends CachedSimpleDao<String, ? extends IPersistable>> daoList = Arrays.asList(
                dcmRuleDAO, firmwareRuleDao, firmwareRuleTemplateDao, telemetryRuleDAO, featureRuleDAO, settingRuleDAO
        );
        for (CachedSimpleDao<String, ? extends IPersistable> dao : daoList) {
            for (IPersistable persistable : Optional.presentInstances(dao.asLoadingCache().asMap().values())) {
                XRule xRule = (XRule) persistable;
                if (RuleUtil.isExistConditionByFreeArgAndFixedArg(xRule.getRule(), RuleFactory.MODEL.getName(), modelId)) {
                    throw new EntityConflictException("Model " + modelId + " is used by " + xRule.getName() + " " + getUsedRuleType(xRule));
                }
            }
        }
    }

    private String getUsedRuleType(XRule xRule) {
        try {
            if (xRule instanceof FirmwareRule && TemplateNames.ACTIVATION_VERSION.equals(xRule.getTemplateId())) {
                return ActivationVersion.class.getSimpleName();
            }
        } catch (OperationNotSupportedException e) {
            logger.error("Exception:", e);
        }
        return xRule.getRuleType();
    }

    private void validateUsageByFirmwareConfig(String modelId) {
        for (FirmwareConfig firmwareConfig : firmwareConfigDAO.getAll()) {
            if (CollectionUtils.isNotEmpty(firmwareConfig.getSupportedModelIds()) && firmwareConfig.getSupportedModelIds().contains(modelId)) {
                throw new EntityConflictException("Model " + modelId + " is used by FirmwareConfig " + firmwareConfig.getDescription());
            }
        }
    }
}
