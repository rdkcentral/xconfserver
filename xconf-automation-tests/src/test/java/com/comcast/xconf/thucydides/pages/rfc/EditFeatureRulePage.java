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

@DefaultUrl("/featurerule")
public class EditFeatureRulePage extends PageObject {

    public EditFeatureRulePage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(id = "name")
    private WebElementFacade nameInput;

    @FindBy(id = "saveButton")
    private WebElementFacade saveButton;

    @FindBy(id = "priority")
    private WebElementFacade priorityDropbox;

    @FindBy(className = "ui-select-choices-row")
    private WebElementFacade featureIdsSelect;


    @FindBy(id = "featureIds")
    private WebElementFacade featureIdsInput;

    @FindBy(className = "ui-select-choices-group")
    private WebElementFacade featureSelect;

    @FindBy(className = "ui-select-choices-row-inner")
    private WebElementFacade featureRowInner;

    public void typeName(String key) {
        nameInput.type(key);
    }

    public void selectFeatureName(String featureName) {
        featureIdsInput.click();
        featureRowInner.click();
    }

    public void selectPriority(String priority) {
        priorityDropbox.selectByVisibleText(priority);
    }

    public void clickSaveButton() {
        saveButton.click();
    }
}