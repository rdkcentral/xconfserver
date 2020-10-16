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
 * Created: 3/28/16  2:36 PM
 */
package com.comcast.xconf.thucydides.pages.dcm;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class DeviceSettingsViewPageObjects extends PageObject {

    public DeviceSettingsViewPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#deviceSettingsName")
    private WebElementFacade name;

    @FindBy(css = "input#checkOnReboot")
    private WebElementFacade checkOnReboot;

    @FindBy(css = "input#settingsAreActive")
    private WebElementFacade settingsAreActive;

    @FindBy(css = "input#scheduleType")
    private WebElementFacade scheduleType;

    @FindBy(css = "input#expression")
    private WebElementFacade expression;

    @FindBy(css = "input#timeWindow")
    private WebElementFacade timeWindow;

    public String getName() {
        return name.getValue();
    }

    public String getCheckOnReboot() {
        return checkOnReboot.getValue();
    }

    public String getSettingsAreActive() {
        return settingsAreActive.getValue();
    }

    public String getScheduleType() {
        return scheduleType.getValue();
    }

    public String getExpression() {
        return expression.getValue();
    }

    public String getTimeWindow() {
        return timeWindow.getValue();
    }
}
