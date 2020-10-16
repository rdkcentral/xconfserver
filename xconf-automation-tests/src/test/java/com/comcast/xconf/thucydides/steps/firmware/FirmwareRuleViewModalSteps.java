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
 * Created: 3/15/16  6:06 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.xconf.thucydides.pages.firmware.FirmwareRuleViewModalPageObjects;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class FirmwareRuleViewModalSteps {

    private FirmwareRuleViewModalPageObjects page;

    @Step
    public FirmwareRuleViewModalSteps verifyBypassFilterListItem(String expectedBypassFilterValue) {
        assertEquals(expectedBypassFilterValue, page.getBypassFilterListItem());
        return this;
    }

    @Step
    public FirmwareRuleViewModalSteps verifyId(String expectedId) {
        assertEquals(expectedId, page.getId());
        return this;
    }

    @Step
    public FirmwareRuleViewModalSteps verifyActionType(String expectedActionType) {
        assertEquals(expectedActionType, page.getActionType());
        return this;
    }

    @Step
    public FirmwareRuleViewModalSteps verifyPropertyKey(String expectedPropertyKey) {
        assertEquals(expectedPropertyKey, page.getPropertyKey());
        return this;
    }

    @Step
    public FirmwareRuleViewModalSteps verifyPropertyValue(String expectedPropertyValue) {
        assertEquals(expectedPropertyValue, page.getPropertyValue());
        return this;
    }

    @Step
    public FirmwareRuleViewModalSteps verifyConfigId(String expectedConfigId) {
        assertEquals(expectedConfigId, page.getConfigId());
        return this;
    }

    @Step
    public FirmwareRuleViewModalSteps verifyConfigDescription(String expectedConfigDescription) {
        assertEquals(expectedConfigDescription, page.getConfigDescription());
        return this;
    }

    @Step
    public FirmwareRuleViewModalSteps verifyConfigFileName(String expectedFileName) {
        assertEquals(expectedFileName, page.getConfigFileName());
        return this;
    }

    @Step
    public FirmwareRuleViewModalSteps verifyConfigVersion(String expectedConfigVersion) {
        assertEquals(expectedConfigVersion, page.getConfigVersion());
        return this;
    }

    @Step
    public FirmwareRuleViewModalSteps verifyConfigSupportedModel(String expectedModelId) {
        assertEquals(expectedModelId, page.getConfigSupportedModel());
        return this;
    }
}
