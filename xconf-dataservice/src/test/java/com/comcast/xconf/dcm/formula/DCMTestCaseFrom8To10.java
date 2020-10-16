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
 *  Created: 12/16/15 4:37 PM
 */

package com.comcast.xconf.dcm.formula;

import com.comcast.xconf.Environment;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.estbfirmware.Model;
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

/**
 * Testing expression (A and B and C or D)
 */
public class DCMTestCaseFrom8To10 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId = "formulaId8To10";
    private FormulaDataObject formula = createFormula();
    private DeviceSettings deviceSettings = createDeviceSettings();
    private LogUploadSettings logUploadSettings = createLogUploadSettings();
    private VodSettings vodSettings = createVodSettings();
    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();
    private Environment environment = createEnv();
    private Model model = createModel();
    private String sufficeModel = "GXG1";
    private String sufficeEnv = "QA";
    private String sufficeFimwareVersion = "GXG1AN_1.3.1p2s1_DEVsd-signed";
    private String sufficeMacAddress = "AA:0A:A7:79:9E:E4";


    @Before()
    public void init() throws Exception {
        initDcmFormula(formula);
        initLists(namespacedLists, logFiles);
        initDcmSettings(deviceSettings, logUploadSettings, vodSettings);
        initEnvironmentModel(environment, model);
    }

    @Test
    public void ruleNotFound_WhenFirstOrSecondANDParamIsMissing() throws Exception {
        assertRuleNotFound(sufficeEnv, "", sufficeFimwareVersion, sufficeMacAddress);
        assertRuleNotFound("", sufficeModel, sufficeFimwareVersion, sufficeMacAddress);
    }

    @Test
    public void ruleFound_WhenANDParamJustPriorToORParamIsMissing_ButORParamIsPresent() throws Exception {
        assertRuleFound(sufficeEnv, sufficeModel, "", sufficeMacAddress);
    }

    @Test
    public void ruleNotFound_WhenANDParamJustPriorToORParamIsMissing_AndORParamIsMissing() throws Exception {
        assertRuleNotFound(sufficeEnv, sufficeModel, "", "");
    }

    @Test
    public void ruleFound_WhenORParamIsMissing() throws Exception {
        assertRuleFound(sufficeEnv, sufficeModel, sufficeFimwareVersion, "");
    }

    private void assertRuleNotFound(String env, String model, String firmwareVersion, String macAddress) throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?" +
                    "model=" + model +
                    "&env=" + env +
                    "&firmwareVersion=" + firmwareVersion +
                    "&ecmMacAddress=" + macAddress))
                .andExpect(status().isNotFound());
    }

    private void assertRuleFound(String env, String model, String firmwareVersion, String macAddress) throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?" +
                "model=" + model +
                "&env=" + env +
                "&firmwareVersion=" + firmwareVersion +
                "&ecmMacAddress=" + macAddress))
                .andExpect(status().isOk());
    }

    private DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(defaultFormulaId);
        deviceSettings.setName("guneet_device_TC8");
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createDeviceSettingsSchedule());
        return deviceSettings;
    }

    private Schedule createDeviceSettingsSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("ActNow");
        schedule.setExpression("10 11 * * *");
        schedule.setTimeWindowMinutes(10);
        schedule.setStartDate("2014-02-02 02:01:01");
        schedule.setEndDate("2016-03-01 02:01:03");
        return schedule;
    }

    private LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(defaultFormulaId);
        logUploadSettings.setName("guneet_log_TC8");
        logUploadSettings.setUploadOnReboot(true);
        logUploadSettings.setNumberOfDays(2);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setFromDateTime("2014-03-01 02:03:04");
        logUploadSettings.setToDateTime("2016-01-02 03:04:04");
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
        schedule.setExpression("15 01 * * *");
        schedule.setTimeWindowMinutes(15);
        schedule.setStartDate("2014-03-01 02:03:04");
        schedule.setEndDate("2016-01-02 03:04:04");
        return schedule;
    }

    private VodSettings createVodSettings() {
        VodSettings vodSettings = new VodSettings();
        vodSettings.setId(defaultFormulaId);
        vodSettings.setName("guneet_VOD_TC8");
        vodSettings.setLocationsURL("http://foo.com");
        Map<String, String> srmIp = new HashMap<>();
        srmIp.put("guneet", "10.11.13.15");
        vodSettings.setIpNames(Lists.newArrayList("guneet"));
        vodSettings.setIpList(Lists.newArrayList("10.11.13.15"));
        vodSettings.setSrmIPList(srmIp);
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
        formula.setName("formula8To10");
        formula.setDescription("guneet_formula_TC4");
        formula.setEnvList(Lists.newArrayList("QA"));
        formula.setModel("GXG1");
        formula.setFirmwareVersion("GXG1AN_1.3.1p2s1_DEVsd-signed");
        formula.setPriority(3);
        formula.setPercentage(100);
        formula.setEcmMacAddress("ecmMacListFormula8To10");
        formula.setRuleExpression("env AND model AND firmwareVersion or ecmMacAddress");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("ecmMacListFormula8To10", GenericNamespacedListTypes.MAC_LIST, "AA:0A:A7:79:9E:E4"));
        return namespacedLists;
    }

    private Model createModel() {
        Model model = new Model();
        model.setId("GXG1");
        return model;
    }

    private Environment createEnv() {
        Environment environment = new Environment();
        environment.setId("QA");
        return environment;
    }

}
