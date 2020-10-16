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
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.IpRuleBean;
import com.comcast.xconf.estbfirmware.IpRuleService;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.queries.QueryConstants;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.*;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static com.comcast.xconf.queries.QueriesHelper.nullifyUnwantedFields;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IpRuleQueriesControllerTest extends BaseQueriesControllerTest {

    @Autowired
    IpRuleService ipRuleService;

    @Test
    public void getIPRulesTest() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        ipRuleService.save(ipRuleBean, STB);

        nullifyUnwantedFields(ipRuleBean.getFirmwareConfig());

        performRequestAndVerifyResponse(QueryConstants.QUERIES_RULES_IPS, Collections.singleton(ipRuleBean));
    }

    @Test
    public void getIpRulesByApplicationType() throws Exception {
        Map<String, IpRuleBean> ipRuleBeans = createAndSaveIpRuleBeans(STB, XHOME);
        nullifyUnwantedFields(ipRuleBeans.get(STB).getFirmwareConfig());
        nullifyUnwantedFields(ipRuleBeans.get(XHOME).getFirmwareConfig());
        String url = "/" + QueryConstants.QUERIES_RULES_IPS;
        performGetWithApplication(url, XHOME, Collections.singletonList(ipRuleBeans.get(XHOME)));

        performGetWithApplication(url, STB, Collections.singletonList(ipRuleBeans.get(STB)));

        performGetWithApplication(url, "", Collections.singletonList(ipRuleBeans.get(STB)));
    }

    @Test
    public void getIpRuleByNameAndApplicationType() throws Exception {
        Map<String, IpRuleBean> ipRuleBeans = createAndSaveIpRuleBeans(STB, STB);
        nullifyUnwantedFields(ipRuleBeans.get(STB).getFirmwareConfig());
        nullifyUnwantedFields(ipRuleBeans.get(XHOME).getFirmwareConfig());
        String url = "/" + QueryConstants.QUERIES_RULES_IPS + "/" + STB;
        performGetWithApplication(url, XHOME, ipRuleBeans.get(XHOME));

        performGetWithApplication(url, STB, ipRuleBeans.get(STB));

        performGetWithApplication(url, "", ipRuleBeans.get(STB));
    }

    @Test
    public void testGetIPRuleByName() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        ipRuleService.save(ipRuleBean, STB);

        QueriesHelper.nullifyUnwantedFields(ipRuleBean.getFirmwareConfig());

        performRequestAndVerifyResponse(QueryConstants.QUERIES_RULES_IPS + "/" + ipRuleBean.getName(), ipRuleBean);
    }

    @Test
    public void getIpRuleByWrongName() throws Exception {
        String wrongIpRuleName = "wrongId";

        mockMvc.perform(get("/" + QueryConstants.QUERIES_RULES_IPS + "/" + wrongIpRuleName)
                .param("version", "3.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetIPRuleByIpAddressGroupName() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        ipRuleService.save(ipRuleBean, STB);

        QueriesHelper.nullifyUnwantedFields(ipRuleBean.getFirmwareConfig());

        performRequestAndVerifyResponse(QueryConstants.QUERIES_RULES_IPS + "/byIpAddressGroup/" + ipRuleBean.getIpAddressGroup().getName(), Collections.singleton(ipRuleBean));
    }

    @Test
    public void getIpRuleByIpAddressGroupNameAndApplicationType() throws Exception {
        Map<String, IpRuleBean> ipRuleBeans = createAndSaveIpRuleBeans(STB, XHOME);

        nullifyUnwantedFields(ipRuleBeans.get(STB).getFirmwareConfig());
        nullifyUnwantedFields(ipRuleBeans.get(XHOME).getFirmwareConfig());

        String url = "/" + QueryConstants.QUERIES_RULES_IPS + "/byIpAddressGroup/" + ipRuleBeans.get(STB).getIpAddressGroup().getName();
        List<IpRuleBean> stbExpectedResult = Collections.singletonList(ipRuleBeans.get(STB));
        performGetWithApplication(url, "", stbExpectedResult);

        performGetWithApplication(url, STB, stbExpectedResult);

        List<IpRuleBean> xhomeExpectedResult = Collections.singletonList(ipRuleBeans.get(XHOME));
        performGetWithApplication(url, XHOME, xhomeExpectedResult);

    }

    @Test
    public void testUpdateIpRule() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        nullifyUnwantedFields(ipRuleBean.getFirmwareConfig());

        mockMvc.perform(
                post("/" + QueryConstants.UPDATE_RULES_IPS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(ipRuleBean)))
                .andExpect(status().isOk());
        assertEquals(ipRuleBean, ipRuleService.getOne(ipRuleBean.getId()));
    }

    @Test
    public void updateIpRuleByApplicationType() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        FirmwareConfig firmwareConfig = ipRuleBean.getFirmwareConfig();
        firmwareConfig.setApplicationType(XHOME);
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        String url = "/" + QueryConstants.UPDATE_RULES_IPS;
        performPostWithApplication(url, XHOME, ipRuleBean, ipRuleBean);

        assertEquals(ipRuleBean, ipRuleService.getByApplicationType(XHOME).iterator().next());
    }

    @Test
    public void testDeleteIpRule() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        ipRuleService.save(ipRuleBean, STB);
        nullifyUnwantedFields(ipRuleBean.getFirmwareConfig());

        mockMvc.perform(
                delete("/" + QueryConstants.DELETE_RULES_IPS + "/" + ipRuleBean.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Assert.assertNull(ipRuleService.getOne(ipRuleBean.getId()));
    }

    @Test
    public void deleteIpRuleByNameAndApplicationType() throws Exception {
        createAndSaveIpRuleBeans(STB, STB);

        mockMvc.perform(delete("/" + QueryConstants.DELETE_RULES_IPS + "/" + STB)
                .contentType(MediaType.APPLICATION_JSON).param("applicationType", XHOME))
                .andExpect(status().isNoContent());

        assertEquals(0, ipRuleService.getByApplicationType(XHOME).size());
        assertEquals(1, ipRuleService.getByApplicationType(STB).size());
    }

    @Test
    public void saveIpRuleWithWrongApplicationType() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();

        String url = "/" + QueryConstants.UPDATE_RULES_IPS;
        performPostWithWrongApplicationType(url, ipRuleBean);
    }

    @Test
    public void getIpRulesWithWrongApplicationType() throws Exception {
        performGetWithWrongApplicationType("/" + QueryConstants.QUERIES_RULES_IPS);
    }

    @Test
    public void getIpRuleWithWrongApplicationTypeByName() throws Exception {
        performGetWithWrongApplicationType("/" + QueryConstants.QUERIES_RULES_IPS + "/someName");
    }

    private Map<String, IpRuleBean> createAndSaveIpRuleBeans(String stbName, String xhomeName) throws Exception {
        Map<String, IpRuleBean> ipRuleBeans = new HashMap<>();
        IpRuleBean ipRuleBean1 = createDefaultIpRuleBean();
        ipRuleBean1.setId(UUID.randomUUID().toString());
        ipRuleBean1.setName(stbName);
        ipRuleService.save(ipRuleBean1, STB);
        ipRuleBeans.put(STB, ipRuleBean1);

        IpRuleBean ipRuleBean2 = createDefaultIpRuleBean();
        ipRuleBean2.setId(UUID.randomUUID().toString());
        ipRuleBean2.setName(xhomeName);
        ipRuleService.save(ipRuleBean2, XHOME);
        ipRuleBeans.put(XHOME, ipRuleBean2);

        return ipRuleBeans;
    }
}