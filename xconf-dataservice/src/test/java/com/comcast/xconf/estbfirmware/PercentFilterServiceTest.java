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
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.UUID;

/**
 * User: ikostrov
 * Date: 14.08.14
 * Time: 18:07
 */
public class PercentFilterServiceTest extends BaseQueriesControllerTest {

    public static final String EMPTY_RULE_KEY = "EnvModelRule4";

    @Before
    public void setUp() throws Exception {
        createFirmwareEnvModelRule("QA", "Parker", "EnvModelRule1");
        createFirmwareEnvModelRule("QA", "X1", "EnvModelRule2");
        createFirmwareEnvModelRule("DEV", "Parker", "EnvModelRule3");
        createFirmwareEnvModelRule("DEV", "X1", EMPTY_RULE_KEY);
    }

    private FirmwareRule createFirmwareEnvModelRule(String env, String model, String name) {
        FirmwareRule firmwareRule = new FirmwareRule();
        Rule rule = RuleFactory.newEnvModelRule(env, model);
        firmwareRule.setId(UUID.randomUUID().toString());
        firmwareRule.setRule(rule);
        firmwareRule.setName(name);
        firmwareRule.setType(TemplateNames.ENV_MODEL_RULE);
        firmwareRule.setApplicableAction(new RuleAction());
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);
        return firmwareRule;
    }

    @Test
    public void testSave() throws Exception {
        PercentFilterValue filter = createFilter();

        percentFilterService.save(filter, ApplicationType.STB);

        // all env-model percentages should be left in filter except one
        Assert.assertTrue(filter.getEnvModelPercentages().containsKey("EnvModelRule1"));
        Assert.assertTrue(filter.getEnvModelPercentages().containsKey("EnvModelRule2"));
        Assert.assertTrue(filter.getEnvModelPercentages().containsKey("EnvModelRule3"));
        // whitelist should not be empty before saving
        Assert.assertTrue(filter.getWhitelist() != null && !filter.getWhitelist().getIpAddresses().isEmpty());
    }

    @Test
    public void testGet() throws Exception {
        PercentFilterValue filter = createFilter();
        // removing non-active rule, it should be restored after get
        filter.getEnvModelPercentages().remove(EMPTY_RULE_KEY);
        percentFilterService.save(filter, ApplicationType.STB);

        PercentFilterValue filterValue = percentFilterService.get(ApplicationType.STB);

        Assert.assertTrue(filterValue.getEnvModelPercentages().containsKey("EnvModelRule1"));
        Assert.assertTrue(filterValue.getEnvModelPercentages().containsKey("EnvModelRule2"));
        Assert.assertTrue(filterValue.getEnvModelPercentages().containsKey("EnvModelRule3"));
        Assert.assertTrue(filterValue.getEnvModelPercentages().containsKey(EMPTY_RULE_KEY));
        Assert.assertEquals(1, filterValue.getWhitelist().getIpAddresses().size());
    }

    private PercentFilterValue createFilter() throws Exception {
        PercentFilterValue value = new PercentFilterValue();

        IpAddressGroupExtended whitelist = createAndSaveDefaultIpAddressGroupExtended();
        value.setWhitelist(whitelist);

        HashMap<String, EnvModelPercentage> percentages = new HashMap<String, EnvModelPercentage>();
        EnvModelPercentage percentage1 = new EnvModelPercentage();
        percentage1.setActive(true);
        EnvModelPercentage percentage2 = new EnvModelPercentage();
        percentage2.setPercentage(40);
        EnvModelPercentage percentage3 = new EnvModelPercentage();
        percentage3.setWhitelist(whitelist);
        EnvModelPercentage percentage4 = new EnvModelPercentage();
        percentage4.setPercentage(100);

        percentages.put("EnvModelRule1", percentage1);
        percentages.put("EnvModelRule2", percentage2);
        percentages.put("EnvModelRule3", percentage3);
        percentages.put(EMPTY_RULE_KEY, percentage4);
        value.setEnvModelPercentages(percentages);

        return value;
    }
}
