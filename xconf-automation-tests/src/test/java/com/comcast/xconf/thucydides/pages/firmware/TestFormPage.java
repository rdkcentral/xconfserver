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
 * Created: 3/21/16  3:18 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/firmware/testpage")
public class TestFormPage extends PageObject {

    public TestFormPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "div#parameterEntries ul.dropdown-menu li")
    private WebElementFacade typeaheadListItem;

    @FindBy(css = "div#firmwareRuleTestPage input[ng-model='property.key']")
    private WebElementFacade keyParameterInput;

    @FindBy(css = "div#firmwareRuleTestPage input[ng-model='property.value']")
    private WebElementFacade valueParameterInput;

    @FindBy(css = "button#test")
    private WebElementFacade testButton;

    @FindBy(css = "div#firmwareRuleTestPage td.test-page-config-description")
    private WebElementFacade description;

    @FindBy(css = "div#firmwareRuleTestPage td.test-page-config-id")
    private WebElementFacade id;

    @FindBy(css = "div#firmwareRuleTestPage td.test-page-config-download-protocol")
    private WebElementFacade downloadProtocol;

    @FindBy(css = "div#firmwareRuleTestPage td.test-page-config-filename")
    private WebElementFacade fileName;

    @FindBy(css = "div#firmwareRuleTestPage td.test-page-config-version")
    private WebElementFacade version;

    @FindBy(css = "div#firmwareRuleTestPage td.test-page-config-reboot-immediately")
    private WebElementFacade rebootImmediately;

    @FindBy(css = "div#firmwareRuleTestPage td.test-page-config-supported-models")
    private WebElementFacade supportedModels;

    public void clickTypeaheadItem() {
        typeaheadListItem.waitUntilPresent();
        typeaheadListItem.click();
    }

    public void typeKeyParameter(String value) {
        keyParameterInput.type(value);
    }

    public void typeValueParameter(String value) {
        valueParameterInput.type(value);
    }

    public void clickTestButton() {
        testButton.click();
    }

    public String getDescription() {
        return description.getText();
    }

    public String getId() {
        return id.getText();
    }

    public String getDownloadProtocol() {
        return downloadProtocol.getText();
    }

    public String getFileName() {
        return fileName.getText();
    }

    public String getVersion() {
        return version.getText();
    }

    public Boolean getRebootImmediately() {
        return Boolean.valueOf(rebootImmediately.getText());
    }

    public String getSupportedModels() {
        return supportedModels.getText();
    }

    public void waitUntilResultIsPresent() {
        description.waitUntilPresent();
    }
}
