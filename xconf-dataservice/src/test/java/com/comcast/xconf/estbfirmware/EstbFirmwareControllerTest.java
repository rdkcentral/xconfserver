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
 * <p/>
 * Author: Stanislav Menshykov
 * Created: 12/3/15  2:02 PM
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.hydra.astyanax.config.XconfSpecificConfig;
import com.comcast.xconf.ConfigNames;
import com.comcast.xconf.Environment;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.estbfirmware.converter.TimeFilterConverter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.estbfirmware.factory.TemplateFactory;
import com.comcast.xconf.estbfirmware.util.LogsCompatibilityUtils;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.DefinePropertiesAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.queries.QueryConstants;
import com.comcast.xconf.queries.beans.DownloadLocationFilterWrapper;
import com.comcast.xconf.queries.beans.PercentFilterWrapper;
import com.comcast.xconf.queries.beans.TimeFilterWrapper;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;

import java.util.*;

import static com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation.IS;
import static com.comcast.xconf.estbfirmware.factory.RuleFactory.MODEL;
import static com.comcast.xconf.firmware.ApplicationType.SKY;
import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.util.RequestUtil.XCONF_HTTP_HEADER;
import static com.comcast.xconf.util.RequestUtil.XCONF_HTTP_VALUE;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EstbFirmwareControllerTest extends BaseQueriesControllerTest {

    @Autowired
    private IpRuleService ipRuleService;
    @Autowired
    private IpFilterService ipFilterService;
    @Autowired
    private EnvModelRuleService envModelRuleService;
    @Autowired
    private TimeFilterConverter timeFilterConverter;
    @Autowired
    private EstbFirmwareLogger estbFirmwareLogger;
    @Autowired
    private TemplateFactory templateFactory;
    @Autowired
    private XconfSpecificConfig xconfSpecificConfig;
    @Autowired
    private FirmwarePermissionService firmwarePermissionService;
    @Autowired
    private ListingDao<String, String, ConfigChangeLog> configChangeLogDAO;

    @Before
    public void initPermissionService() {
        when(firmwarePermissionService.getReadApplication()).thenReturn(STB);
        when(firmwarePermissionService.getWriteApplication()).thenReturn(STB);
        when(firmwarePermissionService.canWrite()).thenReturn(true);
    }

    @Test
    public void getConfigForIpRuleWithNoFilters() throws Exception {
        createAndSaveDefaultIpRuleBean();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void getConfigAndVerifyConfigChangeLog() throws Exception {
        configChangeLogDAO.deleteAll(defaultMacAddress);
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();

        EstbFirmwareContext defaultContext = createDefaultContext();
        String firmwareResponse = mockMvc.perform(postContext("/xconf/swu/stb", defaultContext)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        waitUntilLogIsWrittenInAsyncWay();

        performLogRequestAndVerify("/estbfirmware/lastlog", ipRuleBean, firmwareResponse);
        performLogRequestAndVerify("/estbfirmware/changelogs", ipRuleBean, firmwareResponse);
    }

    private void performLogRequestAndVerify(String api, IpRuleBean ipRuleBean, String firmwareResponse) throws Exception {
        String content = mockMvc.perform(
                get(api).param("mac", defaultMacAddress)).andReturn().getResponse().getContentAsString();
        assertTrue(content.contains(getExplanationForRule(ipRuleService.convertIpRuleBeanToFirmwareRule(ipRuleBean))));
        assertTrue(content.contains(firmwareResponse));
    }

    private void waitUntilLogIsWrittenInAsyncWay() throws InterruptedException {
        Thread.sleep(500);
    }

    @Test
    public void getConfigForMacRuleWithNoFilters() throws Exception {
        saveMacRuleBean(createDefaultMacRuleBean());

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void skyConfigIsUsedWhenPartnerStartsWithSky() throws Exception {
        xconfSpecificConfig.setReadSkyApplicationTypeFromPartnerParam(true);
        FirmwareConfig skyFirmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, defaultModelId, defaultFirmwareDownloadProtocol, SKY);
        MacRuleBean skyMacRuleBean = createDefaultMacRuleBean();
        skyMacRuleBean.setFirmwareConfig(skyFirmwareConfig);
        macRuleService.save(skyMacRuleBean, SKY);

        EstbFirmwareContext defaultContext = createDefaultContext();
        defaultContext.setPartnerId("sky-italy");
        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", defaultContext)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
        xconfSpecificConfig.setReadSkyApplicationTypeFromPartnerParam(false);
    }

    @Test
    public void skyConfigIsNotUsedForDefaultContext() throws Exception {
        xconfSpecificConfig.setReadSkyApplicationTypeFromPartnerParam(true);
        MacRuleBean skyMacRuleBean = createDefaultMacRuleBean();
        macRuleService.save(skyMacRuleBean, SKY);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();
        assertTrue(actualResult.contains(getExplanationForNoMatchedRules()));
        xconfSpecificConfig.setReadSkyApplicationTypeFromPartnerParam(false);
    }

    @Test
    public void getConfigForMacRuleWithNoFiltersAndContextVersionSameAsDefault() throws Exception {
        saveMacRuleBean(createDefaultMacRuleBean());

        EstbFirmwareContext defaultContext = createDefaultContext();
        defaultContext.setFirmwareVersion(defaultFirmwareVersion);
        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", defaultContext)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void getConfigForEnvModelRuleWithNoFilters() throws Exception {
        createAndSaveDefaultEnvModelRuleBean();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void ruleWithOnlyModelHasLowerPriority() throws Exception {
        createAndSaveDefaultEnvModelRuleBean();

        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        envModelRuleBean.setEnvironmentId(null);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig("modelOnlyFirmwareConfig", defaultModelId, FirmwareConfig.DownloadProtocol.http, STB);
        envModelRuleBean.setFirmwareConfig(firmwareConfig);
        saveEnvModelRuleBean(envModelRuleBean);

        mockMvc.perform(
                post("/xconf/swu/stb")
                        .param("param", "value")
                        .param("model", defaultModelId)
                        .param("env", defaultEnvironmentId)
                        .param("firmwareVersion", defaultFirmwareVersion)
                        .param("eStbMac", defaultMacAddress))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firmwareVersion").value(defaultFirmwareVersion));
    }

    @Test
    public void resultIsBlockedByIpFilter() throws Exception {
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();
        IpFilter ipFilter = createDefaultIpFilter();
        saveIpFilter(ipFilter);
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        verifyExplanation(ipRuleService.convertIpRuleBeanToFirmwareRule(ipRuleBean),
                ipRuleBean.getFirmwareConfig(), ipFilterService.convertIpFilterToFirmwareRule(ipFilter), actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenIpFilterNotInRange() throws Exception {
        String notDefaultIpAddress = "2.2.2.2";
        createAndSaveDefaultIpRuleBean();
        saveIpFilter(createIpFilter(defaultIpFilterId, "filterName", Collections.singleton(notDefaultIpAddress)));
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenIpFilterInBypassFilters() throws Exception {
        createAndSaveDefaultIpRuleBean();
        saveIpFilter(createDefaultIpFilter());
        EstbFirmwareContext context = createDefaultContext();
        context.setBypassFilters(TemplateNames.IP_FILTER);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void resultIsBlockedByTimeFilter() throws Exception {
        EstbFirmwareContext context = createContextWithTime(12);
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();
        TimeFilter timeFilter = createAndSaveTimeFilterFrom9to15();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        verifyExplanation(ipRuleService.convertIpRuleBeanToFirmwareRule(ipRuleBean),
                ipRuleBean.getFirmwareConfig(), timeFilterConverter.convert(timeFilter), actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenTimeFilterNotInRange() throws Exception {
        EstbFirmwareContext context = createContextWithTime(8);
        createAndSaveDefaultIpRuleBean();
        createAndSaveTimeFilterFrom9to15();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context))
                .andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenTimeFilterInBypassFilters() throws Exception {
        EstbFirmwareContext context = createContextWithTime(12);
        context.setBypassFilters(TemplateNames.TIME_FILTER);
        createAndSaveDefaultIpRuleBean();
        createAndSaveTimeFilterFrom9to15();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenTimeFilterNeverBlockRebootDecoupledIsTrue() throws Exception {
        EstbFirmwareContext context = createContextWithTime(12);
        context.setCapabilities(Collections.singletonList(Capabilities.rebootDecoupled.toString()));
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();
        TimeFilter timeFilter = createTimeFilterFrom9to15();
        timeFilter.setNeverBlockRebootDecoupled(true);
        saveTimeFilter(timeFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = ipRuleBean.getFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenTimeFilterNeverBlockHttpDownloadIsTrue() throws Exception {
        EstbFirmwareContext context = createContextWithTime(12);
        createAndSaveDefaultIpRuleBean();
        TimeFilter timeFilter = createTimeFilterFrom9to15();
        timeFilter.setNeverBlockHttpDownload(true);
        saveTimeFilter(timeFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void resultIsBlocked_WhenTimeFilterNeverBlockHttpDownloadIsTrueButDownloadProtocolIsTftp() throws Exception {
        EstbFirmwareContext context = createContextWithTime(12);
        defaultFirmwareDownloadProtocol = FirmwareConfig.DownloadProtocol.tftp;
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();
        TimeFilter timeFilter = createTimeFilterFrom9to15();
        timeFilter.setNeverBlockHttpDownload(true);
        saveTimeFilter(timeFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyExplanation(ipRuleService.convertIpRuleBeanToFirmwareRule(ipRuleBean),
                ipRuleBean.getFirmwareConfig(), timeFilterConverter.convert(timeFilter), actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenTimeFilterTimeIsLocal() throws Exception {
        EstbFirmwareContext context = createContextWithTime(12);
        createAndSaveDefaultIpRuleBean();
        TimeFilter timeFilter = createTimeFilterFrom9to15();
        timeFilter.setLocalTime(true);
        saveTimeFilter(timeFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenTimeFilterIsUTCButContextTimeIsLocal() throws Exception {
        EstbFirmwareContext context = createContextWithTime(12);
        context.setTimeZoneOffset("-01:00");
        createAndSaveDefaultIpRuleBean();
        TimeFilter timeFilter = createTimeFilterFrom9to15();
        saveTimeFilter(timeFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void resultIsBlocked_WhenTimeFilterAndContextTimeBothInLocalFormat() throws Exception {
        EstbFirmwareContext context = createContextWithTime(12);
        context.setTimeZoneOffset("-01:00");
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();
        TimeFilter timeFilter = createTimeFilterFrom9to15();
        timeFilter.setLocalTime(true);
        saveTimeFilter(timeFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyExplanation(ipRuleService.convertIpRuleBeanToFirmwareRule(ipRuleBean),
                ipRuleBean.getFirmwareConfig(), timeFilterConverter.convert(timeFilter), actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenContextIsInTimeFilterEnvModelWhiteList() throws Exception {
        EstbFirmwareContext context = createContextWithTime(12);
        createAndSaveDefaultIpRuleBean();
        TimeFilter timeFilter = createTimeFilterFrom9to15();
        timeFilter.setEnvModelWhitelist(createAndSaveEnvModelRuleBean(defaultEnvModelId, defaultEnvironmentId, defaultModelId,
                defaultFirmwareVersion, defaultFirmwareDownloadProtocol, STB));
        saveTimeFilter(timeFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenContextIsInTimeFilterIpWhiteList() throws Exception {
        EstbFirmwareContext context = createContextWithTime(12);
        createAndSaveDefaultIpRuleBean();
        TimeFilter timeFilter = createTimeFilterFrom9to15();
        timeFilter.setIpWhitelist(createAndSaveIpAddressGroupExtended(Collections.singleton(defaultIpAddress)));
        saveTimeFilter(timeFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void configIsSetToRebootImmediatelyByContextForceFilter() throws Exception {
        EstbFirmwareContext context = createDefaultContext();
        context.setForceFilters(TemplateNames.REBOOT_IMMEDIATELY_FILTER);
        createAndSaveDefaultIpRuleBean();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setRebootImmediately(true);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void configIsSetToRebootImmediatelyByRebootImmediatelyFilter_WhenMacsAreEqual() throws Exception {
        RebootImmediatelyFilter rebootImmediatelyFilter = createDefaultRebootImmediatelyFilter();
        rebootImmediatelyFilter.setMacAddresses(defaultMacAddress);

        verifyRebootImmediatelyValueInConfig(true, rebootImmediatelyFilter);
    }

    @Test
    public void configIsSetToRebootImmediatelyByRebootImmediatelyFilter_WhenIpIsInRange() throws Exception {
        RebootImmediatelyFilter rebootImmediatelyFilter = createDefaultRebootImmediatelyFilter();
        rebootImmediatelyFilter.setIpAddressGroups(Collections.singleton(createDefaultIpAddressGroup()));

        verifyRebootImmediatelyValueInConfig(true, rebootImmediatelyFilter);
    }

    @Test
    public void configIsSetToRebootImmediatelyByRebootImmediatelyFilter_WhenModelsAreEqual() throws Exception {
        RebootImmediatelyFilter rebootImmediatelyFilter = createDefaultRebootImmediatelyFilter();
        rebootImmediatelyFilter.setModels(Collections.singleton(createAndSaveModel(defaultModelId).getId()));

        verifyRebootImmediatelyValueInConfig(true, rebootImmediatelyFilter);
    }

    @Test
    public void configIsSetToRebootImmediatelyByRebootImmediatelyFilter_WhenEnvironmentsAreEqual() throws Exception {
        RebootImmediatelyFilter rebootImmediatelyFilter = createDefaultRebootImmediatelyFilter();
        rebootImmediatelyFilter.setEnvironments(Collections.singleton(createAndSaveEnvironment(defaultEnvironmentId).getId()));

        verifyRebootImmediatelyValueInConfig(true, rebootImmediatelyFilter);
    }

    @Test
    public void configIsSetToRebootImmediatelyFalse_WhenFilterIsEmpty() throws Exception {
        RebootImmediatelyFilter rebootImmediatelyFilter = createDefaultRebootImmediatelyFilter();

        verifyRebootImmediatelyValueInConfig(false, rebootImmediatelyFilter);
    }

    @Test
    public void resultIsBlocked_WhenPercentFilterPercentageIs0AndWhiteListIsNull() throws Exception {
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();
        PercentFilterValue percentFilterValue = createPercentFilter(null, 0, null);
        savePercentFilter(percentFilterValue);
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyExplanation(ipRuleService.convertIpRuleBeanToFirmwareRule(ipRuleBean),
                ipRuleBean.getFirmwareConfig(), percentFilterValue, actualResult);
    }

    @Test
    public void resultNotBlocked_WhenPercentFilterPercentageIs0AndWhiteListIsNullButPercentFilterIsInBypassFilters() throws Exception {
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();
        savePercentFilter(createPercentFilter(null, 0, null));
        EstbFirmwareContext context = createDefaultContext();
        context.setBypassFilters(EstbFirmwareRuleBase.PERCENT_FILTER_NAME);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyFirmwareConfig(ipRuleBean.getFirmwareConfig(), actualResult);
    }

    @Test
    public void resultIsBlocked_WhenPercentFilterPercentageIs0AndContextIpIsNotInWhiteList() throws Exception {
        String ipNotInWhiteList = "99.99.99.99";
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();
        PercentFilterValue percentFilterValue = createPercentFilter(
                createAndSaveIpAddressGroupExtended(ipNotInWhiteList, Collections.singleton(ipNotInWhiteList)),
                0, null);
        savePercentFilter(percentFilterValue);
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyExplanation(ipRuleService.convertIpRuleBeanToFirmwareRule(ipRuleBean),
                ipRuleBean.getFirmwareConfig(), percentFilterValue, actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenPercentFilterPercentageIs100() throws Exception {
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();
        savePercentFilter(createPercentFilter(null, 100, null));
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyFirmwareConfig(ipRuleBean.getFirmwareConfig(), actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenContextIpIsInPercentFilterWhiteList() throws Exception {
        IpRuleBean ipRuleBean = createAndSaveDefaultIpRuleBean();
        savePercentFilter(createPercentFilter(createAndSaveIpAddressGroupExtended(Collections.singleton(defaultIpAddress)), 100, null));
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyFirmwareConfig(ipRuleBean.getFirmwareConfig(), actualResult);
    }

    @Test
    public void resultIsBlocked_WhenEnvModelPercentageIs0AndWhiteListIsNull() throws Exception {
        EnvModelRuleBean envModelRuleBean = createAndSaveDefaultEnvModelRuleBean();
        PercentFilterValue percentFilterValue = createPercentFilter(null, 100,
                Collections.singletonMap(envModelRuleBean.getName(), createDefaultEnvModelPercentage(0)));
        savePercentFilter(percentFilterValue);
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyExplanationForDistributionRule(envModelRuleService.convertModelRuleBeanToFirmwareRule(envModelRuleBean), actualResult);
    }

    @Test
    public void resultIsBlocked_WhenEnvModelPercentageIs0AndContextIpIsNotInWhiteList() throws Exception {
        String ipNotInWhiteList = "99.99.99.99";
        EnvModelRuleBean envModelRuleBean = createAndSaveDefaultEnvModelRuleBean();
        EnvModelPercentage envModelPercentage = createDefaultEnvModelPercentage(0);
        envModelPercentage.setWhitelist(createAndSaveIpAddressGroupExtended(Collections.singleton(ipNotInWhiteList)));
        PercentFilterValue percentFilterValue = createPercentFilter(null, 100,
                Collections.singletonMap(envModelRuleBean.getName(), envModelPercentage));
        savePercentFilter(percentFilterValue);
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyExplanationForDistributionRule(envModelRuleService.convertModelRuleBeanToFirmwareRule(envModelRuleBean), actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenEnvModelPercentageIs100() throws Exception {
        String ipNotInWhiteList = "99.99.99.99";
        EnvModelRuleBean envModelRuleBean = createAndSaveDefaultEnvModelRuleBean();
        EnvModelPercentage envModelPercentage = createDefaultEnvModelPercentage(100);
        envModelPercentage.setWhitelist(createAndSaveIpAddressGroupExtended(Collections.singleton(ipNotInWhiteList)));
        savePercentFilter(createPercentFilter(null, 0, Collections.singletonMap(envModelRuleBean.getName(), envModelPercentage)));
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyFirmwareConfig(envModelRuleBean.getFirmwareConfig(), actualResult);
    }

    @Test
    public void resultIsNotBlocked_WhenContextIpIsInEnvModelPercentageWhiteList() throws Exception {
        EnvModelRuleBean envModelRuleBean = createAndSaveDefaultEnvModelRuleBean();
        EnvModelPercentage envModelPercentage = createDefaultEnvModelPercentage(0);
        IpAddressGroupExtended whitelist = createAndSaveIpAddressGroupExtended(Collections.singleton(defaultIpAddress));
        envModelPercentage.setWhitelist(whitelist);
        savePercentFilter(createPercentFilter(whitelist, 0, Collections.singletonMap(envModelRuleBean.getName(), envModelPercentage)));
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyFirmwareConfig(envModelRuleBean.getFirmwareConfig(), actualResult);
    }

    @Test
    public void contextIsSetToRebootImmediatelyByPercentFilter() throws Exception {
        String someFirmwareVersionDifferentFromVersionInContext = "firmwareVersion42";
        EnvModelRuleBean envModelRuleBean = createAndSaveDefaultEnvModelRuleBean();
        EnvModelPercentage envModelPercentage = createDefaultEnvModelPercentage(0);
        envModelPercentage.setRebootImmediately(true);
        envModelPercentage.setFirmwareCheckRequired(true);
        envModelPercentage.setFirmwareVersions(Collections.singleton(someFirmwareVersionDifferentFromVersionInContext));
        savePercentFilter(createPercentFilter(null, 0, Collections.singletonMap(envModelRuleBean.getName(), envModelPercentage)));
        EstbFirmwareContext context = createDefaultContext();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setRebootImmediately(true);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void resultIsNotBlockedByTimeFilter_WhenPercentFilterAddsTimeFilterIntoBypassFilters() throws Exception {
        String someFirmwareVersionDifferentFromVersionInContext = "firmwareVersion42";
        EstbFirmwareContext context = createContextWithTime(12);
        TimeFilter timeFilter = createTimeFilterFrom9to15();
        saveTimeFilter(timeFilter);
        EnvModelRuleBean envModelRuleBean = createAndSaveDefaultEnvModelRuleBean();
        EnvModelPercentage envModelPercentage = createDefaultEnvModelPercentage(0);
        envModelPercentage.setFirmwareCheckRequired(true);
        envModelPercentage.setFirmwareVersions(Collections.singleton(someFirmwareVersionDifferentFromVersionInContext));
        savePercentFilter(createPercentFilter(null, 0, Collections.singletonMap(envModelRuleBean.getName(), envModelPercentage)));

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        verifyFirmwareConfig(envModelRuleBean.getFirmwareConfig(), actualResult);
    }

    @Test
    public void getVersionFromByPercentFilterWithCustomCondition() throws Exception {
        createAndSaveModel(defaultModelId);
        createAndSaveEnvironment(defaultEnvironmentId);
        Rule optionalCondition = Rule.Builder.of(new Condition(new FreeArg(StandardFreeArgType.STRING, "param"), IS, FixedArg.from("value"))).build();
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, defaultModelId, FirmwareConfig.DownloadProtocol.http, STB);
        PercentageBean percentageBean = createPercentageBean("percentageBean", optionalCondition, null, firmwareConfig, 100, STB);
        savePercentageBean(percentageBean);

        mockMvc.perform(
                post("/xconf/swu/stb")
                        .param("param", "value")
                        .param("model", defaultModelId)
                        .param("env", defaultEnvironmentId)
                        .param("firmwareVersion", defaultFirmwareVersion)
                        .param("eStbMac", defaultMacAddress))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firmwareVersion").value(defaultFirmwareVersion));

    }

    @Test
    public void roundRobinFilterSetsFullUrlHttpLocationAndFirmwareDownloadProtocolInConfig() throws Exception {
        defaultFirmwareDownloadProtocol = FirmwareConfig.DownloadProtocol.http;
        EstbFirmwareContext context = createDefaultContext();
        context.setCapabilities(Lists.newArrayList(
                Capabilities.supportsFullHttpUrl.name(),
                Capabilities.RCDL.name()));
        createAndSaveDefaultIpRuleBean();
        createAndSaveDefaultRoundRobinFilter();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();
        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setFirmwareDownloadProtocol(FirmwareConfig.DownloadProtocol.http);
        expectedResult.setFirmwareLocation(defaultHttpsFullUrlLocation);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void roundRobinFilterSetsHttpLocationAndFirmwareDownloadProtocolInConfig() throws Exception {
        defaultFirmwareDownloadProtocol = FirmwareConfig.DownloadProtocol.http;
        EstbFirmwareContext context = createDefaultContext();
        createAndSaveDefaultIpRuleBean();
        createAndSaveDefaultRoundRobinFilter();

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setFirmwareDownloadProtocol(FirmwareConfig.DownloadProtocol.http);
        expectedResult.setFirmwareLocation(defaultHttpLocation);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void roundRobinFilterSetDefaultHttpLocation_WhenContextDoesNotContainRCDLCapability() throws Exception {
        EstbFirmwareContext context = createDefaultContext();
        createAndSaveDefaultIpRuleBean();
        createAndSaveDefaultRoundRobinFilter();
        verifyFilterSetDefaultHttpLocation(context);
    }

    @Test
    public void roundRobinFilterDoesNotSetHttpLocation_WhenFilterHttpLocationIsBlank() throws Exception {
        EstbFirmwareContext context = createDefaultContext();
        DownloadLocationRoundRobinFilterValue roundRobinFilterValue = createDefaultRoundRobinFilter();
        roundRobinFilterValue.setHttpLocation("");

        verifyRoundRobinFilterDoesNotSetHttpLocation(roundRobinFilterValue, context);
    }

    @Test
    public void downloadLocationFilterSetsTftpLocationInConfig_WhenDownloadProtocolIsTftpAndForceHttpIsFalseAndFirmwareLocationIsNotEmpty() throws Exception {
        DownloadLocationFilter locationFilter = createDefaultDownloadLocationFilter();
        locationFilter.setHttpLocation(null);

        verifyDownloadLocationfilterSetsTftpLocationInConfig(locationFilter);
    }

    @Test
    public void downloadLocationFilterSetsTftpLocationAndFirmwareDownloadProtocolInConfig_WhenFilterHttpLocationIsNull() throws Exception {
        DownloadLocationFilter locationFilter = createDefaultDownloadLocationFilter();
        locationFilter.setHttpLocation(null);

        verifyDownloadLocationfilterSetsTftpLocationInConfig(locationFilter);
    }

    @Test
    public void downloadLocationFilterSetsIPv6FirmwareLocationFromRoundRobinFilter_WhenItsOwnIPv6FirmwareLocationIsNull() throws Exception {
        String ipv6FirmwareLocationIpForRoundRobinFilter = "66::66";
        EstbFirmwareContext context = createDefaultContext();
        createAndSaveDefaultIpRuleBean();
        DownloadLocationRoundRobinFilterValue downloadLocationRoundRobinFilterValue = createDefaultRoundRobinFilter();
        downloadLocationRoundRobinFilterValue.setIpv6locations(Collections.singletonList(createLocation(ipv6FirmwareLocationIpForRoundRobinFilter, 100)));
        saveRoundRobinFilter(downloadLocationRoundRobinFilterValue);
        DownloadLocationFilter locationFilter = createDefaultDownloadLocationFilter();
        locationFilter.setHttpLocation(null);
        locationFilter.setIpv6FirmwareLocation(null);
        saveDownloadLocationFilter(locationFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setFirmwareDownloadProtocol(FirmwareConfig.DownloadProtocol.tftp);
        expectedResult.setFirmwareLocation(defaultIpAddress);
        expectedResult.setIpv6FirmwareLocation(ipv6FirmwareLocationIpForRoundRobinFilter);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void downloadLocationFilterSetsHttpLocationAndFirmwareDownloadProtocolInConfig_WhenForceHttpIsTrue() throws Exception {
        DownloadLocationFilter locationFilter = createDefaultDownloadLocationFilter();
        locationFilter.setForceHttp(true);

        verifyDownloadLocationFilterSetsHttpLocationInConfig(locationFilter);
    }

    private void setAllowedFirmwareVersionsAndModels(String firmwareVersion, String modelId) {
        xconfSpecificConfig.setRecoveryFirmwareVersions(firmwareVersion + " " + modelId.toUpperCase());
    }

    @Test
    public void downloadLocationFilterSetsHttpLocationInConfig_WhenFirmwareDownloadProtocolIsHttp() throws Exception {
        DownloadLocationFilter locationFilter = createDefaultDownloadLocationFilter();
        locationFilter.setFirmwareLocation(null);
        locationFilter.setIpv6FirmwareLocation(null);

        verifyDownloadLocationFilterSetsHttpLocationInConfig(locationFilter);
    }

    @Test
    public void downloadLocationFilterSetsHttpLocationAndFirmwareDownloadProtocolInConfig_WhenFilterFirmwareLocationIsNull() throws Exception {
        DownloadLocationFilter locationFilter = createDefaultDownloadLocationFilter();
        locationFilter.setFirmwareLocation(null);
        locationFilter.setIpv6FirmwareLocation(null);

        verifyDownloadLocationFilterSetsHttpLocationInConfig(locationFilter);
    }

    @Test
    public void downloadLocationFilterSetsHttpLocationAndSetsToNullIPv6FirmwareLocationWhichWasSetByRoundRobinFilterPreviously() throws Exception {
        EstbFirmwareContext context = createDefaultContext();
        createAndSaveDefaultIpRuleBean();
        createAndSaveDefaultRoundRobinFilter();
        DownloadLocationFilterWrapper locationFilter = createDefaultDownloadLocationFilter();
        locationFilter.setFirmwareLocation(null);
        locationFilter.setIpv6FirmwareLocation(null);
        saveDownloadLocationFilter(locationFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setFirmwareLocation(defaultHttpLocation);
        expectedResult.setIpv6FirmwareLocation(null);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void noMatchingRuleWasFound() throws Exception {
        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();

        assertTrue(actualResult.contains(getExplanationForNoMatchedRules()));
    }

    @Test
    public void noopRuleWasFound() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        ipRuleBean.setFirmwareConfig(null);
        saveIpRuleBean(ipRuleBean);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();

        assertTrue(actualResult.contains(getExplanationForNoopRule(ipRuleService.convertIpRuleBeanToFirmwareRule(ipRuleBean))));
    }

    @Test
    public void macRuleHasHighestPriorityWhenMatchingRulesWithDifferentTypes() throws Exception {
        createAndSaveIpRuleBean("firmwareConfigFileNameForIpRule");
        MacRuleBean macRuleBean = createAndSaveMacRuleBean("firmwareConfigFileNameForMacRule");
        createAndSaveEnvModelRuleBean("firmwareConfigFileNameForEnvModelRule");

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = macRuleBean.getFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void macRuleHasHighestPriorityWhenMatchingRulesWithDifferentTypes2() throws Exception {
        MacRuleBean macRuleBean = createAndSaveMacRuleBean("firmwareConfigFileNameForMacRule");
        createAndSaveEnvModelRuleBean("firmwareConfigFileNameForEnvModelRule");

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = macRuleBean.getFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void macRuleHasHighestPriorityWhenMatchingRulesWithDifferentTypes3() throws Exception {
        createAndSaveIpRuleBean("firmwareConfigFileNameForIpRule");
        MacRuleBean macRuleBean = createAndSaveMacRuleBean("firmwareConfigFileNameForMacRule");

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = macRuleBean.getFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void ipRuleHasSecondPriorityWhenMatchingRulesWithDifferentTypes() throws Exception {
        IpRuleBean ipRuleBean = createAndSaveIpRuleBean("firmwareConfigFileNameForIpRule");
        createAndSaveEnvModelRuleBean("firmwareConfigFileNameForEnvModelRule");

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = ipRuleBean.getFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void ipRuleHasSecondPriorityWhenMatchingRulesWithDifferentTypes2() throws Exception {
        createAndSaveEnvModelRuleBean("firmwareConfigFileNameForEnvModelRule");
        IpRuleBean ipRuleBean = createAndSaveIpRuleBean("firmwareConfigFileNameForIpRule");

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext())).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = ipRuleBean.getFirmwareConfig();
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    @Test
    public void nonSecureConnection_IsNotAllowedForNonRecoveryModel() throws Exception {
        xconfSpecificConfig.setRecoveryFirmwareVersions("^version$ .*X1.*");
        mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext()).header(XCONF_HTTP_HEADER, XCONF_HTTP_VALUE))
                .andExpect(status().isForbidden());

        xconfSpecificConfig.setRecoveryFirmwareVersions(null);
    }

    @Test
    public void nonSecureConnection_AllowedForRecoveryModels() throws Exception {
        xconfSpecificConfig.setRecoveryFirmwareVersions(contextFirmwareVersion + " " + defaultModelId.toUpperCase());
        mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext()).header(XCONF_HTTP_HEADER, XCONF_HTTP_VALUE))
                .andExpect(status().isNotFound());

        xconfSpecificConfig.setRecoveryFirmwareVersions(null);
    }

    @Test
    public void secureConnection_AlwaysAllowed() throws Exception {
        xconfSpecificConfig.setRecoveryFirmwareVersions("version X1");
        mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext()))
                .andExpect(status().isNotFound());

        xconfSpecificConfig.setRecoveryFirmwareVersions(null);
    }

    @Test
    public void percentFilterWithPartnerHasHigherPriority() throws Exception {
        createAndSaveModel(defaultModelId);
        createAndSaveEnvironment(defaultEnvironmentId);

        FirmwareConfig firmwareConfig1 = createAndSaveFirmwareConfig(defaultFirmwareVersion, defaultModelId, FirmwareConfig.DownloadProtocol.http, STB);
        PercentageBean percentageBean = createPercentageBean("envModelBean", null, null, firmwareConfig1, 100, STB);
        savePercentageBean(percentageBean);

        Rule percentageBeanRule = Rule.Builder.of(new Condition(RuleFactory.PARTNER_ID, IS, FixedArg.from(defaultPartnerId.toUpperCase()))).build();
        FirmwareConfig firmwareConfig2 = createAndSaveFirmwareConfig("firmwareVersion2", defaultModelId, FirmwareConfig.DownloadProtocol.http, STB);
        PercentageBean envModelPartnerBean = createPercentageBean("envModelPartnerBean", percentageBeanRule, null, firmwareConfig2, 100, STB);
        savePercentageBean(envModelPartnerBean);

        EstbFirmwareContext defaultContext = createDefaultContext();
        defaultContext.setPartnerId(defaultPartnerId);
        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", defaultContext)).andReturn().getResponse().getContentAsString();

        verifyFirmwareConfig(firmwareConfig2, actualResult);
    }

    @Test
    public void estbMacIsEmpty_Throws500ByDefault() throws Exception {
        mockMvc.perform(get("/xconf/swu/stb").param("test", "test"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("\"eStbMac should be specified\""));
    }

    @Test
    public void estbMacIsEmpty_Throws500WhenVersionLessThan2() throws Exception {
        mockMvc.perform(
                get("/xconf/swu/stb").param("test", "test").param("version", "1.2"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("\"eStbMac should be specified\""));
    }

    @Test
    public void estbMacIsEmpty_Throws400WhenVersionGreaterThan2() throws Exception {
        mockMvc.perform(
                get("/xconf/swu/stb").param("test", "test").param("version", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("\"eStbMac should be specified\""));
    }

    @Test
    public void getBseConfig() throws Exception {
        IpRuleBean ipRuleBean = createAndSaveIpRuleBean("ipRuleFirmwareConfig");
        DownloadLocationFilter downloadLocationFilter = createDefaultDownloadLocationFilter();
        downloadLocationFilter.setHttpLocation("http://1.1.1.1");
        downloadLocationFilter.setForceHttp(true);
        downloadLocationFilter.setIpv6FirmwareLocation(null);
        saveDownloadLocationFilter(downloadLocationFilter);
        createAndSaveDefaultRoundRobinFilter();

        verifyBseResponse(ipRuleBean);
    }

    @Test
    public void getBseConfigIfIpAddressHasInListOperation() throws Exception {
        IpRuleBean ipRuleBean = createAndSaveIpRuleBean("ipRuleFirmwareConfig");
        createAndSaveDefaultDownloadLocationRule();
        createAndSaveDefaultRoundRobinFilter();

        verifyBseResponse(ipRuleBean);
    }

    @Test
    public void getBseConfigIfIpAddressHasInOperation() throws Exception {
        IpRuleBean ipRuleBean = createAndSaveIpRuleBean("ipRuleFirmwareConfig");
        createAndSaveDownloadLocationRuleWithInOperation();
        createAndSaveDefaultRoundRobinFilter();

        verifyBseResponse(ipRuleBean);
    }

    @Test
    public void getBseConfigIfIpAddressHasIsOperation() throws Exception {
        IpRuleBean ipRuleBean = createAndSaveIpRuleBean("ipRuleFirmwareConfig");
        createAndSaveDownloadLocationRuleWithIsOperation();
        createAndSaveDefaultRoundRobinFilter();

        verifyBseResponse(ipRuleBean);
    }

    @Test
    public void getBseConfigIfIpAddressIsWrong() throws Exception {
        createAndSaveIpRuleBean("ipRuleFirmwareConfig");
        createAndSaveDefaultDownloadLocationRule();
        createAndSaveDefaultRoundRobinFilter();

        mockMvc.perform(
                get("/xconf/swu/bse")
                        .param("ipAddress", "10.10.10.10"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getConfigWhenPartnerCaseInsensitive() throws Exception {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        save(firmwareConfig);
        String partnerId = "PARTNERID";
        FirmwareRule partnerFirmwareRule = createAndSavePartnerFirmwareRule(partnerId, firmwareConfig);
        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId("partnerId");

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();
        verifyFirmwareConfig(firmwareConfig, actualResult);

        context.setPartnerId("PartnerId");
        actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();
        verifyFirmwareConfig(firmwareConfig, actualResult);

        firmwareRuleTemplateDao.deleteOne(partnerFirmwareRule.getType());
    }

    @Test
    public void setRebootImmediatelyTrueIfActivationVersionIsNotAppliedByFirmwareVersion() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());

        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        save(firmwareConfig);
        createAndSaveActivationVersion(defaultPartnerId, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), new HashSet<>());

        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null, firmwareConfig.getFirmwareVersion(), STB);

        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId(defaultPartnerId);
        context.setFirmwareVersion("NOT_AMV_VERSION");

        verifyActivationVersionResponse(context, true);
    }

    @Test
    public void setRebootImmediatelyTrueIfActivationVersionIsNotAppliedByRegExp() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());

        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        save(firmwareConfig);
        createAndSaveActivationVersion(defaultPartnerId, new HashSet<>(), Sets.newHashSet(".*Version"));

        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null, firmwareConfig.getFirmwareVersion(), STB);

        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId(defaultPartnerId);
        context.setFirmwareVersion("NOT_AMV_VERSION");

        verifyActivationVersionResponse(context, true);
    }

    @Test
    public void verifyRebootImmediatelyByActivationVersionIsOverriddenByDefineProperties() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());

        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        save(firmwareConfig);
        createAndSaveActivationVersion(null, new HashSet<>(), Sets.newHashSet(".*Version"));

        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null, firmwareConfig.getFirmwareVersion(), STB);

        Rule modelRule = Rule.Builder.of(new Condition(MODEL, IS, FixedArg.from(model.getId()))).build();
        FirmwareRuleTemplate modelTemplate = createAndSaveDefinePropertiesTemplate("MODEL_DEFINE_TEMPLATE", modelRule);
        FirmwareRule definePropertiesRule = createAndSaveFirmwareDefinePropertiesRule(modelTemplate);

        EstbFirmwareContext context = createDefaultContext();
        context.setFirmwareVersion("NOT_AMV_VERSION");

        verifyActivationVersionResponse(context, false);

        firmwareRuleTemplateDao.deleteOne(modelTemplate.getId());
    }

    @Test
    public void appliedActivationAndMinimumVersionsAreTrueIfPercentRuleIsNotMatched() throws Exception {
        FirmwareConfig firmwareConfig = initActivationVersionAndReturnConfig(true, defaultPartnerId, new HashSet<String>(), Sets.newHashSet(".*Version"));

        Rule rule = createRule(null, MODEL, IS, defaultModelId.toUpperCase());
        FirmwareRule firmwareRule = createModelFirmwareRule(firmwareConfig.getId(), rule);

        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId(defaultPartnerId);
        RunningVersionInfo runningVersionInfo = new RunningVersionInfo(true, true);

        mockMvc.perform(postContext("/xconf/stb/runningFirmwareVersion/info", context))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(runningVersionInfo)));

        firmwareRuleTemplateDao.deleteOne(firmwareRule.getType());
    }

    @Test
    public void percentFilterIsMatchedAndFirmwareVersionIsNotAmvAndNotMinimumVersion() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());

        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        save(firmwareConfig);

        createAndSaveActivationVersion(defaultPartnerId, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), new HashSet<>());
        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null, firmwareConfig.getFirmwareVersion(), STB);

        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId(defaultPartnerId);
        context.setFirmwareVersion("NOT_AMV_VERSION");

        RunningVersionInfo runningVersionInfo = new RunningVersionInfo(false, false);

        mockMvc.perform(postContext("/xconf/stb/runningFirmwareVersion/info", context))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(runningVersionInfo)));
    }

    @Test
    public void percentFilterIsMatchedAndFirmwareVersionIsInAmvAndNotInMinimumVersion() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());

        FirmwareConfig percentFilterConfig = createDefaultFirmwareConfig();
        save(percentFilterConfig);

        FirmwareConfig activationConfig = createAndSaveFirmwareConfig("ACTIVATION_VERSION", model.getId().toUpperCase(), FirmwareConfig.DownloadProtocol.http, STB);
        createAndSaveActivationVersion(defaultPartnerId, Sets.newHashSet(activationConfig.getFirmwareVersion()), new HashSet<>());

        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null, percentFilterConfig.getFirmwareVersion(), STB);

        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId(defaultPartnerId);
        context.setFirmwareVersion(activationConfig.getFirmwareVersion());

        RunningVersionInfo runningVersionInfo = new RunningVersionInfo(true, false);

        mockMvc.perform(postContext("/xconf/stb/runningFirmwareVersion/info", context))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(runningVersionInfo)));
    }

    @Test
    public void percentFilterIsMatchedAndFirmwareVersionIsNotInAmvAndInMinimumVersion() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());

        FirmwareConfig percentFilterConfig = createDefaultFirmwareConfig();
        save(percentFilterConfig);

        FirmwareConfig activationConfig = createAndSaveFirmwareConfig("ACTIVATION_VERSION", model.getId().toUpperCase(), FirmwareConfig.DownloadProtocol.http, STB);
        createAndSaveActivationVersion(defaultPartnerId, Sets.newHashSet(activationConfig.getFirmwareVersion()), new HashSet<>());

        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null, percentFilterConfig.getFirmwareVersion(), STB);

        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId(defaultPartnerId);
        context.setFirmwareVersion(percentFilterConfig.getFirmwareVersion());

        RunningVersionInfo runningVersionInfo = new RunningVersionInfo(false, true);

        mockMvc.perform(postContext("/xconf/stb/runningFirmwareVersion/info", context))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(runningVersionInfo)));
    }

    @Test
    public void percentFilterIsMatchedAndFirmwareVersionIsInAmvAndInMinimumVersion() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());

        FirmwareConfig percentFilterConfig = createDefaultFirmwareConfig();
        save(percentFilterConfig);

        createAndSaveActivationVersion(defaultPartnerId, Sets.newHashSet(percentFilterConfig.getFirmwareVersion()), new HashSet<>());

        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null, percentFilterConfig.getFirmwareVersion(), STB);

        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId(defaultPartnerId);
        context.setFirmwareVersion(percentFilterConfig.getFirmwareVersion());

        RunningVersionInfo runningVersionInfo = new RunningVersionInfo(true, true);

        mockMvc.perform(postContext("/xconf/stb/runningFirmwareVersion/info", context))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(runningVersionInfo)));
    }

    @Test
    public void percentFilterIsMatchedAndFirmwareVersionIsInMinCheckAmvRuleIsNotMatched() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());

        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        save(firmwareConfig);

        FirmwareConfig firmwareConfig1 = createAndSaveFirmwareConfig("ANOTHER_FIRMWARE_VERSION", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null, firmwareConfig.getFirmwareVersion(), STB);

        createAndSaveActivationVersion(defaultPartnerId, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), new HashSet<>());


        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId(defaultPartnerId);
        context.setFirmwareVersion(firmwareConfig1.getFirmwareVersion());

        RunningVersionInfo runningVersionInfo = new RunningVersionInfo(false, false);

        mockMvc.perform(postContext("/xconf/stb/runningFirmwareVersion/info", context))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(runningVersionInfo)));
    }

    @Test
    public void verifyActivationVersionPriorityByPartnerId() throws Exception {
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());
        Model model = createAndSaveModel(defaultModelId.toUpperCase());

        String partnerFirmwareVersion = "PARTNER_FIRMWARE_VERSION";
        FirmwareConfig partnerAmvConfig = createFirmwareConfig(partnerFirmwareVersion, model.getId(), FirmwareConfig.DownloadProtocol.http);
        save(partnerAmvConfig);
        createAndSaveActivationVersion(defaultPartnerId, Sets.newHashSet(partnerAmvConfig.getFirmwareVersion()), new HashSet<>());

        String amvVersion = "AMV_VERSION";
        FirmwareConfig amvConfig = createFirmwareConfig(amvVersion, model.getId(), FirmwareConfig.DownloadProtocol.http);
        save(amvConfig);
        createAndSaveActivationVersion(null, Sets.newHashSet(amvConfig.getFirmwareVersion()), new HashSet<>());

        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null, partnerFirmwareVersion, STB);

        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId(defaultPartnerId);
        context.setFirmwareVersion(partnerFirmwareVersion);

        verifyActivationVersionResponse(context, false);
    }

    @Test
    public void matchOnlyAMVRulesEvenIfTemplateHasLowerPriority() throws Exception {
        initActivationVersionAndReturnConfig(false, null, Sets.newHashSet(defaultFirmwareVersion), new HashSet<String>());

        Rule templateRule = Rule.Builder.of(new Condition(MODEL, IS, FixedArg.from(defaultModelId.toUpperCase()))).build();
        FirmwareRuleTemplate template = createAndSaveDefinePropertiesTemplate("DEFINE_PROPERTY_TEMPLATE", templateRule);
        FirmwareRule definePropertyRule = createAndSaveFirmwareDefinePropertiesRule(template);

        EstbFirmwareContext context = createDefaultContext();
        context.setPartnerId(defaultPartnerId);
        context.setFirmwareVersion(defaultFirmwareVersion);

        mockMvc.perform(postContext("/xconf/stb/runningFirmwareVersion/info", context))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasActivationMinFW").value(true));

        firmwareRuleTemplateDao.deleteOne(template.getId());
    }

    /**
     * Priority of MODEL_CUSTOM template is higher than ENV_MODEL_RULE (percent filter) template
     */
    @Test
    public void amvRuleIsNotEvaluatedIfPercentRuleIsNotMatched() throws Exception {
        String activateFirmwareVersion = "ACTIVATION_VERSION";
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());
        FirmwareConfig amvConfig = createAndSaveFirmwareConfig(activateFirmwareVersion,
                model.getId(), FirmwareConfig.DownloadProtocol.http, STB);
        createAndSaveActivationVersion(EMPTY,
                Sets.newHashSet(amvConfig.getFirmwareVersion()), new HashSet<>());

        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null,
                activateFirmwareVersion, STB);

        FirmwareRuleTemplate modelTemplate = createTemplate("MODEL_CUSTOM",
                createRuleAction(ApplicableAction.Type.RULE_TEMPLATE , amvConfig.getId()),
                Rule.Builder.of(new Condition(MODEL, IS, FixedArg.from(model.getId()))).build(),
                0);
        firmwareRuleTemplateDao.setOne(modelTemplate.getId(), modelTemplate);

        FirmwareRule modelRule = createFirmwareRule(createRuleAction(ApplicableAction.Type.RULE_TEMPLATE, amvConfig.getId()), modelTemplate);
        firmwareRuleDao.setOne(modelRule.getId(), modelRule);

        EstbFirmwareContext context = createDefaultContext();
        context.setFirmwareVersion(defaultFirmwareVersion);

        verifyActivationVersionResponse(context, false);

        firmwareRuleTemplateDao.deleteOne(modelTemplate.getId());
    }

    /**
     * Priority of ENV_MODEL_RULE (percent filter) template is higher than MODEL_CUSTOM
     */
    @Test
    public void amvRuleIsEvaluatedIfPercentRuleIsMatched() throws Exception {
        String activateFirmwareVersion = "ACTIVATION_VERSION";
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());
        FirmwareConfig amvConfig = createAndSaveFirmwareConfig(activateFirmwareVersion,
                model.getId(), FirmwareConfig.DownloadProtocol.http, STB);
        createAndSaveActivationVersion(EMPTY,
                Sets.newHashSet(amvConfig.getFirmwareVersion()), new HashSet<>());

        createAndSavePercentageBean("AMV_TEST", env.getId(), model.getId(), null, null,
                activateFirmwareVersion, STB);

        FirmwareRuleTemplate modelTemplate = createTemplate("MODEL_CUSTOM",
                createRuleAction(ApplicableAction.Type.RULE_TEMPLATE , amvConfig.getId()),
                Rule.Builder.of(new Condition(MODEL, IS, FixedArg.from(model.getId()))).build(),
                Long.valueOf(firmwareRuleTemplateDao.getAll().stream()
                        .filter(template -> ApplicableAction.Type.RULE_TEMPLATE.equals(template.getApplicableAction().getActionType())).count()).intValue() + 1);
        firmwareRuleTemplateDao.setOne(modelTemplate.getId(), modelTemplate);

        FirmwareRule modelRule = createFirmwareRule(createRuleAction(ApplicableAction.Type.RULE_TEMPLATE, amvConfig.getId()), modelTemplate);
        firmwareRuleDao.setOne(modelRule.getId(), modelRule);

        EstbFirmwareContext context = createDefaultContext();
        context.setFirmwareVersion(defaultFirmwareVersion);

        verifyActivationVersionResponse(context, true);

        firmwareRuleTemplateDao.deleteOne(modelTemplate.getId());
    }

    @Test
    public void getLKGConfigIfAccountIdPercentageIsOutOfRange() throws Exception {
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgFirwmareVersion", defaultModelId, FirmwareConfig.DownloadProtocol.http);
        createAndSaveUseAccountPercentageBean(lkgConfig);

        String doesntFit66PercentAccountId = "215d20ca-58b5-429b-8ab2-d44d8ec7af1f";
        EstbFirmwareContext context = createDefaultContext();
        context.setFirmwareVersion(defaultFirmwareVersion);

        context.setAccountId(doesntFit66PercentAccountId);

        mockMvc.perform(postContext("/xconf/swu/stb", context))
                .andExpect(status().isOk()).andExpect(jsonPath("$.firmwareVersion").value(lkgConfig.getFirmwareVersion()));
    }

    @Test
    public void getDistributionConfigIfAccountIdPercentageIsInRange() throws Exception {
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgFirwmareVersion", defaultModelId, FirmwareConfig.DownloadProtocol.http);
        createAndSaveUseAccountPercentageBean(lkgConfig);

        EstbFirmwareContext context = createDefaultContext();
        context.setFirmwareVersion(defaultFirmwareVersion);
        String fits66PercentAccountId = "b27968ab-2bbd-40a3-8753-87a4f8883ef6";
        context.setAccountId(fits66PercentAccountId);

        mockMvc.perform(postContext("/xconf/swu/stb", context))
                .andExpect(status().isOk()).andExpect(jsonPath("$.firmwareVersion").value(defaultFirmwareVersion));
    }

    @Test
    public void getLKGConfigIfUseAccountIdPercentageForcedAndAccountIdIsMissingInRequest() throws Exception {
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgFirwmareVersion", defaultModelId, FirmwareConfig.DownloadProtocol.http);
        createAndSaveUseAccountPercentageBean(lkgConfig);

        EstbFirmwareContext context = createDefaultContext();
        context.setFirmwareVersion(defaultFirmwareVersion);

        mockMvc.perform(postContext("/xconf/swu/stb", context))
                .andExpect(status().isOk()).andExpect(jsonPath("$.firmwareVersion").value(lkgConfig.getFirmwareVersion()));
    }

    private PercentageBean createAndSaveUseAccountPercentageBean(FirmwareConfig lkgConfig) {
        PercentageBean useAccountBean = createPercentageBean("useAccountName", defaultEnvironmentId, defaultModelId, null, null, defaultFirmwareVersion, STB);
        useAccountBean.setUseAccountIdPercentage(true);
        useAccountBean.setLastKnownGood(lkgConfig.getId());
        useAccountBean.getFirmwareVersions().add(lkgConfig.getFirmwareVersion());
        percentageBeanQueriesService.save(useAccountBean);
        return useAccountBean;
    }

    private void verifyActivationVersionResponse(EstbFirmwareContext context, boolean rebootImmediately) throws Exception {
        mockMvc.perform(postContext("/xconf/swu/stb", context))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rebootImmediately").value(rebootImmediately));
    }

    private FirmwareConfig initActivationVersionAndReturnConfig(boolean rebootImmediately, String partnerId, Set<String> firmwareVersions, Set<String> regularExpressions) throws Exception {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig.setRebootImmediately(rebootImmediately);
        save(firmwareConfig);
        createAndSaveModel(defaultModelId);
        createAndSaveActivationVersion(partnerId, Sets.newHashSet(firmwareVersions), regularExpressions);
        return firmwareConfig;
    }

    public void definePropertiesAreAppliedFromRuleWithHigherPriority() throws Exception {
        createAndSaveIpRuleBean("firmwareConfigFileNameForIpRule");

        String highPriorityLocation = "highPriorityLocation";
        createAndSaveDefinePropertyTemplateAndRule("HighPriorityTemplate", 10, highPriorityLocation);
        createAndSaveDefinePropertyTemplateAndRule("LowerPriorityTemplate", 11, "lowPriorityLocation");

        String path = "$." + ConfigNames.FIRMWARE_LOCATION;
        mockMvc.perform(postContext("/xconf/swu/stb", createDefaultContext()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(path).value(highPriorityLocation));
    }

    private void verifyFilterSetDefaultHttpLocation(EstbFirmwareContext context) throws Exception {
        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setFirmwareDownloadProtocol(FirmwareConfig.DownloadProtocol.http);
        expectedResult.setFirmwareLocation(defaultHttpLocation);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    private void verifyBseResponse(IpRuleBean ipRuleBean) throws Exception {
        BseConfiguration bseConfig = new BseConfiguration();
        bseConfig.setProtocol(FirmwareConfig.DownloadProtocol.http.toString());
        bseConfig.setLocation("http://1.1.1.1");
        bseConfig.setModelConfigurations(Collections.singletonList(new BseConfiguration.ModelFirmwareConfiguration(
                ipRuleBean.getModelId(), ipRuleBean.getFirmwareConfig().getFirmwareFilename(), ipRuleBean.getFirmwareConfig().getFirmwareVersion())));

        mockMvc.perform(
                get("/xconf/swu/bse")
                        .param("ipAddress", defaultIpAddress))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(bseConfig)));
    }

    private void verifyDownloadLocationFilterSetsHttpLocationInConfig(DownloadLocationFilter locationFilter) throws Exception {
        EstbFirmwareContext context = createDefaultContext();
        setAllowedFirmwareVersionsAndModels(context.getFirmwareVersion(), context.getModel());
        createAndSaveDefaultIpRuleBean();
        saveDownloadLocationFilter(locationFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)
                .header(XCONF_HTTP_HEADER, XCONF_HTTP_VALUE)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setFirmwareDownloadProtocol(FirmwareConfig.DownloadProtocol.http);
        expectedResult.setFirmwareLocation(defaultHttpLocation);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    private void verifyDownloadLocationfilterSetsTftpLocationInConfig(DownloadLocationFilter locationFilter) throws Exception {
        EstbFirmwareContext context = createDefaultContext();
        createAndSaveDefaultIpRuleBean();
        saveDownloadLocationFilter(locationFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setFirmwareDownloadProtocol(FirmwareConfig.DownloadProtocol.tftp);
        expectedResult.setFirmwareLocation(defaultIpAddress);
        expectedResult.setIpv6FirmwareLocation(defaultIpv6Address);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    private void verifyRoundRobinFilterDoesNotSetHttpLocation(DownloadLocationRoundRobinFilterValue roundRobinFilterValue, EstbFirmwareContext context) throws Exception {
        createAndSaveDefaultIpRuleBean();
        saveRoundRobinFilter(roundRobinFilterValue);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setFirmwareDownloadProtocol(FirmwareConfig.DownloadProtocol.http);
        expectedResult.setFirmwareLocation(defaultIpAddress);
        expectedResult.setIpv6FirmwareLocation(defaultIpv6Address);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    private static MockHttpServletRequestBuilder postContext(String url, EstbFirmwareContext context) throws Exception {
        MockHttpServletRequestBuilder form = post(url).characterEncoding("UTF-8").contentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = context.getContext();
        for (String key : map.keySet()) {
            List<String> values = map.get(key);
            for (String value : values) {
                form.param(key, value);
            }
        }

        return form;
    }

    private void nullifyRedundantFirmwareConfigFieldsBeforeAssert(FirmwareConfig config) {
        config.setId(null);
        config.setDescription(null);
        config.setSupportedModelIds(null);
        config.setUpdated(null);
        config.setApplicationType(null);
    }

    private IpRuleBean createAndSaveIpRuleBean(String firmwareConfigFilename) throws Exception {
        String firmwareConfigId = "ipRuleFirmwareConfigId";
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        FirmwareConfig firmwareConfigForIpRule = createDefaultFirmwareConfig();
        firmwareConfigForIpRule.setId(firmwareConfigId);
        firmwareConfigForIpRule.setFirmwareFilename(firmwareConfigFilename);
        firmwareConfigDAO.setOne(firmwareConfigForIpRule.getId(), firmwareConfigForIpRule);
        ipRuleBean.setFirmwareConfig(firmwareConfigForIpRule);
        saveIpRuleBean(ipRuleBean);

        return ipRuleBean;
    }

    private MacRuleBean createAndSaveMacRuleBean(String firmwareFilename) throws Exception {
        String firmwareConfigId = "macRuleFirmwareConfigId";
        MacRuleBean macRuleBean = createDefaultMacRuleBean();
        FirmwareConfig firmwareConfigForMacRule = createDefaultFirmwareConfig();
        firmwareConfigForMacRule.setId(firmwareConfigId);
        firmwareConfigForMacRule.setFirmwareFilename(firmwareFilename);
        firmwareConfigDAO.setOne(firmwareConfigForMacRule.getId(), firmwareConfigForMacRule);
        macRuleBean.setFirmwareConfig(firmwareConfigForMacRule);
        saveMacRuleBean(macRuleBean);

        return macRuleBean;
    }

    private EnvModelRuleBean createAndSaveEnvModelRuleBean(String firmwareConfigFilename) throws Exception {
        String firmwareConfigId = "envModelRuleFirmwareConfigId";
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        FirmwareConfig firmwareConfigForEnvModelRule = createDefaultFirmwareConfig();
        firmwareConfigForEnvModelRule.setIpv6FirmwareLocation(firmwareConfigId);
        firmwareConfigForEnvModelRule.setFirmwareFilename(firmwareConfigFilename);
        firmwareConfigDAO.setOne(firmwareConfigForEnvModelRule.getId(), firmwareConfigForEnvModelRule);
        envModelRuleBean.setFirmwareConfig(firmwareConfigForEnvModelRule);
        saveEnvModelRuleBean(envModelRuleBean);

        return envModelRuleBean;
    }

    private FirmwareRuleTemplate createAndSaveDefinePropertyTemplateAndRule(String templateId, int priority, String propertyValue) throws Exception {
        FirmwareRuleTemplate template = templateFactory.createDownloadLocationTemplate();
        template.setId(templateId);
        template.setPriority(priority);
        firmwareRuleTemplateDao.setOne(template.getId(), template);

        FirmwareRule rule = new FirmwareRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setType(templateId);
        rule.setRule(RuleFactory.newDownloadLocationFilter(defaultIpListId));
        rule.setApplicableAction(new DefinePropertiesAction(Collections.singletonMap(ConfigNames.FIRMWARE_LOCATION, propertyValue)));
        firmwareRuleDao.setOne(rule.getId(), rule);
        return template;
    }

    protected FirmwareRuleTemplate createTemplate(String name, ApplicableAction action, Rule rule, Integer priority) {
        FirmwareRuleTemplate template = new FirmwareRuleTemplate();
        template.setApplicableAction(action);
        template.setId(name);
        template.setRule(rule);
        template.setPriority(priority);
        return template;
    }

    private String getExplanationForFilter(Object filter) {
        String filterStr = "";
        if (filter instanceof FirmwareRule) {
            filterStr = estbFirmwareLogger.toString((FirmwareRule) filter);
        } else if (filter instanceof PercentFilterValue) {
            // PercentFilter now is split into multiple rules so filter output would be different
            //filterStr = estbFirmwareLogger.toString((PercentFilterValue) filter);
        } else if (filter instanceof DownloadLocationRoundRobinFilterValue) {
            filterStr = LogsCompatibilityUtils.getRuleIdInfo(filter) + " " + ((SingletonFilterValue) filter).getId();
        }

        return "was blocked/modified by filter " + filterStr;
    }

    private String getExplanationForNoMatchedRules() {
        return "did not match any rule.";
    }

    private String getExplanationForNoopRule(FirmwareRule rule) {
        StringBuilder result = new StringBuilder();
        result.append("matched NO OP ")
                .append(getExplanationForRule(rule))
                .append("\n received NO config.");

        return JsonUtil.toJson(result.toString()).substring(1, result.length() - 1);
    }

    private String getExplanationForRule(FirmwareRule rule) {
        return rule.getType() + " " + rule.getId() + ": " + rule.getName();
    }

    private String getExplanationForConfig(FirmwareConfigFacade config) {
        int lengthOfClassNameWithIdentityHash = FirmwareConfigFacade.class.getName().length() + 9;
        String withoutIdentityHash = config.toString().substring(lengthOfClassNameWithIdentityHash);

        return JsonUtil.toJson(withoutIdentityHash).substring(1, withoutIdentityHash.length() - 1);
    }

    private void verifyFirmwareConfig(FirmwareConfig expectedConfig, String actualResult) throws Exception {
        nullifyRedundantFirmwareConfigFieldsBeforeAssert(expectedConfig);
        JSONAssert.assertEquals(JsonUtil.toJson(expectedConfig), actualResult, true);
    }

    private void verifyExplanationForDistributionRule(FirmwareRule expectedRule, String actualResult) {
        assertTrue(actualResult.contains(getExplanationForRule(expectedRule)));
        assertTrue(actualResult.contains("and blocked by Distribution percent in RuleAction"));
    }

    private void verifyExplanation(FirmwareRule expectedRule, FirmwareConfig expectedConfig, Object expectedFilter, String actualResult) {
        assertTrue(actualResult.contains(getExplanationForRule(expectedRule)));
        assertTrue(actualResult.contains(getExplanationForConfig(new FirmwareConfigFacade(expectedConfig))));
        assertTrue(actualResult.contains(getExplanationForFilter(expectedFilter)));
    }

    private EstbFirmwareContext createContextWithTime(Integer hour) {
        EstbFirmwareContext result = createDefaultContext();
        result.setTime(new LocalDateTime(2016, 1, 1, hour, 0));

        return result;
    }

    private TimeFilter createTimeFilterFrom9to15() {
        return createDefaultTimeFilter("9", "15");
    }

    private void verifyRebootImmediatelyValueInConfig(Boolean expectedValue, RebootImmediatelyFilter rebootImmediatelyFilter) throws Exception {
        createAndSaveDefaultIpRuleBean();
        EstbFirmwareContext context = createDefaultContext();
        saveRebootImmediatelyFilter(rebootImmediatelyFilter);

        String actualResult = mockMvc.perform(postContext("/xconf/swu/stb", context)).andReturn().getResponse().getContentAsString();

        FirmwareConfig expectedResult = createDefaultFirmwareConfig();
        expectedResult.setRebootImmediately(expectedValue);
        verifyFirmwareConfig(expectedResult, actualResult);
    }

    private TimeFilter createAndSaveTimeFilterFrom9to15() throws Exception {
        TimeFilter timeFilter = createTimeFilterFrom9to15();
        saveTimeFilter(timeFilter);

        return timeFilter;
    }

    private IpAddressGroup createDefaultIpAddressGroup() {
        IpAddressGroup result = new IpAddressGroup();
        result.setId(defaultIpListId);
        result.setName(defaultIpListId);
        result.setIpAddresses(Collections.singleton(new IpAddress(defaultIpAddress)));

        return result;
    }

    private void saveTimeFilter(TimeFilter timeFilter) throws Exception {
        performPostRequest("/" + QueryConstants.UPDATE_FILTERS_TIME, new TimeFilterWrapper(timeFilter));
    }

    private void savePercentFilter(PercentFilterValue percentFilterValue) throws Exception {
        performPostRequest("/" + QueryConstants.UPDATE_FILTERS_PERCENT, new PercentFilterWrapper(percentFilterValue));
    }

    private DownloadLocationRoundRobinFilterValue createAndSaveDefaultRoundRobinFilter() throws Exception {
        DownloadLocationRoundRobinFilterValue roundRobinFilterValue = createDefaultRoundRobinFilter();
        saveRoundRobinFilter(roundRobinFilterValue);

        return roundRobinFilterValue;
    }

    private void saveRoundRobinFilter(DownloadLocationRoundRobinFilterValue roundRobinFilterValue) throws Exception {
        performPostRequest("/" + QueryConstants.UPDATE_FILTERS_DOWNLOADLOCATION, roundRobinFilterValue);
    }

    private EnvModelRuleBean createAndSaveDefaultEnvModelRuleBean() throws Exception {
        EnvModelRuleBean envModelRuleBean = createDefaultEnvModelRuleBean();
        saveEnvModelRuleBean(envModelRuleBean);

        return envModelRuleBean;
    }

    private void saveEnvModelRuleBean(EnvModelRuleBean envModelRuleBean) throws Exception {
        performPostRequest("/" + QueryConstants.UPDATE_RULES_ENV_MODEL, envModelRuleBean);
    }

    private void saveMacRuleBean(MacRuleBean macRuleBean) throws Exception {
        performPostRequest("/" + QueryConstants.UPDATE_RULES_MAC, macRuleBean);
    }

    private void saveIpFilter(IpFilter ipFilter) throws Exception {
        performPostRequest("/" + QueryConstants.UPDATE_FILTERS_IPS, ipFilter);
    }

    private void saveDownloadLocationFilter(DownloadLocationFilter locationFilter) throws Exception {
        performPostRequest("/" + QueryConstants.UPDATE_FILTERS_LOCATION, locationFilter);
    }

    private IpRuleBean createAndSaveDefaultIpRuleBean() throws Exception {
        IpRuleBean ipRuleBean = createDefaultIpRuleBean();
        saveIpRuleBean(ipRuleBean);

        return ipRuleBean;
    }

    private void saveIpRuleBean(IpRuleBean ipRuleBean) throws Exception {
        performPostRequest("/" + QueryConstants.UPDATE_RULES_IPS, ipRuleBean);
    }

    private void saveRebootImmediatelyFilter(RebootImmediatelyFilter rebootImmediatelyFilter) throws Exception {
        performPostRequest("/" + QueryConstants.UPDATES_FILTERS_RI, rebootImmediatelyFilter);
    }
}
