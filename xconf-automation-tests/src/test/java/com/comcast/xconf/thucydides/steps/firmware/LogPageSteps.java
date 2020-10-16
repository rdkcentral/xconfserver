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
 * Created: 3/18/16  4:48 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.thucydides.pages.firmware.LogPage;
import net.thucydides.core.annotations.Step;

import static com.comcast.xconf.thucydides.util.common.GenericNamespacedListUtils.defaultMacAddress;
import static com.comcast.xconf.thucydides.util.firmware.FirmwareConfigUtils.defaultVersion;
import static com.comcast.xconf.thucydides.util.firmware.FirmwareConfigUtils.defaultFileName;
import static org.junit.Assert.assertEquals;

public class LogPageSteps {

    private LogPage page;

    @Step
    public LogPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public LogPageSteps typeMacAddress(String value) {
        page.typeMacAddress(value);
        return this;
    }

    @Step
    public LogPageSteps clickTestButton() {
        page.clickTestButton();
        return this;
    }

    @Step
    public LogPageSteps verifyLastConfigLogEstbMac(String value) {
        assertEquals(value, page.getLastConfigLogEstbMac());
        return this;
    }

    @Step
    public LogPageSteps verifyLastConfigLogRequestFirmwareVersion(String value) {
        assertEquals(value, page.getLastConfigLogRequestFirmwareVersion());
        return this;
    }

    @Step
    public LogPageSteps verifyLastConfigLogResponseFirmwareVersion(String value) {
        assertEquals(value, page.getLastConfigLogResponseFirmwareVersion());
        return this;
    }

    @Step
    public LogPageSteps verifyLastConfigLogFileName(String value) {
        assertEquals(value, page.getLastConfigLogFileName());
        return this;
    }

    @Step
    public LogPageSteps verifyLastConfigLogRuleType(String value) {
        assertEquals(value, page.getLastConfigLogRuleType());
        return this;
    }

    @Step
    public LogPageSteps verifyLastConfigLogRuleName(String value) {
        assertEquals(value, page.getLastConfigLogRuleName());
        return this;
    }

    @Step
    public LogPageSteps verifyLastConfigLogRuleNoop(Boolean value) {
        assertEquals(value, page.getLastConfigLogRuleNoop());
        return this;
    }

    @Step
    public LogPageSteps verifyChangeConfigLogEstbMac(String value) {
        assertEquals(value, page.getChangeConfigLogEstbMac());
        return this;
    }

    @Step
    public LogPageSteps verifyChangeConfigLogRequestFirmwareVersion(String value) {
        assertEquals(value, page.getChangeConfigLogRequestFirmwareVersion());
        return this;
    }

    @Step
    public LogPageSteps verifyChangeConfigLogResponseFirmwareVersion(String value) {
        assertEquals(value, page.getChangeConfigLogResponseFirmwareVersion());
        return this;
    }

    @Step
    public LogPageSteps verifyChangeConfigLogFileName(String value) {
        assertEquals(value, page.getChangeConfigLogFileName());
        return this;
    }

    @Step
    public LogPageSteps verifyChangeConfigLogRuleType(String value) {
        assertEquals(value, page.getChangeConfigLogRuleType());
        return this;
    }

    @Step
    public LogPageSteps verifyChangeConfigLogRuleName(String value) {
        assertEquals(value, page.getChangeConfigLogRuleName());
        return this;
    }

    @Step
    public LogPageSteps verifyChangeConfigLogRuleNoop(Boolean value) {
        assertEquals(value, page.getChangeConfigLogRuleNoop());
        return this;
    }

    @Step
    public LogPageSteps waitUntilResultIsPresent() {
        page.waitUntilResultIsPresent();
        return this;
    }

    @Step
    public LogPageSteps verifyLastConfigLog(FirmwareRule rule, String changedConfigVersion) {
        verifyLastConfigLogEstbMac(defaultMacAddress)
                .verifyLastConfigLogRequestFirmwareVersion(changedConfigVersion)
                .verifyLastConfigLogResponseFirmwareVersion(defaultVersion)
                .verifyLastConfigLogFileName(defaultFileName)
                .verifyLastConfigLogRuleType(rule.getType())
                .verifyLastConfigLogRuleName(rule.getName())
                .verifyLastConfigLogRuleNoop(false);

        return this;
    }

    @Step
    public LogPageSteps verifyChangeConfigLog(FirmwareRule rule, String changedConfigVersion) {
        verifyChangeConfigLogEstbMac(defaultMacAddress)
                .verifyChangeConfigLogRequestFirmwareVersion(changedConfigVersion)
                .verifyChangeConfigLogResponseFirmwareVersion(defaultVersion)
                .verifyChangeConfigLogFileName(defaultFileName)
                .verifyChangeConfigLogRuleType(rule.getType())
                .verifyChangeConfigLogRuleName(rule.getName())
                .verifyChangeConfigLogRuleNoop(false);

        return this;
    }
}
