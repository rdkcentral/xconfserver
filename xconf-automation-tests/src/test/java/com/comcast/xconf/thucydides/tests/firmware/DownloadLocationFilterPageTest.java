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
 * Created: 3/16/16  3:37 PM
 */
package com.comcast.xconf.thucydides.tests.firmware;

import com.comcast.xconf.estbfirmware.DownloadLocationRoundRobinFilterValue;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.firmware.DownloadLocationFilterEditSteps;
import com.comcast.xconf.thucydides.steps.firmware.DownloadLocationFilterPageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.common.ModelUtils;
import com.comcast.xconf.thucydides.util.firmware.DownloadLocationFilterUtils;
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

@RunWith(ThucydidesRunner.class)
public class DownloadLocationFilterPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private DownloadLocationFilterPageSteps downloadLocationFilterPageSteps;
    @Steps
    private DownloadLocationFilterEditSteps downloadLocationFilterEditSteps;
    @Steps
    private GenericSteps genericSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        DownloadLocationFilterUtils.doCleanup();
    }

    @Test
    public void viewDownloadLocationFilter() throws Exception {
        DownloadLocationRoundRobinFilterValue filter = DownloadLocationFilterUtils.createAndSaveDefaultRoundRobinFilter();
        DownloadLocationRoundRobinFilterValue.Location expectedIpv4Location = filter.getLocations().get(0);
        DownloadLocationRoundRobinFilterValue.Location expectedIpv6Location = filter.getIpv6locations().get(0);

        downloadLocationFilterPageSteps.open()
                .verifyHttpLocation(filter.getHttpLocation())
                .verifyHttpFullUrlLocation(filter.getHttpFullUrlLocation())
                .verifyNeverUseHttpIsChecked(true)
                .verifyNeverUseHttpIsDisabled()
                .verifyIpv4Location(expectedIpv4Location.getLocationIp(), expectedIpv4Location.getPercentage())
                .verifyIpv6Location(expectedIpv6Location.getLocationIp(), expectedIpv6Location.getPercentage());
    }

    @Test
    public void editDownloadLocationFilter() throws Exception {
        DownloadLocationFilterUtils.createAndSaveDefaultRoundRobinFilter();
        ModelUtils.createAndSaveDefaultModel();

        downloadLocationFilterPageSteps.open();
        genericSteps.clickEditButton();
        downloadLocationFilterEditSteps.typeHttpLocation("editTest.com")
                .typeFullUrlHttpLocation("http://editTest.com")
                .clickNeverUseHttp()
                .verifyNeverUseHttpIsChecked(false)
                .clickRogueModelsListItem()
                .verifyRogueModelsListItemIsSelected(true)
                .clickRogueModelsListItem()
                .verifyRogueModelsListItemIsSelected(false)
                .typeFirmwareVersions("editTestFirmwareVersion")
                .removeIpv4Location()
                .addIpv4Location()
                .typeIpv4LocationIp("2.2.2.2")
                .typeIpv4LocationPercentage(100)
                .removeIpv6Location()
                .addIpv6Location()
                .typeIpv6LocationIp("2::2")
                .typeIpv6LocationPercentage(100);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster("Download Location Filter");
    }

    @Test
    public void exportDownloadLocationFilter() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        DownloadLocationFilterUtils.createAndSaveDefaultRoundRobinFilter();

        downloadLocationFilterPageSteps.open();
        genericSteps.clickExportAllButton()
                .checkSavedFile(dirForDownload, "roundRobinFilter.json");
    }
}
