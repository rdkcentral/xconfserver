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
 * Created: 3/17/16  6:00 PM
 */
package com.comcast.xconf.thucydides.tests.firmware;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.estbfirmware.EnvModelPercentage;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.PercentFilterValue;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.firmware.PercentFilterEditPageSteps;
import com.comcast.xconf.thucydides.steps.firmware.PercentFilterPageSteps;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.common.GenericNamespacedListUtils;
import com.comcast.xconf.thucydides.util.firmware.FirmwareConfigUtils;
import com.comcast.xconf.thucydides.util.firmware.PercentFilterUtils;
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

import java.util.Map;
import java.util.UUID;

import static com.comcast.xconf.thucydides.util.firmware.PercentFilterUtils.createAndSavePercentFilter;

@RunWith(ThucydidesRunner.class)
public class PercentFilterPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private PercentFilterPageSteps percentFilterPageSteps;
    @Steps
    private PercentFilterEditPageSteps percentFilterEditPageSteps;
    @Steps
    private GenericSteps genericSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        PercentFilterUtils.doCleanup();
    }

    @Test
    public void viewPercentFilter() throws Exception {
        createAndSavePercentFilter();
        GenericNamespacedList whiteList = GenericNamespacedListUtils.createDefaultIpList();

        percentFilterPageSteps.open()
                .verifyPercent(100)
                .verifyWhitelist(whiteList.getId())
                .verifyPercentIsDisabled()
                .verifyWhiteListIsDisabled();
    }

    @Test
    public void editPercentFilterWhenEnvironmentModelRuleIsNotSelected() throws Exception {
        createAndSavePercentFilter();
        GenericNamespacedList whitelist = GenericNamespacedListUtils.createAndSaveDefaultIpList();
        percentFilterPageSteps.open();
        genericSteps.clickEditButton();
        percentFilterPageSteps.typePercentage("100")
                .clickWhitelistSelect(whitelist.getId());
        genericSteps.clickSaveButton();

        percentFilterPageSteps.open()
                .verifyPercent(100)
                .verifyWhitelist(whitelist.getId());
    }

    @Test
    public void editPercentFilterWhenEnvironmentModelRuleIsSelected() throws Exception {
        createAndSavePercentFilter();
        GenericNamespacedList whitelist = GenericNamespacedListUtils.createAndSaveDefaultIpList();
        percentFilterPageSteps.open();
        percentFilterPageSteps.clickEnvModelEditButton();

        percentFilterEditPageSteps
                .typeEnvModelPercentage("90")
                .selectEnvModelWhitelist(whitelist.getId())
                .clickIsActive()
                .selectLastKnownGood(FirmwareConfigUtils.defaultVersion)
                .clickFirmwareVersionCheckRequired()
                .clickFirmwareVersionCheckRequired()
                .clickFirmwareVersion()
                .selectIntermediateVersion(FirmwareConfigUtils.defaultVersion)
                .clickRebootImmediately()
                .clickFirmwareVersion();

        genericSteps
                .clickSaveButton()
                .waitSuccessfullySavedToaster("Percent Filter");
    }

    @Test
    public void verifyLastKnownGoodIsDisabledWhenHundredPercentage() throws Exception {
        createAndSavePercentFilter();
        percentFilterPageSteps.open();
        percentFilterPageSteps.clickEnvModelEditButton();

        percentFilterEditPageSteps
                .typeEnvModelPercentage("90")
                .clickIsActive()
                .selectLastKnownGood(FirmwareConfigUtils.defaultVersion)
                .typeEnvModelPercentage("100")
                .verifyLastKnownGoodDisabledAndEmpty();

        genericSteps
                .clickSaveButton()
                .waitSuccessfullySavedToaster("Percent Filter");
    }

    @Test
    public void viewEnvModelPercentage() throws Exception {
        GenericNamespacedList whitelist = GenericNamespacedListUtils.createAndSaveDefaultIpList();
        PercentFilterValue percentFilter = createAndSavePercentFilter();
        percentFilterPageSteps.open();

        for (Map.Entry<String, EnvModelPercentage> entry : percentFilter.getEnvModelPercentages().entrySet()) {
            percentFilterPageSteps.verifyEnvModelRuleNameTd(entry.getKey())
                    .verifyEnvModelRulePercentageTd(90)
                    .verifyEnvModelRuleWhitelistTd(whitelist.getId())
                    .verifyEnvModelRuleActiveTd(entry.getValue().isActive())
                    .verifyEnvModelRuleFirmwareCheckRequiredTd(entry.getValue().isFirmwareCheckRequired());

            genericSteps.clickViewButton();

            percentFilterPageSteps.verifyEnvModelRuleName(entry.getKey())
                    .verifyEnvModelRuleWhitelist(whitelist.getId())
                    .verifyEnvModelRulePercentage(90)
                    .verifyEnvModelRuleActive(entry.getValue().isActive())
                    .verifyEnvModelRuleImmediately(entry.getValue().isRebootImmediately())
                    .verifyEnvModelRuleFirmwareVersionCheck(entry.getValue().isFirmwareCheckRequired())
                    .verifyEnvModelFirmwareVersionListItem("firmwareConfigVersion")
                    .verifyLastKnownGood("firmwareConfigVersion")
                    .verifyIntermediateVersion("firmwareConfigVersion")
                    .clickEnvModelPercentageViewModalCloseButton();
        }
    }

    @Test
    public void editFirmwareVersionsAndCheckErrors() throws Exception {
        createAndSavePercentFilter();
        String percentFilterConfigVersion = "firmwareConfigVersion";
        FirmwareConfig firmwareConfig = FirmwareConfigUtils.createAndSaveFirmwareConfig(UUID.randomUUID().toString(), "version123");

        GenericNamespacedListUtils.createAndSaveDefaultIpList();
        percentFilterPageSteps.open()
                .clickEnvModelEditButton();
        percentFilterEditPageSteps
                .clickFirmwareVersion(percentFilterConfigVersion)
                .verifyFirmwareVersionsErrorMessage("At last one Firmware Version should be selected")
                .selectLastKnownGood(firmwareConfig.getFirmwareVersion())
                .verifySelectedFirmwareVersion(firmwareConfig.getFirmwareVersion(), true)
                .selectLastKnownGood(percentFilterConfigVersion)
                .verifySelectedFirmwareVersion(percentFilterConfigVersion, true)
                .verifySelectedFirmwareVersion(firmwareConfig.getFirmwareVersion(), false)
                .clickFirmwareVersion(firmwareConfig.getFirmwareVersion())
                .clickFirmwareVersion(percentFilterConfigVersion)
                .verifyFirmwareVersionsErrorMessage("Last Known Good Version should be selected")
        ;
    }
}
