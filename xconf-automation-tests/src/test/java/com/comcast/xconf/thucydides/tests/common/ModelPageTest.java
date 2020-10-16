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
 * Created: 3/9/16  2:38 PM
 */
package com.comcast.xconf.thucydides.tests.common;

import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.common.EnvModelPageSteps;
import com.comcast.xconf.thucydides.steps.common.ModelPageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.common.ModelUtils;
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
public class ModelPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private ModelPageSteps modelPageSteps;
    @Steps
    private GenericSteps genericSteps;
    @Steps
    private EnvModelPageSteps envModelPageSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        ModelUtils.doCleanup();
    }

    @Test
    public void createNewModel() throws Exception {
        ModelUtils.createAndSaveModels();

        String id = "TESTID";

        modelPageSteps.open();
        genericSteps.clickCreateButton()
                .typeId(id);
        envModelPageSteps.typeDescription("testDescription");
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(id);
    }

    @Test
    public void editModel() throws Exception {
        Model modelToEdit = ModelUtils.createAndSaveDefaultModel();
        String newDescription = "testDescription";

        modelPageSteps.open();
        genericSteps.clickEditButton()
                .checkIdInputIsDisabled();
        envModelPageSteps.typeDescription(newDescription);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(modelToEdit.getId());
    }

    @Test
    public void deleteModel() throws Exception {
        Model modelToDelete = ModelUtils.createAndSaveDefaultModel();

        modelPageSteps.open();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(modelToDelete.getId(), "Model")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(modelToDelete.getId());
    }

    @Test
    public void exportAllModels() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        ModelUtils.createAndSaveDefaultModel();

        modelPageSteps.open();
        genericSteps.clickExportAllButton()
                .checkSavedFile(dirForDownload, "allModels.json");
    }

    @Test
    public void exportOneModel() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        Model modelToExport = ModelUtils.createAndSaveDefaultModel();

        modelPageSteps.open();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload, "model_" + modelToExport.getId() + ".json");
    }

    @Test
    public void deleteModelBySearch() throws Exception {
        List<Model> models = ModelUtils.createAndSaveModels();

        modelPageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Id")
                .typeSingleSearchParam(models.get(0).getId())
                .clickDeleteButton()
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(models.get(0).getId())
                .typeSingleSearchParam("")
                .verifyEntitiesCount(1);
    }

    @Test
    public void searchById() throws Exception {
        List<Model> models = ModelUtils.createAndSaveModels();

        modelPageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Id").
                typeSingleSearchParam(models.get(0).getId())
                .verifyEntitiesCount(1)
                .typeSingleSearchParam("wrongId")
                .waitNotFoundResultMessage()
                .typeSingleSearchParam("")
                .verifyEntitiesCount(2);
    }

    @Test
    public void searchByDescription() throws Exception {
        List<Model> models = ModelUtils.createAndSaveModels();

        modelPageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Description").
                typeSingleSearchParam(models.get(0).getDescription())
                .verifyEntitiesCount(1)
                .typeSingleSearchParam("wrongDescription")
                .waitNotFoundResultMessage()
                .typeSingleSearchParam("")
                .verifyEntitiesCount(2);
    }
}
