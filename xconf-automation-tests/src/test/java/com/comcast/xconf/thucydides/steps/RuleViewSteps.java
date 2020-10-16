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
 * Created: 3/15/16  4:34 PM
 */
package com.comcast.xconf.thucydides.steps;

import com.comcast.xconf.thucydides.pages.RuleViewDirectivePageObjects;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuleViewSteps {

    private RuleViewDirectivePageObjects page;

    @Step
    public RuleViewSteps verifyRelation(String expectedRelation) {
        assertEquals(expectedRelation, page.getRelation());
        return this;
    }

    @Step
    public RuleViewSteps verifyIsNegated() {
        assertTrue(page.isNegated());
        return this;
    }

    @Step
    public RuleViewSteps verifyFreeArg(String expectedFreeArg) {
        assertEquals(expectedFreeArg, page.getFreeArg());
        return this;
    }

    @Step
    public RuleViewSteps verifyOperation(String exectedOperation) {
        assertEquals(exectedOperation, page.getOperation());
        return this;
    }

    @Step
    public RuleViewSteps verifyFixedArg(String expectedFixedArg) {
        assertEquals(expectedFixedArg, page.getFixedArg());
        return this;
    }
}
