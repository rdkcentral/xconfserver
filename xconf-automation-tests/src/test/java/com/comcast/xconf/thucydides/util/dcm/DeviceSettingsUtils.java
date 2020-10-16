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
 * Created: 3/28/16  1:47 PM
 */
package com.comcast.xconf.thucydides.util.dcm;

import com.beust.jcommander.internal.Lists;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.logupload.Schedule;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;

import java.util.List;
import java.util.UUID;

public class DeviceSettingsUtils {
    private static final String DEVICE_SETTINGS_URL = "dcm/deviceSettings/";

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(DEVICE_SETTINGS_URL, DeviceSettings.class);
        FormulaUtils.doCleanup();
    }

    public static DeviceSettings createAndSaveDefaultDeviceSettings() throws Exception {
        DeviceSettings result = createDefaultDeviceSettings();
        HttpClient.post(GenericTestUtils.buildFullUrl(DEVICE_SETTINGS_URL), result);

        return result;
    }

    public static List<DeviceSettings> createAndSaveDeviceSettingsList() throws Exception {
        return Lists.newArrayList(
               createAndSaveDeviceSettings("deviceSettings1"),
               createAndSaveDeviceSettings("deviceSettings2")
        );
    }

    public static DeviceSettings createDefaultDeviceSettings() throws Exception {
        DCMGenericRule formula = FormulaUtils.createAndSaveDefaultFormula();
        DeviceSettings result = createDeviceSettingsWithoutFormula(formula.getId());

        return result;
    }

    public static DeviceSettings createDeviceSettingsWithoutFormula(String id) {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(id);
        deviceSettings.setName("deviceSettingsName");
        deviceSettings.setCheckOnReboot(false);
        deviceSettings.setSettingsAreActive(true);
        Schedule schedule = new Schedule();
        schedule.setType("ActNow");
        schedule.setExpression("expression");
        schedule.setTimeWindowMinutes(42);
        deviceSettings.setSchedule(schedule);

        return deviceSettings;
    }

    public static DeviceSettings createAndSaveDeviceSettings(String name) throws Exception {
        DeviceSettings deviceSettings = createDeviceSettingsWithoutFormula(UUID.randomUUID().toString());
        deviceSettings.setName(name);
        HttpClient.post(GenericTestUtils.buildFullUrl(DEVICE_SETTINGS_URL), deviceSettings);
        return deviceSettings;
    }
}
