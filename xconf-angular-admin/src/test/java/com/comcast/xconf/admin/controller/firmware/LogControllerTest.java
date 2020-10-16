/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 * Created: 11/28/2019
 */
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.ConfigChangeLog;
import com.comcast.xconf.estbfirmware.ConfigChangeLogService;
import com.comcast.xconf.estbfirmware.FirmwareConfigFacade;
import com.comcast.xconf.estbfirmware.LastConfigLog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class LogControllerTest extends BaseControllerTest {

    @Autowired
    private ConfigChangeLogService logService;

    @Autowired
    private ListingDao<String, String, ConfigChangeLog> configChangeLogDAO;

    @Before
    @After
    public void setUp() throws Exception {
        configChangeLogDAO.deleteAll(defaultMacAddress);
    }

    @Test
    public void getLogs() throws Exception {
        LastConfigLog lastConfigLog = getLastConfigLog();
        logService.setOne(defaultMacAddress, lastConfigLog);
        ConfigChangeLog configChangeLog = getConfigChangeLog();
        logService.setOne(defaultMacAddress, configChangeLog);

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("lastConfigLog", lastConfigLog);
        expectedResult.put("configChangeLog", Collections.singletonList(configChangeLog));

        performRequestAndVerifyResponse(LogController.URL_MAPPING + "/" + defaultMacAddress, expectedResult);
    }

    private LastConfigLog getLastConfigLog() {
        return setupFields(new LastConfigLog(), "LastConfigExplanation");
    }

    private ConfigChangeLog getConfigChangeLog() {
        return setupFields(new ConfigChangeLog(), "ConfigChangeLogExplanation");
    }

    private <T extends LastConfigLog> T setupFields(T configLog, String explanation) {
        configLog.setExplanation(explanation);
        configLog.setConfig(createFirmwareConfigFacade());
        return configLog;
    }

    private FirmwareConfigFacade createFirmwareConfigFacade() {
        FirmwareConfigFacade firmwareConfig = new FirmwareConfigFacade();
        firmwareConfig.setFirmwareLocation("location");
        firmwareConfig.setRebootImmediately(true);
        return firmwareConfig;
    }
}
