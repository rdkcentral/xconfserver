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
 * Created: 3/29/16  3:37 PM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.xconf.logupload.VodSettings;
import com.comcast.xconf.thucydides.pages.dcm.VodSettingsViewPageObjects;
import net.thucydides.core.annotations.Step;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

public class VodSettingsViewSteps {

    private VodSettingsViewPageObjects page;

    @Step
    public VodSettingsViewSteps verifyName(String value) {
        assertEquals(value, page.getName());
        return this;
    }

    @Step
    public VodSettingsViewSteps verifyLocationsUrl(String value) {
        assertEquals(value, page.getLocationsUrl());
        return this;
    }

    @Step
    public VodSettingsViewSteps verifySrmName(String value) {
        assertEquals(value, page.getSrmName());
        return this;
    }

    @Step
    public VodSettingsViewSteps verifySrmIp(String value) {
        assertEquals(value, page.getSrmIp());
        return this;
    }

    @Step
    public VodSettingsViewSteps verifyViewPage(VodSettings expectedSettings) {
        verifyName(expectedSettings.getName())
                .verifyLocationsUrl(expectedSettings.getLocationsURL())
                .verifySrmName(expectedSettings.getIpNames().get(0))
                .verifySrmIp(expectedSettings.getIpList().get(0));
        return this;
    }
}
