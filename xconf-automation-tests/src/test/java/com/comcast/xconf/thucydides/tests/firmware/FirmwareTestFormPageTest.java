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
 * <p>
 * Author: Stanislav Menshykov
 * Created: 3/21/16  3:52 PM
 */
package com.comcast.xconf.thucydides.tests.firmware;

import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.thucydides.steps.RuleViewSteps;
import com.comcast.xconf.thucydides.steps.firmware.TestFormPageSteps;
import com.comcast.xconf.thucydides.util.*;
import com.comcast.xconf.thucydides.util.common.GenericNamespacedListUtils;
import com.comcast.xconf.thucydides.util.firmware.FirmwareRuleUtils;
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
public class FirmwareTestFormPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private TestFormPageSteps testFormPageSteps;
    @Steps
    private RuleViewSteps ruleViewSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        FirmwareRuleUtils.doCleanup();
    }

    @Test
    public void testFirmwareTestPage() throws Exception {
        Condition condition = FirmwareRuleUtils.createAndSaveDefaultMacRule().getRule().getCondition();

        testFormPageSteps.open()
                .typeKeyParameter(StbContext.ESTB_MAC)
                .clickOnTypeaheadItem()
                .typeValueParameter(GenericNamespacedListUtils.defaultMacAddress)
                .clickTestButton()
                .waitUntilResultIsPresent()
                .verifyConfig();

        ruleViewSteps.verifyFreeArg(condition.getFreeArg().getName())
                .verifyOperation(condition.getOperation().toString())
                .verifyFixedArg(condition.getFixedArg().getValue().toString());
    }

}
