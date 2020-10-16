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
 *  Created: 12/24/15 6:02 PM
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
* When a query matches more than one formula, confirm that it retrieves the device settings
* and log upload settings from the high priority formula(XCONF-234)
*/
public class DCMTestCase22 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId1 = "formulaId22A";
    private final String defaultFormulaId2 = "formulaId22B";
    private FormulaDataObject formula = createFormula();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();

    private FormulaDataObject formula1 = createFormula1();
    private DeviceSettings deviceSettings1 = createDeviceSettings1();
    private LogUploadSettings logUploadSettings1 = createLogUploadSettings1();

    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();

    @Before()
    public void init() throws Exception {

        initDcmFormula(formula);
        initDcmSettings(deviceSettings, logUploadSettings, null);
        initLists(namespacedLists, logFiles);

        initDcmFormula(formula1);
        initDcmSettings(deviceSettings1, logUploadSettings1, null);
    }

    @Test
    public void testGetDeviceSettingsAndLogUploadSettingsFromHighPriorityFormula() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?estbMacAddress=0E:45:A7:AA:64:CB&vodId=16078&ecmMacAddress=5A:08:13:BC:85:E9"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId1, defaultFormulaId2)));
    }


    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId1);
        deviceSettings.setName("guneet_device_TC22A");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("45 01 * * *");
        schedule.setTimeWindowMinutes(5);
        schedule.setStartDate("2014-02-10 03:01:02");
        schedule.setEndDate("2016-07-15 01:03:04");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId1);
        logUploadSettings.setName("guneet_log_TC22A");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(4);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-03-02 02:04:05");
        logUploadSettings.setToDateTime("2016-04-01 05:09:03");
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
        schedule.setType("CronExpression");
        schedule.setExpression("17 03 * * *");
        schedule.setTimeWindowMinutes(3);
        schedule.setStartDate("2014-03-02 02:04:05");
        schedule.setEndDate("2016-04-01 05:09:03");
        return schedule;
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
        formula.setId(defaultFormulaId1);
        formula.setName("formula22A");
        formula.setDescription("guneet_formula_TC22A");
        formula.setEstbMacAddress("estbMacListFormula22");
        formula.setVodId("16078");
        formula.setPercentage(100);
        formula.setPriority(7);
        formula.setRuleExpression("estbMacAddress and vodId");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("estbMacListFormula22", GenericNamespacedListTypes.MAC_LIST, "0E:45:A7:AA:64:CB"));
        namespacedLists.add(createGenericNamespacedList("ecmMacListFormula22", GenericNamespacedListTypes.MAC_LIST, "5A:08:13:BC:85:E9"));
        return namespacedLists;
    }


    //#################################################################################################################


    private DeviceSettings createDeviceSettings1() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId2);
        deviceSettings.setName("guneet_device_TC22B");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule1());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule1() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("52 01 * * *");
        schedule.setTimeWindowMinutes(10);
        schedule.setStartDate("2014-03-02 01:02:03");
        schedule.setEndDate("2016-05-03 01:04:02");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings1() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId2);
        logUploadSettings.setName("guneet_log_TC22B");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(4);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-03-02 02:04:05");
        logUploadSettings.setToDateTime("2016-04-01 05:09:03");
        logUploadSettings.setModeToGetLogFiles("LogFiles");
        logUploadSettings.setSchedule(createLogUploadSettingsSchedule1());
        List<String> logFileNameList = new ArrayList<>();
        for (LogFile logFile: createLogFileList()) {
            logFileNameList.add(logFile.getId());
        }
        logUploadSettings.setLogFileIds(logFileNameList);
        return logUploadSettings;
    }

    private Schedule createLogUploadSettingsSchedule1() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("22 03 * * *");
        schedule.setTimeWindowMinutes(15);
        schedule.setStartDate("2014-03-04 01:03:04");
        schedule.setEndDate("2016-02-01 03:04:01");
        return schedule;
    }

    private FormulaDataObject createFormula1() {
        FormulaDataObject formula = new FormulaDataObject();
        formula.setId(defaultFormulaId2);
        formula.setName("formula22B");
        formula.setDescription("guneet_formula_TC22B");
        formula.setEstbMacAddress("estbMacListFormula22");
        formula.setEcmMacAddress("ecmMacListFormula22");
        formula.setPercentage(100);
        formula.setPriority(8);
        formula.setRuleExpression("estbMacAddress AND ecmMacAddress");
        return formula;
    }
}
