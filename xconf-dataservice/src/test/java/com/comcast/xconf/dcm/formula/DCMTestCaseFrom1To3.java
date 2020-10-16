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
 *  Created: 12/14/15 5:40 PM
 */

package com.comcast.xconf.dcm.formula;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.logupload.LogFile;
import com.comcast.xconf.logupload.LogUploadSettings;
import com.comcast.xconf.logupload.Schedule;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
* Testing expression (A and B and C)
*/
public class DCMTestCaseFrom1To3 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId = "formulaId1To3";
    private FormulaDataObject formula = createFormulaObject();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();
    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();

    @Before
    public void init() throws Exception {
        initDcmFormula(formula);
        initDcmSettings(deviceSettings, logUploadSettings, null);
        initLists(namespacedLists, logFiles);
    }

    @Test
    public void testBehaviorWhenEcmMacIsWrongAndAntoherParametersIsMissing() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?ecmMacAddress=AE:28:CB:AA:AA:21"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testBehaviorWhenEcmMacIsMissing() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?estbMacAddress=AC:24:38:AA:19:EB&firmwareVersion=MX011ANG_1.2.2s9_DEVsd"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testBehaviorWhenAllParametersAreCorrect() throws Exception {
        mockMvc.perform(
                get("/loguploader/getSettings?ecmMacAddress=AE:28:CB:AA:AA:32&estbMacAddress=AC:24:38:AA:19:EB&firmwareVersion=MX011ANG_1.2.2s9_DEVsd"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId)));
    }


    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_TC1");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("30 4 * * *");
        schedule.setTimeWindowMinutes(30);
        schedule.setStartDate("2014-01-01 01:01:01");
        schedule.setEndDate("2016-02-02 02:02:02");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC1");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(2);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setModeToGetLogFiles("AllLogFiles");
        logUploadSettings.setFromDateTime("2014-01-01 01:01:01");
        logUploadSettings.setToDateTime("2016-02-02 02:02:02");
        logUploadSettings.setSchedule(createLogUploadSettingsSchedule());
        List<String> logFileIdList = new ArrayList<>();
        for (LogFile logFile: createLogFileList()) {
            logFileIdList.add(logFile.getId());
        }
        logUploadSettings.setLogFileIds(logFileIdList);
        return logUploadSettings;
    }

    private Schedule createLogUploadSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("20 08 * * *");
        schedule.setTimeWindowMinutes(20);
        schedule.setStartDate("2014-01-01 01:01:01");
        schedule.setEndDate("2016-02-02 02:02:02");
        return schedule;
    }

    private List<LogFile> createLogFileList() {
        LogFile logFile1 = new LogFile();
        logFile1.setId("XQA_LogFile_001");
        logFile1.setName("XQA_LogFile_001");
        logFile1.setDeleteOnUpload(false);
        LogFile logFile2 = new LogFile();
        logFile2.setId("XQA_LogFile_002");
        logFile2.setName("XQA_LogFile_002");
        logFile2.setDeleteOnUpload(false);
        return Lists.newArrayList(logFile1, logFile2);
    }

    private FormulaDataObject createFormulaObject() {
        FormulaDataObject formulaDataObject = new FormulaDataObject();
        formulaDataObject.setId(defaultFormulaId);
        formulaDataObject.setName("formula1To3");
        formulaDataObject.setEcmMacAddress("ecmMacListFormula1To3");
        formulaDataObject.setEstbMacAddress("estbMacListFormula1To3");
        formulaDataObject.setFirmwareVersion("MX011ANG_1.2.2s9_DEVsd");
        formulaDataObject.setPercentage(100);
        formulaDataObject.setPriority(1);
        formulaDataObject.setRuleExpression("ecmMacAddress and estbMacAddress and firmwareVersion");
        return formulaDataObject;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("ecmMacListFormula1To3", GenericNamespacedListTypes.MAC_LIST, "AE:28:CB:AA:AA:32"));
        namespacedLists.add(createGenericNamespacedList("estbMacListFormula1To3", GenericNamespacedListTypes.MAC_LIST, "AC:24:38:AA:19:EB"));
        return namespacedLists;
    }


}
