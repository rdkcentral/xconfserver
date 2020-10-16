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
 * Created: 3/28/16  10:18 AM
 */
package com.comcast.xconf.thucydides.pages.dcm;

import com.comcast.xconf.thucydides.util.PageObjectUtils;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class LogUploadSettingsEditPageObjects extends PageObject {

    public LogUploadSettingsEditPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#logUploadSettingsName")
    private WebElementFacade nameInput;

    @FindBy(css = "select#uploadOnReboot")
    private WebElementFacade uploadOnRebootSelect;

    @FindBy(css = "input#numberOfDays")
    private WebElementFacade numberOfDays;

    @FindBy(css = "select#settingsAreActive")
    private WebElementFacade settingsAreActiveSelect;

    @FindBy(css = "select#uploadRepository")
    private WebElementFacade uploadRepositorySelect;

    @FindBy(css = "select#scheduleType")
    private WebElementFacade scheduleTypeSelect;

    @FindBy(css = "input#cronExpression")
    private WebElementFacade cronExpressionInput;

    @FindBy(css = "input#timeWindow")
    private WebElementFacade timeWindowInput;

    @FindBy(css = "input#expressionL1")
    private WebElementFacade expressionL1Input;

    @FindBy(css = "input#expressionL2")
    private WebElementFacade expressionL2Input;

    @FindBy(css = "input#expressionL3")
    private WebElementFacade expressionL3Input;

    @FindBy(id = "hours")
    private WebElementFacade cronHoursInput;

    @FindBy(id = "minutes")
    private WebElementFacade cronMinutesInput;

    public void typeName(String value) {
        nameInput.type(value);
    }

    public void selectUploadOnReboot(String value) {
        uploadOnRebootSelect.selectByVisibleText(value);
    }

    public void typeNumberOfDays(String value) {
        numberOfDays.type(value);
    }

    public void selectSettingsAreActive(String value) {
        settingsAreActiveSelect.selectByVisibleText(value);
    }

    public void selectUploadRepository(String value) {
        uploadRepositorySelect.selectByVisibleText(value);
    }

    public void selectScheduleType(String value) {
        scheduleTypeSelect.selectByVisibleText(value);
    }

    public void typeTimeWindow(String value) {
        timeWindowInput.type(value);
    }

    public void typeExpressionL1(String value) {
        expressionL1Input.type(value);
    }

    public void typeExpressionL2(String value) {
        expressionL2Input.type(value);
    }

    public void typeExpressionL3(String value) {
        expressionL3Input.type(value);
    }

    public Boolean cronExpressionInputIsDisabled() {
        return PageObjectUtils.inputIsDisabled(cronExpressionInput);
    }

    public Boolean timeWindowInputIsDisabled() {
        return PageObjectUtils.inputIsDisabled(timeWindowInput);
    }

    public Boolean expressionL1InputIsDisabled() {
        return PageObjectUtils.inputIsDisabled(expressionL1Input);
    }

    public Boolean expressionL2InputIsDisabled() {
        return PageObjectUtils.inputIsDisabled(expressionL2Input);
    }

    public Boolean expressionL3InputIsDisabled() {
        return PageObjectUtils.inputIsDisabled(expressionL3Input);
    }

    public void typeCronMinutes(Integer minutes) {
        cronMinutesInput.type(minutes.toString());
    }

    public void typeCronHours(Integer hours) {
        cronHoursInput.type(hours.toString());
    }
}
