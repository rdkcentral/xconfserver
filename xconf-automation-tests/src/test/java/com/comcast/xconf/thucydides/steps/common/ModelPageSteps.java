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
 * Author: Stanislav Menshykov
 * Created: 3/9/16  2:37 PM
 */
package com.comcast.xconf.thucydides.steps.common;

import com.comcast.xconf.thucydides.pages.common.ModelPage;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class ModelPageSteps {

    private ModelPage page;

    @Step
    public ModelPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public ModelPageSteps verifyId(String expectedId) {
        assertEquals(expectedId, page.getId());
        return this;
    }

    @Step
    public ModelPageSteps verifyDescription(String expectedDescriontion) {
        assertEquals(expectedDescriontion, page.getDescrition());
        return this;
    }

    @Step
    public ModelPageSteps verifyModelsCount(int expectedCount) {
        assertEquals(expectedCount, page.getModelsCount());
        return this;
    }
}
