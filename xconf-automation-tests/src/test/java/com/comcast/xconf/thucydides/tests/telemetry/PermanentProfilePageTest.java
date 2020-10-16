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
 * Author: rdolomansky
 * Created: 3/14/16  8:19 PM
 */
package com.comcast.xconf.thucydides.tests.telemetry;

import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.telemetry.PermanentProfilePageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.telemetry.TelemetryUtils;
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
public class PermanentProfilePageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private PermanentProfilePageSteps permanentProfilePageSteps;

    @Steps
    private GenericSteps genericSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        GenericTestUtils.deleteEntities(TelemetryUtils.PERMANENT_PROFILE_URL, PermanentTelemetryProfile.class);
    }

    @Test
    public void createPermanentProfile() throws Exception {
        TelemetryUtils.createAndSavePermanentTelemetryProfiles();

        final String name = "testName";
        permanentProfilePageSteps.open();
        genericSteps.clickCreateButton();
        permanentProfilePageSteps
                .typeName(name)
                .typeSchedule("123")
                .selectProtocol("HTTP")
                .typeUrl("localhost.com")

                .typeHeader("1")
                .typeContent("1")
                .typeType("1")
                .typePollingFrequency("1");

        genericSteps.clickSaveButton();
        genericSteps.waitSuccessfullySavedToaster(name);

    }

    @Test
    public void editPermanentProfile() throws Exception {
        final PermanentTelemetryProfile permanentTelemetryProfile = TelemetryUtils.createAndSavePermanentProfile();

        final String newSchedule = "456";
        permanentProfilePageSteps.open();
        genericSteps.clickEditButton();
        permanentProfilePageSteps.typeSchedule(newSchedule);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(permanentTelemetryProfile.getName());
    }

    @Test
    public void deletePermanentProfile() throws Exception {
        final PermanentTelemetryProfile permanentTelemetryProfile = TelemetryUtils.createAndSavePermanentProfile();

        permanentProfilePageSteps.open();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(permanentTelemetryProfile.getName(), "Permanent Profile")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(permanentTelemetryProfile.getName());
    }

    @Test
    public void exportAllPermanentProfiles() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        TelemetryUtils.createAndSavePermanentTelemetryProfiles();

        permanentProfilePageSteps.open();
        genericSteps.clickExportAllButton()
                .checkSavedFile(dirForDownload, "allPermanentProfiles.json");
    }

    @Test
    public void exportOnePermanentProfile() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        PermanentTelemetryProfile permanentTelemetryProfile = TelemetryUtils.createAndSavePermanentProfile();

        permanentProfilePageSteps.open();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload, "permanentProfile_" + permanentTelemetryProfile.getId() + ".json");
    }

    @Test
    public void searchSettingProfile() throws Exception {
        TelemetryUtils.createAndSavePermanentTelemetryProfile("Profile123");
        TelemetryUtils.createAndSavePermanentTelemetryProfile("Profile456");

        permanentProfilePageSteps.open();
        genericSteps.typeSingleSearchParam("123");
        permanentProfilePageSteps.verifyPermanentProfilesCount(1);
    }

    @Test
    public void searchTwoProfiles() throws Exception {
        TelemetryUtils.createAndSavePermanentTelemetryProfile("Profile123");
        TelemetryUtils.createAndSavePermanentTelemetryProfile("Profile456");

        permanentProfilePageSteps.open();
        genericSteps.typeSingleSearchParam("proFile");
        permanentProfilePageSteps.verifyPermanentProfilesCount(2);
    }

    @Test
    public void showNotFound() throws Exception {
        TelemetryUtils.createAndSavePermanentTelemetryProfile("Profile123");
        TelemetryUtils.createAndSavePermanentTelemetryProfile("Profile456");
        permanentProfilePageSteps.open();
        genericSteps.typeSingleSearchParam("NOT_FOUND");
        permanentProfilePageSteps.waitNotFoundResultMessage();
    }

    @Test
    public void deleteBySearch() throws IOException {
        List<PermanentTelemetryProfile> profiles = TelemetryUtils.createAndSavePermanentTelemetryProfiles();
        permanentProfilePageSteps.open();
        genericSteps.typeSingleSearchParam(profiles.get(0).getName());
        genericSteps.clickDeleteButton()
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(profiles.get(0).getName())
                .typeSingleSearchParam("");
        permanentProfilePageSteps.verifyPermanentProfilesCount(1);
    }
}
