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
 * Created: 3/29/2016
*/
package com.comcast.xconf.thucydides.tests.setting;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.RuleBuilderSteps;
import com.comcast.xconf.thucydides.steps.RuleViewSteps;
import com.comcast.xconf.thucydides.steps.setting.SettingRulePageSteps;
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

import static org.junit.Assert.assertEquals;

@RunWith(ThucydidesRunner.class)
public class SettingRulePageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private SettingRulePageSteps settingRulePageSteps;

    @Steps
    private RuleBuilderSteps ruleBuilderSteps;

    @Steps
    private RuleViewSteps ruleViewSteps;

    @Steps
    private GenericSteps genericSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        GenericTestUtils.deleteEntities(SettingUtils.SETTING_PROFILE_URL, SettingProfile.class);
        GenericTestUtils.deleteEntities(SettingUtils.SETTING_RULE_URL, SettingRule.class);
    }

    @Test
    public void createSettingRule() throws Exception {
        final SettingProfile profile = SettingUtils.createAndSaveSettingProfile();

        final String name = "testName";
        settingRulePageSteps.open();
        genericSteps.clickCreateButton();
        settingRulePageSteps
                .typeName(name)
                .typeBoundSettingId(profile.getSettingProfileId());
        ruleBuilderSteps.addCondition("mac", StandardOperation.IS, "11:11:11:11:11:11");
        genericSteps
                .clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void editSettingRule() throws Exception {
        final SettingRule telemetryRule = SettingUtils.createAndSaveSettingRule();

        settingRulePageSteps.open();
        genericSteps.clickEditButton();
        ruleBuilderSteps.clickRuleConditionForEdit()
                .addCondition("mac", StandardOperation.IS, "22:22:22:22:22:22");
        genericSteps
                .clickSaveButton()
                .waitSuccessfullySavedToaster(telemetryRule.getName());
    }

    @Test
    public void deleteSettingRule() throws Exception {
        final SettingRule targetingRule = SettingUtils.createAndSaveSettingRule();

        settingRulePageSteps.open();
        genericSteps
                .clickDeleteButton()
                .verifyDeleteModalWindow(targetingRule.getName(), "Setting rule")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(targetingRule.getName());
    }

    @Test
    public void exportAllSettingRules() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        SettingProfile profile = SettingUtils.createAndSaveSettingProfile();
        SettingUtils.createAndSaveSettingRule("name1", profile.getId());
        SettingUtils.createAndSaveSettingRule("name2", profile.getId());

        settingRulePageSteps.open();
        genericSteps
                .clickExportAllButton()
                .checkSavedFile(dirForDownload, "allSettingRules.json");
    }

    @Test
    public void exportOneSettingRule() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        SettingRule rule = SettingUtils.createAndSaveSettingRule();

        settingRulePageSteps.open();
        genericSteps
                .clickExportOneButton()
                .checkSavedFile(dirForDownload, "settingRule_" + rule.getId() + ".json");
    }

    @Test
    public void searchTargetingRules() throws Exception {
        final SettingRule telemetryRule = SettingUtils.createAndSaveSettingRule();

        final String name = telemetryRule.getName();
        final String freeArg = telemetryRule.getRule().getCondition().getFreeArg().getName();
        final String fixedArg = (String) telemetryRule.getRule().getCondition().getFixedArg().getValue();
        final String normalizedFixedArg = fixedArg.toUpperCase();

        settingRulePageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Key and Value")
                .typeMultipleSearchParamByKey(freeArg)
                .typeMultipleSearchParamByValue(normalizedFixedArg);
        ruleViewSteps
                .verifyFreeArg(freeArg)
                .verifyFixedArg(normalizedFixedArg);
        assertEquals(name, settingRulePageSteps.getSettingRuleName());
    }

    @Test
    public void deleteBySearch() throws IOException {
        SettingProfile profile = SettingUtils.createAndSaveSettingProfile();
        List<SettingRule> settingRules = SettingUtils.createAndSaveSettingRules(profile.getSettingProfileId());

        settingRulePageSteps.open();
        genericSteps.clickSearchArrow()
                .selectSearchParam("Search by Name")
                .typeSingleSearchParam(settingRules.get(0).getName())
                .clickDeleteButton()
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(settingRules.get(0).getName())
                .typeSingleSearchParam("");
        settingRulePageSteps.verifySettingRulesCount(1);
    }
}
