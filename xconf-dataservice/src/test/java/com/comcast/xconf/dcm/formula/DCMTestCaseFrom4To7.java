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
 *  Created: 12/15/15 2:31 PM
 */

package com.comcast.xconf.dcm.formula;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.logupload.*;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * Testing expression (A or B or C)
 */
public class DCMTestCaseFrom4To7 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId = "formulaId4To7";
    private FormulaDataObject formula = createFormula();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();
    private VodSettings vodSettings = createVodSettings();
    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();

    @Before
    public void init() throws Exception {
        initLists(namespacedLists, logFiles);
        initDcmFormula(formula);
        initDcmSettings(deviceSettings, logUploadSettings, vodSettings);
    }

    @Test
    public void testBehaviorWhenEcmMacIsPresent() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?ecmMacAddress=EA:AA:4A:9B:02:03"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId)));
    }

    @Test
    public void testBehaviorWhenEstbMacAndEstbIpArePresent() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?estbMacAddress=BC:98:01:AA:AA:BE&estbIP=35.45.75.95"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId)));
    }

    @Test
    public void testBehaviorWhenAllParametersArePresent() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?ecmMacAddress=EA:AA:4A:9B:02:03&estbMacAddress=BC:98:01:AA:AA:BE&estbIP=28.58.98.138"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId)));
    }

    @Test
    public void testBehaviorWhenAllParametersAreWrong() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?ecmMacAddress=EA:AA:4B:9B:02:03&estbMacAddress=BC:78:01:AA:AA:BE&estbIP=28.48.98.138"))
                .andExpect(status().isNotFound());
    }

    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_TC4");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("ActNow");
        schedule.setExpression("35 09 * * *");
        schedule.setTimeWindowMinutes(30);
        schedule.setStartDate("2014-01-01 01:01:01");
        schedule.setEndDate("2016-02-01 01:02:01");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC4");
        logUploadSettings.setUploadOnReboot(false);
        logUploadSettings.setNumberOfDays(1);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-01-02 02:01:02");
        logUploadSettings.setToDateTime("2016-02-01 02:02:02");
        logUploadSettings.setModeToGetLogFiles("AllLogFiles");
        logUploadSettings.setSchedule(createLogUploadSettingsSchedule());
        List<String> logFileNameList = new ArrayList<>();
        for (LogFile logFile: createLogFileList()) {
            logFileNameList.add(logFile.getId());
        }
        logUploadSettings.setLogFileIds(logFileNameList);
        return logUploadSettings;
    }

    private Schedule createLogUploadSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("ActNow");
        schedule.setExpression("20 10 * * *");
        schedule.setTimeWindowMinutes(20);
        schedule.setStartDate("2014-01-02 02:01:02");
        schedule.setEndDate("2016-02-01 02:02:02");
        return schedule;
    }

    private VodSettings createVodSettings() {
        VodSettings vodSettings = new VodSettings();
        vodSettings.setId(defaultFormulaId);
        vodSettings.setName("guneet_VOD_TC4");
        vodSettings.setLocationsURL("http://foo.com");
        vodSettings.setSrmIPList(new HashMap<String, String>());
        return vodSettings;
    }

    private List<LogFile> createLogFileList() {
        LogFile logFile1 = new LogFile();
        logFile1.setId("XQA_LogFile_001");
        logFile1.setName("XQA_LogFile_001");
        logFile1.setDeleteOnUpload(false);
        return Lists.newArrayList(logFile1);
    }

    private FormulaDataObject createFormula() {
        FormulaDataObject formula = new FormulaDataObject();
        formula.setId(defaultFormulaId);
        formula.setName("formulaId");
        formula.setName("formula4To7");
        formula.setDescription("guneet_formula_TC4");
        formula.setPriority(2);
        formula.setPercentage(100);
        formula.setEcmMacAddress("ecmMacListFormula4To7");
        formula.setEstbMacAddress("estbMacListFormula4To7");
        formula.setEstbIP("ipListFormula4To7");
        formula.setRuleExpression("ecmMacAddress or estbMacAddress or estbIP");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("ecmMacListFormula4To7", GenericNamespacedListTypes.MAC_LIST, "EA:AA:4A:9B:02:03"));
        namespacedLists.add(createGenericNamespacedList("estbMacListFormula4To7", GenericNamespacedListTypes.MAC_LIST, "BC:98:01:AA:AA:BE"));
        namespacedLists.add(createGenericNamespacedList("ipListFormula4To7", GenericNamespacedListTypes.IP_LIST, "35.45.75.95,28.58.98.138"));
        return namespacedLists;
    }
}
