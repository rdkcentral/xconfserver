/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.comcast.xconf.estbfirmware.ConfigChangeLogService.BOUNDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigChangeLogServiceTest extends BaseQueriesControllerTest {

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
    public void saveAndReadLastConfigLog() {
        LastConfigLog log = getLastConfigLog();

        logService.setOne(defaultMacAddress, log);

        LastConfigLog lastConfigLog = logService.getLastConfigLog(defaultMacAddress);
        assertNotNull(lastConfigLog);
        assertEquals(log.getExplanation(), lastConfigLog.getExplanation());
        assertEquals(log.getConfig(), lastConfigLog.getConfig());
    }

    @Test
    public void getConfigChangeLogsOnly() {
        logService.setOne(defaultMacAddress, getLastConfigLog());
        ConfigChangeLog configChangeLog = getConfigChangeLog(1);
        logService.setOne(defaultMacAddress, configChangeLog);

        List<ConfigChangeLog> changeLogsOnly = logService.getChangeLogsOnly(defaultMacAddress);
        assertEquals(1, changeLogsOnly.size());
        assertEquals(configChangeLog.getExplanation(), changeLogsOnly.get(0).getExplanation());

    }

    @Test
    public void canGetConfigChangeLogsOnlyWithinBounds() {
        for (int i = 1; i < 2 * BOUNDS; i++) {
            ConfigChangeLog configChangeLog = getConfigChangeLog(i);
            logService.setOne(defaultMacAddress, configChangeLog);
        }

        List<ConfigChangeLog> changeLogsOnly = logService.getChangeLogsOnly(defaultMacAddress);
        assertEquals(BOUNDS.intValue(), changeLogsOnly.size());

    }

    private LastConfigLog getLastConfigLog() {
        return setupFields(new LastConfigLog(), "LastConfigExplanation");
    }

    private ConfigChangeLog getConfigChangeLog(int number) {
        return setupFields(new ConfigChangeLog(), "ConfigChangeLogExplanation_" + number);
    }

    private <T extends LastConfigLog> T setupFields(T configLog, String explanation) {
        configLog.setExplanation(explanation);
        configLog.setConfig(createFirmwareConfig());
        return configLog;
    }

    private FirmwareConfigFacade createFirmwareConfig() {
        FirmwareConfigFacade firmwareConfig = new FirmwareConfigFacade();
        firmwareConfig.setFirmwareLocation("location");
        firmwareConfig.setRebootImmediately(true);
        return firmwareConfig;
    }
}
