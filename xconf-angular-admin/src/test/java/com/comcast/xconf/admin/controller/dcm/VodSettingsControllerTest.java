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
 *  Created: 12/8/15 3:14 PM
 */

package com.comcast.xconf.admin.controller.dcm;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.logupload.VodSettings;
import com.comcast.xconf.search.SearchFields;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VodSettingsControllerTest extends BaseControllerTest {

    @Test
    public void getVodSettings() throws Exception {
        VodSettings vodSettings = createVodSettings();
        vodSettingsDAO.setOne(vodSettings.getId(), vodSettings);

        mockMvc.perform(get("/" + VodSettingsController.URL_MAPPING + "/" + vodSettings.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(vodSettings)));

        mockMvc.perform(get("/" + VodSettingsController.URL_MAPPING))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(vodSettings))));
    }



    @Test
    public void save() throws Exception {
        VodSettings vodSettings = createVodSettings();

        mockMvc.perform(post("/" + VodSettingsController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(vodSettings)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(vodSettings)));
    }

    @Test
    public void getPage() throws Exception {
        VodSettings vodSettings = createVodSettings();
        vodSettingsDAO.setOne(vodSettings.getId(), vodSettings);

        mockMvc.perform(get("/" + VodSettingsController.URL_MAPPING + "/page")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(vodSettings))));
    }

    @Test
    public void deleteVodSettings() throws Exception {
        VodSettings vodSettings = createVodSettings();
        vodSettingsDAO.setOne(vodSettings.getId(), vodSettings);

        mockMvc.perform(delete("/" + VodSettingsController.URL_MAPPING + "/" + vodSettings.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/" + VodSettingsController.URL_MAPPING + "/" + vodSettings.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getVodSettingsSize() throws Exception {
        VodSettings vodSettings = createVodSettings();
        vodSettingsDAO.setOne(vodSettings.getId(), vodSettings);

        mockMvc.perform(get("/" + VodSettingsController.URL_MAPPING + "/size"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson("1")));
    }

    @Test
    public void checkSorting() throws Exception {
        List<VodSettings> vodSettingsList = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            VodSettings vodSettings = changeIdAndName(createVodSettings(), "vodSettingsId" + i, "vodSettingsName" + i);
            vodSettingsDAO.setOne(vodSettings.getId(), vodSettings);
            vodSettingsList.add(vodSettings);
        }

        mockMvc.perform(get("/" + VodSettingsController.URL_MAPPING + "/page")
                .param("pageSize", "10")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtil.toJson(vodSettingsList)));
    }

    @Test
    public void getFiltered() throws Exception {
        VodSettings vodSettings1 = createVodSettings("vodSettings1");
        vodSettings1.setName("vodSettings1");
        vodSettingsDAO.setOne(vodSettings1.getId(), vodSettings1);

        VodSettings vodSettings2 = createVodSettings("vodSettings2");
        vodSettings2.setName("vodSettings2");
        vodSettingsDAO.setOne(vodSettings2.getId(), vodSettings2);

        Map<String, String> searchContext = Collections.singletonMap(SearchFields.NAME, vodSettings1.getName());

        mockMvc.perform(post("/" + VodSettingsController.URL_MAPPING + "/filtered")
                .param("pageNumber", "1")
                .param("pageSize", "10").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(searchContext)))
                .andExpect(status().isOk()).andExpect(content().json(JsonUtil.toJson(Collections.singletonList(vodSettings1))));
    }

    private VodSettings changeIdAndName(VodSettings vodSettings, String id, String name) {
        vodSettings.setId(id);
        vodSettings.setName(name);
        return vodSettings;
    }
}