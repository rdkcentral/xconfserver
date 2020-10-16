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
 * Created: 3/30/16  9:52 AM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.xconf.logupload.*;
import com.comcast.xconf.thucydides.pages.dcm.TestFormPage;
import net.thucydides.core.annotations.Step;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestFormPageSteps {

    private TestFormPage page;

    @Step
    public TestFormPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public TestFormPageSteps typeEstbIp(String value) {
        page.typeEstbIp(value);
        return this;
    }

    @Step
    public TestFormPageSteps clickTestButton() {
        page.clickTestButton();
        return this;
    }

    @Step
    public TestFormPageSteps verifyRuleType(String value) {
        assertEquals(value, page.getRuleType());
        return this;
    }

    @Step
    public TestFormPageSteps verifyMatchedRuleId(String value) {
        assertEquals(value, page.getMatchedRuleId());
        return this;
    }

    @Step
    public TestFormPageSteps verifyDeviceSettingsName(String value) {
        assertEquals(value, page.getDeviceSettingsName());
        return this;
    }

    @Step
    public TestFormPageSteps verifyDeviceSettingsCheckOnReboot(Boolean value) {
        assertEquals(value, Boolean.valueOf(page.getDeviceSettingsCheckOnReboot()));
        return this;
    }

    @Step
    public TestFormPageSteps verifyDeviceSettingsCronExpression(String value) {
        assertEquals(value, page.getDeviceSettingsCronExpression());
        return this;
    }

    @Step
    public TestFormPageSteps verifyDeviceSettingsDurationMinutes(Integer value) {
        assertEquals(value, Integer.valueOf(page.getDeviceSettingsDurationMinutes()));
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsName(String value) {
        assertEquals(value, page.getLogUploadSettingsName());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsNumberOfDays(Integer value) {
        assertEquals(value, Integer.valueOf(page.getLogUploadSettingsNumberOfDays()));
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsUploadRepositoryName(String value) {
        assertEquals(value, page.getLogUploadSettingsUploadRepositoryName());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsRepositoryUrl(String value) {
        assertEquals(value, page.getLogUploadSettingsRepositoryUrl());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsUploadRepositoryUrl(String value) {
        assertEquals(value, page.getLogUploadSettingsUploadRepositoryUrl());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsUploadProtocol(String value) {
        assertEquals(value, page.getLogUploadSettingsUploadProtocol());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsUploadOnReboot(Boolean value) {
        assertEquals(value, Boolean.valueOf(page.getLogUploadSettingsUploadOnReboot()));
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsUpload(String value) {
        assertEquals(value, page.getLogUploadSettingsUpload());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsCronExpression(String value) {
        assertEquals(value, page.getLogUploadSettingsCronExpression());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsCronExpressionL1(String value) {
        assertEquals(value, page.getLogUploadSettingsCronExpressionL1());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsCronExpressionL2(String value) {
        assertEquals(value, page.getLogUploadSettingsCronExpressionL2());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsCronExpressionL3(String value) {
        assertEquals(value, page.getLogUploadSettingsCronExpressionL3());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsMessage(String value) {
        assertEquals(value, page.getLogUploadSettingsMessage());
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsUploadImmediately(Boolean value) {
        assertEquals(value, Boolean.valueOf(page.getLogUploadSettingsUploadImmediately()));
        return this;
    }

    @Step
    public TestFormPageSteps verifyLogUploadSettingsDurationMinutes(Integer value) {
        assertEquals(value, Integer.valueOf(page.getLogUploadSettingsDurationMinutes()));
        return this;
    }

    @Step
    public TestFormPageSteps verifyVodSettingsName(String value) {
        assertEquals(value, page.getVodSettingsName());
        return this;
    }

    @Step
    public TestFormPageSteps verifyVodSettingsLocationsUrl(String value) {
        assertEquals(value, page.getVodSettingsLocationsUrl());
        return this;
    }

    @Step
    public TestFormPageSteps verifyVodSettingsSrmIpName(String value) {
        assertEquals(value, page.getVodSettingsSrmIpName());
        return this;
    }

    @Step
    public TestFormPageSteps verifyVodSettingsSrmIp(String value) {
        assertEquals(value, page.getVodSettingsSrmIp());
        return this;
    }

    @Step
    public TestFormPageSteps verifyTestResult(DCMGenericRule formula,
                                              DeviceSettings deviceSettings,
                                              LogUploadSettings logUploadSettings,
                                              VodSettings vodSettings,
                                              UploadRepository uploadRepository) {
        Schedule dsSchedule = deviceSettings.getSchedule();
        Schedule lucSchedule = logUploadSettings.getSchedule();
        verifyRuleType("DCMRule")
                .verifyMatchedRuleId(formula.getId())
                .verifyDeviceSettingsName(deviceSettings.getName())
                .verifyDeviceSettingsCheckOnReboot(deviceSettings.getCheckOnReboot())
                .verifyDeviceSettingsCronExpression(dsSchedule.getExpression())
                .verifyDeviceSettingsDurationMinutes(dsSchedule.getTimeWindowMinutes())
                .verifyLogUploadSettingsName(logUploadSettings.getName())
                .verifyLogUploadSettingsNumberOfDays(logUploadSettings.getNumberOfDays())
                .verifyLogUploadSettingsUploadRepositoryName(uploadRepository.getName())
                .verifyLogUploadSettingsRepositoryUrl(uploadRepository.getUrl())
                .verifyLogUploadSettingsUploadProtocol(uploadRepository.getProtocol().name())
                .verifyLogUploadSettingsUploadOnReboot(logUploadSettings.getUploadOnReboot())
                .verifyLogUploadSettingsCronExpression(lucSchedule.getExpression())
                .verifyLogUploadSettingsCronExpressionL1("")
                .verifyLogUploadSettingsCronExpressionL2(lucSchedule.getExpressionL2())
                .verifyLogUploadSettingsCronExpressionL3("")
                .verifyLogUploadSettingsDurationMinutes(lucSchedule.getTimeWindowMinutes())
                .verifyVodSettingsName(vodSettings.getName())
                .verifyVodSettingsLocationsUrl(vodSettings.getLocationsURL())
                .verifyVodSettingsSrmIpName(vodSettings.getIpNames().get(0))
                .verifyVodSettingsSrmIp(vodSettings.getIpList().get(0));
        return this;
    }

    @Step
    public TestFormPageSteps typeKey(String key) {
        page.typeKey(key);
        return this;
    }

    @Step
    public TestFormPageSteps typeValue(String value) {
        page.typeValue(value);
        return this;
    }

    @Step
    public TestFormPageSteps clickTypeHeadListItem(String itemName) {
        page.clickTypeHeadListItem(itemName);
        return this;
    }
}
