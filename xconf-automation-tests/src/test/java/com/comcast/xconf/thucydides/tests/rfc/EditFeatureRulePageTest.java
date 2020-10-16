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
package com.comcast.xconf.thucydides.tests.rfc;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.RuleBuilderSteps;
import com.comcast.xconf.thucydides.steps.rfc.EditFeatureRulePageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.rfc.FeatureRuleUtils;
import com.comcast.xconf.thucydides.util.rfc.FeatureUtils;
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
public class EditFeatureRulePageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private EditFeatureRulePageSteps editFeatureRulePageSteps;

    @Steps
    private RuleBuilderSteps ruleBuilderSteps;

    @Steps
    private GenericSteps genericSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        GenericTestUtils.deleteEntities(FeatureUtils.RFC_FEATURE, Feature.class);
        GenericTestUtils.deleteEntities(FeatureRuleUtils.RFC_FEATURE_RULE, FeatureRule.class);
    }

    @Test
    public void createFeatureRule() throws Exception {
        Feature testFeature = FeatureUtils.createAndSaveFeature("testFeature");
        final String name = "testName";
        editFeatureRulePageSteps.open();
        genericSteps.clickCreateButton();
        editFeatureRulePageSteps
                .typeName(name)
                .selectPriority("1")
                .selectFeatureName(testFeature.getName());
        ruleBuilderSteps.addCondition("mac", StandardOperation.IS, "11:11:11:11:11:11");
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void editFeatureRule() throws Exception {
        FeatureRule testFeatureRule = FeatureRuleUtils.createAndSaveFeatureRule("testFeatureRule", "testFeature");
        final String editName = "testFeatureRuleEdit";
        FeatureUtils.createAndSaveFeature(editName);
        editFeatureRulePageSteps.open();
        genericSteps.clickEditButton();
        editFeatureRulePageSteps
                .typeName(editName)
                .selectPriority("1")
                .selectFeatureName(editName);
        ruleBuilderSteps.clickRuleConditionForEdit()
                .addConditionCustomFreeArg(editName, StandardOperation.IS, editName);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(editName);
    }

    @Test
    public void deleteFeatureRule() throws Exception {
        final String ruleName = "testFeatureRule";
        final FeatureRule testFeatureRule = FeatureRuleUtils.createAndSaveFeatureRule(ruleName, "testFeature");
        editFeatureRulePageSteps.open();
        genericSteps.clickDeleteButton()
            .verifyDeleteModalWindow(testFeatureRule.getName(),"Feature Rule")
            .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(ruleName);
    }

    @Test
    public void exportFeatureRule() throws Exception {
        FeatureRule testFeatureRule = FeatureRuleUtils.createAndSaveFeatureRule("testFeatureRule", "testFeature");
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        editFeatureRulePageSteps.open();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload,"featureRule_" + testFeatureRule.getId() + ".json");
    }
}
