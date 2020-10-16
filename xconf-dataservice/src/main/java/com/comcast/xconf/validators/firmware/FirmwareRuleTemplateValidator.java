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
 *  Created: 3:58 PM
 */
package com.comcast.xconf.validators.firmware;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.DefinePropertiesTemplateAction;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.BaseRuleValidator;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FirmwareRuleTemplateValidator extends BaseRuleValidator<FirmwareRuleTemplate> {

    @Override
    public void validate(FirmwareRuleTemplate template) {
        if (template == null) {
            throw new ValidationRuntimeException("FirmwareRuleTemplate is not specified");
        }
        validateRule(template);
        validateApplicationAction(template);
    }

    private void validateRule(FirmwareRuleTemplate template) {
        List<Condition> conditions = RuleUtil.toConditions(template.getRule());
        if (CollectionUtils.isEmpty(conditions)) {
            throw new RuleValidationException("FirmwareRuleTemplate " + template.getId() + " should have as minimum one condition");
        }
        validateRuleStructure(template.getRule());
        for (final Condition condition : conditions) {
            checkOperationName(condition);
        }
        validateRelation(template.getRule());
        checkDuplicateConditions(template.getRule());
        validateProperties(template.getApplicableAction());
    }

    private void validateProperties(ApplicableAction applicableAction) {
        ApplicableAction.Type type = applicableAction.getActionType();
        if (ApplicableAction.Type.DEFINE_PROPERTIES_TEMPLATE.equals(type)) {
            Map<String, DefinePropertiesTemplateAction.PropertyValue> properties = ((DefinePropertiesTemplateAction) applicableAction).getProperties();
            for (Map.Entry<String, DefinePropertiesTemplateAction.PropertyValue> entry : properties.entrySet()) {
                if (StringUtils.isBlank(entry.getKey())) {
                    throw new RuleValidationException("Properties key is blank");
                }
            }
        }
    }

    private void validateApplicationAction(FirmwareRuleTemplate template) {
        if (template.getApplicableAction() == null) {
            throw new ValidationRuntimeException("ApplicationAction is required");
        }
        if (!getAllowedActionTypes().contains(template.getApplicableAction().getActionType())) {
            throw new ValidationRuntimeException(template.getApplicableAction().getActionType() + " action type is not supported by template");
        }
    }

    @Override
    public List<Operation> getAllowedOperations() {
        List<Operation> operations = super.getAllowedOperations();
        operations.add(StandardOperation.IN);
        operations.add(StandardOperation.GTE);
        operations.add(StandardOperation.LTE);
        operations.add(StandardOperation.EXISTS);
        operations.add(RuleFactory.MATCH);
        return operations;
    }

    private List<ApplicableAction.Type> getAllowedActionTypes() {
        return Lists.newArrayList(ApplicableAction.Type.RULE_TEMPLATE, ApplicableAction.Type.DEFINE_PROPERTIES_TEMPLATE, ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE);
    }
}