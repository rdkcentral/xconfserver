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
 * Author: Igor Kostrov
 * Created: 10/30/2017
*/
package com.comcast.xconf.dcm.formula;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.firmware.ApplicationType;
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

public class DcmMultipleApplicationTypesTest extends BaseDcmFormulaTestCase {

    private final String estbMacAddress = "11:11:11:22:22:22";
    private final String ecmMacAddress = "33:33:33:44:44:44";

    private final String stbFormulaId = "formula_stb";
    private final String xhomeFormulaId = "formula_xhome";

    private FormulaDataObject stbFormula = createStbFormula();
    private DeviceSettings stbDeviceSettings = createStbDeviceSettings();
    private LogUploadSettings logUploadSettings = createStbLogUploadSettings();

    private FormulaDataObject xhomeFormula = createXhomeFormula();
    private DeviceSettings xhomeDeviceSettings = createXHomeDeviceSettings();
    private LogUploadSettings xhomeLogUploadSettings = createXhomeLogUploadSettings();

    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();

    @Before
    public void init() throws Exception {
        deleteAllEntities();

        initDcmFormula(stbFormula);
        initDcmSettings(stbDeviceSettings, logUploadSettings, null);
        initLists(namespacedLists, logFiles);

        initDcmFormula(xhomeFormula);
        initDcmSettings(xhomeDeviceSettings, xhomeLogUploadSettings, null);
    }

    @Test
    public void testFormulaAppliedForAppropriateApplicationType() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?" + getRequestPath()))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(stbFormulaId)));

        mockMvc.perform(get("/loguploader/getSettings/" + ApplicationType.XHOME + "?" + getRequestPath()))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(xhomeFormulaId)));
    }

    private String getRequestPath() {
        return "estbMacAddress=" + estbMacAddress + "&ecmMacAddress=" + ecmMacAddress;
    }

    private DeviceSettings createStbDeviceSettings() {
        DeviceSettings deviceSettings = createDeviceSettings(stbFormulaId, "deviceSettings_STB", createDeviceSettingsSchedule());
        deviceSettings.setApplicationType(ApplicationType.STB);
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("45 01 * * *");
        schedule.setTimeWindowMinutes(5);
        schedule.setStartDate("2014-02-10 03:01:02");
        schedule.setEndDate("2018-07-15 01:03:04");
        return schedule;
    }

    private LogUploadSettings createStbLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(stbFormulaId);
        logUploadSettings.setName("guneet_log_TC22A");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(4);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-03-02 02:04:05");
        logUploadSettings.setToDateTime("2018-04-01 05:09:03");
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
        schedule.setEndDate("2018-04-01 05:09:03");
        return schedule;
    }

    private List<LogFile> createLogFileList() {
        LogFile logFile1 = new LogFile();
        logFile1.setId("logFileId");
        logFile1.setName("LogFileName");
        logFile1.setDeleteOnUpload(false);
        return Lists.newArrayList(logFile1);
    }

    private FormulaDataObject createStbFormula() {
        return createFormula(stbFormulaId, "StbFormulaName", ApplicationType.STB);
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("estbMacList", GenericNamespacedListTypes.MAC_LIST, estbMacAddress));
        namespacedLists.add(createGenericNamespacedList("ecmMacList", GenericNamespacedListTypes.MAC_LIST, ecmMacAddress));
        return namespacedLists;
    }

    private DeviceSettings createXHomeDeviceSettings() {
        DeviceSettings deviceSettings = createDeviceSettings(xhomeFormulaId, "deviceSetting_Xhome", createDeviceSettingsSchedule1());
        deviceSettings.setApplicationType(ApplicationType.XHOME);
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule1() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("52 01 * * *");
        schedule.setTimeWindowMinutes(10);
        schedule.setStartDate("2014-03-02 01:02:03");
        schedule.setEndDate("2018-05-03 01:04:02");
        return schedule;
    }

    private LogUploadSettings createXhomeLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(xhomeFormulaId);
        logUploadSettings.setName("xhome_log_TC22B");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(4);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-03-02 02:04:05");
        logUploadSettings.setToDateTime("2018-04-01 05:09:03");
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
        schedule.setEndDate("2018-02-01 03:04:01");
        return schedule;
    }

    private FormulaDataObject createXhomeFormula() {
        return createFormula(xhomeFormulaId, "XhomeFormula", ApplicationType.XHOME);
    }

    private FormulaDataObject createFormula(String id, String name, String applicationType) {
        FormulaDataObject formula = new FormulaDataObject();
        formula.setId(id);
        formula.setName(name);
        formula.setDescription(name);
        formula.setApplicationType(applicationType);
        formula.setEstbMacAddress("estbMacList");
        formula.setEcmMacAddress("ecmMacList");
        formula.setPercentage(100);
        formula.setPriority(1);
        formula.setRuleExpression("estbMacAddress AND ecmMacAddress");
        return formula;
    }
}
