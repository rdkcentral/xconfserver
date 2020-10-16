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
 * Created: 3/16/16  6:56 PM
 */
package com.comcast.xconf.thucydides.tests.telemetry;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.RuleBuilderSteps;
import com.comcast.xconf.thucydides.steps.telemetry.TargetingRulePageSteps;
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

@RunWith(ThucydidesRunner.class)
public class TargetingRulePageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private TargetingRulePageSteps targetingRulePageSteps;

    @Steps
    private RuleBuilderSteps ruleBuilderSteps;

    @Steps
    private GenericSteps genericSteps;


    @Before
    @After
    public void cleanup() throws Exception {
        GenericTestUtils.deleteEntities(TelemetryUtils.PERMANENT_PROFILE_URL, PermanentTelemetryProfile.class);
        GenericTestUtils.deleteEntities(TelemetryUtils.TARGETING_RULE_URL, TelemetryRule.class);
    }

    @Test
    public void createTargetingRule() throws Exception {
        final PermanentTelemetryProfile permanentTelemetryProfile = TelemetryUtils.createAndSavePermanentProfile();

        TelemetryUtils.createAndSaveTargetingRules(permanentTelemetryProfile.getId());

        final String name = "testName";
        targetingRulePageSteps.open();
        genericSteps.clickCreateButton();
        targetingRulePageSteps
                .typeName(name)
                .typeBoundTelemetryId(permanentTelemetryProfile.getName());
        ruleBuilderSteps.addCondition("mac", StandardOperation.IS, "11:11:11:11:11:11");
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void editTargetingRule() throws Exception {
        final TelemetryRule telemetryRule = TelemetryUtils.createAndSaveTargetingRule();

        targetingRulePageSteps.open();
        genericSteps.clickEditButton();
        ruleBuilderSteps.clickRuleConditionForEdit()
                .addCondition("mac", StandardOperation.IS, "11:11:11:11:11:11");
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(telemetryRule.getName());
    }

    @Test
    public void deleteTargetingRule() throws Exception {
        final TelemetryRule targetingRule = TelemetryUtils.createAndSaveTargetingRule();

        targetingRulePageSteps.open();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(targetingRule.getName(), "Targeting rule")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(targetingRule.getName());
    }

    @Test
    public void exportAllTargetingRules() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        PermanentTelemetryProfile permanentProfile = TelemetryUtils.createAndSavePermanentProfile();
        TelemetryUtils.createAndSaveTargetingRules(permanentProfile.getId());

        targetingRulePageSteps.open();
        genericSteps.clickExportAllButton()
                .checkSavedFile(dirForDownload, "allTelemetryRules.json");
    }


    @Test
    public void exportOneTargetingRule() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        TelemetryRule telemetryRule = TelemetryUtils.createAndSaveTargetingRule();

        targetingRulePageSteps.open();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload, "telemetryRule_" + telemetryRule.getId() + ".json");
    }
}
