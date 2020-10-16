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
 * Created: 3/28/16  10:30 AM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.xconf.thucydides.pages.dcm.LogUploadSettingsEditPageObjects;
import com.comcast.xconf.thucydides.util.dcm.UploadRepositoryUtils;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class LogUploadSettingsEditSteps {

    private LogUploadSettingsEditPageObjects page;

    public LogUploadSettingsEditSteps typeName(String value) {
        page.typeName(value);
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps selectUploadOnReboot(Boolean value) {
        page.selectUploadOnReboot(value.toString());
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps typeNumberOfDays(Integer value) {
        page.typeNumberOfDays(value.toString());
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps selectSettingsAreActive(Boolean value) {
        page.selectSettingsAreActive(value.toString());
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps selectUploadRepository(String value) {
        page.selectUploadRepository(value);
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps selectScheduleType(String value) {
        page.selectScheduleType(value);
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps typeTimeWindow(Integer value) {
        page.typeTimeWindow(value.toString());
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps typeExpressionL1(String value) {
        page.typeExpressionL1(value);
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps typeExpressionL2(String value) {
        page.typeExpressionL2(value);
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps typeExpressionL3(String value) {
        page.typeExpressionL3(value);
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps typeCronMinutes(Integer minutes) {
        page.typeCronMinutes(minutes);
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps typeCronHours(Integer hours) {
        page.typeCronHours(hours);
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps fillForm(String name) {
        typeName(name)
                .selectUploadOnReboot(true)
                .typeNumberOfDays(42)
                .selectSettingsAreActive(true)
                .selectUploadRepository(UploadRepositoryUtils.defaultName)
                .verifyWholeDayRandomizedScheduleType()
                .selectScheduleType("CronExpression")
                .typeCronHours(5)
                .typeCronMinutes(35)
                .typeTimeWindow(69)
                .typeExpressionL1("expressionL1")
                .typeExpressionL2("expressionL2")
                .typeExpressionL3("expressionL3");
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps verifyCronExpressionInputIsDisabled(Boolean value) {
        assertEquals(value, page.cronExpressionInputIsDisabled());
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps verifyTimeWindowInputIsDisabled(Boolean value) {
        assertEquals(value, page.timeWindowInputIsDisabled());
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps verifyExpressionL1InputIsDisabled(Boolean value) {
        assertEquals(value, page.expressionL1InputIsDisabled());
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps verifyExpressionL2InputIsDisabled(Boolean value) {
        assertEquals(value, page.expressionL2InputIsDisabled());
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps verifyExpressionL3InputIsDisabled(Boolean value) {
        assertEquals(value, page.expressionL3InputIsDisabled());
        return this;
    }

    @Step
    public LogUploadSettingsEditSteps verifyWholeDayRandomizedScheduleType() {
        selectScheduleType("WholeDayRandomized")
                .verifyCronExpressionInputIsDisabled(true)
                .verifyTimeWindowInputIsDisabled(true)
                .verifyExpressionL1InputIsDisabled(true)
                .verifyExpressionL2InputIsDisabled(true)
                .verifyExpressionL3InputIsDisabled(true);
        return this;
    }
}
