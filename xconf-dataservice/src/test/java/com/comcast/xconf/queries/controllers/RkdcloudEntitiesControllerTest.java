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

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.IpRuleBean;
import com.comcast.xconf.estbfirmware.MacRuleBean;
import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.queries.QueryConstants;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.UUID;

import static com.comcast.xconf.firmware.ApplicationType.RDKCLOUD;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RkdcloudEntitiesControllerTest extends BaseQueriesControllerTest {

    @Test
    public void createRdkcloudFirmwareConfig() throws Exception {
        FirmwareConfig firmwareConfig = createFirmwareConfig(UUID.randomUUID().toString());

        mockMvc.perform(post("/" + QueryConstants.UPDATE_FIRMWARES)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(firmwareConfig))
                .param(APPLICATION_TYPE_PARAM, RDKCLOUD))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.applicationType").value(RDKCLOUD));
        
        assertEquals(RDKCLOUD, firmwareConfigDAO.getOne(firmwareConfig.getId()).getApplicationType());
    }

    @Test
    public void updateRdkcloudIpRule() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, defaultModelId, FirmwareConfig.DownloadProtocol.http, RDKCLOUD);
        ipRuleBean.setFirmwareConfig(firmwareConfig);
        ipRuleService.save(ipRuleBean, RDKCLOUD);

        IpAddressGroupExtended ipAddressGroup = createAndSaveIpAddressGroupExtended("newIpAddressGroup", Sets.newHashSet("100.12.12.12", "101.12.12.12"));
        ipRuleBean.setIpAddressGroup(ipAddressGroup);

        mockMvc.perform(post("/" + QueryConstants.UPDATE_RULES_IPS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(ipRuleBean))
                .param(APPLICATION_TYPE_PARAM, RDKCLOUD))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(ipRuleBean)));

        assertEquals(RDKCLOUD, firmwareRuleDao.getOne(ipRuleBean.getId()).getApplicationType());
    }

    @Test
    public void getAllRdkcloudPercentageBeans() throws Exception {
        PercentageBean xhomePercentageBean = createAndSavePercentageBean("xhomeBean", "xhomeEnv", "xhomeModel", defaultIpListId, "12.12.12.12", "xhomeVersion", XHOME);
        PercentageBean rdkcloudPercentageBean = createAndSavePercentageBean("rdkcloudBean", "rdkcloudEnv", "rdkcloudModel", defaultIpListId, "12.12.12.12", "rdkcloudVersion", RDKCLOUD);

        rdkcloudPercentageBean = percentageBeanHelper.replaceConfigIdWithFirmwareVersion(rdkcloudPercentageBean);
        QueriesHelper.nullifyUnwantedFields(rdkcloudPercentageBean);
        
        mockMvc.perform(get("/" + QueryConstants.QUERIES_PERCENTAGE_BEAN)
                .contentType(MediaType.APPLICATION_JSON)
                .param(APPLICATION_TYPE_PARAM, RDKCLOUD))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(rdkcloudPercentageBean))));
    }

    @Test
    public void deleteRdkcloudMacRule() throws Exception {
        MacRuleBean macRuleBean = createDefaultMacRuleBean();
        macRuleService.save(macRuleBean, RDKCLOUD);

        assertEquals(RDKCLOUD, firmwareRuleDao.getOne(macRuleBean.getId()).getApplicationType());

        mockMvc.perform(delete("/" + QueryConstants.DELETE_RULES_MAC + "/{name}", macRuleBean.getName())
                .param(APPLICATION_TYPE_PARAM, RDKCLOUD))
                .andExpect(status().isNoContent());

        assertNull(firmwareRuleDao.getOne(macRuleBean.getId()));
    }
}
