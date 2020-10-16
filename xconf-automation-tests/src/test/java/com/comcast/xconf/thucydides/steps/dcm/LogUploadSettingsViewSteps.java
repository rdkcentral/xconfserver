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
 * Created: 3/29/16  11:17 AM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.xconf.logupload.LogUploadSettings;
import com.comcast.xconf.logupload.Schedule;
import com.comcast.xconf.thucydides.pages.dcm.LogUploadSettingsViewPageObjects;
import com.comcast.xconf.thucydides.util.dcm.UploadRepositoryUtils;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.*;

public class LogUploadSettingsViewSteps {

    private LogUploadSettingsViewPageObjects page;

    @Step
    public LogUploadSettingsViewSteps verifyName(String value) {
        assertEquals(value, page.getName());
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifyUploadOnReboot(Boolean value) {
        assertEquals(value, Boolean.valueOf(page.getUploadOnReboot()));
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifyNumberOfDays(Integer value) {
        assertEquals(value, Integer.valueOf(page.getNumberOfDays()));
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifySettingsAreActive(Boolean value) {
        assertEquals(value, Boolean.valueOf(page.getSettingsAreActive()));
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifyUploadRepository(String value) {
        assertEquals(value, page.getUploadRepository());
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifyScheduleType(String value) {
        assertEquals(value, page.getScheduleType());
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifyCronExpression(String value) {
        assertEquals(value, page.getCronExpression());
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifyTimeWindow(Integer value) {
        assertEquals(value, Integer.valueOf(page.getTimeWindow()));
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifyExpressionL1(String value) {
        assertEquals(value, page.getExpressionL1());
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifyExpressionL2(String value) {
        assertEquals(value, page.getExpressionL2());
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifyExpressionL3(String value) {
        assertEquals(value, page.getExpressionL3());
        return this;
    }

    @Step
    public LogUploadSettingsViewSteps verifyViewPage(LogUploadSettings expectedSettings) {
        Schedule expectedSchedule = expectedSettings.getSchedule();
        String expectedUploadRepositoryName = UploadRepositoryUtils.createDefaultUploadRepository().getName();
        verifyName(expectedSettings.getName())
                .verifyUploadOnReboot(expectedSettings.getUploadOnReboot())
                .verifyNumberOfDays(expectedSettings.getNumberOfDays())
                .verifySettingsAreActive(expectedSettings.getAreSettingsActive())
                .verifyUploadRepository(expectedUploadRepositoryName)
                .verifyScheduleType(expectedSchedule.getType())
                .verifyCronExpression(expectedSchedule.getExpression())
                .verifyTimeWindow(expectedSchedule.getTimeWindowMinutes())
                .verifyExpressionL1(expectedSchedule.getExpressionL1())
                .verifyExpressionL2(expectedSchedule.getExpressionL2())
                .verifyExpressionL3(expectedSchedule.getExpressionL3());
        return this;
    }
}
