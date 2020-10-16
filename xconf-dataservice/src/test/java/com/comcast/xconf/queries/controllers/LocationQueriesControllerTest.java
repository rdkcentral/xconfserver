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

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.DownloadLocationFilter;
import com.comcast.xconf.estbfirmware.DownloadLocationFilterService;
import com.comcast.xconf.estbfirmware.DownloadLocationRoundRobinFilterValue;
import com.comcast.xconf.estbfirmware.SingletonFilterValue;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.estbfirmware.factory.TemplateFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.queries.QueryConstants;
import com.comcast.xconf.queries.beans.DownloadLocationFilterWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.*;

import static com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation.IS;
import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static com.comcast.xconf.queries.QueriesHelper.nullifyUnwantedFields;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocationQueriesControllerTest extends BaseQueriesControllerTest {

    @Autowired
    DownloadLocationFilterService downloadLocationFilterService;

    @Autowired
    CachedSimpleDao<String, SingletonFilterValue> singletonFilterValueDAO;

    @Autowired
    protected TemplateFactory templateFactory;

    @Test
    public void testGetLocationFilters() throws Exception {
        DownloadLocationFilter filter = createLocationFilter();

        downloadLocationFilterService.save(filter, ApplicationType.STB);

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_LOCATION, Collections.singleton(new DownloadLocationFilterWrapper(filter)));
    }

    @Test
    public void getLocationFiltersByApplicationType() throws Exception {
        Map<String, DownloadLocationFilter> downloadLocationFilters = createAndSaveDownloadLocationFilters(ApplicationType.STB, XHOME);

        String url = "/" + QueryConstants.QUERIES_FILTERS_LOCATION;
        Set<DownloadLocationFilterWrapper> stbExpectedResult = Collections.singleton(new DownloadLocationFilterWrapper(downloadLocationFilters.get(STB)));
        performGetWithApplication(url, STB, stbExpectedResult);

        performGetWithApplication(url, "", stbExpectedResult);

        Set<DownloadLocationFilterWrapper> xhomeExpectedResult = Collections.singleton(new DownloadLocationFilterWrapper(downloadLocationFilters.get(XHOME)));
        performGetWithApplication(url, XHOME, xhomeExpectedResult);
    }

    @Test
    public void testGetLocationFilterByName() throws Exception {
        DownloadLocationFilter filter = createLocationFilter();
        downloadLocationFilterService.save(filter, ApplicationType.STB);
        DownloadLocationFilterWrapper downloadLocationFilterWrapper = new DownloadLocationFilterWrapper(filter);

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_LOCATION + "/" + filter.getName(), downloadLocationFilterWrapper);
    }

    @Test
    public void getLocationFilterByNameAndApplicationType() throws Exception {
        Map<String, DownloadLocationFilter> downloadLocationFilters = createAndSaveDownloadLocationFilters(ApplicationType.STB, ApplicationType.STB);

        String url = "/" + QueryConstants.QUERIES_FILTERS_LOCATION + "/" + ApplicationType.STB;
        DownloadLocationFilterWrapper xhomeExpectedResult = new DownloadLocationFilterWrapper(downloadLocationFilters.get(XHOME));
        performGetWithApplication(url, XHOME, xhomeExpectedResult);

        DownloadLocationFilterWrapper stbExpectedResult = new DownloadLocationFilterWrapper(downloadLocationFilters.get(STB));
        performGetWithApplication(url, STB, stbExpectedResult);

        performGetWithApplication(url, "", stbExpectedResult);

    }

    @Test
    public void createLocationFilterWithWrongApplicationType() throws Exception {
        DownloadLocationFilter filter = createLocationFilter();
        DownloadLocationFilterWrapper downloadLocationFilterWrapper = new DownloadLocationFilterWrapper(filter);

        String url = "/" + QueryConstants.UPDATE_FILTERS_LOCATION;
        performPostWithWrongApplicationType(url, downloadLocationFilterWrapper);
    }

    @Test
    public void createLocationFilterByApplicationType() throws Exception {
        DownloadLocationFilter filter = createLocationFilter();
        DownloadLocationFilterWrapper downloadLocationFilterWrapper = new DownloadLocationFilterWrapper(filter);

        String url = "/" + QueryConstants.UPDATE_FILTERS_LOCATION;
        performPostWithApplication(url, XHOME, downloadLocationFilterWrapper, downloadLocationFilterWrapper);

        assertEquals(1, downloadLocationFilterService.getByApplicationType(ApplicationType.XHOME).size());
        assertEquals(0, downloadLocationFilterService.getByApplicationType(ApplicationType.STB).size());
    }

    @Test
    public void getLocationFilterByWrongName() throws Exception {
        String wrongFilterName = "wrongName";

        mockMvc.perform(get("/" + QueryConstants.QUERIES_FILTERS_LOCATION + "/" + wrongFilterName)
                .param("version", "3.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateLocationFilter() throws Exception {
        DownloadLocationFilter filter = createLocationFilter();
        DownloadLocationFilterWrapper downloadLocationFilterWrapper = new DownloadLocationFilterWrapper(filter);

        mockMvc.perform(post("/" + QueryConstants.UPDATE_FILTERS_LOCATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(downloadLocationFilterWrapper)))
                .andExpect(status().isOk());

        DownloadLocationFilter filterFromDB = downloadLocationFilterService.getOneDwnLocationFilterFromDBById(filter.getId());
        assertEquals(downloadLocationFilterWrapper, new DownloadLocationFilterWrapper(filterFromDB));
    }

    @Test
    public void testDeleteLocationFilter() throws Exception {
        DownloadLocationFilter filter = createLocationFilter();
        downloadLocationFilterService.save(filter, ApplicationType.STB);

        mockMvc.perform(
                delete("/" + QueryConstants.DELETE_FILTERS_LOCATION + "/" + filter.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Assert.assertNull(downloadLocationFilterService.getOneDwnLocationFilterFromDBById(filter.getId()));
    }

    @Test
    public void deleteLocationFilterByNameAndApplicationType() throws Exception {
        createAndSaveDownloadLocationFilters(ApplicationType.STB, ApplicationType.XHOME);
        mockMvc.perform(delete("/" + QueryConstants.DELETE_FILTERS_LOCATION + "/" + ApplicationType.XHOME)
                .param("applicationType", ApplicationType.XHOME))
                .andExpect(status().isNoContent());

        assertEquals(1, downloadLocationFilterService.getByApplicationType(ApplicationType.STB).size());
        assertEquals(0, downloadLocationFilterService.getByApplicationType(ApplicationType.XHOME).size());
    }

    @Test
    public void testGetDownloadLocationRoundRobinFilter() throws Exception {
        DownloadLocationRoundRobinFilterValue filter = createDefaultRoundRobinFilter();
        singletonFilterValueDAO.setOne(DownloadLocationRoundRobinFilterValue.SINGLETON_ID, filter);
        filter = nullifyUnwantedFields(filter);

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_DOWNLOADLOCATION, filter);
    }

    @Test
    public void getRoundRobinFilterByApplicationType() throws Exception {
        Map<String, DownloadLocationRoundRobinFilterValue> roundRobinFilters = createAndSaveRoundRobinFilters();
        String url = "/" + QueryConstants.QUERIES_FILTERS_DOWNLOADLOCATION;
        DownloadLocationRoundRobinFilterValue roundRobinFilterValue = nullifyUnwantedFields(roundRobinFilters.get(ApplicationType.XHOME));

        performGetWithApplication(url, XHOME, roundRobinFilterValue);
    }

    @Test
    public void updateRoundRobinFilterByApplicationType() throws Exception {
        DownloadLocationRoundRobinFilterValue stbFilter = createDefaultRoundRobinFilter();
        singletonFilterValueDAO.setOne(DownloadLocationRoundRobinFilterValue.SINGLETON_ID, stbFilter);

        DownloadLocationRoundRobinFilterValue xhomeFilter = createDefaultRoundRobinFilter();
        xhomeFilter.setApplicationType(ApplicationType.XHOME);
        xhomeFilter.setId(XHOME.toUpperCase() + "_" + DownloadLocationRoundRobinFilterValue.SINGLETON_ID);

        String updateUrl = "/" + QueryConstants.UPDATE_FILTERS_DOWNLOADLOCATION;
        performPostWithApplication(updateUrl, XHOME, xhomeFilter, nullifyUnwantedFields(xhomeFilter));

        String getUrl = "/" + QueryConstants.QUERIES_FILTERS_DOWNLOADLOCATION;
        performGetWithApplication(getUrl, XHOME, nullifyUnwantedFields(xhomeFilter));
    }

    @Test
    public void testUpdateDownloadLocationRoundRobinFilter() throws Exception {
        DownloadLocationRoundRobinFilterValue filter = createDefaultRoundRobinFilter();
        singletonFilterValueDAO.setOne(DownloadLocationRoundRobinFilterValue.SINGLETON_ID, filter);
        filter.setHttpLocation("http://www.changedLocation.com");
        filter = nullifyUnwantedFields(filter);

        mockMvc.perform(
                post("/" + QueryConstants.UPDATE_FILTERS_DOWNLOADLOCATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(filter)))
                .andExpect(status().isOk());
    }

    @Test
    public void exceptionIsNotThrownIfFirmwareRuleWithIpAddressIsOperationIsConverted() throws Exception {
        FirmwareRuleTemplate locationTemplate = templateFactory.createDownloadLocationTemplate();
        firmwareRuleTemplateDao.setOne(locationTemplate.getName(), locationTemplate);

        Rule ipRule = Rule.Builder.of(new Condition(RuleFactory.IP, IS, FixedArg.from(defaultIpAddress))).build();
        String id = UUID.randomUUID().toString();
        FirmwareRule firmwareRule = createFirmwareRule(id, locationTemplate.getTemplateId(), locationTemplate.getApplicableAction(), ipRule);
        firmwareRule.setApplicationType(STB);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        performRequestAndVerifyResponse(QueryConstants.QUERIES_FILTERS_LOCATION, new ArrayList<>());
    }

    private DownloadLocationFilter createLocationFilter() throws Exception {
        DownloadLocationFilter filter = createDefaultDownloadLocationFilter();
        filter.setId("filterId");
        filter.setName("dlFilterName");
        // rule may be only with tftp or http
        filter.setHttpLocation(null);
        filter.setForceHttp(false);
        // we don't store environments and models
        filter.setEnvironments(new HashSet<String>());
        filter.setModels(new HashSet<String>());
        return filter;
    }

    private Map<String, DownloadLocationFilter> createAndSaveDownloadLocationFilters(String stbName, String xhomeName) throws Exception {
        Map<String, DownloadLocationFilter> downloadLocationFilters = new HashMap<>();
        DownloadLocationFilter locationFilter1 = createLocationFilter();
        locationFilter1.setName(stbName);
        locationFilter1.setId(UUID.randomUUID().toString());
        downloadLocationFilterService.save(locationFilter1, ApplicationType.STB);
        downloadLocationFilters.put(ApplicationType.STB, locationFilter1);

        DownloadLocationFilter locationFilter2 = createLocationFilter();
        locationFilter2.setId(UUID.randomUUID().toString());
        locationFilter2.setName(xhomeName);
        downloadLocationFilterService.save(locationFilter2, XHOME);
        downloadLocationFilters.put(XHOME, locationFilter2);

        return downloadLocationFilters;
    }

    private Map<String, DownloadLocationRoundRobinFilterValue> createAndSaveRoundRobinFilters() throws Exception {
        Map<String, DownloadLocationRoundRobinFilterValue> roundRobinFilters = new HashMap<>();
        DownloadLocationRoundRobinFilterValue roundRobinFilter1 = createDefaultRoundRobinFilter();
        roundRobinFilter1.setId(DownloadLocationRoundRobinFilterValue.SINGLETON_ID);
        singletonFilterValueDAO.setOne(roundRobinFilter1.getId(), roundRobinFilter1);
        roundRobinFilters.put(ApplicationType.STB, roundRobinFilter1);

        DownloadLocationRoundRobinFilterValue roundRobinFilter2 = createDefaultRoundRobinFilter();
        roundRobinFilter2.setId(ApplicationType.XHOME.toUpperCase() + "_" + DownloadLocationRoundRobinFilterValue.SINGLETON_ID);
        roundRobinFilter2.setApplicationType(ApplicationType.XHOME);
        singletonFilterValueDAO.setOne(roundRobinFilter2.getId(), roundRobinFilter2);
        roundRobinFilters.put(ApplicationType.XHOME, roundRobinFilter2);

        return roundRobinFilters;
    }
}