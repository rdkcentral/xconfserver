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
 * Created: 3/29/16  11:44 AM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.thucydides.pages.dcm.DcmSettingsPageObjects;
import com.comcast.xconf.thucydides.steps.RuleViewSteps;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.Steps;

public class GenericDcmSettingsSteps {

    private DcmSettingsPageObjects page;

    @Steps
    FormulaViewSteps formulaViewSteps;
    @Steps
    RuleViewSteps ruleViewSteps;

    @Step
    public GenericDcmSettingsSteps clickViewFormulaButton() {
        page.clickViewFormulaButton();
        return this;
    }

    @Step
    public GenericDcmSettingsSteps viewFormula(DCMGenericRule expectedFormula) {
        Condition expectedCondition = expectedFormula.getRule().getCondition();
        formulaViewSteps.verifyName(expectedFormula.getName())
                .verifyDescription(expectedFormula.getDescription())
                .verifyPriority(expectedFormula.getPriority())
                .verifyPercentage(expectedFormula.getPercentage())
                .verifyL1Percentage(expectedFormula.getPercentageL1())
                .verifyL2Percentage(expectedFormula.getPercentageL2())
                .verifyL3Percentage(expectedFormula.getPercentageL3());
        ruleViewSteps.verifyFreeArg(expectedCondition.getFreeArg().getName())
                .verifyOperation(expectedCondition.getOperation().toString())
                .verifyFixedArg(expectedCondition.getFixedArg().getValue().toString());
        return this;
    }
}
