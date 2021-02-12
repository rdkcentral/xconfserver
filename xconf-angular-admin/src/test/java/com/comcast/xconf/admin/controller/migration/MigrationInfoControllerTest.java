/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.admin.controller.migration;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.NamespacedList;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.utils.MigrationInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MigrationInfoControllerTest extends BaseControllerTest {

    private static final String MIGRATION_URL = "/migration";

    @Test
    public void testGetMigrationInfo() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        GenericNamespacedList macList = createMacList();
        genericNamespacedListDAO.setOne(macList.getId(), macList);

        DCMGenericRule dcmRule = createFormula();
        dcmRuleDAO.setOne(dcmRule.getId(), dcmRule);

        IpAddressGroupExtended ipAddressGroupExtended = createIpAddressGroup("ipAddressGroupId");
        ipAddressGroupDAO.setOne(ipAddressGroupExtended.getId(), ipAddressGroupExtended);

        NamespacedList namespacedList = createNamespacedList();
        namespacedListDAO.setOne(namespacedList.getId(), namespacedList);

        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        mockMvc.perform(get(MIGRATION_URL + "/info"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(getMigrationInfo())));
    }

    private List<MigrationInfo> getMigrationInfo() {
        List<MigrationInfo> migrationInfo = new ArrayList<>();
        migrationInfo.add(new MigrationInfo("FirmwareRule", "FirmwareRule", "/rules", 1, 1, true));
        migrationInfo.add(new MigrationInfo("Formula", "DCMGenericRule", "/formulas", 1, 1, true));
        migrationInfo.add(new MigrationInfo("NamespacedList", "MAC_LIST", "/namespacedLists", 1, 1, true));
        migrationInfo.add(new MigrationInfo("IpAddressGroupExtended", "IP_LIST", "/ipAddressGroups", 1, 1, true));

        return migrationInfo;
    }

}