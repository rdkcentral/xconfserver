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
 * Created: 3/29/16  12:27 PM
 */
package com.comcast.xconf.thucydides.tests.dcm;

import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.VodSettings;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.dcm.GenericDcmSettingsSteps;
import com.comcast.xconf.thucydides.steps.dcm.VodSettingsEditSteps;
import com.comcast.xconf.thucydides.steps.dcm.VodSettingsPageSteps;
import com.comcast.xconf.thucydides.steps.dcm.VodSettingsViewSteps;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.dcm.FormulaUtils;
import com.comcast.xconf.thucydides.util.dcm.VodSettingsUtils;
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
public class VodSettingsPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private GenericSteps genericSteps;
    @Steps
    private GenericDcmSettingsSteps genericDcmSettingsSteps;
    @Steps
    private VodSettingsPageSteps vodSettingsPageSteps;
    @Steps
    private VodSettingsEditSteps vodSettingsEditSteps;
    @Steps
    private VodSettingsViewSteps vodSettingsViewSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        VodSettingsUtils.doCleanup();
    }

    @Test
    public void editVodSettings() throws Exception {
        VodSettingsUtils.createAndSaveDefaultVodSettings();
        String name = "editedName";

        vodSettingsPageSteps.open();
        genericSteps.clickEditButton();
        vodSettingsEditSteps.fillFormOnEdit(name);
        genericSteps.clickModalOkButton()
            .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void viewVodSettings() throws Exception {
        VodSettings expectedVodSettings = VodSettingsUtils.createAndSaveDefaultVodSettings();

        vodSettingsPageSteps.open();
        genericSteps.clickViewButton();
        vodSettingsViewSteps.verifyViewPage(expectedVodSettings);
    }

    @Test
    public void deleteVodSettings() throws Exception {
        VodSettings vodSettingsToDelete = VodSettingsUtils.createAndSaveDefaultVodSettings();

        vodSettingsPageSteps.open();
        genericSteps.clickDeleteButton()
            .verifyDeleteModalWindow(vodSettingsToDelete.getName(), "Vod Settings")
            .clickModalOkButton()
            .waitSuccessfullyDeletedToaster(vodSettingsToDelete.getName());
    }

    @Test
    public void viewFormula() throws Exception {
        DCMGenericRule expectedFormula = FormulaUtils.createDefaultFormula();
        VodSettingsUtils.createAndSaveDefaultVodSettings();

        vodSettingsPageSteps.open();
        genericDcmSettingsSteps.clickViewFormulaButton()
                .viewFormula(expectedFormula);
    }

    @Test
    public void searchByName() throws Exception {
        List<VodSettings> vodSettingsList = VodSettingsUtils.createAndSaveVodSettingsList();

        vodSettingsPageSteps.open();
        genericSteps.typeSingleSearchParam(vodSettingsList.get(0).getName())
                .verifyEntitiesCount(1)
                .typeSingleSearchParam("wrongValue")
                .waitNotFoundResultMessage()
                .typeSingleSearchParam("")
                .verifyEntitiesCount(2);
    }
}
