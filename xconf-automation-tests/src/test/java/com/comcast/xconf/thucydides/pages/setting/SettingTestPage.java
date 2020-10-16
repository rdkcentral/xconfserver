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
package com.comcast.xconf.thucydides.pages.setting;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

@DefaultUrl("/settings/testpage")
public class SettingTestPage extends PageObject {

    @FindBy(css = ".editable-map-container")
    private WebElementFacade editableMapContainer;

    @FindBy(id = "noMatchedRules")
    private WebElementFacade noMatchedRules;

    @FindBy(css = "div.matched-rules")
    private WebElementFacade matchedRules;

    @FindBy(css = "#parameterEntries > input:nth-child(1)")
    private WebElementFacade keyInput;

    @FindBy(css = "#parameterEntries > input:nth-child(3)")
    private WebElementFacade valueInput;

    @FindBy(id = "multiselectSettingTypeId")
    private WebElementFacade multiselect;

    @FindBy(id = "addParameter")
    private WebElementFacade addParameter;

    @FindBy(id = "testButton")
    private WebElementFacade testRuleButton;

    public SettingTestPage(final WebDriver webDriver) {
        super(webDriver);
    }

    public void selectSettingType(int lineNumber) {
        multiselect.findBy(".dropdown-toggle").click();
        multiselect.findBy(".dropdown-menu > li:nth-child(" + lineNumber + ") > a").click();
    }

    public void setKeyInput(int index, String key) {
        getParameterByIndex(index).findBy(By.cssSelector("input:nth-child(1)")).type(key);
    }

    public String getKeyInput(int index) {
        return getParameterByIndex(index).findBy(By.cssSelector("input:nth-child(1)")).getValue();
    }

    public void setValueInput(int index, String value) {
        getParameterByIndex(index).findBy(By.cssSelector("input:nth-child(3)")).type(value);
    }
    public String getValueInput(int index) {
        return getParameterByIndex(index).findBy(By.cssSelector("input:nth-child(3)")).getValue();
    }

    public List<WebElementFacade> getParameterEntities() {
        return editableMapContainer.thenFindAll("#parameterEntries");
    }

    public WebElementFacade getParameterByIndex(int index) {
        return getParameterEntities().get(index);
    }

    public void addParameter() {
        addParameter.click();
    }

    public void testRule() {
        testRuleButton.click();
    }

    public boolean hasMatchedRules() {
        matchedRules.waitUntilPresent();
        return getParameterEntities().size() == matchedRules.thenFindAll(".ruleview-rule").size();
    }

    public void clickSettingTypeMultiselect() {
        multiselect.click();
    }

}
