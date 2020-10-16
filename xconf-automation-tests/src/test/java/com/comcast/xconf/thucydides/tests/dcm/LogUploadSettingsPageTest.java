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
 * Created: 3/28/16  5:08 PM
 */
package com.comcast.xconf.thucydides.tests.dcm;

import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.LogUploadSettings;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.dcm.GenericDcmSettingsSteps;
import com.comcast.xconf.thucydides.steps.dcm.LogUploadSettingsEditSteps;
import com.comcast.xconf.thucydides.steps.dcm.LogUploadSettingsPageSteps;
import com.comcast.xconf.thucydides.steps.dcm.LogUploadSettingsViewSteps;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.dcm.FormulaUtils;
import com.comcast.xconf.thucydides.util.dcm.LogUploadSettingsUtils;
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

import java.util.List;

@RunWith(ThucydidesRunner.class)
public class LogUploadSettingsPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private LogUploadSettingsPageSteps logUploadSettingsPageSteps;
    @Steps
    private GenericSteps genericSteps;
    @Steps
    private LogUploadSettingsEditSteps logUploadSettingsEditSteps;
    @Steps
    private LogUploadSettingsViewSteps logUploadSettingsViewSteps;
    @Steps
    private GenericDcmSettingsSteps genericDcmSettingsSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        LogUploadSettingsUtils.doCleanup();
    }

    @Test
    public void editLogUploadSettings() throws Exception {
        LogUploadSettingsUtils.createAndSaveDefaultLogUploadSettings();
        String name = "editedName";

        logUploadSettingsPageSteps.open();
        genericSteps.clickEditButton();
        logUploadSettingsEditSteps.fillForm(name);
        genericSteps.clickModalSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void viewLogUploadSettings() throws Exception {
        LogUploadSettings expectedLogUploadSettings = LogUploadSettingsUtils.createAndSaveDefaultLogUploadSettings();


        logUploadSettingsPageSteps.open();
        genericSteps.clickViewButton();
        logUploadSettingsViewSteps.verifyViewPage(expectedLogUploadSettings);
    }

    @Test
    public void deleteLogUploadSettings() throws Exception {
        LogUploadSettings logUploadSettingsToDelete = LogUploadSettingsUtils.createAndSaveDefaultLogUploadSettings();

        logUploadSettingsPageSteps.open();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(logUploadSettingsToDelete.getName(), "Log Upload Settings")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(logUploadSettingsToDelete.getName());
    }

    @Test
    public void viewFormula() throws Exception {
        DCMGenericRule expectedFormula = FormulaUtils.createDefaultFormula();
        LogUploadSettingsUtils.createAndSaveDefaultLogUploadSettings();

        logUploadSettingsPageSteps.open();
        genericDcmSettingsSteps.clickViewFormulaButton()
                .viewFormula(expectedFormula);
    }

    @Test
    public void searchByName() throws Exception {
        List<LogUploadSettings> uploadSettingsList = LogUploadSettingsUtils.createAndSaveLogUploadSettingsList();

        logUploadSettingsPageSteps.open();
        genericSteps.typeSingleSearchParam(uploadSettingsList.get(0).getName())
                .verifyEntitiesCount(1)
                .typeSingleSearchParam("wrongValue")
                .waitNotFoundResultMessage()
                .typeSingleSearchParam("")
                .verifyEntitiesCount(2);
    }
}
