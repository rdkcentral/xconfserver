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
 * Author: ikostrov
 * Created: 02.09.15 15:15
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.Environment;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.MacRuleBean;
import com.comcast.xconf.estbfirmware.MacRuleService;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.beans.MacRuleBeanWrapper;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.*;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static com.comcast.xconf.queries.QueriesHelper.nullifyUnwantedFields;
import static com.comcast.xconf.queries.QueryConstants.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MacRuleQueriesControllerTest extends BaseQueriesControllerTest {

    public static final String URL_VERSION = "?version=" + API_VERSION;

    @Autowired
    private MacRuleService macRuleService;

    @Autowired
    private MacRuleQueriesController queriesController;

    @Test
    public void getMACRulesTest() throws Exception {
        MacRuleBean macRule = createDefaultMacRuleBean();
        mockMvc.perform(post("/" + UPDATE_RULES_MAC).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(macRule)))
                .andExpect(status().isCreated());

        macRule = nullifyUnwantedFields(queriesController.wrap(new MacRuleBeanWrapper(macRule, null), API_VERSION));

        verifyMacRuleResponse(macRule);

        mockMvc.perform(delete("/" + DELETE_RULES_MAC + "/{name}", macRule.getName()))
                .andExpect(status().isNoContent());
        performRequestAndVerifyResponse(QUERIES_RULES_MAC, Collections.emptyList());
    }

    @Test
    public void getMacRulesByApplicationType() throws Exception {
        Map<String, MacRuleBean> macRuleBeans = createAndSaveMacRuleBean(ApplicationType.STB, XHOME);
        String url = "/" + QUERIES_RULES_MAC;
        List<MacRuleBeanWrapper> xhomeExpectedResult = Collections.singletonList(nullifyUnwantedFields(queriesController.wrap(new MacRuleBeanWrapper(macRuleBeans.get(XHOME), null), API_VERSION)));
        performGetWithApplication(url, XHOME, xhomeExpectedResult);

        List<MacRuleBeanWrapper> stbExpectedResult = Collections.singletonList(nullifyUnwantedFields(queriesController.wrap(new MacRuleBeanWrapper(macRuleBeans.get(ApplicationType.STB), null), API_VERSION)));
        performGetWithApplication(url, STB, stbExpectedResult);

        performGetWithApplication(url, "", stbExpectedResult);
    }

    @Test
    public void getMacRuleByNameAndApplicationType() throws Exception {
        Map<String, MacRuleBean> macRuleBeans = createAndSaveMacRuleBean(STB, STB);
        String url = "/" + QUERIES_RULES_MAC + "/" + STB;

        MacRuleBeanWrapper xhomeExpectedResult = nullifyUnwantedFields(queriesController.wrap(new MacRuleBeanWrapper(macRuleBeans.get(XHOME), null), API_VERSION));
        performGetWithApplication(url, XHOME, xhomeExpectedResult);

        MacRuleBeanWrapper stbExpectedResult = nullifyUnwantedFields(queriesController.wrap(new MacRuleBeanWrapper(macRuleBeans.get(STB), null), API_VERSION));
        performGetWithApplication(url, STB, stbExpectedResult);
        performGetWithApplication(url, "", stbExpectedResult);
    }

    @Test
    public void getMacRuleByMacAddressAndApplication() throws Exception {
        Map<String, MacRuleBean> macRuleBeans = createAndSaveMacRuleBean(ApplicationType.STB, XHOME);
        String url = "/" + QUERIES_RULES_MAC + "/address/" + "11:11:11:11:11:11";

        List<MacRuleBeanWrapper> stbExpectedResult = Collections.singletonList(nullifyUnwantedFields(queriesController.wrap(new MacRuleBeanWrapper(macRuleBeans.get(ApplicationType.STB), null), API_VERSION)));
        performGetWithApplication(url, STB, stbExpectedResult);

        performGetWithApplication(url, "", stbExpectedResult);

        List<MacRuleBeanWrapper> xhomeExpectedResult = Collections.singletonList(nullifyUnwantedFields(queriesController.wrap(new MacRuleBeanWrapper(macRuleBeans.get(XHOME), null), API_VERSION)));
        performGetWithApplication(url, XHOME, xhomeExpectedResult);

    }

    @Test
    public void deleteMacRuleByNameAndApplicationType() throws Exception {
        createAndSaveMacRuleBean(ApplicationType.STB, ApplicationType.STB);

        mockMvc.perform(delete("/" + DELETE_RULES_MAC + "/" + ApplicationType.STB)
                .param("applicationType", XHOME))
                .andExpect(status().isNoContent());

        assertEquals(1, macRuleService.getByApplicationType(ApplicationType.STB).size());
        assertEquals(0, macRuleService.getByApplicationType(XHOME).size());
    }

    @Test
    public void saveWithWrongApplicationType() throws Exception {
        MacRuleBean macRuleBean = createDefaultMacRuleBean();

        String url = "/" + UPDATE_RULES_MAC;
        performPostWithWrongApplicationType(url, macRuleBean);
    }

    @Test
    public void getMacRuleByWrongName() throws Exception {
        String wrongMacRuleName = "wrongName";

        mockMvc.perform(get("/" + QUERIES_RULES_MAC + "/" + wrongMacRuleName)
                .param("version", "3.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void saveMACRuleTest() throws Exception {
        MacRuleBean macRule = createDefaultMacRuleBean();

        mockMvc.perform(post("/" + UPDATE_RULES_MAC).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(macRule)))
                .andExpect(status().isCreated());

        Set<MacRuleBeanWrapper> firmwareRules = macRuleService.getByApplicationType(ApplicationType.STB);
        Assert.assertEquals(1, firmwareRules.size());
        MacRuleBean rule = firmwareRules.iterator().next();

        // rule id would be new after rule creation
        macRule.setId(null);
        rule.setId(null);
        assertEquals(rule, rule);
    }

    @Test
    public void getMacRulesByMacReturnsEmptyList_WhenRuleContainsIdOfNonexistentNamespacedList() throws Exception {
        String nonexistentId = "nonexistentId";
        String someMac = "12:12:12:12:12:12";
        MacRuleBean macRule = createDefaultMacRuleBean();
        macRule.setMacListRef(nonexistentId);
        macRuleService.save(macRule, ApplicationType.STB);

        mockMvc.perform(get("/" + QUERIES_RULES_MAC + "/address/" + someMac).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void getMacRulesIfMacParameterExistsInAnotherFirmwareRuleType() throws Exception {
        Model model = createAndSaveModel("modelId");
        Environment environment = createAndSaveEnvironment("envId");
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig("firwmareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        GenericNamespacedList macList = createAndSaveGenericNamespacedList("macList", GenericNamespacedListTypes.MAC_LIST, defaultMacAddress);

        FirmwareRule firmwareRule = createEnvModelFirmwareRule("envModelRule", firmwareConfig.getId(), environment.getId(), model.getId(), macList.getId());
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        MacRuleBeanWrapper macRuleBeanWrapper = macRuleService.convertFirmwareRuleToMacRuleBean(firmwareRule);
        macRuleBeanWrapper.setFirmwareConfig(nullifyUnwantedFields(macRuleBeanWrapper.getFirmwareConfig()));
        macRuleBeanWrapper.setMacList(macList.getData());

       verifyMacRuleResponse(macRuleBeanWrapper);
    }

    @Test
    public void getMacRulesIfMacParameterExistsInAnotherFirmwareRuleTypeWithIsMacOperation() throws Exception {
        Model model = createAndSaveModel("modelId");
        Environment environment = createAndSaveEnvironment("envId");
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig("firwmareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        GenericNamespacedList macList = createAndSaveGenericNamespacedList("macList", GenericNamespacedListTypes.MAC_LIST, defaultMacAddress);

        Rule rule = createRule(null, RuleFactory.MAC, StandardOperation.IS, defaultMacAddress);
        FirmwareRule firmwareRule = createEnvModelFirmwareRule("envModelRule", firmwareConfig.getId(), environment.getId(), model.getId(), macList.getId());
        firmwareRule.setRule(rule);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        MacRuleBeanWrapper macRuleBeanWrapper = macRuleService.convertFirmwareRuleToMacRuleBean(firmwareRule);
        macRuleBeanWrapper.setFirmwareConfig(nullifyUnwantedFields(macRuleBeanWrapper.getFirmwareConfig()));
        macRuleBeanWrapper.setMacList(macList.getData());

        verifyMacRuleResponse(macRuleBeanWrapper);
    }

    @Test
    public void getMacRulesIfMacParameterExistsInAnotherFirmwareRuleTypeWithInMacOperation() throws Exception {
        Model model = createAndSaveModel("modelId");
        Environment environment = createAndSaveEnvironment("envId");
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig("firwmareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        GenericNamespacedList macList = createAndSaveGenericNamespacedList("macList", GenericNamespacedListTypes.MAC_LIST, defaultMacAddress);

        Rule rule = Rule.Builder.of(new Condition(RuleFactory.MAC, StandardOperation.IN, FixedArg.from(Lists.newArrayList(defaultMacAddress)))).build();
        FirmwareRule firmwareRule = createEnvModelFirmwareRule("envModelRule", firmwareConfig.getId(), environment.getId(), model.getId(), macList.getId());
        firmwareRule.setRule(rule);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        MacRuleBeanWrapper macRuleBeanWrapper = macRuleService.convertFirmwareRuleToMacRuleBean(firmwareRule);
        macRuleBeanWrapper.setFirmwareConfig(nullifyUnwantedFields(macRuleBeanWrapper.getFirmwareConfig()));
        macRuleBeanWrapper.setMacList(macList.getData());

        verifyMacRuleResponse(macRuleBeanWrapper);
    }

    @Test
    public void getMacRuleIfFirmwareConfigIdIsEmpty() throws Exception {
        Model model = createAndSaveModel("modelId");
        Environment environment = createAndSaveEnvironment("envId");
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig("firwmareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        GenericNamespacedList macList = createAndSaveGenericNamespacedList("macList", GenericNamespacedListTypes.MAC_LIST, defaultMacAddress);

        Rule rule = createRule(null, RuleFactory.MAC, StandardOperation.IS, defaultMacAddress);
        FirmwareRule firmwareRule = createEnvModelFirmwareRule("envModelRule", "", environment.getId(), model.getId(), macList.getId());
        firmwareRule.setRule(rule);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        MacRuleBeanWrapper macRuleBeanWrapper = macRuleService.convertFirmwareRuleToMacRuleBean(firmwareRule);
        macRuleBeanWrapper.setFirmwareConfig(nullifyUnwantedFields(macRuleBeanWrapper.getFirmwareConfig()));
        macRuleBeanWrapper.setMacList(macList.getData());

        verifyMacRuleResponse(macRuleBeanWrapper);
    }

    @Test
    public void createMacRuleWithXhomeApplicationType() throws Exception {
        final MacRuleBean macRule = createDefaultMacRuleBean();
        macRule.getFirmwareConfig().setApplicationType(XHOME);
        FirmwareConfig firmwareConfig = macRule.getFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);

        macRule.setFirmwareConfig(firmwareConfig);
        mockMvc.perform(post("/" + UPDATE_RULES_MAC).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(macRule)).param("applicationType", XHOME))
                .andExpect(status().isCreated());

        assertTrue(ApplicationType.equals(XHOME, firmwareRuleDao.getOne(macRule.getId()).getApplicationType()));
    }

    private void verifyMacRuleResponse(MacRuleBean macRuleBean) throws Exception {
        performRequestAndVerifyResponse(QUERIES_RULES_MAC + URL_VERSION, Collections.singleton(macRuleBean));
        performRequestAndVerifyResponse(QUERIES_RULES_MAC + "/{name}" + URL_VERSION, macRuleBean.getName(), macRuleBean);
        performRequestAndVerifyResponse(QUERIES_RULES_MAC + "/address/{macAddress}" + URL_VERSION, defaultMacAddress, Collections.singleton(macRuleBean));
    }

    private Map<String, MacRuleBean> createAndSaveMacRuleBean(String stbName, String xhomeName) throws Exception {
        Map<String, MacRuleBean> macRuleBeans = new HashMap();
        MacRuleBean macRuleBean1 = createDefaultMacRuleBean();
        macRuleBean1.setId(UUID.randomUUID().toString());
        macRuleBean1.setName(stbName);
        macRuleService.save(macRuleBean1, ApplicationType.STB);
        macRuleBeans.put(ApplicationType.STB, macRuleBean1);

        MacRuleBean macRuleBean2 = createDefaultMacRuleBean();
        macRuleBean2.setId(UUID.randomUUID().toString());
        macRuleBean2.setName(xhomeName);
        macRuleService.save(macRuleBean2, XHOME);
        macRuleBeans.put(XHOME, macRuleBean2);

        return macRuleBeans;
    }
}
