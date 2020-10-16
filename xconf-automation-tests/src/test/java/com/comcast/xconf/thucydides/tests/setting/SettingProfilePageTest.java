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
 * Author: Igor Kostrov
 * Created: 3/28/2016
*/
package com.comcast.xconf.thucydides.tests.setting;

import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingType;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.setting.SettingProfilePageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.setting.SettingUtils;
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
public class SettingProfilePageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private GenericSteps genericSteps;

    @Steps
    private SettingProfilePageSteps settingProfilePageSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        GenericTestUtils.deleteEntities(SettingUtils.SETTING_PROFILE_URL, SettingProfile.class);
    }

    @Test
    public void createSettingProfile() throws Exception {
        final String name = "testName";
        settingProfilePageSteps.open();
        genericSteps.clickCreateButton();
        settingProfilePageSteps
                .typeName(name)
                .selectSettingType(SettingType.EPON.toString())
                .typeEntryKey("key")
                .typeEntryValue("value");

        genericSteps
                .clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void editSettingProfile() throws Exception {
        final SettingProfile profile = SettingUtils.createAndSaveSettingProfile();

        settingProfilePageSteps.open();
        genericSteps.clickEditButton();
        settingProfilePageSteps.selectSettingType(SettingType.PARTNER_SETTINGS.toString());
        genericSteps
                .clickSaveButton()
                .waitSuccessfullySavedToaster(profile.getSettingProfileId());
    }

    @Test
    public void deleteSettingProfile() throws Exception {
        final SettingProfile profile = SettingUtils.createAndSaveSettingProfile();

        settingProfilePageSteps.open();
        genericSteps
                .clickDeleteButton()
                .verifyDeleteModalWindow(profile.getSettingProfileId(), "Setting Profile")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(profile.getSettingProfileId());
    }

    @Test
    public void exportAllSettingProfiles() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        SettingUtils.createAndSaveSettingProfile("name1");
        SettingUtils.createAndSaveSettingProfile("name2");

        settingProfilePageSteps.open();
        genericSteps
                .clickExportAllButton()
                .checkSavedFile(dirForDownload, "allSettingProfiles.json");
    }

    @Test
    public void exportOneSettingProfile() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        SettingProfile profile = SettingUtils.createAndSaveSettingProfile();

        settingProfilePageSteps.open();
        genericSteps
                .clickExportOneButton()
                .checkSavedFile(dirForDownload, "settingProfile_" + profile.getId() + ".json");
    }

    @Test
    public void searchSettingProfile() throws Exception {
        SettingUtils.createAndSaveSettingProfile("Profile123");
        SettingUtils.createAndSaveSettingProfile("Profile456");

        settingProfilePageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Name")
                .typeSingleSearchParam("123");
        settingProfilePageSteps.verifySettingProfilesCount(1);
    }

    @Test
    public void searchTwoProfiles() throws Exception {
        SettingUtils.createAndSaveSettingProfile("Profile123");
        SettingUtils.createAndSaveSettingProfile("Profile456");

        settingProfilePageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Name")
                .typeSingleSearchParam("profile");
        settingProfilePageSteps.verifySettingProfilesCount(2);
    }

    @Test
    public void showNotFound() throws Exception {
        SettingUtils.createAndSaveSettingProfile("Profile123");
        SettingUtils.createAndSaveSettingProfile("Profile456");

        settingProfilePageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Name")
                .typeSingleSearchParam("wrongValue");
        settingProfilePageSteps.waitNotFoundResultMessage();
    }

    @Test
    public void deleteBySearch() throws IOException {
        List<SettingProfile> settingProfiles = SettingUtils.createAndSaveSettingProfiles();

        settingProfilePageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Name")
                .typeSingleSearchParam(settingProfiles.get(0).getSettingProfileId())
        .clickDeleteButton()
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(settingProfiles.get(0).getSettingProfileId())
                .typeSingleSearchParam("");
        settingProfilePageSteps.verifySettingProfilesCount(1);
    }
}
