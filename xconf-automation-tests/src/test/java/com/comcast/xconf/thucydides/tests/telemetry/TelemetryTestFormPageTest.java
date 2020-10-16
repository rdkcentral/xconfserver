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
 * Created: 3/18/16  1:18 PM
 */
package com.comcast.xconf.thucydides.tests.telemetry;

import com.beust.jcommander.internal.Lists;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.RuleViewSteps;
import com.comcast.xconf.thucydides.steps.telemetry.TestFormPageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.RuleUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.telemetry.TelemetryUtils;
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
public class TelemetryTestFormPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private TestFormPageSteps testFormPageSteps;

    @Steps
    private RuleViewSteps ruleViewSteps;

    @Steps
    private GenericSteps genericSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        GenericTestUtils.deleteEntities(TelemetryUtils.PERMANENT_PROFILE_URL, PermanentTelemetryProfile.class);
        GenericTestUtils.deleteEntities(TelemetryUtils.TARGETING_RULE_URL, TelemetryRule.class);
    }

    @Test
    public void testTelemetryTestPage() throws Exception {
        PermanentTelemetryProfile telemetryProfile = TelemetryUtils.createAndSavePermanentTelemetryProfile("profile123");
        String modelId = "MODEL_ID";
        String envId = "ENV_ID";
        String firmwareVersion = "FIRMWARE_VERSION";
        TelemetryUtils.createAndSaveTargetingRule(telemetryProfile.getId(),
                Lists.newArrayList(
                        RuleUtils.createCondition(RuleFactory.MODEL, StandardOperation.IS, modelId),
                        RuleUtils.createCondition(RuleFactory.ENV, StandardOperation.IS, envId),
                        RuleUtils.createCondition(RuleFactory.VERSION, StandardOperation.IS, firmwareVersion)
                ));

        testFormPageSteps.open()
                .typeKey(RuleFactory.ENV.getName()).clickTypeaheadListItem(RuleFactory.ENV.getName())
                .typeValue(envId)
                .clickTestButton()
                .waitRuleViewDirective();
        ruleViewSteps.verifyFreeArg("model");
    }
}
