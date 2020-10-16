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
 * Created: 09.11.15  13:16
 */
package com.comcast.xconf.admin.validator.dcm;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionHelper;
import com.comcast.xconf.validators.BaseRuleValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FormulaValidator extends BaseRuleValidator<DCMGenericRule> {

    @Autowired
    private DcmPermissionService permissionService;

    @Override
    public void validate(DCMGenericRule entity) {
        validateApplicationType(entity);

        String msg = validateProperties(entity);
        if (msg != null) {
            throw new ValidationRuntimeException(msg);
        }
        super.validate(entity);
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

    protected String validateProperties(DCMGenericRule formula) {
        if (formula == null) {
            return "Formula is not present";
        }
        if (StringUtils.isBlank(formula.getName())) {
            return "Formula name must not be empty";
        }

        if (CollectionUtils.isEmpty(formula.getCompoundParts()) && formula.getCondition() == null) {
            return "Formula must contain condition";
        }

        int p1 = formula.getPercentageL1() != null ? formula.getPercentageL1() : 0;
        int p2 = formula.getPercentageL2() != null ? formula.getPercentageL2() : 0;
        int p3 = formula.getPercentageL3() != null ? formula.getPercentageL3() : 0;

        if (p1 < 0 || p2 < 0 || p3 < 0) {
            return "Percentage must be in range from 0 to 100";
        }
        int sum = p1 + p2 + p3;
        if (sum > 100 || sum < 0) {
            return "Total Level percentage sum must be in range from 0 to 100";
        }

        if (formula.getPercentage() != null &&
                (formula.getPercentage() < 0 || formula.getPercentage() > 100)) {
            return "Percentage must be in range from 0 to 100";
        }

        return null;
    }

    private void validateApplicationType(DCMGenericRule formula) {
        PermissionHelper.validateWrite(permissionService, formula.getApplicationType());
    }
}
