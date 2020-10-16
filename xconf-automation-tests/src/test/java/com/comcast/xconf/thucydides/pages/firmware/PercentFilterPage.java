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
 * Created: 3/17/16  5:38 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/firmware/percentfilter")
public class PercentFilterPage extends PageObject {

    public PercentFilterPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#percent")
    private WebElementFacade percent;

    @FindBy(css = "input#whitelist")
    private WebElementFacade whitelist;

    @FindBy(css = "select#whitelist")
    private WebElementFacade whitelistSelect;

    @FindBy(css = "button.env-model-percentage-edit")
    private WebElementFacade envModelEditButton;

    @FindBy(css = "input#envModelRuleName")
    private WebElementFacade envModelRuleNameInput;

    @FindBy(css = "input#envModelRulePercentage")
    private WebElementFacade envModelRulePercentage;

    @FindBy(css = "input#envModelRuleWhitelist")
    private WebElementFacade envModelRuleWhitelist;

    @FindBy(css = "input#isEnvModelRuleActive")
    private WebElementFacade isEnvModelRuleActive;

    @FindBy(css = "input#isEnvModelRuleFirmwareCheckRequired")
    private WebElementFacade isEnvModelRuleFirmwareCheckRequired;

    @FindBy(css = "input#isEnvModelRuleRebootImmediately")
    private WebElementFacade isEnvModelRuleRebootImmediately;

    @FindBy(css = "ul#firmwareVersionList li")
    private WebElementFacade envModelRuleFirmwareVersionListItem;

    @FindBy(css = "button[title='Close']")
    private WebElementFacade modalCloseButton;

    @FindBy(css = "td.env-model-percentage-name")
    private WebElementFacade envModelRuleNameTd;

    @FindBy(css = "td.env-model-percentage")
    private WebElementFacade envModelRulePercentageTd;

    @FindBy(css = "td.env-model-whitelist")
    private WebElementFacade envModelRuleWhitelistTd;

    @FindBy(css = "input.env-model-active")
    private WebElementFacade isEnvModelRuleActiveTd;

    @FindBy(css = "input.env-model-firmware-check-required")
    private WebElementFacade isEnvModelRuleCheckRequiredTd;

    @FindBy(id = "lastKnownGood")
    private WebElementFacade lastKnownGoodInput;

    @FindBy(id = "intermediateVersion")
    private WebElementFacade intermediateVersionInput;

    public String getPercent() {
        return percent.getValue();
    }

    public String getWhitelist() {
        return whitelist.getValue();
    }

    public Boolean percentIsDisabled() {
        return Boolean.valueOf(percent.getAttribute("readonly"));
    }

    public Boolean whitelistIsDisabled() {
        return Boolean.valueOf(whitelist.getAttribute("readonly"));
    }

    public void typePercentage(String percentage) {
        percent.type(percentage);
    }

    public void selectWhitelist(String whitelist) {
        whitelistSelect.selectByVisibleText(whitelist);
    }

    public void clickEnvModelEditButton() {
        envModelEditButton.click();
    }

    public String getEnvModelRuleName() {
        return envModelRuleNameInput.getValue();
    }

    public String getEnvModelRulePercentage() {
        return envModelRulePercentage.getValue();
    }

    public String getEnvModelRuleWhitelist() {
        return envModelRuleWhitelist.getValue();
    }

    public String isEnvModelRuleActive() {
        return isEnvModelRuleActive.getValue();
    }

    public String isEnvModelRuleFirmwareCheckRequired() {
        return isEnvModelRuleFirmwareCheckRequired.getValue();
    }

    public String isEnvModelRuleRebootImmediately() {
        isEnvModelRuleRebootImmediately.waitUntilPresent();
        return isEnvModelRuleRebootImmediately.getValue();
    }

    public String getEnvModelRuleFimwareVersionListItem() {
        return envModelRuleFirmwareVersionListItem.getText();
    }

    public void clickEnvModelRuleViewModalCloseButton() {
        modalCloseButton.click();
    }

    public String getEnvModelRuleNameTd() {
        return envModelRuleNameTd.getText();
    }

    public String getEnvModelRuleWhitelistTd() {
        return envModelRuleWhitelistTd.getText();
    }

    public String getEnvModelRulePercentageTd() {
        return envModelRulePercentageTd.getText();
    }

    public boolean isEnvModelRuleActiveTd() {
        return isEnvModelRuleActiveTd.isSelected();
    }

    public boolean isEnvModelRuleFirmwareCheckRequiredTd() {
        return isEnvModelRuleCheckRequiredTd.isSelected();
    }

    public String getLastKnowGood() {
        return lastKnownGoodInput.getValue();
    }

    public String getIntermediateVersion() {
        return intermediateVersionInput.getValue();
    }
}
