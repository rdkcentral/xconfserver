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
 * Author: mdolina
 * Created: 10/4/16  4:50 PM
 */
package com.comcast.xconf.thucydides.steps;

import com.comcast.xconf.thucydides.pages.RuleViewEditorDirectivePageObjects;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class RuleViewEditorSteps {

    private RuleViewEditorDirectivePageObjects page;

    @Step
    public RuleViewEditorSteps removeConditionByFreeArgName(String freeArgName) {
        page.removeConditionByFreeArgName(freeArgName);
        return this;
    }

    @Step
    public RuleViewEditorSteps verifyHeadFreeArg(String expectedFreeArg) {
        assertEquals(expectedFreeArg, page.getHeadFreeArg());
        return this;
    }
}
