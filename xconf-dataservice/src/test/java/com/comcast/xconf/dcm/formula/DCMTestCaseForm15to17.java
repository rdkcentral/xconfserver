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
 *  Created: 12/17/15 4:23 PM
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
 * Testing expression (A or B and C and not D)
 */
public class DCMTestCaseForm15to17 extends BaseDcmFormulaTestCase{

    private final String defaultFormulaId = "formulaId15To17";
    private FormulaDataObject formula = createFormula();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();
    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();
    private String sufficeVodId = "16503";
    private String sufficeControllerId = "2692";
    private String sufficeEstbIp = "23.46.69.92";
    private String unsatisfyingMacAddress = "10:AE:21:31:CB:AA";
    private String sufficeMacAddress = "AA:AA:AA:AA:AA:AA";


    @Before()
    public void init() throws Exception {
        initLists(namespacedLists, logFiles);
        initDcmFormula(formula);
        initDcmSettings(deviceSettings, logUploadSettings, null);
    }

    @Test
    public void ruleNotFound_WhenThirdOrForthParamIsMissingOrUnsatisfying() throws Exception {
        assertRuleNotFound(sufficeVodId, sufficeControllerId, "", sufficeMacAddress);
        assertRuleNotFound(sufficeVodId, sufficeControllerId, sufficeEstbIp, unsatisfyingMacAddress);
    }

    @Test
    public void ruleFound_WhenANDParamJustPriorToORParamIsMissing_ButORParamIsPresent() throws Exception {
        assertRuleFound("", sufficeControllerId, sufficeEstbIp, sufficeMacAddress);
    }

    @Test
    public void ruleNotFound_WhenANDParamJustPriorToORParamIsMissing_AndORParamIsMissing() throws Exception {
        assertRuleNotFound("", "", sufficeEstbIp, sufficeMacAddress);
    }

    @Test
    public void ruleFound_WhenORParamIsMissing() throws Exception {
        assertRuleFound(sufficeVodId, "", sufficeEstbIp, sufficeMacAddress);
    }

    private void assertRuleNotFound(String vodId, String controllerId, String estbIP, String estbMacAddress) throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?" +
                    "vodId=" + vodId +
                    "&controllerId=" + controllerId +
                    "&estbIP=" + estbIP +
                    "&estbMacAddress=" + estbMacAddress))
                .andExpect(status().isNotFound());
    }

    private void assertRuleFound(String vodId, String controllerId, String estbIP, String estbMacAddress) throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?" +
                    "vodId=" + vodId +
                    "&controllerId=" + controllerId +
                    "&estbIP=" + estbIP +
                    "&estbMacAddress=" + estbMacAddress))
                .andExpect(status().isOk());
    }

    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_TC15");
        deviceSettings.setCheckOnReboot(false);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("CronExpression");
        schedule.setExpression("30 05 * * *");
        schedule.setTimeWindowMinutes(20);
        schedule.setStartDate("2014-03-01 02:01:04");
        schedule.setEndDate("2016-02-01 03:04:02");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC15");
        logUploadSettings.setUploadOnReboot(false);
        logUploadSettings.setNumberOfDays(5);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setModeToGetLogFiles("AllLogFiles");
        logUploadSettings.setFromDateTime("2014-02-01 02:01:03");
        logUploadSettings.setToDateTime("2016-03-02 01:02:03");
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
        schedule.setExpression("40 09 * * *");
        schedule.setTimeWindowMinutes(30);
        schedule.setStartDate("2014-02-01 02:01:03");
        schedule.setEndDate("2016-03-02 01:02:03");
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
        formula.setName("formula15To17");
        formula.setDescription("guneet_formula_TC15");
        formula.setPriority(5);
        formula.setPercentage(100);
        formula.setControllerId("2692");
        formula.setVodId("16503");
        formula.setEstbIP("ipListFormula15To17");
        formula.setEstbMacAddress("estbMacListFormula11To14");
        formula.setRuleExpression("vodId or controllerId and estbIP and NOT estbMacAddress");
        return formula;
    }

    /*MacAddress is added in DcmTestCaseFrom11To14*/
    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("ipListFormula15To17", GenericNamespacedListTypes.IP_LIST, "65.12.57.36,23.46.69.92"));
        return namespacedLists;
    }
}
