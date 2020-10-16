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
 * Created: 3/28/16  2:47 PM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.logupload.Schedule;
import com.comcast.xconf.thucydides.pages.dcm.DeviceSettingsViewPageObjects;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.*;

public class DeviceSettingsViewSteps {

    private DeviceSettingsViewPageObjects page;

    @Step
    public DeviceSettingsViewSteps verifyName(String value) {
        assertEquals(value, page.getName());
        return this;
    }

    @Step
    public DeviceSettingsViewSteps verifyCheckOnReboot(Boolean value) {
        assertEquals(value, Boolean.valueOf(page.getCheckOnReboot()));
        return this;
    }

    @Step
    public DeviceSettingsViewSteps verifySettingsAreActive(Boolean value) {
        assertEquals(value, Boolean.valueOf(page.getSettingsAreActive()));
        return this;
    }

    @Step
    public DeviceSettingsViewSteps verifyScheduleType(String value) {
        assertEquals(value, page.getScheduleType());
        return this;
    }

    @Step
    public DeviceSettingsViewSteps verifyExpression(String value) {
        assertEquals(value, page.getExpression());
        return this;
    }

    @Step
    public DeviceSettingsViewSteps verifyTimeWindow(Integer value) {
        assertEquals(value, Integer.valueOf(page.getTimeWindow()));
        return this;
    }
    @Step
    public DeviceSettingsViewSteps verifyViewPage(DeviceSettings expectedSettings) {
        Schedule expectedSchedule = expectedSettings.getSchedule();
        verifyName(expectedSettings.getName())
                .verifyCheckOnReboot(expectedSettings.getCheckOnReboot())
                .verifySettingsAreActive(expectedSettings.getSettingsAreActive())
                .verifyScheduleType(expectedSchedule.getType())
                .verifyExpression(expectedSchedule.getExpression())
                .verifyTimeWindow(expectedSchedule.getTimeWindowMinutes());
        return this;
    }
}
