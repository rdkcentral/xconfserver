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
package com.comcast.xconf.thucydides.steps.rfc;

import com.comcast.xconf.thucydides.pages.rfc.FeatureTestPage;
import net.thucydides.core.annotations.Step;

public class FeatureTestPageSteps {

    private FeatureTestPage page;

    @Step
    public FeatureTestPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public FeatureTestPageSteps typeKey(String key) {
        page.typeKey(key);
        return this;
    }

    @Step
    public FeatureTestPageSteps typeValue(String value) {
        page.typeValue(value);
        return this;
    }

    @Step
    public FeatureTestPageSteps clickTestButton() {
        page.clickTestButton();
        return this;
    }

    @Step
    public FeatureTestPageSteps waitRuleViewDirective() {
        page.waitRuleViewDirective();
        return this;
    }
}
