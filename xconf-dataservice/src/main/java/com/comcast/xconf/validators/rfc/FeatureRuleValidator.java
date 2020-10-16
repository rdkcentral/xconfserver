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
 * Author: Yury Stagit
 * Created: 11/06/16  12:00 PM
 */
package com.comcast.xconf.validators.rfc;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.rfc.PercentRange;
import com.comcast.xconf.service.rfc.FeatureRuleService;
import com.comcast.xconf.service.rfc.FeatureService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.utils.PercentRangeParser;
import com.comcast.xconf.validators.BaseRuleValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class FeatureRuleValidator extends BaseRuleValidator<FeatureRule> {

    @Autowired
    private FeatureService featureService;

    @Autowired
    private DcmPermissionService permissionService;

    @Autowired
    private FeatureRuleService featureRuleService;

    @Override
    public void validate(FeatureRule entity) {
        super.validate(entity);
        if (StringUtils.isBlank(entity.getName())) {
            throw new ValidationRuntimeException("Feature Rule name is blank");
        }
        if (CollectionUtils.isEmpty(entity.getFeatureIds())) {
            throw new ValidationRuntimeException("Features should be specified");
        } else if (entity.getFeatureIds().size() > featureRuleService.getAllowedNumberOfFeatures()) {
            throw new ValidationRuntimeException("Number of Features should be up to " + featureRuleService.getAllowedNumberOfFeatures() + " items");
        } else {
            for (String featureId : entity.getFeatureIds()) {
                featureService.getOne(featureId);
            }
        }
        validateApplicationType(entity);
        List<PercentRange> percentRanges = getPercentRanges(entity.getRule());
        for (PercentRange percentRange : percentRanges) {
            validateStartRange(percentRange.getStartRange());
            validateEndRange(percentRange.getEndRange());
            validateRanges(percentRange.getStartRange(), percentRange.getEndRange());
            validateRangesOverlapping(percentRange, percentRanges);
        }
    }

    protected void validateApplicationType(FeatureRule featureRule) {
        PermissionHelper.validateWrite(permissionService, featureRule.getApplicationType());
    }

    @Override
    public List<Operation> getAllowedOperations() {
        List<Operation> operations = super.getAllowedOperations();
        operations.add(StandardOperation.EXISTS);
        operations.add(RuleFactory.RANGE);
        return operations;
    }

    private void validateStartRange(Double startRange) {
        if (startRange == null || startRange < 0 || startRange >= 100) {
            throw new RuleValidationException("Start range " + startRange + " is not valid");
        }
    }

    private void validateEndRange(Double endRange) {
        if (endRange == null || endRange < 0 || endRange > 100) {
            throw new RuleValidationException("End range " + endRange + " is not valid");
        }
    }

    private void validateRanges(Double startRange, Double endRange) {
        if (startRange >= endRange) {
            throw new RuleValidationException("Start range should be less than end range");
        }
    }

    private void validateRangesOverlapping(PercentRange rangeToCheck, List<PercentRange> percentRanges) {
        for (PercentRange range : percentRanges) {
            if (rangeToCheck != null && !rangeToCheck.equals(range)
                    && rangeToCheck.getStartRange() <= range.getStartRange()
                    && range.getStartRange() < rangeToCheck.getEndRange()) {
                throw new RuleValidationException("Ranges overlap each other");
            }
        }
    }

    private List<PercentRange> getPercentRanges(Rule rule) {
        List<PercentRange> percentRanges = new ArrayList<>();
        for (Condition condition : RuleUtil.toConditions(rule)) {
            if (RuleFactory.RANGE.equals(condition.getOperation())) {
                PercentRange range = PercentRangeParser.parsePercentRangeCondition(condition);
                percentRanges.add(range);
            }
        }
        Collections.sort(percentRanges);
        return percentRanges;
    }
}
