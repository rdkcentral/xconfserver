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
 * Created: 3/18/16  4:12 PM
 */
package com.comcast.xconf.thucydides.tests.firmware;

import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.thucydides.steps.firmware.LogPageSteps;
import com.comcast.xconf.thucydides.util.firmware.FirmwareRuleUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static com.comcast.xconf.thucydides.util.common.GenericNamespacedListUtils.defaultMacAddress;
import static com.comcast.xconf.thucydides.util.firmware.FirmwareConfigUtils.defaultVersion;

@RunWith(ThucydidesRunner.class)
public class LogPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private LogPageSteps logPageSteps;

    @Test
    public void testFirmwareLogPage() throws Exception {
        String anotherFirmwareConfig = "another" + defaultVersion;
        FirmwareRule rule = FirmwareRuleUtils.createAndSaveDefaultMacRule();
        GenericTestUtils.makeSwuStbRequest(defaultMacAddress, "", "", anotherFirmwareConfig);

        logPageSteps.open()
                .typeMacAddress(defaultMacAddress)
                .clickTestButton()
                .waitUntilResultIsPresent()
                .verifyLastConfigLog(rule, anotherFirmwareConfig)
                .verifyChangeConfigLog(rule, anotherFirmwareConfig);
    }
}
