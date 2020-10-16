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
 *  Created: 12/24/15 7:26 PM
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
public class DCMTestCase24 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId = "formulaId24";
    private FormulaDataObject formula = createFormula();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();
    private VodSettings vodSettings = createVodSettings();
    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();

    @Before()
    public void init() throws Exception {
        initLists(namespacedLists, logFiles);
        initDcmFormula(formula);
        initDcmSettings(deviceSettings, logUploadSettings, vodSettings);
    }

    @Test
    public void testGetDeviceAndLogUploadSettingsFromHighPriorityFormulaAndIfVodSettingsIsMissingGetItFromLowPriorityFormula() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?estbIP=25.50.75.100&ecmMacAddress=29:A4:30:C3:BE:BA"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId)));
    }

    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_TC24");
        deviceSettings.setCheckOnReboot(false);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("18 03 * * *");
        schedule.setTimeWindowMinutes(3);
        schedule.setStartDate("2014-05-02 01:03:04");
        schedule.setEndDate("2016-02-01 04:05:03");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC24");
        logUploadSettings.setUploadOnReboot(false);
        logUploadSettings.setNumberOfDays(5);
        logUploadSettings.setAreSettingsActive(false);
        logUploadSettings.setModeToGetLogFiles("AllLogFiles");
        logUploadSettings.setFromDateTime("2014-02-04 05:02:03");
        logUploadSettings.setToDateTime("2016-03-01 04:01:03");
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
        schedule.setExpression("08 11 * * *");
        schedule.setTimeWindowMinutes(5);
        schedule.setStartDate("2014-02-04 05:02:03");
        schedule.setEndDate("2016-03-01 04:01:03");
        return schedule;
    }

    private VodSettings createVodSettings() {
        VodSettings vodSettings = new VodSettings();
        vodSettings.setId(defaultFormulaId);
        vodSettings.setName("guneet_VOD_TC24");
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
        formula.setName("formula24");
        formula.setDescription("guneet_formula_TC24");
        formula.setPercentage(100);
        formula.setPriority(11);
        formula.setEcmMacAddress("ecmMacListFormula24");
        formula.setEstbIP("ipListFormula24");
        formula.setRuleExpression("estbIP and ecmMacAddress");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("ecmMacListFormula24", GenericNamespacedListTypes.MAC_LIST, "29:A4:30:C3:BE:BA"));
        namespacedLists.add(createGenericNamespacedList("ipListFormula24", GenericNamespacedListTypes.IP_LIST, "25.50.75.100"));
        return namespacedLists;
    }
}
