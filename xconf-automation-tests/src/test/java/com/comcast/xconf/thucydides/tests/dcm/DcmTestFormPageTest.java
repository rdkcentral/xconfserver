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
 * Created: 3/29/16  5:56 PM
 */
package com.comcast.xconf.thucydides.tests.dcm;

import com.comcast.xconf.logupload.*;
import com.comcast.xconf.thucydides.steps.dcm.TestFormPageSteps;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.dcm.*;
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

@RunWith(ThucydidesRunner.class)
public class DcmTestFormPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private TestFormPageSteps testFormPageSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        DeviceSettingsUtils.doCleanup();
        LogUploadSettingsUtils.doCleanup();
    }

    @Test
    public void testDcmTestPage() throws Exception {
        DCMGenericRule formula = FormulaUtils.createAndSaveDefaultFormula();
        DeviceSettings deviceSettings = DeviceSettingsUtils.createAndSaveDefaultDeviceSettings();
        LogUploadSettings logUploadSettings = LogUploadSettingsUtils.createAndSaveDefaultLogUploadSettings();
        VodSettings vodSettings = VodSettingsUtils.createAndSaveDefaultVodSettings();
        UploadRepository uploadRepository = UploadRepositoryUtils.createDefaultUploadRepository();

        testFormPageSteps.open()
                .typeKey(formula.getCondition().getFreeArg().getName())
                .clickTypeHeadListItem(formula.getCondition().getFreeArg().getName())
                .typeValue(formula.getCondition().getFixedArg().getValue().toString())
                .clickTestButton()
                .verifyTestResult(formula, deviceSettings, logUploadSettings, vodSettings, uploadRepository);
    }
}
