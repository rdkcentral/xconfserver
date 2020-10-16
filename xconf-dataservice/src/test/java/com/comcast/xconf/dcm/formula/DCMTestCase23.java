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
 *  Created: 12/24/15 6:22 PM
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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
public class DCMTestCase23 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId = "formulaId23A";
    private final String defaultFormulaId1 = "formulaId23B";
    private FormulaDataObject formula = createFormula();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();

    private FormulaDataObject formula1 = createFormula1();
    private DeviceSettings deviceSettings1 = createDeviceSettings1();
    private LogUploadSettings logUploadSettings1 = createLogUploadSettings1();
    private VodSettings vodSettings1 = createVodSettings1();

    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();

    @Before()
    public void init() throws Exception {

        initDcmFormula(formula);
        initDcmSettings(deviceSettings, logUploadSettings, null);
        initLists(namespacedLists, logFiles);

        initDcmFormula(formula1);
        initDcmSettings(deviceSettings1, logUploadSettings1, vodSettings1);
    }

    @Test
    public void testGetDeviceAndLogUploadSettingsFromHighPriorityFormulaAndIfVodSettingsIsMissingGetItFromLowPriorityFormula() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?controllerId=2651&ecmMacAddress=9E:02:48:B6:61:CA"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId1, defaultFormulaId)));
    }


    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_23A");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("20 04 * * *");
        schedule.setTimeWindowMinutes(2);
        schedule.setStartDate("2014-01-03 05:02:04");
        schedule.setEndDate("2016-04-02 04:01:05");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC23A");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(2);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-03-04 01:04:05");
        logUploadSettings.setToDateTime("2016-01-07 02:03:05");
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
        schedule.setExpression("06 04 * * *");
        schedule.setTimeWindowMinutes(2);
        schedule.setStartDate("2014-03-04 01:04:05");
        schedule.setEndDate("2016-01-07 02:03:05");
        return schedule;
    }

    private List<LogFile> createLogFileList() {
        LogFile logFile1 = new LogFile();
        logFile1.setId("XQA_LogFile_001");
        logFile1.setName("XQA_LogFile_001");
        logFile1.setDeleteOnUpload(true);
        return Lists.newArrayList(logFile1);
    }

    private FormulaDataObject createFormula() {
        FormulaDataObject formula = new FormulaDataObject();
        formula.setId(defaultFormulaId);
        formula.setName("formula23A");
        formula.setDescription("guneet_formula_TC23A");
        formula.setEcmMacAddress("ecmMacListFormula23");
        formula.setControllerId("2651");
        formula.setPercentage(100);
        formula.setPriority(9);
        formula.setRuleExpression("ecmMacAddress or controllerId");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("ecmMacListFormula23", GenericNamespacedListTypes.MAC_LIST, "9E:02:48:B6:61:CA"));
        return namespacedLists;
    }


    //#################################################################################################################


    private DeviceSettings createDeviceSettings1() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId1);
        deviceSettings.setName("formula23B");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule1());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule1() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("24 04 * * *");
        schedule.setTimeWindowMinutes(15);
        schedule.setStartDate("2014-02-01 04:02:03");
        schedule.setEndDate("2016-01-03 04:05:03");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings1() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId1);
        logUploadSettings.setName("guneet_log_TC23B");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(4);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-02-03 05:01:04");
        logUploadSettings.setToDateTime("2016-04-02 01:03:04");
        logUploadSettings.setModeToGetLogFiles("AllLogFiles");
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
        schedule.setExpression("09 04 * * *");
        schedule.setTimeWindowMinutes(15);
        schedule.setStartDate("2014-02-03 05:01:04");
        schedule.setEndDate("2016-04-02 01:03:04");
        return schedule;
    }

    private VodSettings createVodSettings1() {
        VodSettings vodSettings = new VodSettings();
        vodSettings.setId(defaultFormulaId1);
        vodSettings.setName("guneet_VOD_TC23B");
        vodSettings.setLocationsURL("http://foo.com");
        Map<String, String> srmIp = new HashMap<>();
        vodSettings.setSrmIPList(srmIp);
        return vodSettings;
    }

    private FormulaDataObject createFormula1() {
        FormulaDataObject formula = new FormulaDataObject();
        formula.setId(defaultFormulaId1);
        formula.setName("formula23B");
        formula.setDescription("guneet_formula_TC23B");
        formula.setChannelMapId("1798");
        formula.setEcmMacAddress("ecmMacListFormula23");
        formula.setPercentage(100);
        formula.setPriority(10);
        formula.setRuleExpression("ecmMacAddress or channelMapId");
        return formula;
    }
}
