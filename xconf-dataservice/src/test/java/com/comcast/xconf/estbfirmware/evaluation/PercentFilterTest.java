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
package com.comcast.xconf.estbfirmware.evaluation;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.contextconfig.TestContext;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.estbfirmware.legacy.PercentFilterLegacyService;
import com.comcast.xconf.firmware.FirmwareRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

/**
 * User: ikostrov
 * Date: 16.01.15
 * Time: 17:05
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestContext.class})
@WebAppConfiguration
@Deprecated
public class PercentFilterTest {

    public static final String MODEL = "MODEL123";
    public static final String IP_ADDRESS = "1.1.1.1";
    public static final String MAC = "11:22:33:44:55:66";
    public static final String ENV_MODEL_KEY = "X1_QA";
    public static final String FIRMWARE_VERSION = "SR150BN_1.3.3p1s1_PRODse-signed";
    public static final String LAST_KNOWN_GOOD = "LKG_VERSION";
    public static final String ADDITIONAL_VERSION = "ADDITIONAL_VERSION";
    public static final String CONFIG_ID = "CONFIG_ID";
    public static final String ADDITIONAL_CONFIG_ID = "ADDITIONAL_CONFIG_ID";
    public static final String TIME_FILTER_STR = TemplateNames.TIME_FILTER;
    public static final String RI_FILTER_STR = TemplateNames.REBOOT_IMMEDIATELY_FILTER;

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;
    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;
    @Autowired
    private PercentFilterLegacyService percentFilterService;
    @Autowired
    private PercentFilter percentFilter;
    @Autowired
    private EnvModelRuleService envModelRuleService;

    @Before
    public void setUp() throws Exception {
        EnvModelRuleBean envModelBean = getEnvModelBean(ENV_MODEL_KEY, "X1", "QA");
        FirmwareRule firmwareRule = envModelRuleService.convertModelRuleBeanToFirmwareRule(envModelBean);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);
    }

    @Test
    public void testFilter_EnvModelWhitelist() throws Exception {
        // verify env-model whitelist contains ip
        PercentFilterValue filterValue = new PercentFilterValue();
        filterValue.setPercentage(0.0);
        filterValue.setEnvModelPercentages(asMap(getEnvModelPercentage()));
        percentFilterService.save(filterValue);

        boolean result = percentFilter.filter(getEvaluationResult(ENV_MODEL_KEY), createDefaultContext());
        Assert.assertTrue(result); // true - filter won't be applied

        result = percentFilter.filter(getEvaluationResult(""), createDefaultContext());
        Assert.assertFalse(result); // false - filter will be applied
    }

    @Test
    public void testFilter_MinVersionCheck_UnknownVersion() throws Exception {
        createAndSavePercentFilter();

        EstbFirmwareContext.Converted context = createDefaultContext();
        context.setFirmwareVersion("UnknownVersion");

        boolean result = percentFilter.filter(getEvaluationResult(ENV_MODEL_KEY), context);
        Assert.assertTrue(result); // true - filter won't be applied
        Assert.assertTrue(context.getBypassFilters().contains(TemplateNames.TIME_FILTER));
        Assert.assertTrue(context.getForceFilters().contains(TemplateNames.REBOOT_IMMEDIATELY_FILTER));
    }

    @Test
    public void testFilter_MinVersionCheck_ReturnLKG() throws Exception {
        createAndSavePercentFilterWithLKG();
        FirmwareConfig config = createAndSaveFirmwareConfig(ADDITIONAL_CONFIG_ID, ADDITIONAL_VERSION);
        EstbFirmwareContext.Converted context = createDefaultContext();
        context.setFirmwareVersion("UnknownVersion");

        EvaluationResult evaluationResult = getEvaluationResult(ENV_MODEL_KEY);
        boolean result = percentFilter.filter(evaluationResult, context);

        Assert.assertTrue(result); // true - filter won't be applied
        Assert.assertTrue(context.getBypassFilters().contains(TemplateNames.TIME_FILTER));
        Assert.assertFalse(context.getForceFilters().contains(TemplateNames.REBOOT_IMMEDIATELY_FILTER));
    }

    @Test
    public void testFilter_MinVersionCheck_VersionContains() throws Exception {
        createAndSavePercentFilter();

        EstbFirmwareContext.Converted context = createDefaultContext();
        context.setFirmwareVersion(FIRMWARE_VERSION);

        String emptyEnvModelKey = "unknownEnvModelRule";
        boolean result = percentFilter.filter(getEvaluationResult(emptyEnvModelKey), context);

        Assert.assertFalse(result); // false - filter will be applied

        result = percentFilter.filter(getEvaluationResult(ENV_MODEL_KEY), context);

        Assert.assertTrue(result); // true - filter won't be applied
        Assert.assertFalse(context.getBypassFilters().contains(TemplateNames.TIME_FILTER));
        Assert.assertFalse(context.getForceFilters().contains(TemplateNames.REBOOT_IMMEDIATELY_FILTER));
    }

    @Test
    public void testFilter_RebootImmediatelyFalseByDefault() throws Exception {
        PercentFilterValue filterValue = new PercentFilterValue();
        filterValue.setPercentage(0.0);
        EnvModelPercentage envModelPercentage = getEnvModelPercentage();
        envModelPercentage.setFirmwareCheckRequired(true);
        envModelPercentage.setFirmwareVersions(asSet(FIRMWARE_VERSION));
        filterValue.setEnvModelPercentages(asMap(envModelPercentage));
        percentFilterService.save(filterValue);

        EstbFirmwareContext.Converted context = createDefaultContext();
        context.setFirmwareVersion("UnknownVersion");

        boolean result = percentFilter.filter(getEvaluationResult(ENV_MODEL_KEY), context);
        Assert.assertTrue(result); // true - filter won't be applied
        Assert.assertTrue(context.getBypassFilters().contains(TIME_FILTER_STR));
        Assert.assertFalse(context.getForceFilters().contains(RI_FILTER_STR));
    }

    @Test
    public void testFilter_LastKnownGood_Applied() throws Exception {
        createAndSavePercentFilterWithLKG();
        FirmwareConfig config = createAndSaveFirmwareConfig(CONFIG_ID, LAST_KNOWN_GOOD);

        EstbFirmwareContext.Converted context = createDefaultContext();
        context.setFirmwareVersion(FIRMWARE_VERSION);

        EvaluationResult evaluationResult = getEvaluationResult(ENV_MODEL_KEY);
        boolean result = percentFilter.filter(evaluationResult, context);
        Assert.assertTrue(result); // true - filter won't be applied
        Assert.assertEquals(new FirmwareConfigFacade(config), evaluationResult.getFirmwareConfig());
        Assert.assertFalse(context.getBypassFilters().contains(TemplateNames.TIME_FILTER));
        Assert.assertFalse(context.getForceFilters().contains(TemplateNames.REBOOT_IMMEDIATELY_FILTER));
    }

    @Test
    public void testFilter_LastKnownGood_NotApplied() throws Exception {
        createAndSavePercentFilterWithLKG();
        createAndSaveFirmwareConfig(CONFIG_ID, FIRMWARE_VERSION);

        EstbFirmwareContext.Converted context = createDefaultContext();
        context.setFirmwareVersion(FIRMWARE_VERSION);

        EvaluationResult evaluationResult = getEvaluationResult(ENV_MODEL_KEY);
        boolean result = percentFilter.filter(evaluationResult, context);
        Assert.assertFalse(result); // false - filter will be applied
        Assert.assertNull(evaluationResult.getFirmwareConfig());
        Assert.assertFalse(context.getBypassFilters().contains(TIME_FILTER_STR));
        Assert.assertFalse(context.getForceFilters().contains(RI_FILTER_STR));
    }

    private void createAndSavePercentFilterWithLKG() {
        PercentFilterValue filterValue = new PercentFilterValue();
        filterValue.setPercentage(0.0);
        EnvModelPercentage envModelPercentage = getEnvModelPercentage();
        envModelPercentage.setWhitelist(null);
        envModelPercentage.setFirmwareCheckRequired(true);
        envModelPercentage.setFirmwareVersions(asSet(FIRMWARE_VERSION));
        envModelPercentage.setLastKnownGood(CONFIG_ID);
        envModelPercentage.setIntermediateVersion(ADDITIONAL_CONFIG_ID);
        envModelPercentage.setPercentage(0.0);
        filterValue.setEnvModelPercentages(asMap(envModelPercentage));
        percentFilterService.save(filterValue);
        FirmwareConfig config = getFirmwareConfig(CONFIG_ID, FIRMWARE_VERSION);
        firmwareConfigDAO.setOne(config.getId(), config);
    }

    private void createAndSavePercentFilter() {
        PercentFilterValue filterValue = new PercentFilterValue();
        filterValue.setPercentage(0.0);
        EnvModelPercentage envModelPercentage = getEnvModelPercentage();
        envModelPercentage.setFirmwareCheckRequired(true);
        envModelPercentage.setRebootImmediately(true);
        envModelPercentage.setFirmwareVersions(asSet(FIRMWARE_VERSION));
        filterValue.setEnvModelPercentages(asMap(envModelPercentage));
        percentFilterService.save(filterValue);
    }

    private FirmwareConfig createAndSaveFirmwareConfig(String configId, String version) {
        FirmwareConfig config = getFirmwareConfig(configId, version);
        return firmwareConfigDAO.setOne(config.getId(), config);
    }

    private FirmwareConfig getFirmwareConfig(String configId, String firmwareVersion) {
        FirmwareConfig config = new FirmwareConfig();
        config.setId(configId);
        config.setFirmwareVersion(firmwareVersion);
        return config;
    }

    private Map<String, EnvModelPercentage> asMap(EnvModelPercentage value) {
        Map<String, EnvModelPercentage> map = new HashMap<>();
        map.put(ENV_MODEL_KEY, value);
        return map;
    }

    private <T> Set<T> asSet(T value) {
        return new HashSet<>(Collections.singletonList(value));
    }

    private EnvModelPercentage getEnvModelPercentage() {
        EnvModelPercentage value = new EnvModelPercentage();
        value.setPercentage(100);
        value.setActive(true);
        IpAddressGroup group = new IpAddressGroup();
        IpAddress ipAddress = new IpAddress(IP_ADDRESS);
        group.setIpAddresses(asSet(ipAddress));
        value.setWhitelist(group);
        return value;
    }

    private EstbFirmwareContext.Converted createDefaultContext() {
        EstbFirmwareContext context = new EstbFirmwareContext();
        context.seteStbMac(MAC);
        context.setModel(MODEL);
        context.setIpAddress(IP_ADDRESS);
        return context.convert();
    }

    private EvaluationResult getEvaluationResult(String envModelKey) {
        EvaluationResult result = new EvaluationResult();
        FirmwareRule matchedRule = new FirmwareRule();
        matchedRule.setName(envModelKey);
        matchedRule.setType(TemplateNames.ENV_MODEL_RULE);
        result.setMatchedRule(matchedRule);
        return result;
    }

    private EnvModelRuleBean getEnvModelBean(String name, String env, String model) {
        EnvModelRuleBean bean = new EnvModelRuleBean();
        bean.setId(UUID.randomUUID().toString());
        bean.setName(name);
        bean.setEnvironmentId(env);
        bean.setModelId(model);
        return bean;
    }
}
