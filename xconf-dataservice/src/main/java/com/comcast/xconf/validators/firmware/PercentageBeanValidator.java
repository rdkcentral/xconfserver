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
 * Author: Igor Kostrov
 * Created: 12/9/2016
*/
package com.comcast.xconf.validators.firmware;

import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.comcast.xconf.util.RuleUtil.getRuleString;

@Component
public class PercentageBeanValidator implements IValidator<PercentageBean> {

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private FirmwarePermissionService permissionService;

    @Override
    public void validate(PercentageBean percentage) {
        if (StringUtils.isBlank(percentage.getName())) {
            throw new ValidationRuntimeException("Name could not be blank");
        }
        if (StringUtils.isBlank(percentage.getModel())) {
            throw new ValidationRuntimeException("Model could not be blank");
        }
        if (percentage.getOptionalConditions() != null) {
            validateEnvModelConditions(percentage.getOptionalConditions());
        }
        validateApplicationType(percentage);
        Set<String> firmwareVersions = percentage.getFirmwareVersions();
        if (percentage.isFirmwareCheckRequired() && CollectionUtils.isEmpty(firmwareVersions)) {
            throw new ValidationRuntimeException("Please select at least one version or disable firmware check");
        }
        validateDistributionDuplicates(percentage.getDistributions());
        float totalPercentage = 0;
        for (RuleAction.ConfigEntry entry : percentage.getDistributions()) {
            validatePercentageRange(entry.getPercentage(), "Percentage");
            validateDistributionOverlapping(entry, percentage.getDistributions());
            if (entry.getStartPercentRange() != null) {
                validatePercentageRange(entry.getStartPercentRange(), "StartPercentRange");
            }
            if (entry.getEndPercentRange() != null) {
                validatePercentageRange(entry.getEndPercentRange(), "EndPercentRange");
            }
            if (entry.getStartPercentRange() != null && entry.getEndPercentRange() != null
                    && entry.getStartPercentRange() >= entry.getEndPercentRange()) {
                throw new ValidationRuntimeException("StartPercentRange should be less than EndPercentRange");
            }

            FirmwareConfig config = firmwareConfigDAO.getOne(entry.getConfigId(), false);
            if (config == null) {
                throw new EntityNotFoundException("FirmwareConfig with id " + entry.getConfigId() + " does not exist");
            }
            if (!firmwareVersions.contains(config.getFirmwareVersion()) && percentage.isFirmwareCheckRequired()) {
                throw new ValidationRuntimeException("Distribution version should be selected in MinCheck list");
            }
            validateFirmwareConfigApplicationType(config.getApplicationType(), percentage.getApplicationType());
            totalPercentage += entry.getPercentage();
        }
        if (totalPercentage > 100) {
            throw new ValidationRuntimeException("Distribution total percentage > 100");
        }


        String lastKnownGoodConfigId = percentage.getLastKnownGood();
        if (StringUtils.isNotBlank(lastKnownGoodConfigId)) {
            FirmwareConfig lkgConfig = firmwareConfigDAO.getOne(lastKnownGoodConfigId, false);
            if (lkgConfig == null) {
                throw new EntityNotFoundException("LastKnownGood: config with id " + lastKnownGoodConfigId + " does not exist");
            }
            validateFirmwareConfigApplicationType(lkgConfig.getApplicationType(), percentage.getApplicationType());
            if (!percentage.getFirmwareVersions().contains(lkgConfig.getFirmwareVersion())) {
                throw new ValidationRuntimeException("LastKnownGood should be selected in min check list");
            }
            if (Math.abs(totalPercentage - 100.0) < 1.0e-8) {
                throw new ValidationRuntimeException("Can't set LastKnownGood when percentage=100");
            }
        }
        if (percentage.isActive() && CollectionUtils.isNotEmpty(percentage.getDistributions()) && totalPercentage < 100 && StringUtils.isBlank(lastKnownGoodConfigId)) {
            throw new ValidationRuntimeException("LastKnownGood is required when percentage < 100");
        }
        String intermediateVersionConfigId = percentage.getIntermediateVersion();
        if (StringUtils.isNotBlank(intermediateVersionConfigId)) {
            FirmwareConfig intermediateConfig = firmwareConfigDAO.getOne(intermediateVersionConfigId, false);
            if (intermediateConfig == null) {
                throw new EntityNotFoundException("IntermediateVersion: config with id " + intermediateVersionConfigId + " does not exist");
            }
            validateFirmwareConfigApplicationType(intermediateConfig.getApplicationType(), percentage.getApplicationType());
            if (!percentage.isFirmwareCheckRequired()) {
                throw new ValidationRuntimeException("Can't set IntermediateVersion when firmware check is disabled");
            }
        }
    }

    private void validateDistributionDuplicates(List<RuleAction.ConfigEntry> distributions) {
        List<RuleAction.ConfigEntry> newDistributions = new ArrayList<>(distributions);
        Iterator<RuleAction.ConfigEntry> distributionsIterator = newDistributions.iterator();
        while (distributionsIterator.hasNext()) {
            RuleAction.ConfigEntry configEntry = distributionsIterator.next();
            distributionsIterator.remove();
            if (newDistributions.contains(configEntry)) {
                throw new ValidationRuntimeException("Distributions contain duplicates");
            }
        }
    }

    private void validateDistributionOverlapping(RuleAction.ConfigEntry distributionToCheck, List<RuleAction.ConfigEntry> distributions) {
        for (RuleAction.ConfigEntry distribution : distributions) {
            if (distributionToCheck != null && !distributionToCheck.equals(distribution)
                    && isNotBlankDistributionPercentages(distributionToCheck) && isNotBlankDistributionPercentages(distribution)
                    && distributionToCheck.getStartPercentRange() <= distribution.getStartPercentRange()
                    && distribution.getStartPercentRange() < distributionToCheck.getEndPercentRange()) {
                throw new ValidationRuntimeException("Distributions overlap each other");
            }
        }
    }

    private void validateEnvModelConditions(Rule optionalCondition) {
        for (Condition condition : RuleUtil.toConditions(optionalCondition)) {
            if (RuleFactory.ENV.equals(condition.getFreeArg()) || RuleFactory.MODEL.equals(condition.getFreeArg())) {
                throw new ValidationRuntimeException("Optional condition should not contain " + condition.getFreeArg().getName());
            }
        }

    }

    private boolean isNotBlankDistributionPercentages(RuleAction.ConfigEntry distribution) {
        return distribution != null && distribution.getStartPercentRange() != null && distribution.getEndPercentRange() != null;
    }

    @Override
    public void validateAll(PercentageBean percentageBean, Iterable<PercentageBean> existingEntities) {
        String environment = percentageBean.getEnvironment();
        String model = percentageBean.getModel();
        for (PercentageBean old : existingEntities) {
            if (percentageBean.getId().equals(old.getId())) {
                continue;
            }
            if (StringUtils.equalsIgnoreCase(percentageBean.getName(), old.getName())) {
                throw new EntityConflictException("This name " + percentageBean.getName() + " is already used");
            }
            if (StringUtils.equalsIgnoreCase(environment, old.getEnvironment())
                    && StringUtils.equalsIgnoreCase(model, old.getModel())
                    && RuleUtil.equalComplexRules(percentageBean.getOptionalConditions(), old.getOptionalConditions())) {
                throw new EntityConflictException("PercentageBean already exists with such env/model pair "
                        + environment + "/" + model + " and optional condition " + getRuleString(percentageBean.getOptionalConditions()));
            }
        }
    }

    public void validateApplicationType(PercentageBean percentageBean) {
        PermissionHelper.validateWrite(permissionService, percentageBean.getApplicationType());
    }

    private void validateFirmwareConfigApplicationType(String configApplicationType, String beanApplicationType) {
        if (!ApplicationType.equals(configApplicationType, beanApplicationType)) {
            throw new ValidationRuntimeException("ApplicationTypes of FirmwareConfig and PercentageBean do not match");
        }
    }

    private void validatePercentageRange(Double value, String name) {
        if (String.valueOf(value).contains("-")) {
            throw new ValidationRuntimeException(name + " filter contains negative value");
        }
        if (value < 0 || value > 100) {
            throw new ValidationRuntimeException(name + " should be within [0, 100]");
        }
    }
}
