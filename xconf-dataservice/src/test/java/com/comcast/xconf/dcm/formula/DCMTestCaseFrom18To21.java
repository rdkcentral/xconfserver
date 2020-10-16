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
 *  Created: 12/17/15 5:30 PM
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
 * Testing expression (NOT A and B or C or D)
 */
public class DCMTestCaseFrom18To21 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId = "formulaId18To21";
    private FormulaDataObject formula = createFormula();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();
    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();
    private Model model = createModel();
    private String sufficeControllerId = "0007";
    private String unsatisfyingControllerId = "2675";
    private String sufficeVodId = "16465";
    private String sufficeModel = "GRNG150";
    private String sufficeMacAddress = "0E:24:53:5C:9B:CE";

    @Before()
    public void init() throws Exception {
        initDcmFormula(formula);
        initEnvironmentModel(null, model);
        initDcmSettings(deviceSettings, logUploadSettings, null);
        initLists(namespacedLists, logFiles);
    }

    @Test
    public void ruleNotFound_WhenFirstParamIsUnsatisfying() throws Exception {
        assertRuleNotFound(unsatisfyingControllerId, sufficeVodId, sufficeModel, sufficeMacAddress);
    }

    @Test
    public void ruleFound_WhenANDParamJustPriorToORParamIsMissing_ButOneOfORParamsIsPresent() throws Exception {
        assertRuleFound(sufficeControllerId, "", sufficeModel, "");
        assertRuleFound(sufficeControllerId, "", "", sufficeMacAddress);
    }

    @Test
    public void ruleNotFound_WhenANDParamJustPriorToORParamIsMissing_AndBothORParamsIsMissing() throws Exception {
        assertRuleNotFound(sufficeControllerId, "", "", "");
    }

    @Test
    public void ruleFound_WhenBothORParamsIsMissing() throws Exception {
        assertRuleFound(sufficeControllerId, sufficeVodId, "", "");
    }

    private void assertRuleNotFound(String controllerId, String vodId, String model, String ecmMacAddress) throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?" +
                    "controllerId=" + controllerId +
                    "&vodId=" + vodId +
                    "&model=" + model +
                    "&ecmMacAddress=" + ecmMacAddress))
                .andExpect(status().isNotFound());
    }

    private void assertRuleFound(String controllerId, String vodId, String model, String ecmMacAddress) throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?" +
                    "controllerId=" + controllerId +
                    "&vodId=" + vodId +
                    "&model=" + model +
                    "&ecmMacAddress=" + ecmMacAddress))
                .andExpect(status().isOk());
    }

    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_TC18");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("ActNow");
        schedule.setExpression("15 07 * * *");
        schedule.setTimeWindowMinutes(20);
        schedule.setStartDate("2014-02-01 01:01:03");
        schedule.setEndDate("2016-05-02 03:01:02");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC18");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(3);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-04-10 01:02:02");
        logUploadSettings.setToDateTime("2016-06-15 02:03:01");
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
        schedule.setExpression("15 03 * * *");
        schedule.setTimeWindowMinutes(30);
        schedule.setStartDate("2014-04-10 01:02:02");
        schedule.setEndDate("2016-06-15 02:03:01");
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
        formula.setName("formula18To21");
        formula.setDescription("guneet_formula_TC18");
        formula.setControllerId("2675");
        formula.setVodId("16465");
        formula.setPercentage(100);
        formula.setPriority(6);
        formula.setEcmMacAddress("ecmMacListFormula18To21");
        formula.setModel("GRNG150");
        formula.setRuleExpression("NOT controllerId and vodId or model or ecmMacAddress");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("ecmMacListFormula18To21", GenericNamespacedListTypes.MAC_LIST, "0E:24:53:5C:9B:CE"));
        return namespacedLists;
    }

    private Model createModel() {
        Model model = new Model();
        model.setId("GRNG150");
        return model;
    }
}
