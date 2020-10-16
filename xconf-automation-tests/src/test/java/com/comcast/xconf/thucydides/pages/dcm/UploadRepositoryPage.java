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
 * Created: 3/29/16  1:53 PM
 */
package com.comcast.xconf.thucydides.pages.dcm;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/uploadrepository/all")
public class UploadRepositoryPage extends PageObject {

    public UploadRepositoryPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#name")
    private WebElementFacade nameInput;

    @FindBy(css = "textarea#description")
    private WebElementFacade descriptionInput;

    @FindBy(css = "select#webProtocol")
    private WebElementFacade webProtocolSelect;

    @FindBy(css = "input#url")
    private WebElementFacade urlInput;

    public void typeName(String value) {
        nameInput.type(value);
    }

    public void typeDescription(String value) {
        descriptionInput.type(value);
    }

    public void selectProtocol(String value) {
        webProtocolSelect.selectByVisibleText(value);
    }

    public void typeUrl(String value) {
        urlInput.type(value);
    }
}
