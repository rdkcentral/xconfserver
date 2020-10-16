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
 * Created: 3/28/2016
*/
package com.comcast.xconf.thucydides.steps.setting;

import com.comcast.xconf.thucydides.pages.setting.SettingProfilePage;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class SettingProfilePageSteps {

    private SettingProfilePage page;

    @Step
    public SettingProfilePageSteps open() {
        page.open();
        return this;
    }

    @Step
    public SettingProfilePageSteps typeName(final String name) {
        page.typeName(name);
        return this;
    }

    @Step
    public SettingProfilePageSteps selectSettingType(final String name) {
        page.selectType(name);
        return this;
    }

    @Step
    public SettingProfilePageSteps typeEntryKey(final String name) {
        page.typeEntryKey(name);
        return this;
    }

    @Step
    public SettingProfilePageSteps typeEntryValue(final String name) {
        page.typeEntryValue(name);
        return this;
    }

    @Step
    public SettingProfilePageSteps setSearchName(String value) {
        page.setSearchName(value);
        return this;
    }

    @Step
    public SettingProfilePageSteps waitNotFoundResultMessage() {
        page.waitNotFoundResultMessage();
        return this;
    }

    @Step
    public SettingProfilePageSteps verifySettingProfilesCount(int expectedCount) {
        assertEquals(expectedCount, page.getSettingProfilesCount());
        return this;
    }

}
