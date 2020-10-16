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
 *  Created: 12/16/15 5:48 PM
 */

package com.comcast.xconf.dcm.formula;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.estbfirmware.Model;
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
 * Testing expression (A and not B or C and D)
 */
public class DCMTestCaseFrom11To14 extends BaseDcmFormulaTestCase {

    private String defaultFormulaId = "formulaId11To14";
    private FormulaDataObject formula = createFormula();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();
    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();
    private Model model = createModel();
    private String sufficeModel = "GXI3";
    private String sufficeChannelMapId = "2345";
    private String sufficeFimwareVersion = "someAnotherVersion";
    private String unsatisfyingFirmwareVersion = "GXG1AN_1.3.1p2s1_DEVsd-signed";
    private String sufficeMacAddress = "10:AE:21:31:CB:AA";

    @Before()
    public void init() throws Exception {
        initEnvironmentModel(null, model);
        initDcmSettings(deviceSettings, logUploadSettings, null);
        initLists(namespacedLists, logFiles);
        initDcmFormula(formula);
    }

    @Test
    public void ruleNotFound_WhenFirstParamIsMissingOrWhenLastParamIsMissing() throws Exception {
        assertRuleNotFound("", sufficeFimwareVersion, sufficeMacAddress, sufficeChannelMapId);
        assertRuleNotFound(sufficeModel, sufficeFimwareVersion, sufficeMacAddress, "");
    }

    @Test
    public void ruleFound_WhenANDParamJustPriorToORParamIsMissing_ButORParamIsPresent() throws Exception {
        assertRuleFound(sufficeModel, unsatisfyingFirmwareVersion, sufficeMacAddress, sufficeChannelMapId);
    }

    @Test
    public void ruleNotFound_WhenANDParamJustPriorToORParamIsMissing_AndORParamIsMissing() throws Exception {
        assertRuleNotFound(sufficeModel, unsatisfyingFirmwareVersion, "", sufficeChannelMapId);
    }

    @Test
    public void ruleFound_WhenORParamIsMissing() throws Exception {
        assertRuleFound(sufficeModel, sufficeFimwareVersion, "", sufficeChannelMapId);
    }

    private void assertRuleNotFound(String model, String firmwareVersion, String macAddress, String channelMapId) throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?" +
                "model=" + model +
                "&channelMapId=" + channelMapId +
                "&firmwareVersion=" + firmwareVersion +
                "&estbMacAddress=" + macAddress))
                .andExpect(status().isNotFound());
    }

    private void assertRuleFound(String model, String firmwareVersion, String macAddress, String channelMapId) throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?" +
                    "model=" + model +
                    "&channelMapId=" + channelMapId +
                    "&firmwareVersion=" + firmwareVersion +
                    "&estbMacAddress=" + macAddress))
                .andExpect(status().isOk());
    }

    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_TC11");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("05 03 * * *");
        schedule.setTimeWindowMinutes(30);
        schedule.setStartDate("2014-02-01 02:03:04");
        schedule.setEndDate("2016-03-01 02:05:01");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC11");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(1);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-01-02 06:01:02");
        logUploadSettings.setToDateTime("2016-03-04 05:03:01");
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
        schedule.setExpression("108 10 * * *");
        schedule.setTimeWindowMinutes(15);
        schedule.setStartDate("2014-01-02 06:01:02");
        schedule.setEndDate("2016-03-04 05:03:01");
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
        formula.setName("formula11To14");
        formula.setDescription("guneet_formula_TC11");
        formula.setModel("GXI3");
        formula.setFirmwareVersion("GXG1AN_1.3.1p2s1_DEVsd-signed");
        formula.setPriority(4);
        formula.setPercentage(100);
        formula.setChannelMapId("2345");
        formula.setEstbMacAddress("estbMacListFormula11To14");
        formula.setRuleExpression("model AND NOT firmwareVersion OR estbMacAddress AND channelMapId");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("estbMacListFormula11To14", GenericNamespacedListTypes.MAC_LIST, "10:AE:21:31:CB:AA"));
        return namespacedLists;
    }

    private Model createModel() {
        Model model = new Model();
        model.setId("GXI3");
        return model;
    }
}
