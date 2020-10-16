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
 * Created: 3/22/16  1:38 PM
 */
package com.comcast.xconf.thucydides.tests.dcm;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.logupload.LogUploadSettings;
import com.comcast.xconf.logupload.VodSettings;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.RuleBuilderSteps;
import com.comcast.xconf.thucydides.steps.dcm.*;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.dcm.*;
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
public class FormulaPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private FormulaPageSteps formulaPageSteps;
    @Steps
    private GenericSteps genericSteps;
    @Steps
    private RuleBuilderSteps ruleBuilderSteps;
    @Steps
    private DeviceSettingsEditSteps deviceSettingsEditSteps;
    @Steps
    private LogUploadSettingsEditSteps logUploadSettingsEditSteps;
    @Steps
    private VodSettingsEditSteps vodSettingsEditSteps;
    @Steps
    private DeviceSettingsViewSteps deviceSettingsViewSteps;
    @Steps
    private VodSettingsViewSteps vodSettingsViewSteps;
    @Steps
    private LogUploadSettingsViewSteps logUploadSettingsViewSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        DeviceSettingsUtils.doCleanup();
    }

    @Test
    public void createFormula() throws Exception {
        FormulaUtils.createAndSaveFormulas();

        String name = "formulaName";

        formulaPageSteps.open();
        genericSteps.clickCreateButton();
        formulaPageSteps.typeName(name)
                .typeDescription("description")
                .typePercentage(100)
                .verifyIsDefaultFormula(true)
                .verifyValidPriority(3)
                .typeL1Percentage(10)
                .typeL2Percentage(15)
                .typeL3Percentage(42);
        ruleBuilderSteps.addCondition("mac", StandardOperation.IS, "11:11:11:11:11:11");
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void editFormula() throws Exception {
        String name = "editedFormulaName";
        FormulaUtils.createAndSaveDefaultFormula();

        formulaPageSteps.open();
        genericSteps.clickEditButton();
        formulaPageSteps.typeName(name)
                .typePercentage(50)
                .verifyIsDefaultFormula(false);
        ruleBuilderSteps.clickRuleConditionForEdit()
                        .addCondition("mac", StandardOperation.IS, "11:11:11:11:11:11");
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void exportAllFormulas() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        FormulaUtils.createAndSaveDefaultFormula();

        formulaPageSteps.open();
        genericSteps.clickExportAllButton()
            .checkSavedFile(dirForDownload, "allFormulas.json");
    }

    @Test
    public void exportOneFormula() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        DCMGenericRule formulaToExport = FormulaUtils.createAndSaveDefaultFormula();

        formulaPageSteps.open();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload, "formula_" + formulaToExport.getId() + ".json");
    }

    @Test
    public void deleteFormula() throws Exception {
        DCMGenericRule formulaToDelete = FormulaUtils.createAndSaveDefaultFormula();

        formulaPageSteps.open();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(formulaToDelete.getName(), "Formula")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(formulaToDelete.getName());
    }

    @Test
    public void createDeviceSettings() throws Exception {
        String name = "deviceSettingsName";
        FormulaUtils.createAndSaveDefaultFormula();

        formulaPageSteps.open();
        genericSteps.clickEditButton();
        formulaPageSteps.clickCreateDeviceSettingsButton();
        deviceSettingsEditSteps.fillForm(name);
        genericSteps.clickModalSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void createLogUploadSettings() throws Exception {
        String name = "logUploadSettings";
        FormulaUtils.createAndSaveDefaultFormula();
        UploadRepositoryUtils.createAndSaveDefaultUploadRepository();

        formulaPageSteps.open();
        genericSteps.clickEditButton();
        formulaPageSteps.clickCreateLogUploadSettingsButton();
        logUploadSettingsEditSteps.fillForm(name);
        genericSteps.clickModalSaveButton()
            .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void createVodSettings() throws Exception {
        String name = "vodSettingsName";
        FormulaUtils.createAndSaveDefaultFormula();

        formulaPageSteps.open();
        genericSteps.clickEditButton();
        formulaPageSteps.clickCreateVodSettingsButton();
        vodSettingsEditSteps.fillFormOnCreate(name);
        genericSteps.clickModalSaveButton()
            .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void viewDeviceSettings() throws Exception {
        FormulaUtils.createAndSaveDefaultFormula();
        DeviceSettings expectedDeviceSettings = DeviceSettingsUtils.createAndSaveDefaultDeviceSettings();

        formulaPageSteps.open()
            .clickViewDeviceSettingsButton();
        deviceSettingsViewSteps.verifyViewPage(expectedDeviceSettings);
    }

    @Test
    public void viewVodSettings() throws Exception {
        FormulaUtils.createAndSaveDefaultFormula();
        VodSettings expectedVodSettings = VodSettingsUtils.createAndSaveDefaultVodSettings();

        formulaPageSteps.open()
            .clickViewVodSettingsButton();
        vodSettingsViewSteps.verifyViewPage(expectedVodSettings);
    }

    @Test
    public void viewLogUploadSettings() throws Exception {
        FormulaUtils.createAndSaveDefaultFormula();
        LogUploadSettings expectedLogUploadSettings = LogUploadSettingsUtils.createAndSaveDefaultLogUploadSettings();

        formulaPageSteps.open()
            .clickViewLogUploadSettingsButton();
        logUploadSettingsViewSteps.verifyViewPage(expectedLogUploadSettings);
    }

    @Test
    public void deleteBySearch() throws Exception {
        List<DCMGenericRule> formulas = FormulaUtils.createAndSaveFormulas();
        formulaPageSteps.open();
        genericSteps.typeSingleSearchParam(formulas.get(0).getName())
                .clickDeleteButton()
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(formulas.get(0).getName())
                .typeSingleSearchParam("");
        formulaPageSteps.verifyFormulasCount(1);
    }

    @Test
    public void searchByKeyAndValue() throws Exception {
        List<DCMGenericRule> formulas = FormulaUtils.createAndSaveFormulas();
        formulaPageSteps.open();
        String freeArg = formulas.get(0).getCondition().getFreeArg().getName();
        String fixedArg = formulas.get(0).getCondition().getFixedArg().getValue().toString();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Key and Value")
                .typeMultipleSearchParamByKey(freeArg)
                .typeMultipleSearchParamByValue(fixedArg);
        formulaPageSteps.verifyFormulasCount(1);
    }
}
