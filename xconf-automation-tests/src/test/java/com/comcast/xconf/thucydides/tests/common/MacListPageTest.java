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
 * Created: 3/17/2016  2:28 PM
 */
package com.comcast.xconf.thucydides.tests.common;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.common.NamespacedListPageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.common.GenericNamespacedListUtils;
import com.google.common.collect.Sets;
import net.thucydides.core.Thucydides;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.List;

@RunWith(ThucydidesRunner.class)
public class MacListPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private GenericSteps genericSteps;
    @Steps
    private NamespacedListPageSteps namespacedListPageSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        GenericNamespacedListUtils.doCleanup();
    }

    @Test
    public void createNewMacList() throws Exception {
        GenericNamespacedListUtils.createAndSaveMacLists();

        String id = "TESTID";

        namespacedListPageSteps.openMacListPage();
        genericSteps.clickCreateButton()
                .typeId(id);
        namespacedListPageSteps.typeData("11:11:11:11:11:11")
                .clickAddItemToDataButton();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(id);
    }

    @Test
    public void editMacList() throws Exception {
        GenericNamespacedList macListToEdit = GenericNamespacedListUtils.createAndSaveDefaultMacList();

        namespacedListPageSteps.openMacListPage();
        genericSteps.clickEditButton();
        namespacedListPageSteps.clickRemoveDataItemButton()
                .clickRestoreDataItemButton()
                .typeData("99:99:99:99:99:99")
                .clickAddItemToDataButton();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(macListToEdit.getId());
    }

    @Test
    public void deleteMacList() throws Exception {
        GenericNamespacedList macListToEdit = GenericNamespacedListUtils.createAndSaveDefaultMacList();

        namespacedListPageSteps.openMacListPage();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(macListToEdit.getId(), "Namespaced List")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(macListToEdit.getId());
    }

    @Test
    public void exportAllMacLists() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        GenericNamespacedListUtils.createAndSaveDefaultMacList();

        namespacedListPageSteps.openMacListPage();
        genericSteps.clickExportAllButton()
                .checkSavedFile(dirForDownload, "allMAC_LISTS.json");
    }

    @Test
    public void exportOneMacList() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        GenericNamespacedList listToExport = GenericNamespacedListUtils.createAndSaveDefaultMacList();

        namespacedListPageSteps.openMacListPage();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload, "namespacedList_" + listToExport.getId() +  ".json");
    }

    @Test
    public void searchByName() throws Exception {
        GenericNamespacedListUtils.createAndSaveMacLists();
        namespacedListPageSteps.openMacListPage();
        genericSteps.typeSingleSearchParam("123");
        namespacedListPageSteps.verifyNamespacedListsCount(1);
    }

    @Test
    public void searchByData() throws Exception {
        GenericNamespacedListUtils.createAndSaveMacLists();
        namespacedListPageSteps.openMacListPage();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Data")
                .typeSingleSearchParam("A2:B2");
        namespacedListPageSteps.verifyNamespacedListsCount(1);
    }

    @Test
    public void showNotFound() throws Exception {
        GenericNamespacedListUtils.createAndSaveMacLists();
        namespacedListPageSteps.openMacListPage();
        genericSteps.typeSingleSearchParam("NOT_FOUND")
                .waitNotFoundResultMessage();
    }

    @Test
    public void renameMacList() throws Exception {
        GenericNamespacedListUtils.createAndSaveMacList("TESTID", Sets.newHashSet("AA:AA:AA:AA:AA:AA", "BB:BB:BB:BB:BB:BB"));

        String id = "NEWID";

        namespacedListPageSteps.openMacListPage();
        genericSteps.clickEditButton();
        namespacedListPageSteps.typeNewId(id)
                .clickRemoveDataItemButton();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(id);
    }

    @Test
    public void deleteBySearch() throws Exception {
        List<GenericNamespacedList> macLists = GenericNamespacedListUtils.createAndSaveMacLists();

        namespacedListPageSteps.openMacListPage();
        genericSteps.typeSingleSearchParam(macLists.get(0).getId())
                .clickDeleteButton()
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(macLists.get(0).getId()).typeSingleSearchParam("");
        namespacedListPageSteps.verifyNamespacedListsCount(1);
    }
}
