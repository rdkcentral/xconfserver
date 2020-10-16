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
 * <p>
 * Author: Stanislav Menshykov
 * Created: 3/10/16  5:45 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/firmwareruletemplates/")
public class FirmwareRuleTemplatePage extends PageObject {

    @FindBy(css = "select#priority")
    private WebElementFacade prioritySelect;

    @FindBy(css = "input[title='Optional']")
    private WebElementFacade optionalPropertyCheckbox;

    @FindBy(css = "button[title='Create FirmwareRule']")
    private WebElementFacade createFirmwareRuleButton;

    public FirmwareRuleTemplatePage(WebDriver webDriver) {
        super(webDriver);
    }

    public String getSelectedPriorityVisibleValue() {
        return prioritySelect.getSelectedVisibleTextValue();
    }

    public void clickOptionalCheckBox() {
        optionalPropertyCheckbox.click();
    }

    public void clickCreateRuleButton() {
        createFirmwareRuleButton.click();
    }
}
