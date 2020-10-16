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
 * Author: asilchenko
 * Created: 3/17/2016  1:07 PM
 */
package com.comcast.xconf.thucydides.pages.common;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;


public class NamespacedListPageObjects extends PageObject {

    public NamespacedListPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#dataItemValue")
    private WebElementFacade dataInput;

    @FindBy(css = "input#searchByName")
    private WebElementFacade searchNameInput;

    @FindBy(css = "input#searchByData")
    private WebElementFacade searchDataInput;

    @FindBy(css = "tbody")
    private WebElementFacade tbody;

    @FindBy(css = "div#notFoundResults")
    private WebElementFacade notFoundResults;

    @FindBy(css = "button#addItemToData")
    private WebElementFacade addItemToDataButton;

    @FindBy(css = "button.remove-data-item")
    private WebElementFacade removeDataItemButton;

    @FindBy(css = "button.restore-data-item")
    private WebElementFacade restoreDateItemButton;

    @FindBy(id = "newId")
    private WebElementFacade newIdInput;

    public void typeData(String value) { dataInput.type(value); }

    public void setSearchName(String value) {
        searchNameInput.type(value);
    }

    public void setSearchData(String value) {
        searchDataInput.type(value);
    }

    public int getNamespaceListsCount() {
        return tbody.waitUntilVisible()
                .thenFindAll("tr").size();
    }

    public void waitNotFoundResultMessage() {
        notFoundResults.waitUntilVisible();
    }

    public void clickAddItemToDataButton() { addItemToDataButton.click(); }

    public void clickRemoveDataItemButton() {
        removeDataItemButton.click();
    }

    public void clickRestoreDataItemButton() {
        restoreDateItemButton.click();
    }

    public void typeNewId(String newId) {
        newIdInput.type(newId);
    }
}
