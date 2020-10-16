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
 * Created: 3/10/16  5:44 PM
 */
package com.comcast.xconf.thucydides.tests.firmware;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.xconf.Environment;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.RuleBuilderSteps;
import com.comcast.xconf.thucydides.steps.RuleViewEditorSteps;
import com.comcast.xconf.thucydides.steps.firmware.FirmwareRuleBaseSteps;
import com.comcast.xconf.thucydides.steps.firmware.FirmwareRulePageSteps;
import com.comcast.xconf.thucydides.steps.firmware.FirmwareRuleTemplatePageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.common.EnvironmentUtils;
import com.comcast.xconf.thucydides.util.common.ModelUtils;
import com.comcast.xconf.thucydides.util.firmware.FirmwareRuleTemplateUtils;
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
public class FirmwareRuleTemplatePageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private FirmwareRuleTemplatePageSteps firmwareRuleTemplatePageSteps;
    @Steps
    private GenericSteps genericSteps;
    @Steps
    private FirmwareRuleBaseSteps firmwareRuleBaseSteps;
    @Steps
    private RuleBuilderSteps ruleBuilderSteps;
    @Steps
    private FirmwareRulePageSteps firmwareRulePageSteps;
    @Steps
    private RuleViewEditorSteps ruleViewEditorSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        FirmwareRuleTemplateUtils.doCleanup();
    }

    @Test
    public void createNewRuleActionTemplate() throws Exception {
        String id = "ruleActionTemplate";
        Environment environment = EnvironmentUtils.createDefaultEnvironment();
        FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        genericSteps.clickCreateButton()
                .typeId(id);
        firmwareRuleTemplatePageSteps.checkValidPriority(2);
        ruleBuilderSteps.addCondition("env", StandardOperation.IS, environment.getId());
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(id);
    }

    @Test
    public void editRuleActionTemplate() throws Exception {
        FirmwareRuleTemplate templateToEdit = FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        genericSteps.clickEditButton()
                .checkIdInputIsDisabled()
                .clickSaveButton()
                .waitSuccessfullySavedToaster(templateToEdit.getId());
    }

    @Test
    public void createNewDefinePropertiesTemplate() throws Exception {
        String id = "definePropertiesTemplate";
        Environment environment = EnvironmentUtils.createDefaultEnvironment();
        FirmwareRuleTemplate bypassFilter = FirmwareRuleTemplateUtils.createAndSaveDefaultBlockingFiltersRuleTemplate();

        FirmwareRuleTemplateUtils.createAndSaveDefaultDefinePropertiesRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        firmwareRuleBaseSteps.clickDefinePropertiesTab();
        genericSteps.clickCreateButton()
            .typeId(id);
        firmwareRuleTemplatePageSteps.checkValidPriority(2);
        ruleBuilderSteps.addCondition("env", StandardOperation.IS, environment.getId());

        firmwareRuleBaseSteps.typePropertyKey("").typePropertyValue("someValue")
                .clickAddPropertyButton();
        genericSteps.waitFailedToaster("Validation Error", "Key is blank");

        firmwareRuleBaseSteps.selectBypassFilter()
                .verifySelectedBypassFilter(bypassFilter.getId())
                .typePropertyKey("someKey")
                .typePropertyValue("someValue");
        firmwareRuleTemplatePageSteps.clickOptionalCheckBox();
        firmwareRuleBaseSteps.clickAddPropertyButton();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(id);
    }

    @Test
    public void editDefinePropertiesTemplate() throws Exception {
        FirmwareRuleTemplate templateToEdit = FirmwareRuleTemplateUtils.createAndSaveDefaultDefinePropertiesRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        firmwareRuleBaseSteps.clickDefinePropertiesTab();
        genericSteps.clickEditButton()
                .checkIdInputIsDisabled();

        firmwareRuleBaseSteps.typePropertyKey("").typePropertyValue("someValue")
                .clickAddPropertyButton();
        genericSteps.waitFailedToaster("Validation Error", "Key is blank");

        firmwareRuleBaseSteps.typePropertyKey("someKey")
                .typePropertyValue("someValue")
                .clickAddPropertyButton();
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(templateToEdit.getId());
    }

    @Test
    public void createNewBlockingFilterTemplate() throws Exception {
        String id = "blockingFilterTemplateId";
        Environment environment = EnvironmentUtils.createDefaultEnvironment();
        FirmwareRuleTemplateUtils.createAndSaveDefaultBlockingFiltersRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        firmwareRuleBaseSteps.clickBlockingFiltersTab();
        genericSteps.clickCreateButton()
                .typeId(id);
        firmwareRuleTemplatePageSteps.checkValidPriority(2);
        ruleBuilderSteps.addCondition("env", StandardOperation.IS, environment.getId());
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(id);
    }

    @Test
    public void editBlockingFilterTemplate() throws Exception {
        FirmwareRuleTemplate templateToEdit = FirmwareRuleTemplateUtils.createAndSaveDefaultBlockingFiltersRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        firmwareRuleBaseSteps.clickBlockingFiltersTab();
        genericSteps.clickEditButton()
                .checkIdInputIsDisabled()
                .clickSaveButton()
                .waitSuccessfullySavedToaster(templateToEdit.getId());
    }

    @Test
    public void deleteRuleTemplate() throws Exception {
        FirmwareRuleTemplate templateToDelete = FirmwareRuleTemplateUtils.createAndSaveRuleActionsRuleTemplate("FirmwareTemplate");

        firmwareRuleTemplatePageSteps.open();
        genericSteps.clickDeleteButton()
                .verifyDeleteModalWindow(templateToDelete.getId(), "Firmware Rule Template")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(templateToDelete.getId());
    }

    @Test
    public void exportAllRuleTemplates() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        genericSteps.clickExportAllButton()
                .checkSavedFile(dirForDownload, "allFirmwareRuleTemplates_RULE_ACTION_TEMPLATE.json");
    }

    @Test
    public void exportAllRuleTemplateTypes() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        genericSteps.clickExportAllTypesButton()
                .checkSavedFile(dirForDownload, "allFirmwareRuleTemplates.json");
    }

    @Test
    public void exportOneRuleTemplate() throws Exception {
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        FirmwareRuleTemplate templateToExport = FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload, "firmwareRuleTemplate_" + templateToExport.getId() + ".json");
    }

    @Test
    public void viewRuleActionTemplate() throws Exception {
        FirmwareRuleTemplate templateToView = FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        genericSteps.clickViewButton();
        firmwareRuleTemplatePageSteps.verifyRuleActionViewModalWindow(templateToView);
    }

    @Test
    public void viewDefinePropertiesTemplate() throws Exception {
        FirmwareRuleTemplate templateToView = FirmwareRuleTemplateUtils.createAndSaveDefaultDefinePropertiesRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        firmwareRuleBaseSteps.clickDefinePropertiesTab();
        genericSteps.clickViewButton();
        firmwareRuleTemplatePageSteps.verifyDefinePropertiesViewModalWindow(templateToView);
    }

    @Test
    public void viewBlockingFilterTemplate() throws Exception {
        FirmwareRuleTemplate templateToView = FirmwareRuleTemplateUtils.createAndSaveDefaultBlockingFiltersRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        firmwareRuleBaseSteps.clickBlockingFiltersTab();
        genericSteps.clickViewButton();
        firmwareRuleTemplatePageSteps.verifyBlockingFiltersViewModalWindow(templateToView);
    }

    @Test
    public void createRuleFromTemplate() throws Exception {
        FirmwareRuleTemplate ruleTemplate = FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();
        String ruleName = "ruleName";
        firmwareRuleTemplatePageSteps.open();
        firmwareRuleBaseSteps.clickRuleActionsTab();
        firmwareRuleTemplatePageSteps.clickCreateRuleButton();
        ruleBuilderSteps.addCondition("mac", StandardOperation.IS, "11:11:11:11:11:11");
        firmwareRulePageSteps.verifyActionType(ApplicableAction.Type.RULE.name())
                .clickNoopCheckBox()
                .verifyFimwareConfigSelectIsDisabled()
                .clickNoopCheckBox()
                .selectFirmwareConfig()
                .typeName(ruleName);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(ruleName);
    }

    @Test
    public void removeHeadConditionAndVerifyIfAnotherIsExist() throws Exception {
        String id = "ruleActionTemplate";
        Environment environment = EnvironmentUtils.createDefaultEnvironment();
        Model model = ModelUtils.createAndSaveDefaultModel();
        FirmwareRuleTemplateUtils.createAndSaveDefaultRuleActionsRuleTemplate();

        firmwareRuleTemplatePageSteps.open();
        genericSteps.clickCreateButton()
                .typeId(id);
        ruleBuilderSteps.addCondition("env", StandardOperation.IS, environment.getId())
                .clickAndRelation()
                .addCondition("model", StandardOperation.IS, model.getId());
        ruleViewEditorSteps.removeConditionByFreeArgName("env")
                .verifyHeadFreeArg("model");
    }
}
