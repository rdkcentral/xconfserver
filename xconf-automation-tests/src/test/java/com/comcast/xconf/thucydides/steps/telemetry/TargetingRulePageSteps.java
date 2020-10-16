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
 * Created: 3/16/16  8:40 PM
 */
package com.comcast.xconf.thucydides.steps.telemetry;

import com.comcast.xconf.thucydides.pages.telemetry.TargetingRulePage;
import net.thucydides.core.annotations.Step;

public class TargetingRulePageSteps {

    private TargetingRulePage page;

    @Step
    public TargetingRulePageSteps open() {
        page.open();
        return this;
    }

    @Step
    public TargetingRulePageSteps typeName(final String value) {
        page.typeName(value);
        return this;
    }

    @Step
    public TargetingRulePageSteps typeBoundTelemetryId(final String value) {
        page.typeBoundTelemetryId(value);
        return this;
    }
}
