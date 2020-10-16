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
 * Created: 3/25/16  4:19 PM
 */
package com.comcast.xconf.thucydides.pages.dcm;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class DeviceSettingsEditPageObjects extends PageObject {

    public DeviceSettingsEditPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#deviceSettingsName")
    private WebElementFacade nameInput;

    @FindBy(css = "select#checkOnReboot")
    private WebElementFacade checkOnRebootSelect;

    @FindBy(css = "select#settingsAreActive")
    private WebElementFacade settingsAreActiveSelect;

    @FindBy(css = "select#scheduleType")
    private WebElementFacade scheduleTypeSelect;

    @FindBy(css = "input#expression")
    private WebElementFacade expressionInput;

    @FindBy(css = "input#timeWindow")
    private WebElementFacade timeWindowInput;

    @FindBy(id = "hours")
    private WebElementFacade cronHoursInput;

    @FindBy(id = "minutes")
    private WebElementFacade cronMinutesInput;

    public void typeName(String value) {
        nameInput.type(value);
    }

    public void selectCheckOnReboot(String value) {
        checkOnRebootSelect.selectByVisibleText(value);
    }

    public void selectSettingsAreActive(String value) {
        settingsAreActiveSelect.selectByVisibleText(value);
    }

    public void selectScheduleType(String value) {
        scheduleTypeSelect.selectByVisibleText(value);
    }

    public void typeWindowInput(String value) {
        timeWindowInput.type(value);
    }

    public void typeCronMinutes(Integer minutes) {
        cronMinutesInput.type(minutes.toString());
    }

    public void typeCronHours(Integer hours) {
        cronHoursInput.type(hours.toString());
    }
 }
