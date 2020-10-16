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
 *  Created: 12/25/15 1:45 PM
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
public class DCMTestCase31 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId = "formulaId31";
    private FormulaDataObject formula = createFormula();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();
    private VodSettings vodSettings = createVodSettings();
    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();

    @Before()
    public void init() throws Exception {
        initDcmFormula(formula);
        initLists(namespacedLists, logFiles);
        initDcmSettings(deviceSettings, logUploadSettings, vodSettings);
    }

    @Test
    public void testFirstTimeLogUploadAndVodSettingsAreReturnedSecondTimeDeviceSettingsAndVodSettings() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?ecmMacAddress=B6:75:84:EA:A9:02"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId)));

        formula = changePercentage(formula);
        initDcmFormula(formula);

        mockMvc.perform(get("/loguploader/getSettings?ecmMacAddress=B6:75:84:EA:A9:02"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId)));
    }

    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_TC31");
        deviceSettings.setCheckOnReboot(false);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("55 23 * * *");
        schedule.setTimeWindowMinutes(45);
        schedule.setStartDate("2014-01-02 01:02:01");
        schedule.setEndDate("2016-03-04 01:03:02");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC31");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(2);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setModeToGetLogFiles("AllLogFiles");
        logUploadSettings.setFromDateTime("2014-02-01 03:02:01");
        logUploadSettings.setToDateTime("2016-03-01 02:01:03");
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
        schedule.setExpression("32 05 * * *");
        schedule.setTimeWindowMinutes(20);
        schedule.setStartDate("2014-02-01 03:02:01");
        schedule.setEndDate("2016-03-01 02:01:03");
        return schedule;
    }

    private VodSettings createVodSettings() {
        VodSettings vodSettings = new VodSettings();
        vodSettings.setId(defaultFormulaId);
        vodSettings.setName("guneet_VOD_TC31");
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
        formula.setName("formula31");
        formula.setDescription("guneet_formula_TC32");
        formula.setPercentage(0);
        formula.setPriority(18);
        formula.setEcmMacAddress("ecmMacListFormula31");
        formula.setRuleExpression("ecmMacAddress");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("ecmMacListFormula31", GenericNamespacedListTypes.MAC_LIST, "B6:75:84:EA:A9:02"));
        return namespacedLists;
    }

    private FormulaDataObject changePercentage(FormulaDataObject formula) {
        formula.setPercentage(100);
        return formula;
    }
}
