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
 * Created: 3/11/16  4:28 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

@DefaultUrl("/firmwarerules/")
public class FirmwareRulePage extends PageObject {

    @FindBy(css = "input[title='name']")
    private WebElementFacade nameInput;

    @FindBy(css = "ul#templatesList a")
    private WebElementFacade templatesListItem;

    @FindBy(id = "templatesList")
    private WebElementFacade templatesListElement;

    @FindBy(css = "input[title='isActive']")
    private WebElementFacade isActiveCheckbox;

    @FindBy(css = "input[ng-model='vm.selectedFirmwareRuleTemplate.id']")
    private WebElementFacade ruleType;

    @FindBy(css = "input[ng-model='vm.selectedActionType.name']")
    private WebElementFacade actionType;

    @FindBy(css = "input#noopCheckBox")
    private WebElementFacade noopCheckBox;

    @FindBy(css = "select#firmwareConfigId")
    private WebElementFacade firmwareConfigSelect;

    @FindBy(css = "select[name='distributionConfigId']")
    private WebElementFacade distributionConfigSelect;

    @FindBy(css = "button.remove-location")
    private WebElementFacade distributionRemoveButton;

    @FindBy(css = "button#addDistributionButton")
    private WebElementFacade distributionAddButton;

    @FindBy(css = "select#firmwareConfigId option:last-child")
    private WebElementFacade firmwareConfigListItem;

    @FindBy(css = "select[name='distributionConfigId'] option:last-child")
    private WebElementFacade distributionConfigListItem;

    @FindBy(css = "input[name='percentage']")
    private WebElementFacade distributionPercent;

    @FindBy(id = "newName")
    private WebElementFacade newNameInput;

    @FindBy(css = "input[title='Name']")
    private WebElementFacade searchNameInput;

    @FindBy(css = "tbody")
    private WebElementFacade tbody;

    public FirmwareRulePage(WebDriver webDriver) {
        super(webDriver);
    }

    public void typeName(String name) {
        nameInput.type(name);
    }

    public void clickTemplatesListItem() {
        templatesListItem.click();
    }

    public void selectTemplateByIndex(int index) {
        List<WebElementFacade> templates = templatesListElement.thenFindAll(By.className("list-group-item"));
        templates.get(index - 1).click();
    }

    public void clickIsActiveCheckbox() {
        isActiveCheckbox.click();
    }

    public String getRuleType() {
        return ruleType.getValue();
    }

    public String getActionType() {
        return actionType.getValue();
    }

    public void clickNoopCheckbox() {
        noopCheckBox.click();
    }

    public void clickDistributionConfigSelect() {
        distributionConfigSelect.click();
    }

    public void clickFirmwareConfigSelect() {
        firmwareConfigSelect.click();
    }

    public Boolean firmwareConfigSelectIsDisabled() {
        return Boolean.valueOf(firmwareConfigSelect.getAttribute("disabled"));
    }

    public void clickRemoveDistributionButton() {
        distributionRemoveButton.click();
    }

    public void clickAddDistributionButton() {
        distributionAddButton.click();
    }

    public Boolean distributionAddButtonIsDisabled() {
        return Boolean.valueOf(distributionAddButton.getAttribute("disabled"));
    }

    public String getFirmwareConfigSelectedValue() {
        return firmwareConfigSelect.getSelectedValue();
    }

    public String getFirmwareConfigSelectVisibleText() {
        return firmwareConfigSelect.getSelectedVisibleTextValue();
    }

    public void clickFirmwareConfigListItem() {
        firmwareConfigListItem.click();
    }

    public void clickDistributionConfigListItem() {
        distributionConfigListItem.click();
    }

    public void typeDistributionPercent(int percent) {
        distributionPercent.type(String.valueOf(percent));
    }

    public Boolean nameInputIsDisabled() {
        return Boolean.valueOf(nameInput.getAttribute("disabled"));
    }

    public void typeNewName(String newName) {
        newNameInput.type(newName);
    }

    public void typeSearchName(String searchName) {
        searchNameInput.waitUntilPresent()
                .type(searchName);
    }

    public int getFirmwareRulesCount() {
        return tbody.waitUntilPresent()
                .thenFindAll("tr").size();
    }
}
