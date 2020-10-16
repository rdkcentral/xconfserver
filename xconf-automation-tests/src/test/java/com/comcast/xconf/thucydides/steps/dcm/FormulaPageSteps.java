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
 * <p>
 * Author: Stanislav Menshykov
 * Created: 3/22/16  1:37 PM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.xconf.thucydides.pages.dcm.FormulaPage;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FormulaPageSteps {

    private FormulaPage page;

    @Step
    public FormulaPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public FormulaPageSteps typeName(String value) {
        page.typeName(value);
        return this;
    }

    @Step
    public FormulaPageSteps typeDescription(String value) {
        page.typeDescription(value);
        return this;
    }

    @Step
    public FormulaPageSteps typePercentage(Integer value) {
        page.typePercentage(value);
        return this;
    }

    @Step
    public FormulaPageSteps verifyIsDefaultFormula(Boolean isDefault) {
        if (isDefault) {
            assertTrue(page.isDefaultFormula());
        } else {
            assertTrue(page.notDefaultFormula());
        }
        return this;
    }

    @Step
    public FormulaPageSteps verifyValidPriority(Integer value) {
        String selectedPriorityVisibleValue = page.getSelectedPriorityVisibleValue();
        assertEquals(value.toString(), selectedPriorityVisibleValue);
        return this;
    }

    @Step
    public FormulaPageSteps typeL1Percentage(Integer value) {
        page.typeL1Percentage(value);
        return this;
    }

    @Step
    public FormulaPageSteps typeL2Percentage(Integer value) {
        page.typeL2Percentage(value);
        return this;
    }

    @Step
    public FormulaPageSteps typeL3Percentage(Integer value) {
        page.typeL3Percentage(value);
        return this;
    }

    @Step
    public FormulaPageSteps clickEditDeviceSettingsButton() {
        page.clickEditDeviceSettingsButton();
        return this;
    }

    @Step
    public FormulaPageSteps clickCreateDeviceSettingsButton() {
        page.clickCreateDeviceSettingsButton();
        return this;
    }

    @Step
    public FormulaPageSteps clickEditLogUploadSettingsButton() {
        page.clickEditLogUploadSettingsButton();
        return this;
    }

    @Step
    public FormulaPageSteps clickCreateLogUploadSettingsButton() {
        page.clickCreateLogUploadSettingsButton();
        return this;
    }

    @Step
    public FormulaPageSteps clickEditVodSettingsButton() {
        page.clickEditVodSettingsButton();
        return this;
    }

    @Step
    public FormulaPageSteps clickCreateVodSettingsButton() {
        page.clickCreateVodSettingsButton();
        return this;
    }

    @Step
    public FormulaPageSteps clickViewDeviceSettingsButton() {
        page.clickViewDeviceSettingsButton();
        return this;
    }

    @Step
    public FormulaPageSteps clickViewVodSettingsButton() {
        page.clickViewVodSettingsButton();
        return this;
    }

    @Step
    public FormulaPageSteps clickViewLogUploadSettingsButton() {
        page.clickViewLogUploadSettingsButton();
        return this;
    }

    @Step
    public FormulaPageSteps typeSearchName(String searchName) {
        page.typeSearchName(searchName);
        return this;
    }

    @Step
    public FormulaPageSteps verifyFormulasCount(int expectedFormulasCount) {
        assertEquals(expectedFormulasCount, page.getFormulasCount());
        return this;
    }
}
