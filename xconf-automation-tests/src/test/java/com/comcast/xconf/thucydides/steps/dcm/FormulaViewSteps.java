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
 * Created: 3/28/16  3:54 PM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.xconf.thucydides.pages.dcm.FormulaViewPageObjects;
import com.comcast.xconf.thucydides.util.PageObjectUtils;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.*;

public class FormulaViewSteps {

    private FormulaViewPageObjects page;

    @Step
    public FormulaViewSteps verifyName(String value) {
        assertEquals(value, page.getName());
        return this;
    }

    @Step
    public FormulaViewSteps verifyDescription(String value) {
        assertEquals(value, page.getDescription());
        return this;
    }

    @Step
    public FormulaViewSteps verifyPriority(Integer value) {
        assertEquals(value, Integer.valueOf(page.getPriority()));
        return this;
    }

    @Step
    public FormulaViewSteps verifyPercentage(Integer value) {
        assertEquals(value, Integer.valueOf(page.getPercentage()));
        return this;
    }

    @Step
    public FormulaViewSteps verifyL1Percentage(Integer value) {
        assertEquals(value, Integer.valueOf(page.getL1Percentage()));
        return this;
    }

    @Step
    public FormulaViewSteps verifyL2Percentage(Integer value) {
        assertEquals(value, Integer.valueOf(page.getL2Percentage()));
        return this;
    }

    @Step
    public FormulaViewSteps verifyL3Percentage(Integer value) {
        assertEquals(value, Integer.valueOf(page.getL3Percentage()));
        return this;
    }

//    @Step
//    public FormulaViewSteps verifyAllInputsAreDisabled() {
//        PageObjectUtils.verifyInputIsDisabled(page.);
//    }
}
