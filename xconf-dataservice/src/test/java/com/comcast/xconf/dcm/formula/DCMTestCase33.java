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
 *  Created: 12/25/15 2:28 PM
 */

package com.comcast.xconf.dcm.formula;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.logupload.*;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * Validate behavior by changing formula priorities and check gotten settings before and after changing priorities
 */
public class DCMTestCase33 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId = "formulaId33A";
    private final String defaultFormulaId1 = "formulaId33B";

    private FormulaDataObject formula = createFormula();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();

    private FormulaDataObject formula1 = createFormula1();
    private DeviceSettings deviceSettings1 = createDeviceSettings1();
    private LogUploadSettings logUploadSettings1 = createLogUploadSettings1();
    private VodSettings vodSettings1 = createVodSettings1();

    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();

    @Test
    public void testBehaviorDeviceAndLogUploadSettingsIsBasedOn33AVodSettingsOn33BAfterChangingPrioritiesAllSettingsAreBasedOn33B() throws Exception {
        initDcmFormula(formula);
        initDcmSettings(deviceSettings, logUploadSettings, null);
        initLists(namespacedLists, logFiles);

        initDcmFormula(formula1);
        initDcmSettings(deviceSettings1, logUploadSettings1, vodSettings1);

        mockMvc.perform(get("/loguploader/getSettings?estbIP=145.11.24.35&ecmMacAddress=84:AE:75:AA:86:77&channelMapId=1698"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId, defaultFormulaId1)));

        initDcmFormula(changePriorities(formula, 22));
        initDcmFormula(changePriorities(formula1, 21));

        mockMvc.perform(get("/loguploader/getSettings?estbIP=145.11.24.35&ecmMacAddress=84:AE:75:AA:86:77&channelMapId=1698"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId, defaultFormulaId1)));
    }


    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_TC33A");
        deviceSettings.setCheckOnReboot(false);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("50 07 * * *");
        schedule.setTimeWindowMinutes(45);
        schedule.setStartDate("2014-05-02 03:01:02");
        schedule.setEndDate("2016-04-01 03:02:03");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC33A");
        logUploadSettings.setUploadOnReboot(false);
        logUploadSettings.setNumberOfDays(4);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-02-01 03:02:01");
        logUploadSettings.setToDateTime("2016-03-02 01:03:02");
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
        schedule.setExpression("51 23 * * *");
        schedule.setTimeWindowMinutes(35);
        schedule.setStartDate("2014-02-01 03:02:01");
        schedule.setEndDate("2016-03-02 01:03:02");
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
        formula.setName("formula33A");
        formula.setDescription("guneet_formula_TC33A");
        formula.setEcmMacAddress("ecmMacListFormula33");
        formula.setChannelMapId("1698");
        formula.setPercentage(100);
        formula.setPriority(21);
        formula.setRuleExpression("ecmMacAddress and channelMapId");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("ecmMacListFormula33", GenericNamespacedListTypes.MAC_LIST, "84:AE:75:AA:86:77"));
        namespacedLists.add(createGenericNamespacedList("ipListFormula33", GenericNamespacedListTypes.IP_LIST, "78.123.56.32,145.11.24.35"));
        return namespacedLists;
    }


    //#################################################################################################################


    private DeviceSettings createDeviceSettings1() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId1);
        deviceSettings.setName("guneet_device_TC33B");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule1());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule1() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("36 06 * * *");
        schedule.setTimeWindowMinutes(100);
        schedule.setStartDate("2014-03-02 03:01:02");
        schedule.setEndDate("2016-04-02 02:01:03");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings1() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId1);
        logUploadSettings.setName("guneet_log_TC33B");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(5);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-04-01 02:01:02");
        logUploadSettings.setToDateTime("2016-04-05 02:01:03");
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
        schedule.setExpression("55 20 * * *");
        schedule.setTimeWindowMinutes(1000);
        schedule.setStartDate("2014-04-01 02:01:02");
        schedule.setEndDate("2016-04-05 02:01:03");
        return schedule;
    }

    private VodSettings createVodSettings1() {
        VodSettings vodSettings = new VodSettings();
        vodSettings.setId(defaultFormulaId1);
        vodSettings.setName("guneet_VOD_TC33B");
        vodSettings.setLocationsURL("http://foo.com");
        vodSettings.setSrmIPList(new HashMap<String, String>());
        return vodSettings;
    }

    private FormulaDataObject createFormula1() {
        FormulaDataObject formula = new FormulaDataObject();
        formula.setId(defaultFormulaId1);
        formula.setName("formula33B");
        formula.setDescription("guneet_formula_TC33B");
        formula.setPercentage(100);
        formula.setPriority(22);
        formula.setEstbIP("ipListFormula33");
        formula.setChannelMapId("1698");
        formula.setRuleExpression("estbIP and channelMapId");
        return formula;
    }

    private FormulaDataObject changePriorities(FormulaDataObject formula, Integer priority) {
        formula.setPriority(priority);
        return formula;
    }
}
