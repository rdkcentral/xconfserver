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
 * Created: 3/29/16  1:59 PM
 */
package com.comcast.xconf.thucydides.tests.dcm;

import com.comcast.xconf.logupload.UploadRepository;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.dcm.UploadRepositoryPageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.dcm.UploadRepositoryUtils;
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
public class UploadRepositoryPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private GenericSteps genericSteps;
    @Steps
    private UploadRepositoryPageSteps uploadRepositoryPageSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        UploadRepositoryUtils.doCleanup();
    }

    @Test
    public void createNewUploadRepository() throws Exception {
        String name = "uploadRepositoryName";

        uploadRepositoryPageSteps.open();
        genericSteps.clickCreateButton();
        uploadRepositoryPageSteps.fillForm(name);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void editUploadRepository() throws Exception {
        String name = "editedName";
        UploadRepositoryUtils.createAndSaveDefaultUploadRepository();

        uploadRepositoryPageSteps.open();
        genericSteps.clickEditButton();
        uploadRepositoryPageSteps.fillForm(name);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void deleteUploadRepository() throws Exception {
        String repositoryToDeleteName = UploadRepositoryUtils.createAndSaveDefaultUploadRepository().getName();

        uploadRepositoryPageSteps.open();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(repositoryToDeleteName, "Upload Repository")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(repositoryToDeleteName);
    }

    @Test
    public void exportAllUploadRepositories() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        UploadRepositoryUtils.createAndSaveDefaultUploadRepository();

        uploadRepositoryPageSteps.open();
        genericSteps.clickExportAllButton()
                .checkSavedFile(dirForDownload, "allUploadRepositories.json");
    }

    @Test
    public void exportOneEnvironment() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        UploadRepository uploadRepository = UploadRepositoryUtils.createAndSaveDefaultUploadRepository();

        uploadRepositoryPageSteps.open();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload, "uploadRepository_" + uploadRepository.getId() + ".json");
    }

    @Test
    public void searchByName() throws Exception {
        List<UploadRepository> uploadRepositoryList = UploadRepositoryUtils.createAndSaveUploadRepositoryList();

        uploadRepositoryPageSteps.open();
        genericSteps.typeSingleSearchParam(uploadRepositoryList.get(0).getName())
                .verifyEntitiesCount(1)
                .typeSingleSearchParam("wrongValue")
                .waitNotFoundResultMessage()
                .typeSingleSearchParam("")
                .verifyEntitiesCount(2);
    }
}
