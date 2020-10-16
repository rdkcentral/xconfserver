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
 * Created: 3/1/16  4:15 PM
 */
package com.comcast.xconf.thucydides.steps.common;

import com.comcast.xconf.thucydides.pages.common.EnvironmentPage;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class EnvironmentPageSteps {

    private EnvironmentPage page;

    @Step
    public EnvironmentPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public EnvironmentPageSteps verifyId(String expectedId) {
        assertEquals(expectedId, page.getId());
        return this;
    }

    @Step
    public EnvironmentPageSteps verifyDescription(String expectedDescription) {
        assertEquals(expectedDescription, page.getDescription());
        return this;
    }

    @Step
    public EnvironmentPageSteps verifyEnvironmentsCount(int expectedCount) {
        assertEquals(expectedCount, page.getEnvironmentsCount());
        return this;
    }
}
