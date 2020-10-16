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
 * Author: rdolomansky
 * Created: 3/18/16  1:38 PM
 */
package com.comcast.xconf.thucydides.steps.telemetry;

import com.comcast.xconf.thucydides.pages.telemetry.TestFormPage;
import net.thucydides.core.annotations.Step;

public class TestFormPageSteps {

    private TestFormPage page;

    @Step
    public TestFormPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public TestFormPageSteps clickTestButton() {
        page.clickTestButton();
        return this;
    }

    @Step
    public TestFormPageSteps typeKey(String key) {
        page.typeKey(key);
        return this;
    }

    @Step
    public TestFormPageSteps typeValue(String value) {
        page.typeValue(value);
        return this;
    }

    @Step
    public TestFormPageSteps waitRuleViewDirective() {
        page.waitRuleViewDirective();
        return this;
    }

    @Step
    public TestFormPageSteps clickTypeaheadListItem(String itemName) {
        page.clickTypeaheadListItem(itemName);
        return this;
    }
}
