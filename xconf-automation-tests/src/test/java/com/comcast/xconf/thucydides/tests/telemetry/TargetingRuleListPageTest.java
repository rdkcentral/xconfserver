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
 * Created: 3/18/16  6:39 PM
 */
package com.comcast.xconf.thucydides.tests.telemetry;

import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.RuleViewSteps;
import com.comcast.xconf.thucydides.steps.telemetry.TargetingRuleListPageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
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

import java.io.IOException;
import java.util.List;

@RunWith(ThucydidesRunner.class)
public class TargetingRuleListPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private TargetingRuleListPageSteps targetingRuleListPageSteps;

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
    public void searchTargetingRulesByKeyAndValue() throws Exception {
        PermanentTelemetryProfile permanentTelemetryProfile = TelemetryUtils.createAndSavePermanentProfile();
        List<TelemetryRule> targetingRules = TelemetryUtils.createAndSaveTargetingRules(permanentTelemetryProfile.getId());
        String freeArg = targetingRules.get(0).getRule().getCondition().getFreeArg().getName();
        String fixedArg = targetingRules.get(0).getRule().getCondition().getFixedArg().getValue().toString();
        targetingRuleListPageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Key and Value")
                .typeMultipleSearchParamByKey(freeArg)
                .typeMultipleSearchParamByValue(fixedArg)
                .verifyEntitiesCount(1);
    }

    @Test
    public void searchTargetingRuleByName() throws Exception {
        PermanentTelemetryProfile permanentTelemetryProfile = TelemetryUtils.createAndSavePermanentProfile();
        List<TelemetryRule> targetingRules = TelemetryUtils.createAndSaveTargetingRules(permanentTelemetryProfile.getId());
        targetingRuleListPageSteps.open();
        genericSteps.typeSingleSearchParam(targetingRules.get(0).getName())
                .verifyEntitiesCount(1);
    }
    
    @Test
    public void deleteBySearch() throws IOException {
        PermanentTelemetryProfile profile = TelemetryUtils.createAndSavePermanentProfile();
        List<TelemetryRule> targetingRules = TelemetryUtils.createAndSaveTargetingRules(profile.getId());

        targetingRuleListPageSteps.open();
        genericSteps.typeSingleSearchParam(targetingRules.get(0).getName())
                .clickDeleteButton()
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(targetingRules.get(0).getName())
                .typeSingleSearchParam("").verifyEntitiesCount(1);
    }

}
