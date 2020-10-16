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
 * Created: 3/21/16  5:11 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.xconf.thucydides.pages.firmware.ReportPage;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class ReportPageSteps {

    private ReportPage page;

    @Step
    public ReportPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public ReportPageSteps selectMacRule() {
        page.clickMacRulesListItem();
        return this;
    }

    @Step
    public ReportPageSteps verifyMacRuleIsSelected(Boolean isSelected) {
        assertEquals(isSelected, page.macRulesListItemIsSelected());
        return this;
    }

    @Step
    public ReportPageSteps clickUncheckAllButton() {
        page.clickUncheckAllButton();
        return this;
    }

    @Step
    public ReportPageSteps clickCheckAllButton() {
        page.clickCheckAllButton();
        return this;
    }

    @Step
    public ReportPageSteps clickGetReportButton() {
        page.clickGetReportButton();
        return this;
    }
}
