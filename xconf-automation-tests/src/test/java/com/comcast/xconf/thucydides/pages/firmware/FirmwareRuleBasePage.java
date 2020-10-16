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
 * Created: 3/11/16  5:00 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class FirmwareRuleBasePage extends PageObject {

    @FindBy(css = "a#ruleActions")
    protected WebElementFacade ruleActionsTab;

    @FindBy(css = "a#defineProperties")
    protected WebElementFacade definePropertiesTab;

    @FindBy(css = "a#blockingFilters")
    protected WebElementFacade blockingFiltersTab;

    @FindBy(css = "div#bypassFilters button")
    protected WebElementFacade bypassFiltersList;

    @FindBy(css = "div#bypassFilters ul li:last-child a")
    protected WebElementFacade bypassFilterListItem;

    @FindBy(css = "li.ads-list-item-dark-grey")
    protected WebElementFacade selectedByPassFilter;

    @FindBy(css = "div#properties input[title='Key']")
    protected WebElementFacade propertiesKeyInput;

    @FindBy(css = "div#properties input[title='Value']")
    protected WebElementFacade propertiesValueInput;

    @FindBy(css = "a[ng-click='editRow($event, item)']")
    protected WebElementFacade propertiesEditButton;

    @FindBy(css = "a[ng-click='removeRow(item)']")
    protected WebElementFacade propertiesRemoveButton;

    @FindBy(css = "button[title='Add']")
    protected WebElementFacade addPropertyButton;

    public FirmwareRuleBasePage(WebDriver webDriver) {
        super(webDriver);
    }

    public void clickRuleActionsTab() {
        ruleActionsTab.click();
    }

    public void clickDefinePropertiesTab() {
        definePropertiesTab.click();
    }

    public void clickBlockingFiltersTab() {
        blockingFiltersTab.click();
    }

    public void clickBypassFiltersList() {
        bypassFiltersList.click();
    }

    public void clickBypassFilterListItem() {
        bypassFilterListItem.click();
    }

    public String getSelectedBypassFilterId() {
        return selectedByPassFilter.getText();
    }

    public void typePropertyKey(String key) {
        propertiesKeyInput.type(key);
    }

    public void typePropertyValue(String value) {
        propertiesValueInput.type(value);
    }

    public void clickPropertiesEditButton() {
        propertiesEditButton.click();
    }

    public void clickPropertiesRemoveButton() {
        propertiesRemoveButton.click();
    }

    public void clickAddPropertyButton() {
        addPropertyButton.click();
    }

    public Boolean propertiesKeyInputIsDisabled() {
        return Boolean.valueOf(propertiesKeyInput.getAttribute("disabled"));
    }
}
