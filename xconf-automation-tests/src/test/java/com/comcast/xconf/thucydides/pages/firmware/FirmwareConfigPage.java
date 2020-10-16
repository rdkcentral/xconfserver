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
 * Created: 3/10/16  12:11 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/firmwareconfig/all")
public class FirmwareConfigPage extends PageObject {

    @FindBy(css = "input[title='Description']")
    private WebElementFacade descriptionInput;

    @FindBy(css = "input[title='File name']")
    private WebElementFacade fileNameInput;

    @FindBy(css = "input[title='Version']")
    private WebElementFacade versionInput;

    @FindBy(css = "ul#supportedModelList li")
    private WebElementFacade modelSelectButton;

    @FindBy(css = "ul#supportedModels li")
    private WebElementFacade supportedModelId;

    @FindBy(css = "button[title='Close']")
    private WebElementFacade modalCloseButton;

    @FindBy(css = "input[name='description']")
    private WebElementFacade descriptionSearchInput;

    @FindBy(css = "input[name='firmwareVersion']")
    private WebElementFacade firmwareVersionSearchInput;

    @FindBy(css = "input[name='model']")
    private WebElementFacade modelSearchInput;

    @FindBy(css = "td.config-description")
    private WebElementFacade configDescriptionTd;

    public FirmwareConfigPage(WebDriver webDriver) {
        super(webDriver);
    }

    public void typeDescription(String description) {
        descriptionInput.type(description);
    }

    public void typeFileName(String fileName) {
        fileNameInput.type(fileName);
    }

    public void typeVersion(String version) {
        versionInput.type(version);
    }

    public String getDescription() {
        return descriptionInput.getValue();
    }

    public String getFileName() {
        return fileNameInput.getValue();
    }

    public String getVersion() {
        return versionInput.getValue();
    }

    public void selectModel() {
        modelSelectButton.click();
    }

    public String getSupportedModelId() {
        return supportedModelId.getText();
    }

    public void clickModalCloseButton() {
        modalCloseButton.click();
    }

    public void typeSearchDescription(String description) {
        descriptionSearchInput.type(description);
    }

    public void typeSearchModelId(String modelId) {
        modelSearchInput.type(modelId);
    }

    public void typeSearchFirmwareVersion(String firmwareVersion) {
        firmwareVersionSearchInput.type(firmwareVersion);
    }

    public Integer getFirmwareConfigsCount() {
        if(isElementVisible(By.cssSelector("td.config-description"))) {

            return findAll(By.cssSelector("td.config-description")).size();
        }
        return 0;
    }

}
