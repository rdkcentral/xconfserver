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
 * Created: 3/11/16  4:29 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.DefinePropertiesAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.thucydides.pages.firmware.FirmwareRulePage;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.RuleViewSteps;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.steps.ScenarioSteps;

import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FirmwareRulePageSteps extends ScenarioSteps {

    private FirmwareRulePage page;

    @Steps
    public GenericSteps genericSteps;
    @Steps
    public FirmwareRuleBaseSteps firmwareRuleBaseSteps;
    @Steps
    public RuleViewSteps ruleViewSteps;
    @Steps
    public FirmwareRuleViewModalSteps firmwareRuleViewModalSteps;

    @Step
    public FirmwareRulePageSteps open() {
        page.open();
        return this;
    }


    @Step
    public FirmwareRulePageSteps typeName(String name) {
        page.typeName(name);
        return this;
    }

    @Step
    public FirmwareRulePageSteps selectTemplate() {
        page.clickTemplatesListItem();
        return this;
    }

    @Step
    public FirmwareRulePageSteps selectTemplateByIndex(int index) {
        page.selectTemplateByIndex(index);
        return this;
    }

    @Step
    public FirmwareRulePageSteps clickIsActiveCheckbox() {
        page.clickIsActiveCheckbox();
        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyRuleType(String expectedRuleType) {
        assertEquals(expectedRuleType, page.getRuleType());
        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyActionType(String expectedActionType) {
        assertEquals(expectedActionType, page.getActionType());
        return this;
    }

    @Step
    public FirmwareRulePageSteps clickNoopCheckBox() {
        page.clickNoopCheckbox();
        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyFimwareConfigSelectIsDisabled() {
        assertTrue(page.firmwareConfigSelectIsDisabled());
        return this;
    }

    @Step
    public FirmwareRulePageSteps clickAddDistributionButton() {
        page.clickAddDistributionButton();
        return this;
    }

    @Step
    public FirmwareRulePageSteps clickRemoveDistributionButton() {
        page.clickRemoveDistributionButton();
        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyDistributionAddButtonIsDisabled() {
        assertTrue(page.distributionAddButtonIsDisabled());
        return this;
    }

    @Step
    public FirmwareRulePageSteps selectDistributionConfig() {
        page.clickDistributionConfigSelect();
        page.clickDistributionConfigListItem();
        return this;
    }

    @Step
    public FirmwareRulePageSteps typeDistributionPercent(int percent) {
        page.typeDistributionPercent(percent);
        return this;
    }

    @Step
    public FirmwareRulePageSteps selectFirmwareConfig() {
        page.clickFirmwareConfigSelect();
        page.clickFirmwareConfigListItem();
        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyNameInputIsDisabled() {
        assertTrue(page.nameInputIsDisabled());
        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyRuleActionViewModalWindow(FirmwareRule rule, FirmwareConfig firmwareConfig) {
        verifyViewModalWindowGeneral(rule, ApplicableAction.Type.RULE.name());
        firmwareRuleViewModalSteps.verifyConfigId(firmwareConfig.getId())
                .verifyConfigDescription(firmwareConfig.getDescription())
                .verifyConfigFileName(firmwareConfig.getFirmwareFilename())
                .verifyConfigVersion(firmwareConfig.getFirmwareVersion()).verifyConfigSupportedModel(firmwareConfig.getSupportedModelIds().iterator().next());

        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyDefinePropertiesViewModalWindow(FirmwareRule rule) {
        verifyViewModalWindowGeneral(rule, ApplicableAction.Type.DEFINE_PROPERTIES.name());
        DefinePropertiesAction expectedAction = (DefinePropertiesAction) rule.getApplicableAction();
        firmwareRuleViewModalSteps.verifyBypassFilterListItem(expectedAction.getByPassFilters().get(0));
        verifyDefineProperties((TreeMap<String, String>) expectedAction.getProperties());

        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyBlockingFiltersViewModalWindow(FirmwareRule rule) {
        verifyViewModalWindowGeneral(rule, ApplicableAction.Type.BLOCKING_FILTER.name());
        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyFirmwareConfigSelect(String expectedVersion) {
        assertEquals("string:" + expectedVersion, page.getFirmwareConfigSelectedValue());
        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyFirmwareConfigSelectVisibleText(String expectedDescription) {
        assertEquals(expectedDescription, page.getFirmwareConfigSelectVisibleText());
        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyFimrwareConfigSelectedValue(String expectedFirmwareConfigId) {
        assertEquals(expectedFirmwareConfigId, page.getFirmwareConfigSelectedValue());
        return this;
    }

    @Step
    public FirmwareRulePageSteps typeSearchName(String searchName) {
        page.typeSearchName(searchName);
        return this;
    }

    @Step
    public FirmwareRulePageSteps verifyFirmwareRulesCount(int expectedCount) {
        assertEquals(expectedCount, page.getFirmwareRulesCount());
        return this;
    }

    private void verifyViewModalWindowGeneral(FirmwareRule rule, String expectedApplicableActionType) {
        genericSteps.waitForModalWindowToOpen();
        genericSteps.verifyViewModalTitle("View firmware rule " + rule.getName());
        firmwareRuleViewModalSteps.verifyId(rule.getName());
        verifyRule(rule);
        firmwareRuleViewModalSteps.verifyActionType(expectedApplicableActionType);
    }

    private void verifyRule(FirmwareRule rule) {
        if (rule.getRule().isNegated()) {
            ruleViewSteps.verifyIsNegated();
        }
        Condition expectedCondition = rule.getRule().getCondition();
        ruleViewSteps.verifyFreeArg(expectedCondition.getFreeArg().getName());
        ruleViewSteps.verifyOperation(expectedCondition.getOperation().toString());
        ruleViewSteps.verifyFixedArg(expectedCondition.getFixedArg().getValue().toString());
    }

    private void verifyDefineProperties(TreeMap<String, String> expectedProperties) {
        String expectedKey = expectedProperties.firstKey();
        String expectedValue = expectedProperties.firstEntry().getValue();
        firmwareRuleViewModalSteps.verifyPropertyKey(expectedKey);
        firmwareRuleViewModalSteps.verifyPropertyValue(expectedValue);
    }
}
