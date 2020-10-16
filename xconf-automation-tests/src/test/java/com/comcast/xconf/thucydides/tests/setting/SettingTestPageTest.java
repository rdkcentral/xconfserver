/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.thucydides.tests.setting;

import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.settings.SettingType;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.setting.SettingTestPageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.setting.SettingUtils;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

@RunWith(ThucydidesRunner.class)
public class SettingTestPageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private GenericSteps genericSteps;
    @Steps
    private SettingTestPageSteps settingTestPageSteps;

    private List<SettingRule> eponSettingRuleList = new ArrayList<>();
    private List<SettingRule> partnerSettingRuleList = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        GenericTestUtils.deleteEntities(SettingUtils.SETTING_PROFILE_URL, SettingProfile.class);
        GenericTestUtils.deleteEntities(SettingUtils.SETTING_RULE_URL, SettingRule.class);

        SettingProfile eponProfile = SettingUtils.createAndSaveSettingProfile("EponProfileTest", SettingType.EPON);
        eponSettingRuleList.add(SettingUtils.createAndSaveSettingRule("ENVTest", eponProfile.getId(), RuleFactory.ENV));

        SettingProfile partnerProfile = SettingUtils.createAndSaveSettingProfile("PartnerProfileTest", SettingType.PARTNER_SETTINGS);
        partnerSettingRuleList.add(SettingUtils.createAndSaveSettingRule("ModelTest", partnerProfile.getId(), RuleFactory.MODEL));
    }

    @After
    public void cleanup() throws Exception {
        GenericTestUtils.deleteEntities(SettingUtils.SETTING_PROFILE_URL, SettingProfile.class);
        GenericTestUtils.deleteEntities(SettingUtils.SETTING_RULE_URL, SettingRule.class);
    }

    @Test
    public void testParamToasters() throws Exception {
        settingTestPageSteps.open();
        settingTestPageSteps.testRule();
        genericSteps.waitFailedToaster("Key is required");
    }

    @Test
    public void testSettingTypeToasters() throws Exception {
        settingTestPageSteps.open();
        addParameterEntity(eponSettingRuleList);
        settingTestPageSteps.doubleClickSettingTypeMultiselect();
        settingTestPageSteps.testRule();
        genericSteps.waitFailedToaster("Define settings type");
    }

    @Test
    public void evaluateEpon() throws Exception {
        settingTestPageSteps.open();
        addParameterEntity(eponSettingRuleList);
        selectSettingType("epon");
        settingTestPageSteps.clickSettingTypeMultiselect();
        Assert.assertTrue(settingTestPageSteps.testRule().hasMatchedRules());
    }

    @Test
    public void evaluatePartner() throws Exception {
        settingTestPageSteps.open();
        addParameterEntity(partnerSettingRuleList);
        selectSettingType("partnersettings");
        settingTestPageSteps.clickSettingTypeMultiselect();
        Assert.assertTrue(settingTestPageSteps.testRule().hasMatchedRules());
    }

    @Test
    public void evaluateTwoRules() throws Exception {
        List<SettingRule> settingRuleList = new ArrayList<>();
        settingRuleList.addAll(eponSettingRuleList);
        settingRuleList.addAll(partnerSettingRuleList);

        settingTestPageSteps.open();
        addParameterEntity(settingRuleList);
        selectAllSettingTypes();
        settingTestPageSteps.clickSettingTypeMultiselect();
        Assert.assertTrue(settingTestPageSteps.testRule().hasMatchedRules());
    }

    private void selectSettingType(String name) {
        int lineNumber = -1;
        if (SettingType.EPON.isApplicableTo(name)) {
            lineNumber = 4;
        } else if (SettingType.PARTNER_SETTINGS.isApplicableTo(name)) {
            lineNumber = 5;
        }

        if (lineNumber != -1) {
            settingTestPageSteps.selectSettingType(lineNumber);
        }
    }

    private void selectAllSettingTypes() {
        settingTestPageSteps.selectSettingType(1);
    }

    private void addParameterEntity(List<SettingRule> settingRuleList) {
        for (SettingRule rule : settingRuleList) {
            String freeArg = rule.getRule().getCondition().getFreeArg().getName();
            String fixedArg = rule.getRule().getCondition().getFixedArg().getValue().toString().toUpperCase();
            settingTestPageSteps.addParameterEntity(freeArg, fixedArg);
        }
    }

}
