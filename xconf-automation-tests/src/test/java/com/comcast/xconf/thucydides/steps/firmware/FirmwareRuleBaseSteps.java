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
 * Created: 3/15/16  12:29 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.xconf.thucydides.pages.firmware.FirmwareRuleBasePage;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FirmwareRuleBaseSteps {

    private FirmwareRuleBasePage page;

    @Step
    public FirmwareRuleBaseSteps clickRuleActionsTab() {
        page.clickRuleActionsTab();
        return this;
    }

    @Step
    public FirmwareRuleBaseSteps clickDefinePropertiesTab() {
        page.clickDefinePropertiesTab();
        return this;
    }

    @Step
    public FirmwareRuleBaseSteps clickBlockingFiltersTab() {
        page.clickBlockingFiltersTab();
        return this;
    }

    @Step
    public FirmwareRuleBaseSteps selectBypassFilter() {
        page.clickBypassFiltersList();
        page.clickBypassFilterListItem();
        return this;
    }

    @Step
    public FirmwareRuleBaseSteps verifySelectedBypassFilter(String expectedBypassFilterId) {
        String actualBypassFilterId = page.getSelectedBypassFilterId();
        assertEquals(expectedBypassFilterId, actualBypassFilterId);
        return this;
    }

    @Step
    public FirmwareRuleBaseSteps typePropertyValue(String value) {
        page.typePropertyValue(value);
        return this;
    }

    @Step
    public FirmwareRuleBaseSteps clickPropertiesEditButton() {
        page.clickPropertiesEditButton();
        return this;
    }

    @Step
    public FirmwareRuleBaseSteps verifyPropertiesKeyIsInputIsDisabled() {
        assertTrue(page.propertiesKeyInputIsDisabled());
        return this;
    }

    @Step
    public FirmwareRuleBaseSteps clickAddPropertyButton() {
        page.clickAddPropertyButton();
        return this;
    }

    @Step
    public FirmwareRuleBaseSteps typePropertyKey(String key) {
        page.typePropertyKey(key);
        return this;
    }
}
