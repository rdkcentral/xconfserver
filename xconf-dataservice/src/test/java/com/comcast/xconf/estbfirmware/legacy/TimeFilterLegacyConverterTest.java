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
 * Author: mdolina
 * Created: 8/18/15
 */

package com.comcast.xconf.estbfirmware.legacy;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.collect.Sets;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class TimeFilterLegacyConverterTest extends BaseQueriesControllerTest {

    @Autowired
    private TimeFilterLegacyConverter legacyConverter;

    @Test
    public void convertFirmwareRuleIntoTimeFilterTest() {
        FirmwareRule rule = createFirmwareRule();
        TimeFilter filter = legacyConverter.convertFirmwareRuleToTimeFilter(rule);
        Assert.assertEquals(filter.getName(), rule.getName());
        Assert.assertEquals(filter.getId(), rule.getId());
        Assert.assertEquals(filter.getEnvModelWhitelist().getId(), rule.getBoundConfigId());
        checkConditions(rule, filter);
    }

    @Test
    public void convertTimeFilterIntoFirmwareRule() {
        TimeFilter timeFilter = createTimeFilter();
        FirmwareRule firmwareRule = legacyConverter.convertTimeFilterToFirmwareRule(timeFilter);
        Assert.assertEquals(firmwareRule.getId(), timeFilter.getId());
        Assert.assertEquals(timeFilter.getName(), firmwareRule.getName());
        Assert.assertEquals(timeFilter.getEnvModelWhitelist().getId(), firmwareRule.getBoundConfigId());
        checkConditions(firmwareRule, timeFilter);
    }

    private static void checkConditions(FirmwareRule rule, TimeFilter filter) {
        for (Rule r : RuleUtil.getIterableList(rule)) {
            checkCondition(r, filter);
        }
    }

    private static void checkCondition(Rule r, TimeFilter timeFilter) {
        if (r.getCondition() != null) {
            if (FirmwareRule.REBOOT_DECOUPLED.equals(r.getCondition().getFreeArg())) {
                Assert.assertEquals(timeFilter.isNeverBlockRebootDecoupled(), r.getCondition().getFixedArg().getValue());
            } else if (FirmwareRule.FIRMWARE_DOWNLOAD_PROTOCOL.equals(r.getCondition().getFreeArg())) {
                Assert.assertEquals(timeFilter.isNeverBlockHttpDownload(), r.getCondition().getFixedArg().getValue());
            } else if (FirmwareRule.IP.equals(r.getCondition().getFreeArg())
                        || RuleFactory.IP.equals(r.getCondition().getFreeArg())) {
                Assert.assertEquals(timeFilter.getIpWhitelist(), ((IpAddressGroup) r.getCondition().getFixedArg().getValue()));
            } else if (FirmwareRule.MODEL.equals(r.getCondition().getFreeArg())) {
                Assert.assertEquals(timeFilter.getEnvModelWhitelist().getModelId(), r.getCondition().getFixedArg().getValue());
            } else if (FirmwareRule.ENV.equals(r.getCondition().getFreeArg())) {
                Assert.assertEquals(timeFilter.getEnvModelWhitelist().getEnvironmentId(), ((String) r.getCondition().getFixedArg().getValue()));
            } else if (FirmwareRule.LOCAL_TIME.equals(r.getCondition().getFreeArg())) {
                String TIME_PATTERN = "HH:mm";
                if (StandardOperation.GTE.equals(r.getCondition().getOperation())) {
                    String rawTime = (String) r.getCondition().getFixedArg().getValue();
                    LocalTime locTime = LocalTime.parse(rawTime);
                    Assert.assertEquals(timeFilter.getStart().toString(TIME_PATTERN), locTime.toString(TIME_PATTERN));
                } else if (StandardOperation.LTE.equals(r.getCondition().getOperation())) {
                    String rawTime = (String) r.getCondition().getFixedArg().getValue();
                    LocalTime locTime = LocalTime.parse(rawTime);
                    Assert.assertEquals(timeFilter.getEnd().toString(TIME_PATTERN), locTime.toString(TIME_PATTERN));
                }
            } else if (FirmwareRule.TIME_ZONE.equals(r.getCondition().getFreeArg())) {
                Assert.assertEquals(timeFilter.isLocalTime(), r.isNegated());
            }
        }
    }

    private TimeFilter createTimeFilter() {
        TimeFilter timeFilter = new TimeFilter();
        timeFilter.setId("id55555");
        timeFilter.setName("local-time");
        timeFilter.setLocalTime(true);
        timeFilter.setNeverBlockHttpDownload(false);
        timeFilter.setNeverBlockRebootDecoupled(false);
        timeFilter.setStart(new LocalTime());
        timeFilter.setEnd(new LocalTime().plusHours(1));
        timeFilter.setEnvModelWhitelist(createDefaultEnvModelRuleBean());
        timeFilter.setIpWhitelist(createIpAddressGroup());
        return  timeFilter;
    }

    private IpAddressGroup createIpAddressGroup() {
        IpAddressGroup ipAddressGroup = new IpAddressGroup();
        ipAddressGroup.setName("local-group");
        ipAddressGroup.setId("id4454");
        ipAddressGroup.setIpAddresses(Sets.newHashSet(new IpAddress("10.10.10.10")));
        return ipAddressGroup;
    }

    private FirmwareRule createFirmwareRule() {
        FirmwareRule firmwareRule = new FirmwareRule();
        firmwareRule.setName("Rule");
        firmwareRule.setId("id1111");
        firmwareRule.setBoundConfigId("id22222");
        firmwareRule.setNegated(true);
        firmwareRule.setUpdated(new Date());
        firmwareRule.setType(FirmwareRule.RuleType.TIME_FILTER);
        firmwareRule.setCompoundParts(createCompoundParts());

        return firmwareRule;
    }

    private Condition createCondition(String fixedArgValue, FreeArg freeArg) {
        Condition condition = new Condition();
        Operation operation = Operation.forName("IS");
        FixedArg<String> fixedArg = FixedArg.from(fixedArgValue);
        condition.setFreeArg(freeArg);
        condition.setOperation(operation);
        condition.setFixedArg(fixedArg);
        return  condition;
    }

    private List<Rule> createCompoundParts() {
        List<Rule> compoundParts = new ArrayList<>();
        Rule envRule = new Rule();
        envRule.setCondition(createCondition("env", FirmwareRule.ENV));
        envRule.setNegated(false);
        compoundParts.add(envRule);

        Rule modelRule = new Rule();
        modelRule.setNegated(false);
        modelRule.setRelation(Relation.AND);
        modelRule.setCondition(createCondition("model", FirmwareRule.MODEL));
        compoundParts.add(modelRule);

        return compoundParts;
    }
}
