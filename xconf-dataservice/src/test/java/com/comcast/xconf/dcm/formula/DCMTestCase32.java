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
 *  Created: 12/25/15 2:02 PM
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
public class DCMTestCase32 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId = "formulaId32A";
    private final String defaultFormulaId1 = "formulaId32B";

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
    public void testDeviceSettingsIsBasedOn32AVodSettinsIsBasedOn32BLogUploadSettingsIsNotPresent() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?estbMacAddress=41:9A:AA:CE:4C:28"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId1, defaultFormulaId)));
    }


    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_TC32A");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("46 08 * * *");
        schedule.setTimeWindowMinutes(30);
        schedule.setStartDate("2014-02-03 05:01:02");
        schedule.setEndDate("2016-02-01 03:01:02");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC32A");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(1);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-03-04 01:04:05");
        logUploadSettings.setToDateTime("2016-01-07 02:03:05");
        logUploadSettings.setModeToGetLogFiles("LogFiles");
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
        schedule.setExpression("15 11 * * *");
        schedule.setTimeWindowMinutes(35);
        schedule.setStartDate("2014-01-02 01:02:01");
        schedule.setEndDate("2016-01-02 03:01:01");
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
        formula.setId(defaultFormulaId);
        formula.setName("formula32A");
        formula.setDescription("guneet_formula_TC32A");
        formula.setPercentage(0);
        formula.setPriority(19);
        formula.setEstbMacAddress("estbMacListFormula32");
        formula.setRuleExpression("estbMacAddress");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("estbMacListFormula32", GenericNamespacedListTypes.MAC_LIST, "41:9A:AA:CE:4C:28"));
        return namespacedLists;
    }


    //#################################################################################################################


    private DeviceSettings createDeviceSettings1() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId1);
        deviceSettings.setName("guneet_device_TC32B");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule1());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule1() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("18 09 * * *");
        schedule.setTimeWindowMinutes(20);
        schedule.setStartDate("2014-03-01 02:03:01");
        schedule.setEndDate("2016-01-02 01:03:01");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings1() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId1);
        logUploadSettings.setName("guneet_log_TC32B");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(4);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-03-01 02:01:03");
        logUploadSettings.setToDateTime("2016-01-02 01:02:01");
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
        schedule.setExpression("52 11 * * *");
        schedule.setTimeWindowMinutes(25);
        schedule.setStartDate("2014-03-01 02:01:03");
        schedule.setEndDate("2016-01-02 01:02:01");
        return schedule;
    }

    private FormulaDataObject createFormula1() {
        FormulaDataObject formula = new FormulaDataObject();
        formula.setId(defaultFormulaId1);
        formula.setName("formula32B");
        formula.setDescription("guneet_formula_TC32B");
        formula.setPercentage(100);
        formula.setPriority(20);
        formula.setEstbMacAddress("estbMacListFormula32");
        formula.setRuleExpression("estbMacAddress");
        return formula;
    }
}
