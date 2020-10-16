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
 *  Created: 8:07 PM
 */
package com.comcast.xconf.dcm.formula;

import com.comcast.xconf.Environment;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.SortingManager;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.logupload.*;
import com.comcast.xconf.queries.QueryConstants;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BaseDcmFormulaTestCase extends BaseQueriesControllerTest {

    private SettingsUtil settingsUtil = new SettingsUtil();

    @Override
    public void cleanData() {
    }

    protected void initLists(List<GenericNamespacedList> namespacedLists, List<LogFile> logFiles) throws Exception {
        if (namespacedLists != null) {
            String url = "";
            for (GenericNamespacedList namespacedList : namespacedLists) {
                if (genericNamespacedListDAO.getOne(namespacedList.getId()) != null) {
                    continue;
                }
                if (GenericNamespacedListTypes.MAC_LIST.equals(namespacedList.getTypeName())) {
                    url = QueryConstants.UPDATE_NS_LISTS_V2;
                } else if (GenericNamespacedListTypes.IP_LIST.equals(namespacedList.getTypeName())) {
                    url = QueryConstants.UPDATE_IP_ADDRESS_GROUPS_V2;
                }
                performPostRequestAndVerify(url, 201, namespacedList);
            }
        }
        if (logFiles != null) {
            for (LogFile logFile: logFiles) {
                if (logFileDAO.getOne(logFile.getId()) != null) {
                    continue;
                }
                performPostRequestAndVerify(QueryConstants.UPDATES_LOG_FILE, 201, logFile);
            }
        }
    }

    protected void initDcmSettings(DeviceSettings deviceSettings, LogUploadSettings logUploadSettings, VodSettings vodSettings) throws Exception {
        if (deviceSettings != null && deviceSettingsDAO.getOne(deviceSettings.getId()) == null) {
            performPostRequestAndVerify(QueryConstants.UPDATES_DEVICE_SETTINGS + "/UTC", 201, deviceSettings);
        }
        if (logUploadSettings != null && logUploadSettingsDAO.getOne(logUploadSettings.getId()) == null) {
            performPostRequestAndVerify(QueryConstants.UPDATES_LOG_UPLOAD_SETTINGS + "/UTC/UTC", 201, logUploadSettings);
        }
        if (vodSettings != null && vodSettingsDAO.getOne(vodSettings.getId()) == null) {
            performPostRequestAndVerify(QueryConstants.UPDATES_VOD_SETTINGS, 201, vodSettings);
        }
    }

    protected void initDcmFormula(FormulaDataObject formula) throws Exception {
        if (formula != null && dcmRuleDAO.getOne(formula.getId()) == null) {
            performPostRequestAndVerify(QueryConstants.UPADATES_FORMULA + "/" + formula.getPriority(), 201, formula);
        }
    }

    protected void initEnvironmentModel(Environment environment, Model model) throws Exception {
        if (environment != null && environmentDAO.getOne(environment.getId()) == null) {
            performPostRequestAndVerify(QueryConstants.UPDATE_ENVIRONMENTS, 201, environment);
        }
        if (model != null && modelDAO.getOne(model.getId()) == null) {
            performPostRequestAndVerify(QueryConstants.UPDATE_MODELS, 201, model);
        }
    }

    protected ResultMatcher equalResponse(final List<String> formulaIds) {
        return new ResultMatcher() {
            @Override
            public void match(MvcResult mvcResult) throws Exception {
                Settings expectedSettings = new Settings();
                List<DCMGenericRule> rules = new ArrayList<>();

                for (String id : formulaIds) {
                    rules.add(dcmRuleDAO.getOne(id));
                }

                rules = SortingManager.sortRulesByPriorityAsc((Iterable) rules);

                for (DCMGenericRule rule : rules) {
                    settingsUtil.copySettings(expectedSettings, settingsDAO.get(rule.getId()), rule, "", "US/Eastern");
                }

                Settings actualSettings = mapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Settings>() {});
                actualSettings.setUploadImmediately(null);
                actualSettings.setScheduleCron(null);
                actualSettings.setLusScheduleCron(null);
                expectedSettings.setScheduleCron(null);
                expectedSettings.setLusScheduleCron(null);
                expectedSettings.setSchedulerType(null);
                expectedSettings.setScheduleStartDate(null);
                expectedSettings.setScheduleEndDate(null);
                expectedSettings.setLusScheduleStartDate(null);
                expectedSettings.setLusScheduleEndDate(null);
                expectedSettings.setLusLogFiles(null);
                expectedSettings.setLusLogFilesStartDate(null);
                expectedSettings.setLusLogFilesEndDate(null);
                expectedSettings.setRuleIDs(new HashSet<String>());
                assertEquals(expectedSettings, actualSettings);
            }
        };

    }

    protected DeviceSettings createDeviceSettings(String id, String name, Schedule schedule) {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(id);
        deviceSettings.setName(name);
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(schedule);
        return deviceSettings;
    }

}
