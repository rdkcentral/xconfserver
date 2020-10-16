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
 * Created: 3/30/16  1:08 PM
 */
package com.comcast.xconf.thucydides.tests.common;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.estbfirmware.PercentFilterValue;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.common.NamespacedListPageSteps;
import com.comcast.xconf.thucydides.steps.firmware.PercentFilterPageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.common.GenericNamespacedListUtils;
import com.comcast.xconf.thucydides.util.firmware.PercentFilterUtils;
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
import java.util.UUID;

@RunWith(ThucydidesRunner.class)
public class IpListPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private GenericSteps genericSteps;
    @Steps
    private NamespacedListPageSteps namespacedListPageSteps;
    @Steps
    private PercentFilterPageSteps percentFilterPageSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        GenericNamespacedListUtils.doCleanup();
    }

    @Test
    public void createNewIpList() throws Exception {
        GenericNamespacedListUtils.createAndSaveIpLists();

        String id = "TESTID";

        namespacedListPageSteps.openIpListPage();
        genericSteps.clickCreateButton()
                .typeId(id);
        namespacedListPageSteps.typeData("8.1.1.1")
                .clickAddItemToDataButton();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(id);
    }

    @Test
    public void editIpList() throws Exception {
        GenericNamespacedList ipListToEdit = GenericNamespacedListUtils.createAndSaveDefaultIpList();

        namespacedListPageSteps.openIpListPage();
        genericSteps.clickEditButton();
        namespacedListPageSteps.clickRemoveDataItemButton()
                .clickRestoreDataItemButton()
                .typeData("2.2.2.2")
                .clickAddItemToDataButton();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(ipListToEdit.getId());
    }

    @Test
    public void deleteIpList() throws Exception {
        GenericNamespacedList ipListToEdit = GenericNamespacedListUtils.createAndSaveIpList(UUID.randomUUID().toString(), Sets.newHashSet("12.12.12.12", "13.13.13.13"));

        namespacedListPageSteps.openIpListPage();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(ipListToEdit.getId(), "Namespaced List")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(ipListToEdit.getId());
    }

    @Test
    public void exportAllIpLists() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        GenericNamespacedListUtils.createAndSaveDefaultIpList();

        namespacedListPageSteps.openIpListPage();
        genericSteps.clickExportAllButton()
                .checkSavedFile(dirForDownload, "allIP_LISTS.json");
    }

    @Test
    public void exportOneIpList() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        GenericNamespacedList listToExport = GenericNamespacedListUtils.createAndSaveDefaultIpList();

        namespacedListPageSteps.openIpListPage();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload, "namespacedList_" + listToExport.getId() +  ".json");
    }

    @Test
    public void searchByName() throws Exception {
        GenericNamespacedListUtils.createAndSaveIpLists();
        namespacedListPageSteps.openIpListPage();
        genericSteps.typeSingleSearchParam("123");
        namespacedListPageSteps.verifyNamespacedListsCount(1);
    }

    @Test
    public void searchByData() throws Exception {
        GenericNamespacedListUtils.createAndSaveIpLists();
        namespacedListPageSteps.openIpListPage();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Data")
                .typeSingleSearchParam("13.13");
        namespacedListPageSteps.verifyNamespacedListsCount(1);
    }

    @Test
    public void showNotFound() throws Exception {
        GenericNamespacedListUtils.createAndSaveIpLists();
        namespacedListPageSteps.openIpListPage();
        genericSteps.typeSingleSearchParam("NOT_FOUND")
                .waitNotFoundResultMessage();
    }

    @Test
    public void deleteIpListWhenItIsUsedByPercentFilter() throws Exception {
        PercentFilterValue percentFilter = PercentFilterUtils.createAndSavePercentFilter();

        namespacedListPageSteps.openIpListPage();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(percentFilter.getWhitelist().getName(), "Namespaced List")
                .clickModalOkButton()
                .waitFailedToaster(percentFilter.getWhitelist().getName());
    }

    @Test
    public void renameIpList() throws Exception {
        GenericNamespacedListUtils.createAndSaveIpList(UUID.randomUUID().toString(), Sets.newHashSet("10.10.10.10", "11.11.11.11"));

        String id = "NEWID";

        namespacedListPageSteps.openIpListPage();
        genericSteps.clickEditButton();
        namespacedListPageSteps.typeNewId(id)
                .clickRemoveDataItemButton();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(id);
    }

    @Test
    public void deleteBySearch() throws Exception {
        List<GenericNamespacedList> ipLists = GenericNamespacedListUtils.createAndSaveIpLists();

        namespacedListPageSteps.openIpListPage();
        genericSteps.typeSingleSearchParam(ipLists.get(0).getId())
                .clickDeleteButton()
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(ipLists.get(0).getId())
                .typeSingleSearchParam("");
        namespacedListPageSteps.verifyNamespacedListsCount(1);
    }
}
