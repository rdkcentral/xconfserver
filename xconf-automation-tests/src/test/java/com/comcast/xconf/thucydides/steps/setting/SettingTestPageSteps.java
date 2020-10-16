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
package com.comcast.xconf.thucydides.steps.setting;

import com.comcast.xconf.thucydides.pages.setting.SettingTestPage;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang.StringUtils;

public class SettingTestPageSteps {

    private SettingTestPage page;

    @Step
    public SettingTestPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public SettingTestPageSteps selectSettingType(int lineNumber) {
        page.selectSettingType(lineNumber);
        return this;
    }

    public SettingTestPageSteps addParameterEntity(String key, String value) {
        int entitiesSize = page.getParameterEntities().size();
        if (entitiesSize != 1 || StringUtils.isNotBlank(page.getKeyInput(entitiesSize - 1)) && StringUtils.isNotBlank(page.getValueInput(entitiesSize - 1))) {
            page.addParameter();
            entitiesSize = page.getParameterEntities().size();
        }

        page.setKeyInput(entitiesSize - 1, key);
        page.setValueInput(entitiesSize - 1, value);

        return this;
    }

    @Step
    public SettingTestPageSteps testRule() {
        page.testRule();
        return this;
    }

    @Step
    public boolean hasMatchedRules() {
        return page.hasMatchedRules();
    }

    @Step
    public SettingTestPageSteps clickSettingTypeMultiselect() {
        page.clickSettingTypeMultiselect();
        return this;
    }

    @Step
    public SettingTestPageSteps doubleClickSettingTypeMultiselect() {
        page.clickSettingTypeMultiselect();
        page.clickSettingTypeMultiselect();
        return this;
    }

}
