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
 * Created: 3/9/16  2:28 PM
 */
package com.comcast.xconf.thucydides.pages;

import com.comcast.xconf.thucydides.util.PageObjectUtils;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriver;

public class GenericPageObjects extends PageObject {

    public GenericPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "button[title='Create'], #createEntity")
    private WebElementFacade createButton;

    @FindBy(css = "input[title='id'], #id")
    private WebElementFacade idInput;

    @FindBy(css = "button[title='Save'], #saveEntity")
    private WebElementFacade saveButton;

    @FindBy(id = "toast-container")
    private WebElementFacade toaster;

    @FindBy(css = "div.modal")
    private WebElementFacade modalWindow;

    @FindBy(css = "h4.modal-title")
    private WebElementFacade deleteModalTitle;

    @FindBy(css = "h3.modal-title")
    private WebElementFacade viewModalTitle;

    @FindBy(css = "div.modal-body span")
    private WebElementFacade deleteModalBody;

    @FindBy(css = "div.modal-footer button")
    private WebElementFacade deleteModalOkButton;

    @FindBy(css = "button[title='Delete'], button.deleteEntity")
    private WebElementFacade deleteButton;

    @FindBy(css = "button[title='Edit'], button.editEntity")
    private WebElementFacade editButton;

    @FindBy(css = "button[title='Export All'], #exportAll")
    private WebElementFacade exportAllButton;

    @FindBy(css = "button[title='Export one'], button.exportEntity")
    private WebElementFacade exportOneButton;

    @FindBy(css = "button[title='View'], button.viewEntity")
    private WebElementFacade viewButton;

    @FindBy(css = "button#modalSaveEntity")
    private WebElementFacade modalSaveButton;

    @FindBy(css = "button#dropdownOfExportAll")
    private WebElementFacade dropdownOfExportAll;

    @FindBy(css = "a#exportAllTypes")
    private WebElementFacade exportAllTypesButton;

    @FindBy(id = "selectSearchParam")
    private WebElementFacade searchArrow;

    @FindBy(css = "ul#searchParamList")
    private WebElementFacade searchParam;

    @FindBy(id = "singleSearchParam")
    private WebElementFacade singleSearchParamInput;

    @FindBy(id = "multipleSearchByKey")
    private WebElementFacade multipleSearchByKeyInput;

    @FindBy(id = "multipleSearchByValue")
    private WebElementFacade multipleSearchByValue;

    @FindBy(css = "tbody")
    private WebElementFacade tbody;

    @FindBy(css = "div.firmwarerule-table>div")
    private WebElementFacade firmwareruleTable;

    @FindBy(id = "searchByName")
    private WebElementFacade searchByNameInput;

    @FindBy(css = "div#notFoundResults")
    private WebElementFacade notFoundResults;

    public void clickCreateButton() {
        createButton.click();
    }

    public void typeId(String value) {
        idInput.type(value);
    }

    public void clickSaveButton() {
        saveButton.click();
    }

    public void clickDeleteButton() {
        deleteButton.click();
    }

    public void clickDeleteModalOkButton() {
        deleteModalOkButton.click();
    }

    public void waitForToasterToAppear() {
        toaster.waitUntilVisible();
    }

    public String getToasterText() {
        return toaster.getText();
    }

    public void waitForModalWindowToOpen() {
        modalWindow.waitUntilVisible();
    }

    public String getDeleteModalTitleText() {
        return deleteModalTitle.getText();
    }

    public String getViewModalTitleText() {
        return viewModalTitle.getText();
    }

    public String getDeleteModalBodyText() {
        return deleteModalBody.getText();
    }

    public void clickEditButton() {
        editButton.click();
    }

    public Boolean idInputIsDisabled() {
        return PageObjectUtils.inputIsDisabled(idInput);
    }

    public void clickExportAllButton() {
        exportAllButton.click();
    }

    public void clickExportAllTypesButton() {
        dropdownOfExportAll.click();
        exportAllTypesButton.waitUntilVisible();
        exportAllTypesButton.click();
    }

    public void clickExportOneButton() {
        exportOneButton.click();
    }

    public void clickViewButton() {
        viewButton.waitUntilVisible();
        viewButton.click();
    }

    public void clickModalSaveButton() {
        modalSaveButton.click();
    }

    public void clickSearchArrow() {
        searchArrow.click();
    }

    public void selectSearchParamType(String paramClass) {
        searchParam.waitUntilVisible();
        for (WebElementFacade elementFacade : searchParam.thenFindAll("li")) {
            if (StringUtils.equals(elementFacade.getText(), paramClass)) {
                elementFacade.click();
                break;
            }
        }
    }

    public void typeSingleSearchParam(String searchParamValue) {
        singleSearchParamInput.type(searchParamValue);
    }

    public void typeMultipleSearchParamByKey(String key) {
        multipleSearchByKeyInput.type(key);
    }

    public void typeMultipleSearchParamByValue(String value) {
        multipleSearchByValue.type(value);
    }

    public int getEntitiesCount() {
        return tbody.waitUntilVisible()
                .thenFindAll("tr").size();
    }

    public int getRowsCount() {
        return firmwareruleTable.waitUntilVisible()
                .thenFindAll("div").size() - 1;
    }

    public void typeSearchName(String name) {
        searchByNameInput.type(name);
    }

    public void waitNotFoundResultMessage() {
        notFoundResults.waitUntilVisible();
    }
}
