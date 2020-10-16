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
 * Created: 3/29/16  10:48 AM
 */
package com.comcast.xconf.thucydides.pages.dcm;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class LogUploadSettingsViewPageObjects extends PageObject {

    public LogUploadSettingsViewPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#logUploadSettingsName")
    private WebElementFacade name;

    @FindBy(css = "input#uploadOnReboot")
    private WebElementFacade uploadOnReboot;

    @FindBy(css = "input#numberOfDays")
    private WebElementFacade numberOfDays;

    @FindBy(css = "input#settingsAreActive")
    private WebElementFacade settingsAreActive;

    @FindBy(css = "input#uploadRepository")
    private WebElementFacade uploadRepository;

    @FindBy(css = "input#scheduleType")
    private WebElementFacade scheduleType;

    @FindBy(css = "input#cronExpression")
    private WebElementFacade cronExpression;

    @FindBy(css = "input#timeWindow")
    private WebElementFacade timeWindow;

    @FindBy(css = "input#expressionL1")
    private WebElementFacade expressionL1;

    @FindBy(css = "input#expressionL2")
    private WebElementFacade expressionL2;

    @FindBy(css = "input#expressionL3")
    private WebElementFacade expressionL3;

    public String getName() {
        return name.getValue();
    }

    public String getUploadOnReboot() {
        return uploadOnReboot.getValue();
    }

    public String getNumberOfDays() {
        return numberOfDays.getValue();
    }

    public String getSettingsAreActive() {
        return settingsAreActive.getValue();
    }

    public String getUploadRepository() {
        return uploadRepository.getValue();
    }

    public String getScheduleType() {
        return scheduleType.getValue();
    }

    public String getCronExpression() {
        return cronExpression.getValue();
    }

    public String getTimeWindow() {
        return timeWindow.getValue();
    }

    public String getExpressionL1() {
        return expressionL1.getValue();
    }

    public String getExpressionL2() {
        return expressionL2.getValue();
    }

    public String getExpressionL3() {
        return expressionL3.getValue();
    }
}
