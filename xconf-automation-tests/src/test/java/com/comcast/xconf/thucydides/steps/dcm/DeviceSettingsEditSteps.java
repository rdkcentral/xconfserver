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
 * Created: 3/25/16  4:34 PM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.xconf.thucydides.pages.dcm.DeviceSettingsEditPageObjects;
import net.thucydides.core.annotations.Step;

public class DeviceSettingsEditSteps {

    private DeviceSettingsEditPageObjects page;

    @Step
    public DeviceSettingsEditSteps typeName(String value) {
        page.typeName(value);
        return this;
    }

    @Step
    public DeviceSettingsEditSteps selectCheckOnReboot(Boolean value) {
        page.selectCheckOnReboot(value.toString());
        return this;
    }

    @Step
    public DeviceSettingsEditSteps selectSettingsAreActive(Boolean value) {
        page.selectSettingsAreActive(value.toString());
        return this;
    }

    @Step
    public DeviceSettingsEditSteps selectScheduleType(String value) {
        page.selectScheduleType(value);
        return this;
    }

    @Step
    public DeviceSettingsEditSteps typeCronMinutes(Integer minutes) {
        page.typeCronMinutes(minutes);
        return this;
    }

    @Step
    public DeviceSettingsEditSteps typeCronHours(Integer hours) {
        page.typeCronHours(hours);
        return this;
    }

    @Step
    public DeviceSettingsEditSteps typeWindowInput(Integer value) {
        page.typeWindowInput(value.toString());
        return this;
    }

    @Step
    public DeviceSettingsEditSteps fillForm(String name) {
        typeName(name)
                .selectCheckOnReboot(true)
                .selectSettingsAreActive(true)
                .selectScheduleType("CronExpression")
                .typeCronHours(5)
                .typeCronMinutes(35)
                .typeWindowInput(10);
        return this;
    }
}
