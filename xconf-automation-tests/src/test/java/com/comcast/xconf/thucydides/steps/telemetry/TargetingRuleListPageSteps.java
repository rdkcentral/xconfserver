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
 * Created: 3/18/16  6:36 PM
 */
package com.comcast.xconf.thucydides.steps.telemetry;

import com.comcast.xconf.thucydides.pages.telemetry.TargetingRuleListPage;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.Steps;

import static org.junit.Assert.assertEquals;

public class TargetingRuleListPageSteps {

    private TargetingRuleListPage page;

    @Steps
    public GenericSteps genericSteps;


    @Step
    public TargetingRuleListPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public TargetingRuleListPageSteps typeNameSearch(String value) {
        page.typeNameSearch(value);
        return this;
    }

    @Step
    public TargetingRuleListPageSteps typeFreeArgSearch(String value) {
        page.typeFreeArgSearch(value);
        return this;
    }

    @Step
    public TargetingRuleListPageSteps typeFixedArgSearch(String value) {
        page.typeFixedArgSearch(value);
        return this;
    }

    @Step
    public TargetingRuleListPageSteps verifyTelemetryRuleName(String expectedTelemetryRuleName) {
        assertEquals(expectedTelemetryRuleName, page.getTelemetryRuleName());
        return this;
    }

    public TargetingRuleListPageSteps verifyTelemetryRulesCount(int expectedCount) {
        assertEquals(expectedCount, page.getTelemetryRulesCount());
        return this;
    }

}
