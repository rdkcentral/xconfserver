/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.thucydides.pages.rfc;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/feature")
public class EditFeaturePage extends PageObject {

    public EditFeaturePage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(id = "name")
    private WebElementFacade nameInput;

    @FindBy(id = "featureName")
    private WebElementFacade featureNameInput;

    @FindBy(id = "saveEntity")
    private WebElementFacade saveButton;

    @FindBy(id = "effectiveImmediate")
    private WebElementFacade effectiveImmediateCheckbox;

    @FindBy(id = "isEnable")
    private WebElementFacade isEnableCheckbox;

    @FindBy(css = "div input[ng-model='property.key']")
    private WebElementFacade keyParameterInput;

    @FindBy(css = "div input[ng-model='property.value']")
    private WebElementFacade valueParameterInput;

    public void typeName(String name) {
        nameInput.type(name);
    }

    public void typeFeatureName(String featureName) {
        featureNameInput.type(featureName);
    }

    public void typeKey(String key) {
        keyParameterInput.type(key);
    }

    public void typeValue(String value) {
        valueParameterInput.type(value);
    }

    public void clickSaveButton() {
        saveButton.click();
    }
}
