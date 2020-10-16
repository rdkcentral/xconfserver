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
 * Created: 3/15/16  11:17 AM
 */
package com.comcast.xconf.thucydides.steps;

import com.comcast.xconf.thucydides.pages.GenericPageObjects;
import net.thucydides.core.annotations.Step;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenericSteps {

    private GenericPageObjects page;

    @Step
    public GenericSteps clickCreateButton() {
        page.clickCreateButton();
        return this;
    }

    @Step
    public GenericSteps typeId(String id) {
        page.typeId(id);
        return this;
    }

    @Step
    public GenericSteps clickSaveButton() {
        page.clickSaveButton();
        return this;
    }

    @Step
    public GenericSteps waitSuccessfullySavedToaster(String savedEntityId) {
        waitSuccessToaster(page, "Saved " + savedEntityId);
        return this;
    }

    @Step
    public GenericSteps waitSuccessfullyDeletedToaster(String deletedEntityId) {
        waitSuccessToaster(page, deletedEntityId);
        return this;
    }

    @Step
    public GenericSteps waitFailedDeletedToaster(String deletedEntityId) {
        waitFailedToaster(page, deletedEntityId);
        return this;
    }

    @Step
    public GenericSteps waitFailedToaster(String expectedContent) {
        waitFailedToaster(page, expectedContent);
        return this;
    }

    @Step
    public GenericSteps waitFailedToaster(String title, String expectedContent) {
        waitFailedToaster(page, title, expectedContent);
        return this;
    }

    @Step
    public GenericSteps clickDeleteButton() {
        page.clickDeleteButton();
        return this;
    }

    @Step
    public GenericSteps verifyDeleteModalWindow(String entityId, String entityType) {
        page.waitForModalWindowToOpen();
        String modalTitleText = page.getDeleteModalTitleText();
        assertTrue(modalTitleText.contains("Delete confirmation"));
        String modalBodyText = page.getDeleteModalBodyText();
        assertTrue(modalBodyText.contains("Are you sure you want to delete " + entityType + " " + entityId));
        return this;
    }

    @Step
    public GenericSteps clickModalOkButton() {
        page.clickDeleteModalOkButton();
        return this;
    }

    @Step
    public GenericSteps clickEditButton() {
        page.clickEditButton();
        return this;
    }

    @Step
    public GenericSteps checkIdInputIsDisabled() {
        assertTrue(page.idInputIsDisabled());
        return this;
    }

    @Step
    public GenericSteps clickExportAllButton() {
        page.clickExportAllButton();
        return this;
    }

    @Step
    public GenericSteps clickExportAllTypesButton() {
        page.clickExportAllTypesButton();
        return this;
    }

    @Step
    public GenericSteps clickExportOneButton() {
        page.clickExportOneButton();
        return this;
    }

    @Step
    public GenericSteps checkSavedFile(File dirForDownload, String expectedFileName) throws Exception {
        Long startTime = System.currentTimeMillis();
        while (dirForDownload.listFiles().length < 1 && (System.currentTimeMillis() - startTime) < 1000) {
            //wait up to one second for file to be saved
        }
        File[] files = dirForDownload.listFiles();
        assertTrue(files.length == 1);
        File exportedFile = files[0];
        assertEquals(expectedFileName, exportedFile.getName());
        return this;
    }

    @Step
    public GenericSteps clickViewButton() {
        page.clickViewButton();
        return this;
    }

    @Step
    public GenericSteps waitForModalWindowToOpen() {
        page.waitForModalWindowToOpen();
        return this;
    }

    @Step
    public GenericSteps verifyViewModalTitle(String expectedTitlePart) {
        assertTrue(page.getViewModalTitleText().contains(expectedTitlePart));
        return this;
    }

    @Step
    public GenericSteps clickModalSaveButton() {
        page.clickModalSaveButton();
        return this;
    }

    @Step
    public GenericSteps clickSearchArrow() {
        page.clickSearchArrow();
        return this;
    }

    @Step
    public GenericSteps selectSearchParam(String searchParamType) {
        page.selectSearchParamType(searchParamType);
        return this;
    }

    @Step
    public GenericSteps typeSingleSearchParam(String searchParamValue) {
        page.typeSingleSearchParam(searchParamValue);
        return this;
    }

    @Step
    public GenericSteps typeMultipleSearchParamByKey(String key) {
        page.typeMultipleSearchParamByKey(key);
        return this;
    }

    @Step
    public GenericSteps typeMultipleSearchParamByValue(String value) {
        page.typeMultipleSearchParamByValue(value);
        return this;
    }

    @Step
    public GenericSteps verifyEntitiesCount(int expectedEntitiesCount) {
        assertEquals(expectedEntitiesCount, page.getEntitiesCount());
        return this;
    }

    @Step
    public GenericSteps verifyRowsCount(int expectedRowsCount) {
        assertEquals(expectedRowsCount, page.getRowsCount());
        return this;
    }

    @Step
    public GenericSteps typeSearchName(String name) {
        page.typeSearchName(name);
        return this;
    }

    @Step
    public GenericSteps waitNotFoundResultMessage() {
        page.waitNotFoundResultMessage();
        return this;
    }

    private GenericSteps waitSuccessToaster(GenericPageObjects page, String content) {
        String toasterText = getToasterTextAndVerifyContent(content);
        assertTrue(toasterText.contains("Success"));
        return this;
    }

    private GenericSteps waitFailedToaster(GenericPageObjects page, String content) {
        waitFailedToaster(page, "Error", content);
        return this;
    }

    private GenericSteps waitFailedToaster(GenericPageObjects page, String title, String content) {
        String toasterText = getToasterTextAndVerifyContent(content);
        assertTrue(toasterText.contains(title));
        return this;
    }

    private String getToasterTextAndVerifyContent(String content) {
        page.waitForToasterToAppear();
        String toasterText = page.getToasterText();
        assertTrue(toasterText.contains(content));
        return toasterText;
    }
}
