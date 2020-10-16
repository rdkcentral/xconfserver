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
 * Created: 3/10/16  5:45 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.DefinePropertiesTemplateAction;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.thucydides.pages.firmware.FirmwareRuleTemplatePage;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.Steps;

import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class FirmwareRuleTemplatePageSteps {

    private FirmwareRuleTemplatePage page;

    @Steps
    public GenericSteps genericSteps;
    @Steps
    public FirmwareRuleBaseSteps firmwareRuleBaseSteps;
    @Steps
    public FirmwareRuleViewModalSteps firmwareRuleViewModalSteps;

    @Step
    public FirmwareRuleTemplatePageSteps open() {
        page.open();
        return this;
    }

    @Step
    public FirmwareRuleTemplatePageSteps checkValidPriority(Integer value) {
        String selectedPriorityVisibleValue = page.getSelectedPriorityVisibleValue();
        assertEquals(value.toString(), selectedPriorityVisibleValue);

        return this;
    }

    @Step
    public FirmwareRuleTemplatePageSteps clickOptionalCheckBox() {
        page.clickOptionalCheckBox();
        return this;
    }


    @Step
    public FirmwareRuleTemplatePageSteps verifyRuleActionViewModalWindow(FirmwareRuleTemplate template) {
        verifyViewModalWindowGeneral(template, ApplicableAction.Type.RULE_TEMPLATE.name());
        return this;
    }

    @Step
    public FirmwareRuleTemplatePageSteps verifyDefinePropertiesViewModalWindow(FirmwareRuleTemplate template) {
        verifyViewModalWindowGeneral(template, ApplicableAction.Type.DEFINE_PROPERTIES_TEMPLATE.name());
        DefinePropertiesTemplateAction expectedAction = (DefinePropertiesTemplateAction) template.getApplicableAction();
        firmwareRuleViewModalSteps.verifyBypassFilterListItem(expectedAction.getByPassFilters().get(0));
        checkDefinePropertiesOnViewPage((TreeMap<String, DefinePropertiesTemplateAction.PropertyValue>) expectedAction.getProperties());

        return this;
    }

    @Step
    public FirmwareRuleTemplatePageSteps verifyBlockingFiltersViewModalWindow(FirmwareRuleTemplate template) {
        verifyViewModalWindowGeneral(template, ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE.name());
        return this;
    }

    @Step
    public FirmwareRuleTemplatePageSteps clickCreateRuleButton() {
        page.clickCreateRuleButton();
        return this;
    }

    private void verifyViewModalWindowGeneral(FirmwareRuleTemplate template, String expectedApplicableActionType) {
        genericSteps.waitForModalWindowToOpen();
        genericSteps.verifyViewModalTitle("View template " + template.getId());
        firmwareRuleViewModalSteps.verifyId(template.getId());
        firmwareRuleViewModalSteps.verifyActionType(expectedApplicableActionType);
    }

    private void checkDefinePropertiesOnViewPage(TreeMap<String, DefinePropertiesTemplateAction.PropertyValue> expectedProperties) {
        String expectedKey = expectedProperties.firstKey();
        String expectedValue = expectedProperties.firstEntry().getValue().getValue();
        firmwareRuleViewModalSteps.verifyPropertyKey(expectedKey);
        firmwareRuleViewModalSteps.verifyPropertyValue(expectedValue);
    }
}
