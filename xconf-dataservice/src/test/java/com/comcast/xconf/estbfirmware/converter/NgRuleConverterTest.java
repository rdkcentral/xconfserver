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
 * Author: Stanislav Menshykov
 * Created: 2/25/16  10:59 AM
 */
package com.comcast.xconf.estbfirmware.converter;


import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;

import com.comcast.xconf.ConfigNames;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.estbfirmware.FirmwareRule;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.DefinePropertiesAction;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.*;

public class NgRuleConverterTest extends BaseQueriesControllerTest {

    @Autowired
    private NgRuleConverter converter;

    public static final String MODEL = "X1";
    public static final String ENVIRONMENT = "QA";

    @Test
    public void convertOld_RIFilterWithNestedCompoundPartsIsConvertedCorrectly() throws Exception {
        String ipRuleId1 = "a";
        String ipRuleId2 = "b";
        String ipRuleId3 = "c";
        String env = "env";
        String model = "model";
        String macAddress = "12:12:12:12:21:12";
        FirmwareRule oldRule = createNestedRiRule(ipRuleId1, ipRuleId2, ipRuleId3, env, model, macAddress);

        com.comcast.xconf.firmware.FirmwareRule actualResult = converter.convertOld(oldRule);

        validateActualConvertedRIRule(actualResult, ipRuleId1, ipRuleId2, ipRuleId3, env, model, macAddress);
    }

    @Test
    public void macRule() throws Exception {
        com.comcast.xconf.firmware.FirmwareRule firmwareRule = createFirmwareRule(
                TemplateNames.MAC_RULE,
                RuleFactory.newMacRule("macListId")
        );

        assertRule(firmwareRule);
    }

    @Test
    public void ipRule() throws Exception {
        IpAddressGroup group = createIpAddressGroup();
        com.comcast.xconf.estbfirmware.FirmwareRule oldIpRule =
                com.comcast.xconf.estbfirmware.FirmwareRule.newIpRule(group, ENVIRONMENT, MODEL);

        com.comcast.xconf.firmware.FirmwareRule firmwareRule = converter.convertOld(oldIpRule);

        assertConditionEquals(firmwareRule.getRule(), StbContext.ENVIRONMENT, ENVIRONMENT);
        assertConditionEquals(firmwareRule.getRule(), StbContext.MODEL, MODEL);
        assertConditionEquals(firmwareRule.getRule(), StbContext.IP_ADDRESS, group.getName());
    }

    @Test
    public void ipFilter() throws Exception {
        IpAddressGroup group = createIpAddressGroup();
        com.comcast.xconf.estbfirmware.FirmwareRule oldIpRule =
                com.comcast.xconf.estbfirmware.FirmwareRule.newIpFilter(group);

        com.comcast.xconf.firmware.FirmwareRule firmwareRule = converter.convertOld(oldIpRule);

        assertConditionEquals(firmwareRule.getRule(), StbContext.IP_ADDRESS, group.getName());
    }

    @Test
    public void envModelRule() throws Exception {
        com.comcast.xconf.firmware.FirmwareRule firmwareRule = createFirmwareRule(
                TemplateNames.ENV_MODEL_RULE,
                RuleFactory.newEnvModelRule(ENVIRONMENT, MODEL)
        );

        assertRule(firmwareRule);
    }

    @Test
    public void rebootImmediatelyFilter() throws Exception {
        IpAddressGroup group = createIpAddressGroup();
        MacAddress macAddress = new MacAddress("AA:BB:CC:DD:EE:FF");
        HashSet<String> environments = Sets.newHashSet("QA");
        HashSet<String> models = Sets.newHashSet("X1", "X2");
        HashSet<MacAddress> macAddresses = Sets.newHashSet(macAddress);

        com.comcast.xconf.estbfirmware.FirmwareRule oldIpRule =
                com.comcast.xconf.estbfirmware.FirmwareRule.newRebootImmediatelyFilter(
                        Sets.newHashSet(group), macAddresses, environments, models);
        com.comcast.xconf.firmware.FirmwareRule firmwareRule = converter.convertOld(oldIpRule);

        assertConditionEquals(firmwareRule.getRule(), StbContext.IP_ADDRESS, group.getName());
        assertConditionContains(firmwareRule.getRule(), StbContext.ENVIRONMENT, environments);
        assertConditionContains(firmwareRule.getRule(), StbContext.MODEL, models);
    }

    private void validateActualConvertedRIRule(com.comcast.xconf.firmware.FirmwareRule actualRule, String ipId1, String ipId2, String ipId3, String env, String model, String macAddress) throws Exception {
        String expectedRuleType = FirmwareRule.RuleType.REBOOT_IMMEDIATELY_FILTER.name();
        assertEquals(expectedRuleType, actualRule.getType());

        DefinePropertiesAction expectedAction = new DefinePropertiesAction();
        expectedAction.setProperties(new HashMap<String, String>() {{
            put(ConfigNames.REBOOT_IMMEDIATELY, "true");
        }});
        assertEquals(expectedAction, actualRule.getApplicableAction());

        assertEquals(actualRule.getRule().getCondition(), null);
        assertEquals(actualRule.getRule().getRelation(), null);

        List<Rule> actualCompoundParts = actualRule.getRule().getCompoundParts();
        assertContainsCompoundPartsInExpectedOrder(actualCompoundParts);
        assertContainsExpectedCompoundPartsDisregardingRelationAndOrder(actualCompoundParts, ipId1, ipId2, ipId3, env, model, macAddress);
    }

    private void assertContainsExpectedCompoundPartsDisregardingRelationAndOrder(List<Rule> actualCompoundParts, String ipId1, String ipId2, String ipId3, String env, String model, String macAddress) throws Exception {
        List<Rule> expectedCompoundParts = new ArrayList<>();
        expectedCompoundParts.add(createInListIpAddressGroupRule(ipId1, null));
        expectedCompoundParts.add(createInListIpAddressGroupRule(ipId2, null));
        expectedCompoundParts.add(createInListIpAddressGroupRule(ipId3, null));
        expectedCompoundParts.add(createEnvRule(env, null));
        expectedCompoundParts.add(createModelRule(model, null));
        expectedCompoundParts.add(createMacRule(macAddress, null));

        for (Rule part : actualCompoundParts) {
            part.setRelation(null);
        }

        assertTrue(equalCollections(expectedCompoundParts, actualCompoundParts));
    }

    private void assertContainsCompoundPartsInExpectedOrder(List<Rule> compoundParts) throws Exception {
        assertTrue(compoundParts.size() == 6);
        Rule firstPart = compoundParts.get(0);
        assertValidCompoundPart(firstPart, RuleFactory.IP, null);
        Rule secondPart = compoundParts.get(1);
        assertValidCompoundPart(secondPart, RuleFactory.IP, Relation.OR);
        Rule thirdPart = compoundParts.get(2);
        assertValidCompoundPart(thirdPart, RuleFactory.IP, Relation.OR);
        Rule fourthPart = compoundParts.get(3);
        assertValidCompoundPart(fourthPart, RuleFactory.MAC, Relation.AND);
        Rule fifthPart = compoundParts.get(4);
        assertValidCompoundPart(fifthPart, RuleFactory.ENV, Relation.AND);
        Rule sixthPart = compoundParts.get(5);
        assertValidCompoundPart(sixthPart, RuleFactory.MODEL, Relation.AND);
    }

    private void assertValidCompoundPart(Rule compoundPart, FreeArg freeArg, Relation relation) {
        assertEquals(compoundPart.getCondition().getFreeArg(), freeArg);
        assertEquals(compoundPart.getRelation(), relation);
    }

    private FirmwareRule createNestedRiRule(String ipId1, String ipId2, String ipId3, String env, String model, String macAddress) {
        Rule ipRule1 = createInIpAddressGroupRule(ipId1, null);
        Rule ipRule2 = createInIpAddressGroupRule(ipId2, null);
        Rule ipRule3 = createInIpAddressGroupRule(ipId3, null);
        Rule envRule = createEnvRule(env, null);
        envRule.setCompoundParts(Arrays.asList(ipRule1, ipRule2, ipRule3));
        Rule modelRule = createModelRule(model, null);
        modelRule.setCompoundParts(Arrays.asList(envRule));
        Rule macRule = createMacRule(macAddress, null);
        macRule.setCompoundParts(Arrays.asList(modelRule));
        FirmwareRule result = new FirmwareRule();
        result.setType(FirmwareRule.RuleType.REBOOT_IMMEDIATELY_FILTER);
        result.setCompoundParts(Arrays.asList(macRule));

        return result;
    }

    private Rule createRule(Condition condition, Relation relation) {
        Rule result = new Rule();
        result.setCondition(condition);
        result.setRelation(relation);

        return result;
    }

    private Rule createInIpAddressGroupRule(String groupId, Relation relation) {
        IpAddressGroupExtended group = new IpAddressGroupExtended();
        group.setName(groupId);
        group.setId(groupId);
        group.setIpAddresses(Sets.newHashSet(new IpAddress("1.1.1.1")));

        return createRule(new Condition(RuleFactory.IP, StandardOperation.IN, FixedArg.from(group)), relation);
    }

    private Rule createInListIpAddressGroupRule(String groupId, Relation relation) {
        return createRule(new Condition(RuleFactory.IP, Operation.forName("IN_LIST"), FixedArg.from(groupId)), relation);
    }

    private Rule createMacRule(String macAddress, Relation relation) {
        return createRule(createCondition(RuleFactory.MAC, Collections.singleton(macAddress)), relation);
    }

    private Rule createEnvRule(String env, Relation relation) {
        return createRule(createCondition(RuleFactory.ENV, Collections.singleton(env)), relation);
    }

    private Rule createModelRule(String model, Relation relation) {
        return createRule(createCondition(RuleFactory.MODEL, Collections.singleton(model)), relation);
    }

    private Condition createCondition(FreeArg freeArg, Collection fixedArg) {
        return new Condition(freeArg, StandardOperation.IN, FixedArg.from(fixedArg));
    }

    private void assertRule(com.comcast.xconf.firmware.FirmwareRule firmwareRule) {
        com.comcast.xconf.estbfirmware.FirmwareRule oldRule = converter.convertNew(firmwareRule);
        com.comcast.xconf.firmware.FirmwareRule converted = converter.convertOld(oldRule);
        Assert.assertEquals(firmwareRule, converted);
    }

    private void assertConditionContains(Rule rule, final String key, final Collection<String> values) {
        RuleUtil.convertConditions(rule, new Predicate<Condition>() {
            @Override
            public boolean apply(Condition condition) {
                if (condition.getFreeArg() != null &&
                        StringUtils.equals(condition.getFreeArg().getName(), key)) {
                    Collection collection = (Collection) condition.getFixedArg().getValue();
                    for (String value : values) {
                        Assert.assertTrue(collection.contains(value));
                    }
                }
                return false;
            }
        });
    }

    private void assertConditionEquals(Rule rule, final String key, final Object value) {
        RuleUtil.convertConditions(rule, new Predicate<Condition>() {
            @Override
            public boolean apply(Condition condition) {
                if (condition.getFreeArg() != null &&
                        StringUtils.equals(condition.getFreeArg().getName(), key)) {
                    Assert.assertEquals(condition.getFixedArg().getValue(), value);
                }
                return false;
            }
        });
    }

    private IpAddressGroup createIpAddressGroup() {
        IpAddressGroup group = new IpAddressGroup();
        group.setId("IpAddressGroupIp");
        group.setName("IpAddressGroupName");
        group.setIpAddresses(Sets.newHashSet(new IpAddress("1.1.1.1"), new IpAddress("2.2.2.2")));
        return group;
    }

    private com.comcast.xconf.firmware.FirmwareRule createFirmwareRule(String ruleType, Rule rule) {
        com.comcast.xconf.firmware.FirmwareRule firmwareRule = new com.comcast.xconf.firmware.FirmwareRule();
        firmwareRule.setType(ruleType);
        firmwareRule.setRule(rule);
        firmwareRule.setName("RuleName");
        firmwareRule.setId("RuleId");
        firmwareRule.setUpdated(new Date());
        firmwareRule.setApplicableAction(new RuleAction("configId"));
        return firmwareRule;
    }

    private static boolean equalCollections(Collection a, Collection b) {
        if (a.size() == b.size()) {
            return CollectionUtils.intersection(a, b).size() == a.size();
        }

        return false;
    }
}
