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
 * Created: 3/28/16  11:21 AM
 */
package com.comcast.xconf.thucydides.pages.dcm;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class VodSettingsEditPageObjects extends PageObject {

    public VodSettingsEditPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#vodSettingsName")
    private WebElementFacade nameInput;

    @FindBy(css = "input#locationUrl")
    private WebElementFacade locationUrlInput;

    @FindBy(css = "button.remove-srm-ip-list-item")
    private WebElementFacade removeSrmIpItemButton;

    @FindBy(css = "button.add-srm-ip-list-item")
    private WebElementFacade addSrmIpItemButton;

    @FindBy(css = "input.srm-name")
    private WebElementFacade srmNameInput;

    @FindBy(css = "input.srm-ip")
    private WebElementFacade srmIpInput;

    public void typeName(String value) {
        nameInput.type(value);
    }

    public void typeLocationUrl(String value) {
        locationUrlInput.type(value);
    }

    public void clickRemoveSrmIpItemButton() {
        removeSrmIpItemButton.click();
    }

    public void clickAddSrmIpItemButton() {
        addSrmIpItemButton.click();
    }

    public void typeSrmName(String value) {
        srmNameInput.type(value);
    }

    public void typeSrmIp(String value) {
        srmIpInput.type(value);
    }
}
