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
 *  Author: mdolina
 *  Created: 12/9/15 4:35 PM
 */

package com.comcast.xconf.admin.controller.dcm;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.search.SearchFields;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeviceSettingsControllerTest extends BaseControllerTest{

    @Test
    public void testGet() throws Exception {
        DeviceSettings deviceSettings = createDeviceSettings();
        deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);
        mockMvc.perform(get("/" + DeviceSettingsController.URL_MAPPING + "/" + deviceSettings.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(deviceSettings)));
        mockMvc.perform(get("/" + DeviceSettingsController.URL_MAPPING))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(deviceSettings))));
    }

    @Test
    public void testGetPage() throws Exception {
        DeviceSettings deviceSettings = createDeviceSettings();
        deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);
        mockMvc.perform(get("/" + DeviceSettingsController.URL_MAPPING + "/page")
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(deviceSettings))));
    }

    @Test
    public void testSaveDeviceSettings() throws Exception {
        DeviceSettings deviceSettings = createDeviceSettings();
        System.out.println(deviceSettings);
        mockMvc.perform(post("/" + DeviceSettingsController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(deviceSettings)))
                .andExpect(content().json(JsonUtil.toJson(deviceSettings)));
    }

    @Test
    public void saveDeviceSettingsWithInvalidCronDayAndMonth() throws Exception {
        DeviceSettings deviceSettings = createDeviceSettings();
        deviceSettings.setSchedule(createSchedule("4 5 30 1 *"));
        System.out.println(deviceSettings);
        ResultActions resultActions = mockMvc.perform(post("/" + DeviceSettingsController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(deviceSettings)))
                .andExpect(status().isBadRequest());

        assertException(resultActions, ValidationRuntimeException.class, "CronExpression has unparseable day or month value: " + deviceSettings.getSchedule().getExpression());
    }

    @Test
    public void testGetDeviceSettingsNames() throws Exception {
        DeviceSettings deviceSettings = createDeviceSettings();
        deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);
        mockMvc.perform(get("/" + DeviceSettingsController.URL_MAPPING + "/names"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(deviceSettings.getName()))));
    }

    @Test
    public void testDeleteDeviceSettings() throws Exception {
        DeviceSettings deviceSettings = createDeviceSettings();
        deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);
        mockMvc.perform(delete("/" + DeviceSettingsController.URL_MAPPING + "/" + deviceSettings.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/" + DeviceSettingsController.URL_MAPPING + "/" + deviceSettings.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetDeviceSettingsSize() throws Exception {
        DeviceSettings deviceSettings = createDeviceSettings();
        deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);
        mockMvc.perform(get("/" + DeviceSettingsController.URL_MAPPING + "/size"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson("1")));
    }

    @Test
    public void checkSorting() throws Exception {
        List<DeviceSettings> deviceSettingsList = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            DeviceSettings deviceSettings = changeDeviceSettings(createDeviceSettings(), "deviceSettingsId" + i, "deviceSettingsName" + i);
            deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);
            deviceSettingsList.add(deviceSettings);
        }

        mockMvc.perform(get("/" + DeviceSettingsController.URL_MAPPING + "/page")
                .param("pageSize", "10")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(deviceSettingsList)));
    }

    @Test
    public void getFiltered() throws Exception {
        DeviceSettings deviceSettings1 = createDeviceSettings("deviceSettings1");
        deviceSettings1.setName("deviceSettings1");
        deviceSettingsDAO.setOne(deviceSettings1.getId(), deviceSettings1);

        DeviceSettings deviceSettings2 = createDeviceSettings("deviceSettings2");
        deviceSettings2.setName("deviceSettings2");
        deviceSettingsDAO.setOne(deviceSettings2.getId(), deviceSettings2);

        Map<String, String> context = Collections.singletonMap(SearchFields.NAME, deviceSettings1.getName());

        mockMvc.perform(post("/" + DeviceSettingsController.URL_MAPPING + "/filtered")
                .param("pageNumber", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(context)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(deviceSettings1))));
    }

    protected DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId("deviceSettingsId");
        deviceSettings.setName("deviceSettingsName");
        deviceSettings.setSchedule(createSchedule());
        deviceSettings.setCheckOnReboot(false);
        deviceSettings.setSettingsAreActive(false);
        return deviceSettings;
    }

    private DeviceSettings changeDeviceSettings(DeviceSettings deviceSettings, String id, String name) {
        deviceSettings.setId(id);
        deviceSettings.setName(name);
        return deviceSettings;
    }
}