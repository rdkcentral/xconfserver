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
 *  Created: 6:18 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.xconf.thucydides.pages.firmware.PercentFilterEditPage;
import net.thucydides.core.annotations.Step;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;

public class PercentFilterEditPageSteps {

    private PercentFilterEditPage page;

    @Step
    public PercentFilterEditPageSteps typeEnvModelPercentage(String percentage) {
        page.typeEnvModelPercentage(percentage);
        return this;
    }

    @Step
    public PercentFilterEditPageSteps selectEnvModelWhitelist(String whitelist) {
        page.selectEnvModelWhitelist(whitelist);
        return this;
    }

    @Step
    public PercentFilterEditPageSteps selectLastKnownGood(String lkg) {
        page.selectLastKnownGood(lkg);
        return this;
    }

    @Step
    public PercentFilterEditPageSteps verifyLastKnownGoodDisabledAndEmpty() {
        Assert.assertFalse(page.isLastKnownGoodEnabled());
        Assert.assertTrue(page.getLastKnownGood().isEmpty());
        return this;
    }

    @Step
    public PercentFilterEditPageSteps clickIsActive() {
        page.clickIsActive();
        return this;
    }

    @Step
    public PercentFilterEditPageSteps clickFirmwareVersionCheckRequired() {
        page.clickFirmwareVersionCheckRequired();
        return this;
    }

    @Step
    public PercentFilterEditPageSteps clickRebootImmediately() {
        page.clickRebootImmediately();
        return this;
    }

    @Step
    public PercentFilterEditPageSteps clickFirmwareVersion() {
        page.clickFirmwareVersion();
        return this;
    }

    @Step
    public PercentFilterEditPageSteps clickFirmwareVersion(String firmwareVersion) {
        page.clickFirmwareVersion(firmwareVersion);
        return this;
    }

    @Step
    public PercentFilterEditPageSteps selectIntermediateVersion(String intermediateVersion) {
        page.selectIntermediateVersion(intermediateVersion);
        return this;
    }

    @Step
    public PercentFilterEditPageSteps verifyFirmwareVersionsErrorMessage(String expectedMessage) {
        assertEquals(expectedMessage, page.getFirmwareVersionsErrorMessage());
        return this;
    }

    @Step
    public PercentFilterEditPageSteps verifySelectedFirmwareVersion(String firmwareVersion, boolean isSelected) {
        assertEquals(isSelected, page.isFirmwareVersionSelected(firmwareVersion));
        return this;
    }
}
