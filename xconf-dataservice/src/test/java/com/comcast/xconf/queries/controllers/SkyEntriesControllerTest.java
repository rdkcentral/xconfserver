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

import static com.comcast.xconf.firmware.ApplicationType.SKY;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SkyEntriesControllerTest extends BaseQueriesControllerTest {

    @Test
    public void createSkyFirmwareConfig() throws Exception {
        FirmwareConfig firmwareConfig = createFirmwareConfig(UUID.randomUUID().toString());

        mockMvc.perform(post("/" + QueryConstants.UPDATE_FIRMWARES)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(firmwareConfig))
                .param(APPLICATION_TYPE_PARAM, SKY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationType").value(SKY));

        assertEquals(SKY, firmwareConfigDAO.getOne(firmwareConfig.getId()).getApplicationType());
    }

    @Test
    public void updateSkyIpRule() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, defaultModelId, FirmwareConfig.DownloadProtocol.http, SKY);
        ipRuleBean.setFirmwareConfig(firmwareConfig);
        ipRuleService.save(ipRuleBean, SKY);

        IpAddressGroupExtended ipAddressGroup = createAndSaveIpAddressGroupExtended("newIpAddressGroup", Sets.newHashSet("100.12.12.12", "101.12.12.12"));
        ipRuleBean.setIpAddressGroup(ipAddressGroup);

        mockMvc.perform(post("/" + QueryConstants.UPDATE_RULES_IPS)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(ipRuleBean))
                .param(APPLICATION_TYPE_PARAM, SKY))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(ipRuleBean)));

        assertEquals(SKY, firmwareRuleDao.getOne(ipRuleBean.getId()).getApplicationType());
    }

    @Test
    public void getAllSkyPercentageBeans() throws Exception {
        PercentageBean xhomePercentageBean = createAndSavePercentageBean("xhomeBean", "xhomeEnv", "xhomeModel", defaultIpListId, "12.12.12.12", "xhomeVersion", XHOME);
        PercentageBean skyPercentageBean = createAndSavePercentageBean("skyBean", "skyEnv", "skyModel", defaultIpListId, "12.12.12.12", "skyVersion", SKY);

        skyPercentageBean = percentageBeanHelper.replaceConfigIdWithFirmwareVersion(skyPercentageBean);
        QueriesHelper.nullifyUnwantedFields(skyPercentageBean);

        mockMvc.perform(get("/" + QueryConstants.QUERIES_PERCENTAGE_BEAN)
                .contentType(MediaType.APPLICATION_JSON)
                .param(APPLICATION_TYPE_PARAM, SKY))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(skyPercentageBean))));
    }

    @Test
    public void deleteSkyMacRule() throws Exception {
        MacRuleBean macRuleBean = createDefaultMacRuleBean();
        macRuleService.save(macRuleBean, SKY);

        assertEquals(SKY, firmwareRuleDao.getOne(macRuleBean.getId()).getApplicationType());

        mockMvc.perform(delete("/" + QueryConstants.DELETE_RULES_MAC + "/{name}", macRuleBean.getName())
                .param(APPLICATION_TYPE_PARAM, SKY))
                .andExpect(status().isNoContent());

        assertNull(firmwareRuleDao.getOne(macRuleBean.getId()));
    }
}

