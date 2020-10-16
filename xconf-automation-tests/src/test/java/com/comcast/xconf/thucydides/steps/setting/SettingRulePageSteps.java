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
package com.comcast.xconf.thucydides.steps.setting;

import com.comcast.xconf.thucydides.pages.setting.SettingRulePage;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class SettingRulePageSteps {

    private SettingRulePage page;

    @Step
    public SettingRulePageSteps open() {
        page.open();
        return this;
    }

    @Step
    public SettingRulePageSteps typeName(final String value) {
        page.typeName(value);
        return this;
    }

    @Step
    public SettingRulePageSteps typeBoundSettingId(final String value) {
        page.typeBoundSettingId(value);
        return this;
    }

    @Step
    public SettingRulePageSteps verifySettingRulesCount(int expectedSettingRulesCount) {
        assertEquals(expectedSettingRulesCount, page.getSettingRulesCount());
        return this;
    }

    public SettingRulePageSteps typeNameSearch(String value) {
        page.typeNameSearch(value);
        return this;
    }

    public SettingRulePageSteps typeFreeArgSearch(String value) {
        page.typeFreeArgSearch(value);
        return this;
    }

    public SettingRulePageSteps typeFixedArgSearch(String value) {
        page.typeFixedArgSearch(value);
        return this;
    }

    public String getSettingRuleName() {
        return page.getSettingRuleName();
    }

}
