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
package com.comcast.xconf.validators.firmware;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.ConditionInfo;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ClientConnectionType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.BaseRuleValidator;
import com.comcast.xconf.validators.CommonRuleValidator;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.comcast.xconf.validators.CommonRuleValidator.getConditionInfos;

@Component
public class FirmwareRuleValidator extends BaseRuleValidator<FirmwareRule> {

    @Autowired
    private ApplicableActionValidator applicableActionValidator;

    @Autowired
    private TemplateConsistencyValidator templateConsistencyValidator;

    @Autowired
    private FirmwarePermissionService permissionService;

    @Override
    public void validate(FirmwareRule firmwareRule) {
        if (firmwareRule == null) {
            throw new ValidationRuntimeException("FirmwareRule is empty");
        }
        if (StringUtils.isBlank(firmwareRule.getName())) {
            throw new ValidationRuntimeException("Name is empty");
        }
        super.validate(firmwareRule);

        templateConsistencyValidator.validate(firmwareRule);

        checkFreeArgExists(firmwareRule);

        applicableActionValidator.validate(firmwareRule);

        validateApplicationType(firmwareRule);

        validateClientConnectionType(firmwareRule.getConnectionType());
    }

    private void validateClientConnectionType(ClientConnectionType connectionType) {
        if (Objects.isNull(connectionType)) {
            throw new ValidationRuntimeException("Client Connection Type is empty");
        }
    }

    @Override
    public List<Operation> getAllowedOperations() {
        List<Operation> operations = super.getAllowedOperations();
        operations.add(StandardOperation.IN);
        operations.add(StandardOperation.GTE);
        operations.add(StandardOperation.LTE);
        operations.add(StandardOperation.EXISTS);
        return operations;
    }

    public void checkFreeArgExists(FirmwareRule firmwareRule) {
        final String type = firmwareRule.getType();
        final Rule rule = firmwareRule.getRule();

        final List<Condition> conditions = Lists.newArrayList(RuleUtil.toConditions(rule));

        final Set<ConditionInfo> conditionInfos = getConditionInfos(conditions);

        if (equalTypes(TemplateNames.MAC_RULE, type)) {
            checkFreeArgExists(conditionInfos, RuleFactory.MAC);
        } else if (equalTypes(TemplateNames.IP_RULE, type)) {
            checkFreeArgExists(conditionInfos, RuleFactory.IP);
            checkFreeArgExists(conditionInfos, RuleFactory.ENV);
            checkFreeArgExists(conditionInfos, RuleFactory.MODEL);
        } else if (equalTypes(TemplateNames.ENV_MODEL_RULE, type)) {
            checkFreeArgExists(conditionInfos, RuleFactory.ENV);
            checkFreeArgExists(conditionInfos, RuleFactory.MODEL);
        } else if (equalTypes(TemplateNames.TIME_FILTER, type)) {
            CommonRuleValidator.checkFreeArgExists(conditionInfos, RuleFactory.LOCAL_TIME, StandardOperation.GTE);
            CommonRuleValidator.checkFreeArgExists(conditionInfos, RuleFactory.LOCAL_TIME, StandardOperation.LTE);
        } else if (equalTypes(TemplateNames.REBOOT_IMMEDIATELY_FILTER, type)) {
            checkRebootImmediatelyFilter(conditionInfos);
        } else if (equalTypes(TemplateNames.GLOBAL_PERCENT, type)) {
            CommonRuleValidator.checkFreeArgExists(conditionInfos, RuleFactory.MAC, StandardOperation.PERCENT);
        } else if (equalTypes(TemplateNames.IP_FILTER, type)) {
            checkFreeArgExists(conditionInfos, RuleFactory.IP);
        }
    }

    private void checkFreeArgExists(final Set<ConditionInfo> conditionInfos, final FreeArg freeArg) {
        if (!CommonRuleValidator.freeArgExists(conditionInfos, freeArg)) {
            throw new RuleValidationException(freeArg.getName() + " is required");
        }
    }

    private void checkRebootImmediatelyFilter(final Set<ConditionInfo> conditionInfos) {
        final boolean ipExists = CommonRuleValidator.freeArgExists(conditionInfos, RuleFactory.IP);
        final boolean macExists = CommonRuleValidator.freeArgExists(conditionInfos, RuleFactory.MAC);
        final boolean envExists = CommonRuleValidator.freeArgExists(conditionInfos, RuleFactory.ENV);
        final boolean modelExists = CommonRuleValidator.freeArgExists(conditionInfos, RuleFactory.MODEL);

        final boolean isValid = (ipExists || macExists) || (envExists && modelExists);

        if (!isValid) {
            throw new RuleValidationException("Need to set " + StbContext.IP_ADDRESS + " OR " + StbContext.ESTB_MAC +
                    " OR " + StbContext.ENVIRONMENT + " AND " + StbContext.MODEL);
        }
    }

    protected void validateApplicationType(FirmwareRule firmwareRule) {
        PermissionHelper.validateWrite(permissionService, firmwareRule.getApplicationType());
    }
}
