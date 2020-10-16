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
 * Created: 3/15/16  5:57 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class FirmwareRuleViewModalPageObjects extends PageObject {

    public FirmwareRuleViewModalPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "div#bypassFilters span")
    private WebElementFacade bypassFilterListItem;

    @FindBy(css = "input.rule-name")
    private WebElementFacade ruleName;

    @FindBy(css = "input.rule-action-type")
    private WebElementFacade actionType;

    @FindBy(css = "td[title='key']")
    private WebElementFacade propertyKey;

    @FindBy(css = "td[title='value']")
    private WebElementFacade propertyValue;

    @FindBy(css = "input.firmwareconfig-id")
    private WebElementFacade configId;

    @FindBy(css = "input.firmwareconfig-description")
    private WebElementFacade configDescription;

    @FindBy(css = "input.firmwareconfig-filename")
    private WebElementFacade configFileName;

    @FindBy(css = "input.firmwareconfig-version")
    private WebElementFacade configVersion;

    @FindBy(css = "ul#supportedModels li")
    private WebElementFacade configSupportedModel;

    public String getBypassFilterListItem() {
        return bypassFilterListItem.getText();
    }

    public String getId() {
        return ruleName.getValue();
    }

    public String getActionType() {
        return actionType.getValue();
    }

    public String getPropertyKey() {
        return propertyKey.getText();
    }

    public String getPropertyValue() {
        return propertyValue.getText();
    }

    public String getConfigId() {
        return configId.getValue();
    }

    public String getConfigDescription() {
        return configDescription.getValue();
    }

    public String getConfigFileName() {
        return configFileName.getValue();
    }

    public String getConfigVersion() {
        return configVersion.getValue();
    }

    public String getConfigSupportedModel() {
        return configSupportedModel.getText();
    }
}
