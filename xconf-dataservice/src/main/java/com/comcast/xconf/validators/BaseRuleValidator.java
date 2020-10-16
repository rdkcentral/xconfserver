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
 * Created: 2/3/16  3:13 PM
 */
package com.comcast.xconf.validators;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.XRule;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.comcast.xconf.util.IpAddressUtils;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class BaseRuleValidator<T extends XRule> implements IValidator<T> {

    @Override
    public void validate(T entity) {
        if (entity == null) {
            throw new RuleValidationException(T.Factory.class.getSimpleName() + " is empty");
        }
        if (entity.getRule() == null) {
            throw new RuleValidationException("Rule is empty");
        }
        validateRuleStructure(entity.getRule());
        runGlobalValidation(entity.getRule());
    }

    @Override
    public void validateAll(T ruleToCheck, Iterable<T> existingRule) {
        for (XRule rule : existingRule) {
            if (StringUtils.equals(rule.getId(), ruleToCheck.getId())) {
                continue;
            }
            if (StringUtils.equals(ruleToCheck.getName(), rule.getName())) {
                throw new EntityConflictException("Name is already used");
            }
            if (RuleUtil.equalComplexRules(ruleToCheck.getRule(), rule.getRule())) {
                throw new RuleValidationException("Rule has duplicate: " + rule.getName());
            }
        }
    }

    public void runGlobalValidation(final Rule rule) {
        if (rule == null) {
            throw new RuleValidationException("Rule is null");
        }
        final Collection<Condition> conditions = Lists.newArrayList(RuleUtil.toConditions(rule));
        if (CollectionUtils.isEmpty(conditions)) {
            throw new RuleValidationException("Rule is empty");
        }
        validateRelation(rule);
        checkDuplicateConditions(rule);
        for (final Condition condition : conditions) {
            checkConditionNullsOrBlanks(condition);
            checkDuplicateFixedArgListItems(condition);
            checkOperationName(condition);
            final FreeArg freeArg = condition.getFreeArg();
            if (equalFreeArgNames(StbContext.ESTB_MAC, freeArg.getName()) ||
                    equalFreeArgNames(LogUploaderContext.ESTB_MAC, freeArg.getName()) ||
                    equalFreeArgNames(LogUploaderContext.ECM_MAC, freeArg.getName())) {
                checkFixedArgValue(condition, new VoidCallback<String>() {
                    @Override
                    public void call(String input) {
                        if (!MacAddress.isValid(input)) {
                            throw new RuleValidationException("Mac Address is not valid: " + input);
                        }
                    }
                });
            } else if (equalFreeArgNames(StbContext.IP_ADDRESS, freeArg.getName()) ||
                        equalFreeArgNames(LogUploaderContext.ESTB_IP, freeArg.getName())) {
                checkFixedArgValue(condition, new VoidCallback<String>() {
                    @Override
                    public void call(String input) {
                        if (!IpAddressUtils.isValidIpAddress(input)) {
                            throw new RuleValidationException("Ip Address is not valid: " + input);
                        }
                    }
                });
            } else {
                checkFixedArgValue(condition, new VoidCallback<String>() {
                    @Override
                    public void call(String input) {
                        if (StringUtils.isEmpty(input)) {
                            throw new RuleValidationException("FixedArg is empty");
                        }
                    }
                });
            }
        }
    }

    protected void checkFixedArgValue(final Condition condition, final VoidCallback<String> callback) {
        final Operation operation = condition.getOperation();
        if (equalOperations(StandardOperation.IN, operation)) {
            final Collection<String> fixedArgValues = (Collection<String>) condition.getFixedArg().getValue();
            for (final String value: fixedArgValues) {
                callback.call(value); // this method throws exception if rule is not valid
            }
        } else if (equalOperations(StandardOperation.IS, operation)) {
            final String fixedArgValue = String.valueOf(condition.getFixedArg().getValue());
            callback.call(fixedArgValue);
        } else if (equalOperations(StandardOperation.PERCENT, operation)) {
            checkPercentOperation(condition);
        } else if (equalOperations(StandardOperation.LIKE, operation)) {
            checkLikeOperation(condition);
        }
    }

    protected void checkConditionNullsOrBlanks(final Condition condition) {
        if (condition == null) {
            throw new RuleValidationException("Condition is null");
        }
        final FreeArg freeArg = condition.getFreeArg();
        if (freeArg == null || StringUtils.isBlank(freeArg.getName())) {
            throw new RuleValidationException("FreeArg is empty");
        }

        final Operation operation = condition.getOperation();
        if (operation == null) {
            throw new RuleValidationException("Operation is null");
        }

        final FixedArg fixedArg = condition.getFixedArg();
        if (!StandardOperation.EXISTS.equals(operation)) {
            if (fixedArg == null || fixedArg.getValue() == null) {
                throw new RuleValidationException("FixedArg is null");
            }

            if (StandardOperation.IN.equals(condition.getOperation())) {
                if (!(fixedArg.getValue() instanceof Collection)) {
                    throw new RuleValidationException(freeArg.getName() + " is not collection");
                }
                if (CollectionUtils.isEmpty((Collection) fixedArg.getValue())) {
                    throw new RuleValidationException(freeArg.getName() + " is empty");
                }
            } else {
                if (StringUtils.isBlank(String.valueOf(fixedArg.getValue()))) {
                    throw new RuleValidationException(freeArg.getName() + " is empty");
                }
            }
        }
    }

    protected void checkPercentOperation(final Condition condition) {
        boolean isFixedArgValid = true;
        try {
            Double fixedArgDouble = Double.valueOf(String.valueOf(condition.getFixedArg().getValue()));
            if (fixedArgDouble < 0 || fixedArgDouble > 100) {
                isFixedArgValid = false;
            }
        } catch (Exception e) {
            isFixedArgValid = false;
        }
        if (!isFixedArgValid) {
            throw new RuleValidationException(condition.getFreeArg().getName() + " is not valid; 0.0 < value < 100.0");
        }
    }

    protected void checkLikeOperation(final Condition condition) {
        try {
            Pattern.compile(String.valueOf(condition.getFixedArg().getValue()));
        } catch (Exception e) {
            throw new RuleValidationException(condition.getFreeArg().getName() + " is not valid; " + e.getMessage());
        }
    }

    protected void checkOperationName(Condition condition) {
        boolean isExists = false;
        Operation ruleOperation = condition.getOperation();
        for (Operation operation : getAllowedOperations()) {
            if (equalOperations(operation, ruleOperation)) {
                isExists = true;
                break;
            }
        }

        if (!isExists) {
            throw new RuleValidationException("Operation is not valid: " + ruleOperation.toString());
        }
    }

    protected boolean equalTypes(final String type, final String type2) {
        return type.equals(type2);
    }

    protected boolean equalFreeArgNames(final String freeArg, final String freeArg2) {
        return freeArg.equals(freeArg2);
    }

    protected boolean equalOperations(final Operation operation, final Operation operation2) {
        return operation.equals(operation2);
    }

    protected List<Operation> getAllowedOperations() {
        List<Operation> operations = new ArrayList<>();
        operations.add(StandardOperation.IS);
        operations.add(StandardOperation.LIKE);
        operations.add(StandardOperation.EXISTS);
        operations.add(StandardOperation.PERCENT);
        operations.add(RuleFactory.IN_LIST);
        return operations;
    }

    private void checkDuplicateFixedArgListItems(Condition condition) {
        FixedArg fixedArg = condition.getFixedArg();
        if (fixedArg != null && fixedArg.getValue() != null) {
            Collection<String> duplicateFixedArgListItems = RuleUtil.getDuplicateFixedArgListItems(fixedArg.getValue());
            if (duplicateFixedArgListItems.size() > 0) {
                throw new RuleValidationException("FixedArg of condition: " + condition + " contains duplicate items: " + duplicateFixedArgListItems);
            }
        }
    }

    protected void checkDuplicateConditions(Rule rule) {
        assertDuplicateConditions(RuleUtil.getDuplicateConditions(rule));
        assertDuplicateConditions(RuleUtil.getDuplicateConditionsBetweenOR(rule));
    }

    private void assertDuplicateConditions(Collection<Condition> duplicateConditions) {
        if (duplicateConditions.size() > 0) {
            throw new RuleValidationException("Please, remove duplicate conditions first: " + duplicateConditions);
        }
    }

    protected void validateRelation(Rule rule) {
        if (rule.isCompound() && CollectionUtils.isNotEmpty(rule.getCompoundParts())) {
            for (int i = 1; i < rule.getCompoundParts().size(); i++) {
                Rule compoundPart = rule.getCompoundParts().get(i);
                if (compoundPart.getRelation() == null) {
                    throw new RuleValidationException("Relation of " + compoundPart.getCondition() + " is empty");
                }
            }
        }
    }

    public void validateRuleStructure(Rule rule) {
        if (!rule.isCompound() && CollectionUtils.isNotEmpty(rule.getCompoundParts())) {
            throw new RuleValidationException("Rule should have only condition or compoundParts field");
        }
        validateCompoundPartsTree(rule);
    }

    private void validateCompoundPartsTree(Rule rule) {
        if (CollectionUtils.isEmpty(rule.getCompoundParts())) {
            return;
        }
        for (Rule compoundPart : rule.getCompoundParts()) {
            if (CollectionUtils.isNotEmpty(compoundPart.getCompoundParts())) {
                throw new RuleValidationException("CompoundPart rule should not have one more compoundParts");
            }
        }
    }

    private interface VoidCallback<P> {

        void call(P var1);

    }
}
