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
import com.comcast.xconf.logupload.LogFile;
import com.comcast.xconf.logupload.LogUploadSettings;
import com.comcast.xconf.logupload.UploadRepository;
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

public class LogUploadSettingsControllerTest extends BaseControllerTest{

    @Test
    public void getLogUploadSettings() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
        LogUploadSettings logUploadSettings = createLogUploadSettings();
        logUploadSettingsDAO.setOne(logUploadSettings.getId(), logUploadSettings);

        mockMvc.perform(get("/" + LogUploadSettingsController.URL_MAPPING + "/" + logUploadSettings.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(logUploadSettings)));

        mockMvc.perform(get("/" + LogUploadSettingsController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(logUploadSettings))));
    }

    @Test
    public void getPage() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
        LogUploadSettings logUploadSettings = createLogUploadSettings();
        logUploadSettingsDAO.setOne(logUploadSettings.getId(), logUploadSettings);

        mockMvc.perform(get("/" + LogUploadSettingsController.URL_MAPPING + "/page")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10").param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(logUploadSettings))));
    }

    @Test
    public void getDeviceSettingsNames() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
        LogUploadSettings logUploadSettings = createLogUploadSettings();
        logUploadSettingsDAO.setOne(logUploadSettings.getId(), logUploadSettings);

        mockMvc.perform(get("/" + LogUploadSettingsController.URL_MAPPING + "/names")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(logUploadSettings.getName()))));
    }

    @Test
    public void saveLogUploadSettings() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
        LogUploadSettings logUploadSettings = createLogUploadSettings();

        mockMvc.perform(post("/" + LogUploadSettingsController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(logUploadSettings)))
                .andExpect(status().isCreated())
                .andExpect(content().json(JsonUtil.toJson(logUploadSettings)));
    }

    @Test
    public void saveLogUploadSettingsWithInvalidCronDayAndMonth() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
        LogUploadSettings logUploadSettings = createLogUploadSettings();
        logUploadSettings.setSchedule(createSchedule("4 5 30 1 *"));

        ResultActions resultActions = mockMvc.perform(post("/" + LogUploadSettingsController.URL_MAPPING)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(logUploadSettings)))
                .andExpect(status().isBadRequest());

        assertException(resultActions, ValidationRuntimeException.class, "CronExpression has unparseable day or month value: " + logUploadSettings.getSchedule().getExpression());
    }

    @Test
    public void deleteLogUploadSettings() throws Exception {
        LogFile logFile = createLogFile();
        logFileDAO.setOne(logFile.getId(), logFile);
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
        LogUploadSettings logUploadSettings = createLogUploadSettings();
        logUploadSettingsDAO.setOne(logUploadSettings.getId(), logUploadSettings);

        mockMvc.perform(delete("/" + LogUploadSettingsController.URL_MAPPING + "/" + logUploadSettings.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/" + LogUploadSettingsController.URL_MAPPING + "/" + logUploadSettings.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetLogUploadSettingsSize() throws Exception {
        LogFile logFile = createLogFile();
        logFileDAO.setOne(logFile.getId(), logFile);
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
        LogUploadSettings logUploadSettings = createLogUploadSettings();
        logUploadSettingsDAO.setOne(logUploadSettings.getId(), logUploadSettings);

        mockMvc.perform(get("/" + LogUploadSettingsController.URL_MAPPING + "/size")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json(JsonUtil.toJson("1")));

    }

    @Test
    public void checkSorting() throws Exception {
        List<LogUploadSettings> logUploadSettingsList = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            LogUploadSettings logUploadSettings = changeIdAndName(createLogUploadSettings(), "logUploadSettingsId" + i, "logUploadSettingsName" + i);
            logUploadSettingsDAO.setOne(logUploadSettings.getId(), logUploadSettings);
            logUploadSettingsList.add(logUploadSettings);
        }

        mockMvc.perform(get("/" + LogUploadSettingsController.URL_MAPPING + "/page")
                .param("pageSize", "10").param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(logUploadSettingsList)));
    }

    @Test
    public void getFiltered() throws Exception {
        LogUploadSettings logUploadSettings1 = createLogUploadSettings("logUploadSettings1");
        logUploadSettings1.setName("logUploadSettings1");
        logUploadSettingsDAO.setOne(logUploadSettings1.getId(), logUploadSettings1);

        LogUploadSettings logUploadSettings2 = createLogUploadSettings("logUploadSettings2");
        logUploadSettings2.setName("logUploadSettings2");
        logUploadSettingsDAO.setOne(logUploadSettings2.getId(), logUploadSettings2);

        Map<String, String> searchContext = Collections.singletonMap(SearchFields.NAME, logUploadSettings1.getName());

        mockMvc.perform(post("/" + LogUploadSettingsController.URL_MAPPING + "/filtered")
                .param("pageNumber", "1")
                .param("pageSize", "10").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(logUploadSettings1))));
    }

    private LogUploadSettings changeIdAndName(LogUploadSettings logUploadSettings, String id, String name) {
        logUploadSettings.setId(id);
        logUploadSettings.setName(name);
        return logUploadSettings;
    }
}