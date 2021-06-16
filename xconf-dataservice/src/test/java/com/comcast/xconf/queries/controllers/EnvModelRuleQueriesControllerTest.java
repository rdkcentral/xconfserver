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
import com.comcast.xconf.estbfirmware.EnvModelRuleBean;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.queries.QueryConstants;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EnvModelRuleQueriesControllerTest extends BaseQueriesControllerTest{

    @Test
    public void getAllEnvModelRulesTest() throws Exception {
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        envModelRuleService.save(envModelRuleBean, STB);
        QueriesHelper.nullifyUnwantedFields(envModelRuleBean.getFirmwareConfig());

        performRequestAndVerifyResponse(QueryConstants.QUERIES_RULES_ENV_MODEL, Collections.singleton(envModelRuleBean));
    }

    @Test
    public void getEnvModelRuleByApplicationType() throws Exception {
        Map<String, EnvModelRuleBean> envModelRuleBeans = createAndSaveEnvModelRuleBeans(STB, ApplicationType.XHOME);

        QueriesHelper.nullifyUnwantedFields(envModelRuleBeans.get(STB).getFirmwareConfig());
        QueriesHelper.nullifyUnwantedFields(envModelRuleBeans.get(ApplicationType.XHOME).getFirmwareConfig());
        String url = "/" + QueryConstants.QUERIES_RULES_ENV_MODEL;
        performGetWithApplication(url, STB, Collections.singletonList(envModelRuleBeans.get(STB)));

        performGetWithApplication(url, XHOME, Collections.singletonList(envModelRuleBeans.get(XHOME)));

        performGetWithApplication(url, "", Collections.singletonList(envModelRuleBeans.get(STB)));
    }

    @Test
    public void getEnvModelRuleByNameTest() throws Exception {
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        envModelRuleService.save(envModelRuleBean, STB);
        QueriesHelper.nullifyUnwantedFields(envModelRuleBean.getFirmwareConfig());

        performRequestAndVerifyResponse(QueryConstants.QUERIES_RULES_ENV_MODEL + "/{name}", envModelRuleBean.getName(), envModelRuleBean);
    }

    @Test
    public void getEnvModelRuleByNameAndApplicationType() throws Exception {
        Map<String, EnvModelRuleBean> envModelRuleBeans = createAndSaveEnvModelRuleBeans(STB, STB);
        QueriesHelper.nullifyUnwantedFields(envModelRuleBeans.get(ApplicationType.XHOME).getFirmwareConfig());
        String url = "/" + QueryConstants.QUERIES_RULES_ENV_MODEL + "/" + STB;
        performGetWithApplication(url, XHOME, envModelRuleBeans.get(XHOME));
    }

    @Test
    public void getEnvModelRuleByWrongName() throws Exception {
        String wrongEnvModelRuleName = "wrongName";
        mockMvc.perform(get("/" + QueryConstants.QUERIES_RULES_ENV_MODEL + "/" + wrongEnvModelRuleName)
                .param("version", "3.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateEnvModelRuleTest() throws Exception {
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        mockMvc.perform(post("/" + QueryConstants.UPDATE_RULES_ENV_MODEL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(envModelRuleBean)))
                .andExpect(status().isOk());

        assertEquals(envModelRuleBean, envModelRuleService.getOne(envModelRuleBean.getId()));
    }

    @Test
    public void updateEnvModelRuleByApplicationType() throws Exception {
        Map<String, EnvModelRuleBean> envModelRuleBeans = createAndSaveEnvModelRuleBeans(STB, ApplicationType.XHOME);
        envModelRuleBeans.get(ApplicationType.XHOME).setName("changedXhomeName");
        envModelRuleService.delete(envModelRuleBeans.get(ApplicationType.XHOME).getId());
        String url = "/" + QueryConstants.UPDATE_RULES_ENV_MODEL;
        performPostWithApplication(url, XHOME, envModelRuleBeans.get(XHOME), envModelRuleBeans.get(XHOME));

        assertEquals(envModelRuleBeans.get(ApplicationType.XHOME), envModelRuleService.getOneByName("changedXhomeName", ApplicationType.XHOME));
        assertEquals(envModelRuleBeans.get(STB), envModelRuleService.getOneByName(STB, STB));
    }

    @Test
    public void addEnvironmentToEnvModelRule() throws Exception {
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        envModelRuleBean.setEnvironmentId(null);
        envModelRuleService.save(envModelRuleBean, STB);

        assertNull(envModelRuleService.getOne(envModelRuleBean.getId()).getEnvironmentId());

        EnvModelRuleBean envModelRuleBeanWithEnvironment = createDefaultEnvModelRuleBean();

        mockMvc.perform(post("/" + QueryConstants.UPDATE_RULES_ENV_MODEL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(envModelRuleBeanWithEnvironment)))
                .andExpect(status().isOk());

        assertEquals(envModelRuleBean, envModelRuleService.getOne(envModelRuleBeanWithEnvironment.getId()));
        assertEquals(defaultEnvironmentId.toUpperCase(), envModelRuleService.getOne(envModelRuleBeanWithEnvironment.getId()).getEnvironmentId());
    }

    @Test
    public void deleteEnvModelTest() throws Exception {
        EnvModelRuleBean bean = createDefaultEnvModelRuleBean();
        envModelRuleService.save(bean, STB);

        mockMvc.perform(delete("/" + QueryConstants.DELETE_RULES_ENV_MODEL + "/" + bean.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertNull(envModelRuleService.getOne(bean.getId()));
    }

    @Test
    public void deleteEnvModelRuleByNameAndApplicationType() throws Exception {
        Map<String, EnvModelRuleBean> envModelRuleBeans = createAndSaveEnvModelRuleBeans(STB, STB);

        mockMvc.perform(delete("/" + QueryConstants.DELETE_RULES_ENV_MODEL + "/" + STB)
                .contentType(MediaType.APPLICATION_JSON)
                .param("applicationType", ApplicationType.XHOME))
                .andExpect(status().isNoContent());

        assertNull(envModelRuleService.getOneByName(STB, ApplicationType.XHOME));
        assertEquals(envModelRuleBeans.get(STB), envModelRuleService.getOneByName(STB, STB));
    }

    @Test
    public void updateWithWrongApplicationType() throws Exception {
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        String url = "/" + QueryConstants.UPDATE_RULES_ENV_MODEL;

        performPostWithWrongApplicationType(url, envModelRuleBean);

    }

    @Test
    public void getEnvModelRuleWithWrongApplicationType() throws Exception {
        performGetWithWrongApplicationType("/" + QueryConstants.QUERIES_RULES_ENV_MODEL);
    }

    @Test
    public void getEnvModelRuleWithWrongApplicationTypeByName() throws Exception {
        performGetWithWrongApplicationType("/" + QueryConstants.QUERIES_RULES_ENV_MODEL + "/someName");
    }

    private Map<String, EnvModelRuleBean> createAndSaveEnvModelRuleBeans(String stbName, String xhomeName) throws Exception {
        Map<String, EnvModelRuleBean> envModelRuleBeans = new HashMap<>();
        EnvModelRuleBean envModelRuleBean1 = createDefaultEnvModelRuleBean();
        envModelRuleBean1.setId(UUID.randomUUID().toString());
        envModelRuleBean1.setName(stbName);
        envModelRuleService.save(envModelRuleBean1, STB);
        envModelRuleBeans.put(STB, envModelRuleBean1);

        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig.setApplicationType(ApplicationType.XHOME);
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        EnvModelRuleBean envModelRuleBean2 = createDefaultEnvModelRuleBean();
        envModelRuleBean2.setFirmwareConfig(firmwareConfig);
        envModelRuleBean2.setId(UUID.randomUUID().toString());
        envModelRuleBean2.setName(xhomeName);
        envModelRuleService.save(envModelRuleBean2, ApplicationType.XHOME);
        envModelRuleBeans.put(ApplicationType.XHOME, envModelRuleBean2);

        return envModelRuleBeans;
    }
}