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
 * Created: 3/11/16  4:38 PM
 */
package com.comcast.xconf.thucydides.tests.firmware;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.xconf.Environment;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.RuleBuilderSteps;
import com.comcast.xconf.thucydides.steps.firmware.FirmwareRuleBaseSteps;
import com.comcast.xconf.thucydides.steps.firmware.FirmwareRulePageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.RuleUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.common.EnvironmentUtils;
import com.comcast.xconf.thucydides.util.common.ModelUtils;
import com.comcast.xconf.thucydides.util.firmware.FirmwareConfigUtils;
import com.comcast.xconf.thucydides.util.firmware.FirmwareRuleTemplateUtils;
import com.comcast.xconf.thucydides.util.firmware.FirmwareRuleUtils;
import net.thucydides.core.Thucydides;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.UUID;

@RunWith(ThucydidesRunner.class)
public class FirmwareRulePageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private FirmwareRulePageSteps firmwareRulePageSteps;
    @Steps
    private GenericSteps genericSteps;
    @Steps
    private FirmwareRuleBaseSteps firmwareRuleBaseSteps;
    @Steps
    private RuleBuilderSteps ruleBuilderSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        FirmwareRuleUtils.doCleanup();
    }

    @Test
    public void createNewRuleActionRule() throws Exception {
        String name = "ruleName";
        FirmwareRuleTemplate template = FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();
        FirmwareConfig firmwareConfig = FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();

        FirmwareRuleUtils.createAndSaveDefaultEnvModelRule();

        firmwareRulePageSteps.open();
        genericSteps.clickCreateButton();
        firmwareRulePageSteps.selectTemplateByIndex(2)
                .verifyRuleType(template.getId())
                .verifyActionType(ApplicableAction.Type.RULE.name())
                .clickNoopCheckBox()
                .verifyFimwareConfigSelectIsDisabled()
                .verifyDistributionAddButtonIsDisabled()
                .clickNoopCheckBox()
                .verifyFirmwareConfigSelect(firmwareConfig.getId())
                .verifyFirmwareConfigSelectVisibleText(firmwareConfig.getDescription())
                .selectFirmwareConfig()
                .typeName(name);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    @Ignore
    public void createRuleWithDistributionAction() throws Exception {
        FirmwareRule ruleToEdit = FirmwareRuleUtils.createAndSaveDefaultRuleActionRule();

        firmwareRulePageSteps.open();
        genericSteps.clickEditButton();
        firmwareRulePageSteps
                .clickAddDistributionButton()
                .clickRemoveDistributionButton()
                .clickAddDistributionButton()
                .selectDistributionConfig()
                .typeDistributionPercent(50);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(ruleToEdit.getName());
    }

    @Test
    public void editRuleActionRule() throws Exception {
        FirmwareRule ruleToEdit = FirmwareRuleUtils.createAndSaveDefaultRuleActionRule();

        firmwareRulePageSteps.open();
        genericSteps.clickEditButton();
        firmwareRulePageSteps.verifyNameInputIsDisabled();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(ruleToEdit.getName());
    }

    @Test
    public void createNewDefinePropertiesRule() throws Exception {
        String name = "ruleName";
        FirmwareRuleTemplate template = FirmwareRuleTemplateUtils.createAndSaveDefaultDefinePropertiesRuleTemplate();

        FirmwareRuleUtils.createAndSaveDefaultEnvModelRule();

        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickDefinePropertiesTab();
        genericSteps.clickCreateButton();
        firmwareRulePageSteps.selectTemplate()
                .typeName(name)
                .verifyRuleType(template.getId())
                .verifyActionType(ApplicableAction.Type.DEFINE_PROPERTIES.name());
        firmwareRuleBaseSteps.selectBypassFilter()
                .clickPropertiesEditButton()
                .verifyPropertiesKeyIsInputIsDisabled()
                .typePropertyValue("newPropertyValue")
                .clickAddPropertyButton();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void createDefinePropertiesRuleWithConditions() throws Exception {
        String modelName = "modelId1";
        ModelUtils.createAndSaveModel("modelId1");

        FirmwareRuleTemplate template = FirmwareRuleTemplateUtils.createDefinePropertiesRuleTemplateWithConditions();
        FirmwareRuleUtils.createAndSaveDefaultEnvModelRule();

        String name = "DefinePropertiesRuleName";
        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickDefinePropertiesTab();
        genericSteps.clickCreateButton();

        firmwareRulePageSteps.selectTemplate()
                .typeName(name)
                .verifyRuleType(template.getId())
                .verifyActionType(ApplicableAction.Type.DEFINE_PROPERTIES.name());

        ruleBuilderSteps
                .editAndValidateCondition(1, false, "model", StandardOperation.IN, modelName.toUpperCase(), "AND")
                .editAndValidateCondition(3, false, "mac", StandardOperation.IS, "11:11:11:11:11:11", "OR");

        firmwareRuleBaseSteps.selectBypassFilter()
                .clickPropertiesEditButton()
                .verifyPropertiesKeyIsInputIsDisabled()
                .typePropertyValue("newPropertyValue")
                .clickAddPropertyButton();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void createNewBlockingFilterRuleWithConditions() throws Exception {
        String name = "ruleName";
        FirmwareRuleTemplate template = FirmwareRuleTemplateUtils.createBlockingFiltersRuleTemplateWithConditions();

        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickBlockingFiltersTab();
        genericSteps.clickCreateButton();
        firmwareRulePageSteps.selectTemplate()
                .typeName(name)
                .verifyRuleType(template.getId())
                .verifyActionType(ApplicableAction.Type.BLOCKING_FILTER.name());

        ruleBuilderSteps
                .editAndValidateCondition(2, false, "time", StandardOperation.GTE, "01:00:00", "AND")
                .editAndValidateCondition(1, false, "time", StandardOperation.LTE, "02:00:00", "AND");

        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void editDefinePropertiesRule() throws Exception {
        FirmwareRule ruleToEdit = FirmwareRuleUtils.createAndSaveDefaultDefinePropertiesRule();

        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickDefinePropertiesTab();
        genericSteps.clickEditButton();
        firmwareRulePageSteps.verifyNameInputIsDisabled();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(ruleToEdit.getName());
    }

    @Test
    public void createNewBlockingFilterRule() throws Exception {
        String name = "ruleName";
        FirmwareRuleTemplate template = FirmwareRuleTemplateUtils.createAndSaveDefaultBlockingFiltersRuleTemplate();
        FirmwareConfig firmwareConfig = FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();

        FirmwareRuleUtils.createAndSaveDefaultEnvModelRule();

        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickBlockingFiltersTab();
        genericSteps.clickCreateButton();
        firmwareRulePageSteps.selectTemplate()
                .typeName(name)
                .verifyRuleType(template.getId())
                .verifyActionType(ApplicableAction.Type.BLOCKING_FILTER.name());
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void editBlockingFilterRule() throws Exception {
        FirmwareRule ruleToEdit = FirmwareRuleUtils.createAndSaveDefaultBlockingFilterRule();

        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickBlockingFiltersTab();
        genericSteps.clickEditButton();
        firmwareRulePageSteps.verifyNameInputIsDisabled();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(ruleToEdit.getName());
    }

    @Test
    public void deleteFirmwareRule() throws Exception {
        FirmwareRule ruleToDelete = FirmwareRuleUtils.createAndSaveDefaultRuleActionRule();

        firmwareRulePageSteps.open();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(ruleToDelete.getName(), "Firmware Rule")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(ruleToDelete.getName());
    }

    @Test
    public void exportAllFirmwareRules() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        FirmwareRuleUtils.createAndSaveDefaultRuleActionRule();

        firmwareRulePageSteps.open();
        genericSteps.clickExportAllButton()
                .checkSavedFile(dirForDownload, "allFirmwareRules_RULE_ACTION.json");
    }

    @Test
    public void exportAllFirmwareRuleTypes() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        FirmwareRuleUtils.createAndSaveDefaultRuleActionRule();

        firmwareRulePageSteps.open();
        genericSteps.clickExportAllTypesButton()
                .checkSavedFile(dirForDownload, "allFirmwareRules.json");
    }

    @Test
    public void exportOneFirmwareRule() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        FirmwareRule ruleToExport = FirmwareRuleUtils.createAndSaveDefaultRuleActionRule();

        firmwareRulePageSteps.open();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload, "firmwareRule_" + ruleToExport.getId() + ".json");
    }

    @Test
    public void viewRuleActionRule() throws Exception {
        FirmwareRule ruleToView = FirmwareRuleUtils.createAndSaveDefaultRuleActionRule();
        FirmwareConfig config = FirmwareConfigUtils.createDefaultFirmwareConfig();

        firmwareRulePageSteps.open();
        genericSteps.clickViewButton();
        firmwareRulePageSteps.verifyRuleActionViewModalWindow(ruleToView, config);
    }

    @Test
    public void viewDefinePropertiesRule() throws Exception {
        FirmwareRule ruleToView = FirmwareRuleUtils.createAndSaveDefaultDefinePropertiesRule();

        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickDefinePropertiesTab();
        genericSteps.clickViewButton();
        firmwareRulePageSteps.verifyDefinePropertiesViewModalWindow(ruleToView);
    }

    @Test
    public void viewBlockingFilterRule() throws Exception {
        FirmwareRule ruleToView = FirmwareRuleUtils.createAndSaveDefaultBlockingFilterRule();

        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickBlockingFiltersTab();
        genericSteps.clickViewButton();
        firmwareRulePageSteps.verifyBlockingFiltersViewModalWindow(ruleToView);
    }

    @Test
    public void firmwareVersionAutocomplete() throws Exception {
        FirmwareConfig firmwareConfig = FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();
        FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();
        String modelName = firmwareConfig.getSupportedModelIds().iterator().next();
        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickRuleActionsTab();
        genericSteps.clickCreateButton();
        firmwareRulePageSteps.selectTemplate();

        ruleBuilderSteps
                .editAndValidateCondition(0, false, "fir", StandardOperation.IS, firmwareConfig.getFirmwareVersion(), null)
                .editAndValidateCondition(0, false, "fir", StandardOperation.IN, firmwareConfig.getFirmwareVersion(), null);

        ruleBuilderSteps
                .editAndValidateCondition(0, false, "model", StandardOperation.IS, modelName, "AND")
                .addAndValidateCondition(false, "fir", StandardOperation.IS, firmwareConfig.getFirmwareVersion(), "AND")
                .editAndValidateCondition(1, false, "fir", StandardOperation.IN, firmwareConfig.getFirmwareVersion(), "AND");

        ruleBuilderSteps
                .editAndValidateCondition(0, false, "model", StandardOperation.IN, modelName, "AND")
                .editAndValidateCondition(1, false, "fir", StandardOperation.IS, firmwareConfig.getFirmwareVersion(), "AND")
                .editAndValidateCondition(1, false, "fir", StandardOperation.IN, firmwareConfig.getFirmwareVersion(), "AND");
    }

    @Test
    public void modelAutocomplete() throws Exception {
        Model model = ModelUtils.createAndSaveDefaultModel();
        FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();
        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickRuleActionsTab();
        genericSteps.clickCreateButton();
        firmwareRulePageSteps.selectTemplate();
        ruleBuilderSteps.clickAndRelation()
                .addCondition("model", StandardOperation.IS, model.getId());
    }

    @Test
    public void environmentAutocomplete() throws Exception {
        Environment environment = EnvironmentUtils.createAndSaveDefaultEnvironment();
        FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();
        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickRuleActionsTab();
        genericSteps.clickCreateButton();
        firmwareRulePageSteps.selectTemplate();
        ruleBuilderSteps.clickAndRelation().
                addCondition("env", StandardOperation.IS, environment.getId());
    }

    @Test
    public void createEnvModelRule() throws Exception {
        Environment environment = EnvironmentUtils.createAndSaveDefaultEnvironment();
        Model model = ModelUtils.createDefaultModel();
        FirmwareConfig firmwareConfig = FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();
        FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate(TemplateNames.ENV_MODEL_RULE, RuleUtils.buildEnvModelRule());

        firmwareRulePageSteps.open();
        firmwareRuleBaseSteps.clickRuleActionsTab();
        genericSteps.clickCreateButton();
        firmwareRulePageSteps.selectTemplate().typeName("testRuleName")
                .verifyFimrwareConfigSelectedValue("string:" + firmwareConfig.getId());
    }

    @Test
    public void renameEnvModelRule() throws Exception {
        FirmwareRuleUtils.createAndSaveDefaultEnvModelRule();
        String newName = "newName";
        firmwareRulePageSteps.open();
        genericSteps.clickEditButton();
        firmwareRulePageSteps.typeName(newName);
        genericSteps.clickSaveButton().waitSuccessfullySavedToaster(newName);
    }

    @Test
    public void deleteRuleBySearch() throws Exception {
        FirmwareConfig firmwareConfig = FirmwareConfigUtils.createDefaultFirmwareConfig();
        FirmwareRule macRule = FirmwareRuleUtils.createAndSaveDefaultMacRule();
        FirmwareRule envModelRule = FirmwareRuleUtils.createAndSaveDefaultEnvModelRule();

        firmwareRulePageSteps.open();
        genericSteps.typeSingleSearchParam(envModelRule.getName())
                .clickDeleteButton().clickModalOkButton()
                .waitSuccessfullyDeletedToaster(envModelRule.getName())
                .typeSingleSearchParam("")
                .verifyRowsCount(1);
    }

    @Test
    public void searchFirmwareRuleByFirmwareVersion() throws Exception {
        ModelUtils.createAndSaveModel("MODELID");
        FirmwareConfig firmwareConfig = FirmwareConfigUtils.createAndSaveFirmwareConfig(UUID.randomUUID().toString(), "firmwareVersion1");
        FirmwareRuleUtils.createAndSaveMacRule(new RuleAction(firmwareConfig.getId()));
        FirmwareRuleUtils.createAndSaveEnvModelRule(UUID.randomUUID().toString(), "testEnvModelRule123456", RuleUtils.buildEnvModelRule("MODEL_ID1", "ENV_ID1"));

        firmwareRulePageSteps.open();
        genericSteps.clickSearchArrow().selectSearchParam("Search by FirmwareConfig")
                .typeSingleSearchParam("wrongValue")
                .typeSingleSearchParam(firmwareConfig.getDescription())
                .verifyRowsCount(1)
                .typeSingleSearchParam("wrongValue")
                .typeSingleSearchParam("")
                .verifyRowsCount(2);
    }
  }
