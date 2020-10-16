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
package com.comcast.xconf.estbfirmware.legacy;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.BaseTestUtils;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.estbfirmware.EnvModelPercentage;
import com.comcast.xconf.estbfirmware.PercentFilterValue;
import com.comcast.xconf.estbfirmware.SingletonFilterValue;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.FirmwareRule;
import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * User: ikostrov
 * Date: 14.08.14
 * Time: 18:07
 */
public class PercentFilterLegacyServiceTest {

    public static final String EMPTY_RULE_KEY = "EnvModelRule4";
    public static final String WHITELIST_ID = "listId";
    public static final String ENV_MODEL_RULE1 = "EnvModelRule1";

    @Test
    public void testSave() throws Exception {
        PercentFilterLegacyService service = new PercentFilterLegacyService();
        CachedSimpleDao<String, SingletonFilterValue> filterDaoMock = mock(CachedSimpleDao.class);
        service.singletonFilterValueDAO = filterDaoMock;
        PercentFilterValue filter = createFilter();
        service.save(filter);

        verify(filterDaoMock, only()).setOne(PercentFilterValue.SINGLETON_ID, filter);

        // all env-model percentages should be left in filter except one
        assertTrue(filter.getEnvModelPercentages().containsKey("EnvModelRule1"));
        assertTrue(filter.getEnvModelPercentages().containsKey("EnvModelRule2"));
        assertTrue(filter.getEnvModelPercentages().containsKey("EnvModelRule3"));

        // whitelist should not be empty before saving
        assertTrue(filter.getWhitelist() != null && !filter.getWhitelist().getIpAddresses().isEmpty());
    }

    @Test
    public void testGet() throws Exception {
        PercentFilterLegacyService service = new PercentFilterLegacyService();
        PercentFilterValue filter = createFilter();

        // removing non-active rule, it should be restored after get
        filter.getEnvModelPercentages().remove(EMPTY_RULE_KEY);

        CachedSimpleDao<String, SingletonFilterValue> singletonFilterDaoMock = mock(CachedSimpleDao.class);
        service.singletonFilterValueDAO = singletonFilterDaoMock;
        when(singletonFilterDaoMock.getOne(PercentFilterValue.SINGLETON_ID)).thenReturn(filter);

        CachedSimpleDao<String, FirmwareRule> firmwareRuleDaoMock = createFirmwareDaoMock();
        service.firmwareRuleDao = firmwareRuleDaoMock;

        PercentFilterValue filterValue = service.get();
        verify(singletonFilterDaoMock, only()).getOne(anyString());
        verify(firmwareRuleDaoMock, only()).asLoadingCache();

        assertTrue(filterValue.getEnvModelPercentages().containsKey("EnvModelRule1"));
        assertTrue(filterValue.getEnvModelPercentages().containsKey("EnvModelRule2"));
        assertTrue(filterValue.getEnvModelPercentages().containsKey("EnvModelRule3"));
        assertTrue(filterValue.getEnvModelPercentages().containsKey(EMPTY_RULE_KEY));
        Assert.assertEquals(2, filterValue.getWhitelist().getIpAddresses().size());
    }

    @Test
    public void renameNamespacedListInAllWhitelist() {
        PercentFilterLegacyService service = new PercentFilterLegacyService();
        PercentFilterValue filter = createFilter();

        CachedSimpleDao<String, SingletonFilterValue> singletonFilterDaoMock = mock(CachedSimpleDao.class);
        service.singletonFilterValueDAO = singletonFilterDaoMock;
        when(singletonFilterDaoMock.getOne(PercentFilterValue.SINGLETON_ID)).thenReturn(filter);

        CachedSimpleDao<String, FirmwareRule> firmwareRuleDaoMock = createFirmwareDaoMock();
        service.firmwareRuleDao = firmwareRuleDaoMock;
        String newWhitelistId = "newId";
        service.renameNamespacedListInAllWhitelist(WHITELIST_ID, newWhitelistId);
        PercentFilterValue filterValue = service.get();
        assertEquals(filterValue.getWhitelist().getName(), newWhitelistId);

        for (EnvModelPercentage envModelPercentage : filter.getEnvModelPercentages().values()) {
            if (envModelPercentage.getWhitelist() != null) {
                assertEquals(envModelPercentage.getWhitelist().getName(), newWhitelistId);
            }
        }
    }

    @Test
    public void renameEnvModeRule() {
        PercentFilterLegacyService service = new PercentFilterLegacyService();
        PercentFilterValue filter = createFilter();

        CachedSimpleDao<String, SingletonFilterValue> singletonFilterDaoMock = mock(CachedSimpleDao.class);
        service.singletonFilterValueDAO = singletonFilterDaoMock;
        when(singletonFilterDaoMock.getOne(PercentFilterValue.SINGLETON_ID)).thenReturn(filter);

        CachedSimpleDao<String, FirmwareRule> firmwareRuleDaoMock = createFirmwareDaoMock();
        service.firmwareRuleDao = firmwareRuleDaoMock;

        String newEnvModelRuleName = "newEnvModelRuleName";
        service.renameEnvModelRule(ENV_MODEL_RULE1, newEnvModelRuleName);

        assertFalse(filter.getEnvModelPercentages().containsKey(ENV_MODEL_RULE1));
        assertTrue(filter.getEnvModelPercentages().containsKey(newEnvModelRuleName));
    }

    private CachedSimpleDao<String, FirmwareRule> createFirmwareDaoMock() {
        CachedSimpleDao<String, FirmwareRule> firmwareRuleDaoMock = mock(CachedSimpleDao.class);

        LoadingCache<String,Optional<FirmwareRule>> loadingCache = mock(LoadingCache.class);
        when(firmwareRuleDaoMock.asLoadingCache()).thenReturn(loadingCache);

        ConcurrentMap<String,Optional<FirmwareRule>> concurrentMap = mock(ConcurrentMap.class);
        when(loadingCache.asMap()).thenReturn(concurrentMap);

        FirmwareRule rule1 = createEnvModelRule("EnvModelRule1", "DEV", "X1");
        FirmwareRule rule2 = createEnvModelRule("EnvModelRule2", "QA", "X1");
        FirmwareRule rule3 = createEnvModelRule("EnvModelRule3", "DEV", "Parker");
        FirmwareRule rule4 = createEnvModelRule(EMPTY_RULE_KEY, "DEV", "Parker");

        Collection<Optional<FirmwareRule>> values = Arrays.asList(
                Optional.of(rule1), Optional.of(rule2), Optional.of(rule3), Optional.of(rule4));
                when(concurrentMap.values()).thenReturn(values);

        return firmwareRuleDaoMock;
    }

    private PercentFilterValue createFilter() {
        PercentFilterValue value = new PercentFilterValue();

        value.setWhitelist(createWhitelist());

        HashMap<String, EnvModelPercentage> percentages = new HashMap<>();
        EnvModelPercentage percentage1 = new EnvModelPercentage();
        percentage1.setActive(true);
        EnvModelPercentage percentage2 = new EnvModelPercentage();
        percentage2.setPercentage(40);
        EnvModelPercentage percentage3 = new EnvModelPercentage();
        percentage3.setWhitelist(createWhitelist());
        EnvModelPercentage percentage4 = new EnvModelPercentage();
        percentage4.setPercentage(100);

        percentages.put(ENV_MODEL_RULE1, percentage1);
        percentages.put("EnvModelRule2", percentage2);
        percentages.put("EnvModelRule3", percentage3);
        percentages.put(EMPTY_RULE_KEY, percentage4);
        value.setEnvModelPercentages(percentages);

        return value;
    }

    private IpAddressGroupExtended createWhitelist() {
        IpAddressGroupExtended whitelist = new IpAddressGroupExtended();
        whitelist.setId(WHITELIST_ID);
        whitelist.setName(WHITELIST_ID);
        IpAddress address1 = new IpAddress("1.1.1.1");
        IpAddress address2 = new IpAddress("1.1.1.2");
        HashSet<IpAddress> addresses = new HashSet<IpAddress>(Arrays.asList(address1, address2));
        whitelist.setIpAddresses(addresses);
        return whitelist;
    }

    private FirmwareRule createEnvModelRule(String id, String envId, String modelId) {
        FirmwareRule firmwareRule = new FirmwareRule();
        firmwareRule.setId(id);
        firmwareRule.setName(id);
        firmwareRule.setType(TemplateNames.ENV_MODEL_RULE);
        Rule rule = new Rule();
        List<Rule> compoundParts = new ArrayList<>();
        compoundParts.add(BaseTestUtils.createRule(RuleFactory.ENV, StandardOperation.IS, FixedArg.from(envId.toUpperCase()), null));
        compoundParts.add(BaseTestUtils.createRule(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from(modelId.toUpperCase()), Relation.AND));
        rule.setCompoundParts(compoundParts);
        firmwareRule.setRule(rule);
        return firmwareRule;
    }
}
