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
 * Created: 3/22/16  1:34 PM
 */
package com.comcast.xconf.thucydides.pages.dcm;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/formulas/all")
public class FormulaPage extends PageObject {

    public FormulaPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#name")
    private WebElementFacade nameInput;

    @FindBy(css = "input#description")
    private WebElementFacade descriptionInput;

    @FindBy(css = "input#percentage")
    private WebElementFacade percentageInput;

    @FindBy(css = "div.is-default-formula span.glyphicon-ok")
    private WebElementFacade isDefaultFormulaIcon;

    @FindBy(css = "div.is-default-formula span.glyphicon-remove")
    private WebElementFacade isNotDefaultFormulaIcon;

    @FindBy(css = "select#priority")
    private WebElementFacade prioritySelect;

    @FindBy(css = "input#l1Percentage")
    private WebElementFacade l1PercentageInput;

    @FindBy(css = "input#l2Percentage")
    private WebElementFacade l2PercentageInput;

    @FindBy(css = "input#l3Percentage")
    private WebElementFacade l3PercentageInput;

    @FindBy(css = "button#editDeviceSettings")
    private WebElementFacade editDeviceSettingsButton;

    @FindBy(css = "button#createDeviceSettings")
    private WebElementFacade createDeviceSettingsButton;

    @FindBy(css = "button#editLogUploadSettings")
    private WebElementFacade editLogUploadSettingsButton;

    @FindBy(css = "button#createLogUploadSettings")
    private WebElementFacade createLogUploadSettingsButton;

    @FindBy(css = "button#editVodSettings")
    private WebElementFacade editVodSettingsButton;

    @FindBy(css = "button#createVodSettings")
    private WebElementFacade createVodSettingsButton;

    @FindBy(css = "button.view-device-settings")
    private WebElementFacade viewDeviceSettingsButton;

    @FindBy(css = "button.view-vod-settings")
    private WebElementFacade viewVodSettingsButton;

    @FindBy(css = "button.view-log-upload-settings")
    private WebElementFacade viewLogUploadSettingsButton;

    @FindBy(css = "input[title='Name']")
    private WebElementFacade searchNameInput;

    @FindBy(css = "tbody")
    private WebElementFacade tbody;

    public void typeName(String value) {
        nameInput.type(value);
    }

    public void typeDescription(String value) {
        descriptionInput.type(value);
    }

    public void typePercentage(Integer value) {
        percentageInput.type(value.toString());
    }

    public Boolean isDefaultFormula() {
        return isDefaultFormulaIcon.isPresent();
    }

    public Boolean notDefaultFormula() {
        return isNotDefaultFormulaIcon.isPresent();
    }

    public String getSelectedPriorityVisibleValue() {
        return prioritySelect.getSelectedVisibleTextValue();
    }

    public void typeL1Percentage(Integer value) {
        l1PercentageInput.type(value.toString());
    }

    public void typeL2Percentage(Integer value) {
        l2PercentageInput.type(value.toString());
    }

    public void typeL3Percentage(Integer value) {
        l3PercentageInput.type(value.toString());
    }

    public void clickEditDeviceSettingsButton() {
        editDeviceSettingsButton.click();
    }

    public void clickCreateDeviceSettingsButton() {
        createDeviceSettingsButton.click();
    }

    public void clickEditLogUploadSettingsButton() {
        editLogUploadSettingsButton.click();
    }

    public void clickCreateLogUploadSettingsButton() {
        createLogUploadSettingsButton.click();
    }

    public void clickEditVodSettingsButton() {
        editVodSettingsButton.click();
    }

    public void clickCreateVodSettingsButton() {
        createVodSettingsButton.click();
    }

    public void clickViewDeviceSettingsButton() {
        viewDeviceSettingsButton.click();
    }

    public void clickViewVodSettingsButton() {
        viewVodSettingsButton.click();
    }

    public void clickViewLogUploadSettingsButton() {
        viewLogUploadSettingsButton.click();
    }

    public void typeSearchName(String searchName) {
        searchNameInput.type(searchName);
    }

    public int getFormulasCount() {
        return tbody.waitUntilVisible()
                .thenFindAll("tr").size();
    }
}
