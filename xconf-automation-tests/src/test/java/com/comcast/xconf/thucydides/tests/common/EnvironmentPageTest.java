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
 * Created: 3/1/16  4:21 PM
 */
package com.comcast.xconf.thucydides.tests.common;

import com.comcast.xconf.Environment;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.common.EnvModelPageSteps;
import com.comcast.xconf.thucydides.steps.common.EnvironmentPageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.common.EnvironmentUtils;
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
public class EnvironmentPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private EnvironmentPageSteps environmentPageSteps;
    @Steps
    private GenericSteps genericSteps;
    @Steps
    private EnvModelPageSteps envModelPageSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        EnvironmentUtils.doCleanup();
    }

    @Test
    public void createNewEnvironment() throws Exception {
        EnvironmentUtils.createAndSaveEnvironments();

        String id = "TESTID";

        environmentPageSteps.open();
        genericSteps.clickCreateButton()
                .typeId(id);
        envModelPageSteps.typeDescription("testDescription");
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(id);
    }

    @Test
    public void editEnvironment() throws Exception {
        Environment envToEdit = EnvironmentUtils.createAndSaveDefaultEnvironment();
        String newDescription = "testDescription";

        environmentPageSteps.open();
        genericSteps.clickEditButton()
                .checkIdInputIsDisabled();
        envModelPageSteps.typeDescription(newDescription);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(envToEdit.getId());
    }

    @Test
    public void deleteEnvironment() throws Exception {
        Environment envToDelete = EnvironmentUtils.createAndSaveDefaultEnvironment();

        environmentPageSteps.open();
        genericSteps.clickDeleteButton()
            .verifyDeleteModalWindow(envToDelete.getId(), "Environment")
            .clickModalOkButton()
            .waitSuccessfullyDeletedToaster(envToDelete.getId());
    }

    @Test
    public void exportAllEnvironments() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        EnvironmentUtils.createAndSaveDefaultEnvironment();

        environmentPageSteps.open();
        genericSteps.clickExportAllButton()
                .checkSavedFile(dirForDownload, "allEnvironments.json");
    }

    @Test
    public void exportOneEnvironment() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        Environment environmentToExport = EnvironmentUtils.createAndSaveDefaultEnvironment();

        environmentPageSteps.open();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload, "environment_" + environmentToExport.getId() + ".json");
    }

    @Test
    public void deleteEnvironmentBySearch() throws Exception {
        List<Environment> environments = EnvironmentUtils.createAndSaveEnvironments();

        environmentPageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Id")
                .typeSingleSearchParam(environments.get(0).getId())
                .clickDeleteButton()
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(environments.get(0).getId())
                .typeSingleSearchParam("")
                .verifyEntitiesCount(1);
    }

    @Test
    public void searchById() throws IOException {
        List<Environment> environments = EnvironmentUtils.createAndSaveEnvironments();

        environmentPageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Id")
                .typeSingleSearchParam(environments.get(0).getId())
                .verifyEntitiesCount(1).typeSingleSearchParam("wrongId")
                .waitNotFoundResultMessage()
                .typeSingleSearchParam("")
                .verifyEntitiesCount(2);
    }

    @Test
    public void searchByDescription() throws IOException {
        List<Environment> environments = EnvironmentUtils.createAndSaveEnvironments();

        environmentPageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Description")
                .typeSingleSearchParam(environments.get(0).getDescription())
                .verifyEntitiesCount(1).typeSingleSearchParam("wrongDescription")
                .waitNotFoundResultMessage()
                .typeSingleSearchParam("")
                .verifyEntitiesCount(2);
    }
}
