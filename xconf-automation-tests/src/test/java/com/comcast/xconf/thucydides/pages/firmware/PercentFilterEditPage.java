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
 *  Author: mdolina
 *  Created: 6:08 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;


import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class PercentFilterEditPage extends PageObject {

    public PercentFilterEditPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "ul#firmwareVersionList li")
    private WebElementFacade firmwareVersion;

    @FindBy(css = "ul#firmwareVersionList")
    private WebElementFacade firmwareVersionList;

    @FindBy(css = "input#envModelPercentage")
    private WebElementFacade envModelPercentage;

    @FindBy(css = "select#envModelWhitelist")
    private WebElementFacade envModelWhitelistSelect;

    @FindBy(css = "select#lastKnownGood")
    private WebElementFacade lastKnownGoodSelect;

    @FindBy(css = "input#isActive")
    private WebElementFacade isActive;

    @FindBy(css = "input#isFirmwareCheckRequired")
    private WebElementFacade isFirmwareCheckRequired;

    @FindBy(css = "input#isRebootImmediately")
    private WebElementFacade isRebootImmediately;

    @FindBy(css = "select#intermediateVersion")
    private WebElementFacade intermediateVersionSelect;

    @FindBy(id = "firmwareVersionError")
    private WebElementFacade firmwareVersionsErrorMessage;

    public void typeEnvModelPercentage(String percentage) {
        envModelPercentage.type(percentage);
    }

    public void selectEnvModelWhitelist(String whitelist) {
        envModelWhitelistSelect.waitUntilEnabled();
        envModelWhitelistSelect.selectByVisibleText(whitelist);
    }

    public void selectLastKnownGood(String whitelist) {
        lastKnownGoodSelect.selectByVisibleText(whitelist);
    }

    public String getLastKnownGood() {
        return lastKnownGoodSelect.getSelectedValue();
    }

    public boolean isLastKnownGoodEnabled() {
        lastKnownGoodSelect.waitUntilDisabled();
        return lastKnownGoodSelect.isEnabled();
    }

    public void clickFirmwareVersion() {
        firmwareVersion.click();
    }

    public void clickFirmwareVersion(String firmwareVersionName) {
        for (WebElementFacade li : firmwareVersionList.thenFindAll("li")) {
            if (li.getText().equals(firmwareVersionName)) {
                li.click();
                break;
            }
        }
    }

    public void clickIsActive() {
        isActive.click();
    }

    public void clickFirmwareVersionCheckRequired() {
        isFirmwareCheckRequired.click();
    }

    public void clickRebootImmediately() {
        isRebootImmediately.click();
    }

    public void selectIntermediateVersion(String intermediateVersion) {
        intermediateVersionSelect.selectByVisibleText(intermediateVersion);
    }

    public String getFirmwareVersionsErrorMessage() {
        return firmwareVersionsErrorMessage
                .waitUntilVisible()
                .getText();
    }

    public boolean isFirmwareVersionSelected(String firmwareVersionName) {
        for (WebElementFacade li : firmwareVersionList.thenFindAll("li")) {
            if (li.getText().equals(firmwareVersionName)
                    && li.getAttribute("class").contains("checked-in-list")) {
                return true;
            }
        }
        return false;
    }
}
