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
 * Created: 3/28/16  1:00 PM
 */
package com.comcast.xconf.thucydides.tests.dcm;

import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.dcm.DeviceSettingsEditSteps;
import com.comcast.xconf.thucydides.steps.dcm.DeviceSettingsPageSteps;
import com.comcast.xconf.thucydides.steps.dcm.DeviceSettingsViewSteps;
import com.comcast.xconf.thucydides.steps.dcm.GenericDcmSettingsSteps;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.dcm.DeviceSettingsUtils;
import com.comcast.xconf.thucydides.util.dcm.FormulaUtils;
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
public class DeviceSettingsPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private DeviceSettingsPageSteps deviceSettingsPageSteps;
    @Steps
    private GenericSteps genericSteps;
    @Steps
    private DeviceSettingsViewSteps deviceSettingsViewSteps;
    @Steps
    private DeviceSettingsEditSteps deviceSettingsEditSteps;
    @Steps
    private GenericDcmSettingsSteps genericDcmSettingsSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        DeviceSettingsUtils.doCleanup();
    }

    @Test
    public void editDeviceSettings() throws Exception {
        DeviceSettingsUtils.createAndSaveDefaultDeviceSettings();
        String name = "editedName";

        deviceSettingsPageSteps.open();
        genericSteps.clickEditButton();
        deviceSettingsEditSteps.fillForm(name);
        genericSteps.clickModalSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void viewDeviceSettings() throws Exception {
        DeviceSettings expectedDeviceSettings = DeviceSettingsUtils.createAndSaveDefaultDeviceSettings();

        deviceSettingsPageSteps.open();
        genericSteps.clickViewButton();
        deviceSettingsViewSteps.verifyViewPage(expectedDeviceSettings);
    }

    @Test
    public void deleteDeviceSettings() throws Exception {
        DeviceSettings deviceSettingsToDelete = DeviceSettingsUtils.createAndSaveDefaultDeviceSettings();

        deviceSettingsPageSteps.open();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(deviceSettingsToDelete.getName(), "Device Settings")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(deviceSettingsToDelete.getName());
    }

    @Test
    public void viewFormula() throws Exception {
        DCMGenericRule expectedFormula = FormulaUtils.createDefaultFormula();
        DeviceSettingsUtils.createAndSaveDefaultDeviceSettings();

        deviceSettingsPageSteps.open();
        genericDcmSettingsSteps.clickViewFormulaButton()
                .viewFormula(expectedFormula);
    }

    @Test
    public void searchByName() throws Exception {
        List<DeviceSettings> deviceSettingsList = DeviceSettingsUtils.createAndSaveDeviceSettingsList();

        deviceSettingsPageSteps.open();
        genericSteps.typeSingleSearchParam(deviceSettingsList.get(0).getName())
                .verifyEntitiesCount(1)
                .typeSingleSearchParam("wrongValue")
                .waitNotFoundResultMessage()
                .typeSingleSearchParam("")
                .verifyEntitiesCount(2);
    }
}
