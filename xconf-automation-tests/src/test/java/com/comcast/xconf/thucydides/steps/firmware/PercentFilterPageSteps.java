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
 * Created: 3/17/16  5:56 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.xconf.thucydides.pages.firmware.PercentFilterPage;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PercentFilterPageSteps {

    private PercentFilterPage page;

    @Step
    public PercentFilterPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyPercent(Integer value) {
        assertEquals(value.toString(), page.getPercent());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyWhitelist(String value) {
        assertEquals(value, page.getWhitelist());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyPercentIsDisabled() {
        assertTrue(page.percentIsDisabled());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyWhiteListIsDisabled() {
        assertTrue(page.whitelistIsDisabled());
        return this;
    }

    @Step
    public PercentFilterPageSteps typePercentage(String percentage) {
        page.typePercentage(percentage);
        return this;
    }

    @Step
    public PercentFilterPageSteps clickWhitelistSelect(String whitelist) {
        page.selectWhitelist(whitelist);
        return this;
    }

    @Step
    public PercentFilterPageSteps clickEnvModelEditButton() {
        page.clickEnvModelEditButton();
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRuleName(String name) {
        assertEquals(name, page.getEnvModelRuleName());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRulePercentage(Integer percentage) {
        assertEquals(percentage.toString(), page.getEnvModelRulePercentage());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRuleWhitelist(String whitelistId) {
        assertEquals(whitelistId, page.getEnvModelRuleWhitelist());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRuleActive(boolean isEnvModelRuleActive) {
        assertEquals(String.valueOf(isEnvModelRuleActive), page.isEnvModelRuleActive());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRuleFirmwareVersionCheck(boolean isFirmwareVersionCheckRequired) {
        assertEquals(String.valueOf(isFirmwareVersionCheckRequired), page.isEnvModelRuleFirmwareCheckRequired());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRuleImmediately(boolean isEnvModelRuleImmediately) {
        assertEquals(String.valueOf(isEnvModelRuleImmediately), page.isEnvModelRuleRebootImmediately());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelFirmwareVersionListItem(String envModelRuleFirmwareVersion) {
        assertEquals(envModelRuleFirmwareVersion, page.getEnvModelRuleFimwareVersionListItem());
        return this;
    }

    @Step
    public PercentFilterPageSteps clickEnvModelPercentageViewModalCloseButton() {
        page.clickEnvModelRuleViewModalCloseButton();
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRuleNameTd(String name) {
        assertEquals(name, page.getEnvModelRuleNameTd());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRuleWhitelistTd(String whitelist) {
        assertEquals(whitelist, page.getEnvModelRuleWhitelistTd());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRulePercentageTd(Integer percentage) {
        assertEquals(percentage.toString(), page.getEnvModelRulePercentageTd());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRuleActiveTd(boolean isActive) {
        assertEquals(isActive, page.isEnvModelRuleActiveTd());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyEnvModelRuleFirmwareCheckRequiredTd(boolean isFirmwareCheckRequired) {
        assertEquals(isFirmwareCheckRequired, page.isEnvModelRuleFirmwareCheckRequiredTd());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyLastKnownGood(String expectedLastKnownGoodVersion) {
        assertEquals(expectedLastKnownGoodVersion, page.getLastKnowGood());
        return this;
    }

    @Step
    public PercentFilterPageSteps verifyIntermediateVersion(String expectedIntermediateVersion) {
        assertEquals(expectedIntermediateVersion, page.getIntermediateVersion());
        return this;
    }
}
