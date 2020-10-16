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
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.queries.QueryConstants;
import com.comcast.xconf.queries.beans.EnvModelPercentageWrapper;
import com.comcast.xconf.queries.beans.PercentFilterWrapper;
import com.comcast.xconf.queries.beans.TimeFilterWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.LocalTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by mdolina on 8/28/15.
 */
public class BlockingFilterQueriesControllerTest extends BaseQueriesControllerTest {

    @Autowired
    private IpFilterService ipFilterService;

    @Autowired
    private TimeFilterService timeFilterService;

    @Test
    public void testGetAllIpFilters() throws Exception {
        IpFilter ipFilter = createIpFilter();
        ipFilterService.save(ipFilter, ApplicationType.STB);

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_IPS, Lists.newArrayList(ipFilter));
    }

    @Test
    public void getIpFiltersByApplicationType() throws Exception {
        Map<String, IpFilter> ipFilters = createIpFilters(ApplicationType.STB, XHOME);
        String url = "/" +QueryConstants.QUERIES_FILTERS_IPS;

        performGetWithApplication(url, XHOME, Collections.singleton(ipFilters.get(XHOME)));

        performGetWithApplication(url, STB, Collections.singleton(ipFilters.get(STB)));

        performGetWithApplication(url, "", Collections.singleton(ipFilters.get(STB)));
    }

    @Test
    public void testCreateAndGetIpFilterById() throws Exception {
        IpFilter ipFilter = createIpFilter();
        ipFilterService.save(ipFilter, ApplicationType.STB);

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_IPS + "/" + ipFilter.getName(), ipFilter);
    }

    @Test
    public void getIpFilterByNameAndApplicationType() throws Exception {
        Map<String, IpFilter> ipFilters = createIpFilters(ApplicationType.STB, XHOME);
        String url = "/" +QueryConstants.QUERIES_FILTERS_IPS + "/";
        performGetWithApplication(url + STB, ApplicationType.STB, ipFilters.get(STB));

        performGetWithApplication(url + XHOME, XHOME, ipFilters.get(XHOME));
    }

    @Test
    public void createIpFilterWithWrongApplicationType() throws Exception {
        IpFilter ipFilter = createIpFilter();
        String url = "/" + QueryConstants.UPDATE_FILTERS_IPS;

        performPostWithWrongApplicationType(url, ipFilter);
    }

    @Test
    public void getIpFilterByWrongName() throws Exception {
        String wrongIpFilterName = "wrongName";

        mockMvc.perform(get("/" + QueryConstants.QUERIES_FILTERS_IPS + "/" + wrongIpFilterName)
                .param("version", "3.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateIpFilter() throws Exception {
        IpFilter ipFilter = createIpFilter();
        ipFilterService.save(ipFilter, ApplicationType.STB);
        IpFilter changedIpFilter = createIpFilter();
        createAndSaveDefaultIpAddressGroupExtended();
        changedIpFilter.setName("changedTestFilter");

        mockMvc.perform(
                post("/" + QueryConstants.UPDATE_FILTERS_IPS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(changedIpFilter)))
                .andExpect(status().isOk());

        assertEquals(changedIpFilter, ipFilterService.getOneIpFilterFromDB(ipFilter.getId()));
    }

    @Test
    public void testRemoveIpFilter() throws Exception {
        IpFilter ipFilter = createIpFilter();
        ipFilterService.save(ipFilter, ApplicationType.STB);

        mockMvc.perform(
                delete("/" + QueryConstants.DELETE_FILTERS_IPS + "/" + ipFilter.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(ipFilter)))
                .andExpect(status().isNoContent());

        Assert.assertNull(ipFilterService.getOneIpFilterFromDB(ipFilter.getId()));
    }

    @Test
    public void deleteIpFilterByNameAndApplicationType() throws Exception {
        createIpFilters("name", "name");

        mockMvc.perform(
                delete("/" + QueryConstants.DELETE_FILTERS_IPS + "/" + "name")
                        .param("applicationType", XHOME))
                .andExpect(status().isNoContent());

        assertEquals(1, ipFilterService.getByApplicationType(ApplicationType.STB).size());
        assertEquals(0, ipFilterService.getByApplicationType(XHOME).size());
    }

    @Test
    public void getPercentFilterTest() throws Exception {
        percentFilterService.save(createPercentFilter(), ApplicationType.STB);
        PercentFilterWrapper percentFilterWrapper = new PercentFilterWrapper(percentFilterService.get(ApplicationType.STB));
        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_PERCENT, nullifyUnwantedFields(convertLastKnownGoodToVersion(percentFilterWrapper)));
    }

    @Test
    public void getPercentFilterByApplicationType() throws Exception {
        Map<String, PercentFilterValue> percentFilters = createAndSavePercentFilters();
        SingletonFilterValue expectedStbResult = QueriesHelper.nullifyUnwantedFields(percentFilterService.toHumanReadableForm(new PercentFilterWrapper(percentFilters.get(ApplicationType.STB))));
        String url = "/" + QueryConstants.QUERIES_FILTERS_PERCENT;
        performGetWithApplication(url, ApplicationType.STB, expectedStbResult);

        SingletonFilterValue expectedXhomeResult = QueriesHelper.nullifyUnwantedFields(percentFilterService.toHumanReadableForm(new PercentFilterWrapper(percentFilters.get(XHOME))));

        performGetWithApplication(url, XHOME, expectedXhomeResult);

        performGetWithApplication(url, "", expectedStbResult);
    }

    @Test
    public void createPercentFilterWithWrongApplicationType() throws Exception {
        PercentFilterValue percentFilter = createPercentFilter();

        mockMvc.perform(post("/" + QueryConstants.UPDATE_FILTERS_PERCENT)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .param("applicationType", "wrongApplicationType")
                .content(JsonUtil.toJson(new PercentFilterWrapper(percentFilter))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"ApplicationType wrongApplicationType is not valid\""));
    }

    @Test
    public void createPercentFilterByApplicationType() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfigByApplicationType(XHOME);

        PercentFilterValue percentFilter = createPercentFilter(XHOME, 55, firmwareConfig);
        PercentFilterWrapper expectedResult = new PercentFilterWrapper(percentFilter);
        EnvModelPercentageWrapper envModelPercentageWrapper = expectedResult.getEnvModelPercentageWrappers().get(0);
        envModelPercentageWrapper.setIntermediateVersion(firmwareConfig.getId());
        envModelPercentageWrapper.setLastKnownGood(firmwareConfig.getId());
        String url = "/" + QueryConstants.UPDATE_FILTERS_PERCENT;
        performPostWithApplication(url,XHOME, new PercentFilterWrapper(percentFilter), expectedResult);

        PercentFilterValue expectedPercentFilterValue = new PercentFilterValue();
        expectedPercentFilterValue.setEnvModelPercentages(new HashMap<String, EnvModelPercentage>());

        assertEquals(expectedPercentFilterValue, percentFilterService.get(ApplicationType.STB));
        assertEquals(percentFilter, percentFilterService.get(XHOME));
    }

    @Test
    public void getPercentFilterParameter() throws Exception {
        PercentFilterWrapper filter = new PercentFilterWrapper(createPercentFilter());
        percentFilterService.save(filter, ApplicationType.STB);

        Set<String> lastKnowGoodVersions = new HashSet<>();
        for (EnvModelPercentage envModelPercentage : filter.getEnvModelPercentages().values()) {
            FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne(envModelPercentage.getLastKnownGood());
            lastKnowGoodVersions.add(firmwareConfig.getFirmwareVersion());
        }
        String fieldName = "lastKnownGood";
        mockMvc.perform(get("/" + QueryConstants.QUERIES_FILTERS_PERCENT)
                .contentType(MediaType.APPLICATION_JSON)
                .param("field", fieldName))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonMap(fieldName, lastKnowGoodVersions))));
    }

    private PercentFilterWrapper convertLastKnownGoodToVersion(PercentFilterWrapper filter) {
        filter.getEnvModelPercentageWrappers().get(0).setLastKnownGood(defaultFirmwareVersion);
        filter.getEnvModelPercentageWrappers().get(0).setIntermediateVersion(defaultFirmwareVersion);
        return filter;
    }

    @Test
    public void testUpdatePercentFilter() throws Exception {
        PercentFilterWrapper filter = new PercentFilterWrapper(createPercentFilter());
        percentFilterService.save(filter, ApplicationType.STB);
        createAndSaveDefaultIpAddressGroupExtended();
        filter.setPercentage(15);
        convertLastKnownGoodToVersion(filter);

        mockMvc.perform(
                post("/" + QueryConstants.UPDATE_FILTERS_PERCENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(filter)))
                .andExpect(status().isOk());

        PercentFilterWrapper percentFilterWrapper = new PercentFilterWrapper(percentFilterService.get(ApplicationType.STB));
        percentFilterWrapper.setPercentage(15);
        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_PERCENT, nullifyUnwantedFields(convertLastKnownGoodToVersion(percentFilterWrapper)));
    }

    @Test
    public void testGetAllTimeFilters() throws Exception {
        TimeFilter timeFilter = createTimeFilter();
        timeFilterService.save(timeFilter, ApplicationType.STB);

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_TIME, Lists.newArrayList(new TimeFilterWrapper(timeFilter)));
    }

    @Test
    public void testGetTimeFilterById() throws Exception {
        TimeFilter timeFilter = createTimeFilter();
        timeFilterService.save(timeFilter, ApplicationType.STB);

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_TIME + "/" + timeFilter.getName(), new TimeFilterWrapper(timeFilter));
    }

    @Test
    public void getNotExistedTimeFilter() throws Exception {
        TimeFilter timeFilter = createTimeFilter();
        timeFilterService.save(timeFilter, ApplicationType.STB);
        mockMvc.perform(get("/" + QueryConstants.QUERIES_FILTERS_TIME + "/{id}", "wrongId")
                .param("version", "3.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateTimeFilter() throws Exception {
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        envModelRuleService.save(envModelRuleBean, ApplicationType.STB);
        TimeFilterWrapper timeFilter = new TimeFilterWrapper(createTimeFilter());
        mockMvc.perform(
                post("/" + QueryConstants.UPDATE_FILTERS_TIME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(timeFilter)))
                .andExpect(status().isOk());
        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_TIME + "/" + timeFilter.getName(), timeFilter);
    }

    @Test
    public void testDeleteTimeFilter() throws Exception {
        TimeFilter timeFilter = createTimeFilter();
        timeFilterService.save(timeFilter, ApplicationType.STB);

        mockMvc.perform(
                delete("/" + QueryConstants.DELETE_FILTERS_TIME + "/" + timeFilter.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(timeFilter)))
                .andExpect(status().isNoContent());
        Assert.assertNull(timeFilterService.getOneTimeFilterFromDB(timeFilter.getId()));
    }

    @Test
    public void deleteTimeFilterByApplicationType() throws Exception {
        createTimeFilters(ApplicationType.STB, XHOME);

        mockMvc.perform(delete("/" + QueryConstants.DELETE_FILTERS_TIME + "/" + XHOME)
                .param("applicationType", XHOME))
                .andExpect(status().isNoContent());

        assertEquals(0, timeFilterService.getByApplicationType(XHOME).size());
        assertEquals(1, timeFilterService.getByApplicationType(ApplicationType.STB).size());
    }

    @Test
    public void getTimeFiltersByApplicationType() throws Exception {
        Map<String, TimeFilter> timeFilters = createTimeFilters(ApplicationType.STB, XHOME);
        String url = "/" + QueryConstants.QUERIES_FILTERS_TIME;
        performGetWithApplication(url, XHOME, Lists.newArrayList(new TimeFilterWrapper(timeFilters.get(XHOME))));

        performGetWithApplication(url, STB, Lists.newArrayList(new TimeFilterWrapper(timeFilters.get(STB))));

        performGetWithApplication(url, "", Lists.newArrayList(new TimeFilterWrapper(timeFilters.get(STB))));
    }

    @Test
    public void saveTimeFilterWithWrongApplicationType() throws Exception {
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        envModelRuleService.save(envModelRuleBean, ApplicationType.STB);
        TimeFilterWrapper timeFilter = new TimeFilterWrapper(createTimeFilter());
        String url = "/" + QueryConstants.UPDATE_FILTERS_TIME;
        performPostWithWrongApplicationType("/" + QueryConstants.UPDATE_FILTERS_TIME, timeFilter);
    }

    TimeFilter createTimeFilter() {
        TimeFilter timeFilter = new TimeFilter();
        timeFilter.setName("timeFilter");
        timeFilter.setId("timeFilterId");
        timeFilter.setNeverBlockHttpDownload(true);
        timeFilter.setNeverBlockRebootDecoupled(true);
        timeFilter.setIpWhitelist(createAndSaveDefaultIpAddressGroupExtended());

        timeFilter.setStart(new LocalTime(2, 2));
        timeFilter.setEnd(new LocalTime(2, 2).plusHours(1));
        timeFilter.setEnvModelWhitelist(new EnvModelRuleBean());
        timeFilter.getEnvModelWhitelist().setEnvironmentId(defaultEnvironmentId.toUpperCase());
        timeFilter.getEnvModelWhitelist().setModelId(defaultModelId.toUpperCase());
        return timeFilter;
    }

    Map<String, TimeFilter> createTimeFilters(String stbName, String xhomeName) {
        Map<String, TimeFilter> timeFilters = new HashMap<>();
        TimeFilter timeFilter1 = createTimeFilter();
        timeFilter1.setId(UUID.randomUUID().toString());
        timeFilter1.setName(stbName);
        timeFilterService.save(timeFilter1, ApplicationType.STB);
        timeFilters.put(ApplicationType.STB, timeFilter1);

        TimeFilter timeFilter2 = createTimeFilter();
        timeFilter2.setId(UUID.randomUUID().toString());
        timeFilter2.setName(xhomeName);
        timeFilterService.save(timeFilter2, XHOME);
        timeFilters.put(XHOME, timeFilter2);

        return timeFilters;
    }

    IpFilter createIpFilter() {
        IpFilter ipFilter = new IpFilter();
        ipFilter.setName("testFilter");
        ipFilter.setId("testId");
        ipFilter.setIpAddressGroup(createAndSaveDefaultIpAddressGroupExtended());
        return ipFilter;
    }

    private Map<String, IpFilter> createIpFilters(String stbName, String xhomeName) throws Exception {
        Map<String, IpFilter> ipFilters = new HashMap<>();
        IpFilter ipFilter1 = createIpFilter();
        ipFilter1.setId(UUID.randomUUID().toString());
        ipFilter1.setName(stbName);
        ipFilterService.save(ipFilter1, ApplicationType.STB);
        ipFilters.put(ApplicationType.STB, ipFilter1);

        IpFilter ipFilter2 = createIpFilter();
        ipFilter2.setId(UUID.randomUUID().toString());
        ipFilter2.setName(xhomeName);
        ipFilterService.save(ipFilter2, XHOME);
        ipFilters.put(XHOME, ipFilter2);

        return ipFilters;
    }

    private PercentFilterValue createPercentFilter() {
        PercentFilterValue percentFilterValue = new PercentFilterValue();
        percentFilterValue.setId(PercentFilterValue.SINGLETON_ID);
        percentFilterValue.setPercentage(50);
        percentFilterValue.setWhitelist(createAndSaveDefaultIpAddressGroupExtended());
        percentFilterValue.setEnvModelPercentages(createEnvModelPercentages());
        return percentFilterValue;
    }

    private Map<String, EnvModelPercentage> createEnvModelPercentages() {
        Map<String, EnvModelPercentage> map = new HashMap<>();
        EnvModelPercentage envModelPercentage = createEnvModelPercentage();
        EnvModelRuleBean envModelRuleBean = createEnvModelRuleBean();
        map.put(envModelRuleBean.getName(), envModelPercentage);
        return map;
    }

    private EnvModelPercentage createEnvModelPercentage() {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig.setId(defaultFirmwareConfigId);
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        EnvModelPercentage envModelPercentage = new EnvModelPercentage();
        envModelPercentage.setActive(true);
        envModelPercentage.setWhitelist(createIpAddressGroupExtended("envModelWhiteList", Sets.newHashSet("12.12.12.12", "13.13.13.13")));
        envModelPercentage.setRebootImmediately(true);
        envModelPercentage.setFirmwareCheckRequired(true);
        createFirmwareConfig(defaultFirmwareConfigId);
        envModelPercentage.setLastKnownGood(defaultFirmwareConfigId);
        envModelPercentage.setIntermediateVersion(defaultFirmwareConfigId);
        envModelPercentage.setPercentage(33);
        envModelPercentage.setFirmwareVersions(Collections.singleton("1.2.3"));
        return envModelPercentage;
    }

    private EnvModelPercentage createEnvModelPercentage(FirmwareConfig firmwareConfig) {
        EnvModelPercentage envModelPercentage = createEnvModelPercentage();
        envModelPercentage.setFirmwareVersions(Sets.newHashSet(firmwareConfig.getFirmwareVersion()));
        envModelPercentage.setIntermediateVersion(firmwareConfig.getFirmwareVersion());
        envModelPercentage.setLastKnownGood(firmwareConfig.getFirmwareVersion());
        envModelPercentage.setActive(true);
        return envModelPercentage;
    }

    private EnvModelRuleBean createEnvModelRuleBean() {
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        FirmwareRule rule = envModelRuleService.convertModelRuleBeanToFirmwareRule(envModelRuleBean);
        firmwareRuleDao.setOne(rule.getId(), rule);
        return envModelRuleBean;
    }

    private Map<String, PercentFilterValue> createAndSavePercentFilters() throws Exception {
        Map<String, PercentFilterValue> percentFilters = new HashMap<>();
        FirmwareConfig firmwareConfigStb = createAndSaveFirmwareConfigByApplicationType(ApplicationType.STB);
        percentFilters.put(ApplicationType.STB, createAndSavePercentFilter(ApplicationType.STB, 45, firmwareConfigStb));
        FirmwareConfig firmwareConfigXhome = createAndSaveFirmwareConfigByApplicationType(XHOME);

        percentFilters.put(XHOME, createAndSavePercentFilter(XHOME, 60, firmwareConfigXhome));
        return percentFilters;
    }

    private PercentFilterValue createPercentFilter(String applicationType, Integer globalPercentage, FirmwareConfig firmwareConfig) {
        PercentFilterValue percentFilter = new PercentFilterValue();
        percentFilter.setPercentage(globalPercentage);
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        envModelRuleBean.setFirmwareConfig(firmwareConfig);
        envModelRuleBean.setName(applicationType);
        envModelRuleBean.setId(UUID.randomUUID().toString());
        envModelRuleService.save(envModelRuleBean, applicationType);
        EnvModelPercentage envModelPercentage = createEnvModelPercentage(firmwareConfig);
        envModelPercentage.setWhitelist(null);
        percentFilter.setEnvModelPercentages(Collections.singletonMap(envModelRuleBean.getName(), envModelPercentage));

        return percentFilter;
    }

    private PercentFilterValue createAndSavePercentFilter(String applicationType, Integer globalPercentage, FirmwareConfig firmwareConfig) {
        PercentFilterValue percentFilter = createPercentFilter(applicationType, globalPercentage, firmwareConfig);
        percentFilterService.save(percentFilter, applicationType);
        return percentFilter;
    }
}