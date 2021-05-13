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
 *  Created: 4:42 PM
 */
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.queries.QueryConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.*;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static com.comcast.xconf.util.RuleUtil.getRuleString;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PercentageBeanQueriesControllerTest extends BaseQueriesControllerTest {

    @Test
    public void getAll() throws Exception {
        PercentageBean percentageBean = createPercentageBean(STB);
        savePercentageBean(percentageBean);
        QueriesHelper.nullifyUnwantedFields(percentageBean);
        percentageBean = percentageBeanHelper.replaceConfigIdWithFirmwareVersion(percentageBean);
        String expectedJson = JsonUtil.toJson(Lists.newArrayList(percentageBean));
        String fullUrl = "/" + QueryConstants.QUERIES_PERCENTAGE_BEAN;

        mockMvc.perform(get(fullUrl)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void getAll_dontProduceNpeWhenDistributionIsNull() throws Exception {
        PercentageBean percentageBean = createPercentageBean(STB);
        percentageBean.setDistributions(null);
        savePercentageBean(percentageBean);
        String fullUrl = "/" + QueryConstants.QUERIES_PERCENTAGE_BEAN;

        mockMvc.perform(get(fullUrl)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getAllByApplicationType() throws Exception {
        Map<String, PercentageBean> percentageBeans = createAndSavePercentageBeans(STB, XHOME);

        String url = "/" + QueryConstants.QUERIES_PERCENTAGE_BEAN;
        PercentageBean stbPercentageBean = percentageBeanHelper.replaceConfigIdWithFirmwareVersion(percentageBeans.get(STB));
        List<PercentageBean> expectedResult = Lists.newArrayList(QueriesHelper.nullifyUnwantedFields(stbPercentageBean));
        performGetWithApplication(url, "", expectedResult);

        PercentageBean xhomePercentageBean = percentageBeanHelper.replaceConfigIdWithFirmwareVersion(percentageBeans.get(XHOME));
        List<PercentageBean> xhomePercentageBeans = Lists.newArrayList(QueriesHelper.nullifyUnwantedFields(xhomePercentageBean));

        performGetWithApplication(url, XHOME, xhomePercentageBeans);
    }

    @Test
    public void getById() throws Exception {
        PercentageBean percentageBean = createPercentageBean(STB);
        savePercentageBean(percentageBean);
        QueriesHelper.nullifyUnwantedFields(percentageBean);
        percentageBean = percentageBeanHelper.replaceConfigIdWithFirmwareVersion(percentageBean);
        String fullUrl = "/" + QueryConstants.QUERIES_PERCENTAGE_BEAN + "/" + percentageBean.getId();

        mockMvc.perform(get(fullUrl)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(percentageBean)));
    }

    @Test
    public void getByIdInXmlAndJsonFormatAndIgnoreNullValues() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, defaultModelId, FirmwareConfig.DownloadProtocol.http, STB);
        PercentageBean percentageBean = createPercentageBean("percentageBean", null, null, firmwareConfig, 100, STB);
        percentageBean.setLastKnownGood(null);
        savePercentageBean(percentageBean);

        String fullUrl = "/" + QueryConstants.QUERIES_PERCENTAGE_BEAN + "/" + percentageBean.getId();

        mockMvc.perform(get(fullUrl)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(xpath("/PercentageBean/lastKnownGood", "").doesNotExist());

        mockMvc.perform(get(fullUrl)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastKnownGood").doesNotExist());
    }

    @Test
    public void getByWrongId() throws Exception {
        PercentageBean percentageBean = createPercentageBean(STB);
        savePercentageBean(percentageBean);
        mockMvc.perform(
                get("/" + QueryConstants.QUERIES_PERCENTAGE_BEAN + "/wrongId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void update() throws Exception {
        PercentageBean percentageBean = createPercentageBean(STB);
        savePercentageBean(percentageBean);
        Model intermediateModel = createAndSaveModel("newModelId");
        FirmwareConfig newIntermediateConfig = createAndSaveFirmwareConfig("newIntermediateVersion", intermediateModel.getId(), FirmwareConfig.DownloadProtocol.http);

        percentageBean.setIntermediateVersion(newIntermediateConfig.getId());
        percentageBean.getFirmwareVersions().add(newIntermediateConfig.getFirmwareVersion());

        mockMvc.perform(put("/" + QueryConstants.UPDATES_PERCENTAGE_BEAN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(percentageBean)))
                .andExpect(status().isOk());
        assertEquals(newIntermediateConfig.getFirmwareVersion(), percentageBeanQueriesService.getOne(percentageBean.getId()).getIntermediateVersion());
    }

    @Test
    public void updateWithApplicationTypeAsRequestParam() throws Exception {
        PercentageBean percentageBean = createPercentageBean(XHOME);
        savePercentageBean(percentageBean);

        Model intermediateModel = createAndSaveModel("newModelId");
        FirmwareConfig newIntermediateConfig = createAndSaveFirmwareConfig("newIntermediateVersion", intermediateModel.getId(), FirmwareConfig.DownloadProtocol.http, XHOME);

        percentageBean.setIntermediateVersion(newIntermediateConfig.getId());
        percentageBean.getFirmwareVersions().add(newIntermediateConfig.getFirmwareVersion());

        mockMvc.perform(put("/" + QueryConstants.UPDATES_PERCENTAGE_BEAN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(percentageBean))
                .param("applicationType", XHOME))
                .andExpect(status().isOk());

        assertEquals(newIntermediateConfig.getFirmwareVersion(), percentageBeanQueriesService.getOne(percentageBean.getId()).getIntermediateVersion());
        assertEquals(XHOME, firmwareRuleDao.getOne(percentageBean.getId()).getApplicationType());
    }

    @Test
    public void createWithoutApplicationTypeAsRequestParam() throws Exception {
        PercentageBean percentageBean = createPercentageBean(STB);

        mockMvc.perform(post("/" + QueryConstants.UPDATES_PERCENTAGE_BEAN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(percentageBean)))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(percentageBean)));
    }

    @Test
    public void createPercentageBeanWithApplicationTypeAsRequestParam() throws Exception {
        PercentageBean percentageBean = createPercentageBean(XHOME);

        String url = "/" + QueryConstants.UPDATES_PERCENTAGE_BEAN;
        PercentageBean expectedResult = CloneUtil.clone(percentageBean);
        expectedResult.setApplicationType(XHOME);
        performPostWithApplication(url, XHOME, percentageBean, expectedResult);
    }

    @Test
    public void deleteById() throws Exception {
        PercentageBean percentageBean = createPercentageBean(STB);
        savePercentageBean(percentageBean);

        mockMvc.perform(
                delete("/" + QueryConstants.DELETES_PERCENTAGE_BEAN + "/" + percentageBean.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(percentageBean)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void getPercentageBeanWhitelistField() throws Exception {
        createAndSavePercentageBeans();
        createAndSaveGlobalPercentage();
        String fieldToSearch = "whitelist";
        Map<String, HashSet<String>> expectedResponse = Collections.singletonMap(fieldToSearch, Sets.newHashSet("percentageBeanWhitelist1", "percentageBeanWhitelist2", "globalPercentageWhitelist"));
        mockMvc.perform(get("/" + QueryConstants.QUERIES_PERCENTAGE_BEAN).param("field", fieldToSearch))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(expectedResponse)));
    }

    @Test
    public void getPercentageBeanFirmwareVersionField() throws Exception {
        createAndSavePercentageBeans();
        String fieldToSearch = "firmwareVersions";
        Map<String, HashSet<String>> expectedResponse = Collections.singletonMap(fieldToSearch, Sets.newHashSet("firmwareVersion1", "firmwareVersion2"));
        mockMvc.perform(get("/" + QueryConstants.QUERIES_PERCENTAGE_BEAN).param("field", fieldToSearch))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(expectedResponse)));
    }

    @Test
    public void verifyDuplicateChecking() throws Exception {
        Map<String, PercentageBean> percentageBeans = createAndSavePercentageBeans(STB, XHOME);
        PercentageBean newPercentageBean = CloneUtil.clone(percentageBeans.get(XHOME));
        newPercentageBean.setId(null);
        newPercentageBean.setName("newPercentageBean");
        mockMvc.perform(post("/" + QueryConstants.UPDATES_PERCENTAGE_BEAN)
                .contentType(MediaType.APPLICATION_JSON)
                .param(APPLICATION_TYPE_PARAM, XHOME)
                .content(JsonUtil.toJson(newPercentageBean)))
                .andExpect(status().isConflict())
                .andExpect(errorMessageMatcher("PercentageBean already exists with such env/model pair XHOME/XHOME and optional condition " + getRuleString(newPercentageBean.getOptionalConditions())));

        newPercentageBean.setOptionalConditions(createExistsRule("defaultTag"));

        mockMvc.perform(post("/" + QueryConstants.UPDATES_PERCENTAGE_BEAN)
                .contentType(MediaType.APPLICATION_JSON)
                .param(APPLICATION_TYPE_PARAM, XHOME)
                .content(JsonUtil.toJson(newPercentageBean)))
                .andExpect(status().isOk());
    }

    @Test
    public void savePercentageBeanWithoutPercentageRanges() throws Exception {
        PercentageBean percentageBean = createPercentageBean(STB);
        String firmwareConfigId = percentageBean.getDistributions().get(0).getConfigId();
        percentageBean.setDistributions(Lists.newArrayList(createNotRangeDistribution(firmwareConfigId)));

        mockMvc.perform(post("/" + QueryConstants.UPDATES_PERCENTAGE_BEAN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(percentageBean)))
                .andExpect(status().isOk());
    }

    private RuleAction.ConfigEntry createNotRangeDistribution(String configId) {
        RuleAction.ConfigEntry distribution = new RuleAction.ConfigEntry();
        distribution.setConfigId(configId);
        distribution.setPercentage(50.0);
        return distribution;
    }

    private Map<String, PercentageBean> createAndSavePercentageBeans(String stbName, String xhomeName) throws Exception {
        Map<String, PercentageBean> percentageBeans = new HashMap<>();
        PercentageBean percentageBean1 = createPercentageBean(stbName, defaultEnvironmentId, defaultModelId, defaultIpListId, defaultIpAddress, defaultFirmwareVersion, STB);
        percentageBean1.setId(UUID.randomUUID().toString());
        percentageBeanQueriesService.create(percentageBean1);
        percentageBeans.put(STB, percentageBean1);

        PercentageBean percentageBean2 = createPercentageBean(xhomeName, xhomeName, xhomeName, defaultIpListId, defaultIpAddress, defaultFirmwareVersion, XHOME);
        percentageBean2.setId(UUID.randomUUID().toString());
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfigByApplicationType(XHOME);
        RuleAction.ConfigEntry configEntry = new RuleAction.ConfigEntry(firmwareConfig.getId(), 0.0, 60.0);
        percentageBean2.setDistributions(Lists.newArrayList(configEntry));
        percentageBean2.getFirmwareVersions().add(firmwareConfig.getFirmwareVersion());
        percentageBeanQueriesService.create(percentageBean2);
        percentageBeans.put(XHOME, percentageBean2);

        return percentageBeans;
    }
}
