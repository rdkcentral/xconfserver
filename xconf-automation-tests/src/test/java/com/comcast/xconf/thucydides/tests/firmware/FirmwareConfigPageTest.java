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
 * Created: 3/10/16  12:04 PM
 */
package com.comcast.xconf.thucydides.tests.firmware;

import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.firmware.FirmwareConfigPageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.common.ModelUtils;
import com.comcast.xconf.thucydides.util.firmware.FirmwareConfigUtils;
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
import java.io.IOException;
import java.util.List;

@RunWith(ThucydidesRunner.class)
public class FirmwareConfigPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private FirmwareConfigPageSteps firmwareConfigPageSteps;
    @Steps
    private GenericSteps genericSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        FirmwareConfigUtils.doCleanup();
    }

    @Test
    public void createNewFirmwareConfig() throws Exception {
        ModelUtils.createAndSaveDefaultModel();

        FirmwareConfigUtils.createAndSaveFirmwareConfigs();

        String description = "testDescription";

        firmwareConfigPageSteps.open();
        genericSteps.clickCreateButton();
        firmwareConfigPageSteps.typeDescription(description)
                .typeFileName("someFileName")
                .typeVersion("someVersion")
                .selectModel();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(description);
    }

    @Test
    public void editFirmwareConfig() throws Exception {
        FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();
        String newDescription = "newDescription";

        firmwareConfigPageSteps.open();
        genericSteps.clickEditButton();
        firmwareConfigPageSteps.typeDescription(newDescription);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(newDescription);
    }

    @Test
    public void deleteFirmwareConfig() throws Exception {
        FirmwareConfig configToDelete = FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();

        firmwareConfigPageSteps.open();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(configToDelete.getDescription(), "FirmwareConfig")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(configToDelete.getDescription());
    }

    @Test
    public void exportAllFirmwareConfigs() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();

        firmwareConfigPageSteps.open();
        genericSteps.clickExportAllButton()
            .checkSavedFile(dirForDownload, "allFirmwareConfigs.json");
    }

    @Test
    public void exportOneFirmwareConfig() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        FirmwareConfig configToExport = FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();

        firmwareConfigPageSteps.open();
        genericSteps.clickExportOneButton()
            .checkSavedFile(dirForDownload, "firmwareConfig_" + configToExport.getDescription() + ".json");
    }

    @Test
    public void viewFirmwareConfig() throws Exception {
        FirmwareConfig configToView = FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();

        firmwareConfigPageSteps.open();
        genericSteps.clickViewButton();
        firmwareConfigPageSteps.verifyViewModalWindow(configToView);
    }

    @Test
    public void searchByDescription() throws IOException {
        List<FirmwareConfig> firmwareConfigs = FirmwareConfigUtils.createAndSaveFirmwareConfigs();

        firmwareConfigPageSteps.open()
                .verifyConfigsCount(2);
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Description").typeSingleSearchParam("wrongDescription");
        firmwareConfigPageSteps
                .verifyConfigsCount(0);
        genericSteps.typeSingleSearchParam(firmwareConfigs.get(0).getDescription());
        genericSteps.clickViewButton();
        firmwareConfigPageSteps.verifyViewModalWindow(firmwareConfigs.get(0));
    }

    @Test
    public void searchByFirmwareVersion() throws IOException {
        List<FirmwareConfig> firmwareConfigs = FirmwareConfigUtils.createAndSaveFirmwareConfigs();

        firmwareConfigPageSteps.open()
                .verifyConfigsCount(2);

        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Version")
                .typeSingleSearchParam("wrong-version")
                .waitNotFoundResultMessage()
                .typeSingleSearchParam(firmwareConfigs.get(0).getFirmwareVersion())
                .clickViewButton();
        firmwareConfigPageSteps.verifyViewModalWindow(firmwareConfigs.get(0));
    }

    @Test
    public void searchByModel() throws IOException {
        List<FirmwareConfig> firmwareConfigs = FirmwareConfigUtils.createAndSaveFirmwareConfigs();

        firmwareConfigPageSteps.open()
                .verifyConfigsCount(2);
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Model")
                .typeSingleSearchParam("wrongModel")
                .waitNotFoundResultMessage()
                .typeSingleSearchParam(firmwareConfigs.get(0).getSupportedModelIds().iterator().next())
                .clickViewButton();
        firmwareConfigPageSteps.verifyViewModalWindow(firmwareConfigs.get(0));
    }

    @Test
    public void deleteBySearch() throws IOException {
        List<FirmwareConfig> firmwareConfigs = FirmwareConfigUtils.createAndSaveFirmwareConfigs();

        firmwareConfigPageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Description")
                .typeSingleSearchParam(firmwareConfigs.get(0).getDescription())
                .clickDeleteButton()
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(firmwareConfigs.get(0).getDescription())
                .typeSingleSearchParam("")
                .verifyEntitiesCount(1);
    }
}
