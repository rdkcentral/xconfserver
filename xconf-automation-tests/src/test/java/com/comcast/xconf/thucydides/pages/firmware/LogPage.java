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
 * Created: 3/18/16  3:52 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/firmware/log")
public class LogPage extends PageObject {

    public LogPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#macAddress")
    private WebElementFacade macAddressInput;

    @FindBy(css = "button#test")
    private WebElementFacade testButton;

    @FindBy(css = "div#lastConfigLog span.log-page-estb-mac")
    private WebElementFacade lastConfigLogEstbMac;

    @FindBy(css = "div#lastConfigLog span.log-page-request-firmware-version")
    private WebElementFacade lastConfigLogRequestFirmwareVersion;

    @FindBy(css = "div#lastConfigLog span.log-page-response-firmware-version")
    private WebElementFacade lastConfigLogResponseFirmwareVersion;

    @FindBy(css = "div#lastConfigLog span.log-page-file-name")
    private WebElementFacade lastConfigLogFileName;

    @FindBy(css = "div#lastConfigLog span.log-page-rule-type")
    private WebElementFacade lastConfigLogRuleType;

    @FindBy(css = "div#lastConfigLog span.log-page-rule-name")
    private WebElementFacade lastConfigLogRuleName;

    @FindBy(css = "div#lastConfigLog span.log-page-rule-noop")
    private WebElementFacade lastConfigLogRuleNoop;


    @FindBy(css = "div#changeConfigLog span.log-page-estb-mac")
    private WebElementFacade changeConfigLogEstbMac;

    @FindBy(css = "div#changeConfigLog span.log-page-request-firmware-version")
    private WebElementFacade changeConfigLogRequestFirmwareVersion;

    @FindBy(css = "div#changeConfigLog span.log-page-response-firmware-version")
    private WebElementFacade changeConfigLogResponseFirmwareVersion;

    @FindBy(css = "div#changeConfigLog span.log-page-file-name")
    private WebElementFacade changeConfigLogFileName;

    @FindBy(css = "div#changeConfigLog span.log-page-rule-type")
    private WebElementFacade changeConfigLogRuleType;

    @FindBy(css = "div#changeConfigLog span.log-page-rule-name")
    private WebElementFacade changeConfigLogRuleName;

    @FindBy(css = "div#changeConfigLog span.log-page-rule-noop")
    private WebElementFacade changeConfigLogRuleNoop;

    public String getLastConfigLogEstbMac() {
        return lastConfigLogEstbMac.getText();
    }

    public String getLastConfigLogRequestFirmwareVersion() {
        return lastConfigLogRequestFirmwareVersion.getText();
    }

    public String getLastConfigLogResponseFirmwareVersion() {
        return lastConfigLogResponseFirmwareVersion.getText();
    }

    public String getLastConfigLogFileName() {
        return lastConfigLogFileName.getText();
    }

    public String getLastConfigLogRuleType() {
        return lastConfigLogRuleType.getText();
    }

    public String getLastConfigLogRuleName() {
        return lastConfigLogRuleName.getText();
    }

    public Boolean getLastConfigLogRuleNoop() {
        return Boolean.valueOf(lastConfigLogRuleNoop.getText());
    }


    public String getChangeConfigLogEstbMac() {
        return changeConfigLogEstbMac.getText();
    }

    public String getChangeConfigLogRequestFirmwareVersion() {
        return changeConfigLogRequestFirmwareVersion.getText();
    }

    public String getChangeConfigLogResponseFirmwareVersion() {
        return changeConfigLogResponseFirmwareVersion.getText();
    }

    public String getChangeConfigLogFileName() {
        return changeConfigLogFileName.getText();
    }

    public String getChangeConfigLogRuleType() {
        return changeConfigLogRuleType.getText();
    }

    public String getChangeConfigLogRuleName() {
        return changeConfigLogRuleName.getText();
    }

    public Boolean getChangeConfigLogRuleNoop() {
        return Boolean.valueOf(changeConfigLogRuleNoop.getText());
    }

    public void typeMacAddress(String value) {
        macAddressInput.type(value);
    }

    public void clickTestButton() {
        testButton.click();
    }

    public void waitUntilResultIsPresent() {
        lastConfigLogEstbMac.waitUntilPresent();
    }
}
