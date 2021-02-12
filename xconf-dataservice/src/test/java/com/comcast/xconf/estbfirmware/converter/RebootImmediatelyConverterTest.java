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
package com.comcast.xconf.estbfirmware.converter;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.BaseTestUtils;
import com.comcast.xconf.estbfirmware.RebootImmediatelyFilter;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RebootImmediatelyConverterTest {

    private RebootImmediatelyConverter converter = new RebootImmediatelyConverter();

    @Test
    public void testConvertFirmwareRuleToRebootFilter() throws Exception {
        FirmwareRule firmwareRule = createFirmareRule();
        RebootImmediatelyFilter riFilter = converter.convertFirmwareRuleToRebootFilter(firmwareRule);

        assertEquals(firmwareRule.getId(), riFilter.getId());
        assertEquals(firmwareRule.getName(), riFilter.getName());
        assertEquals(Sets.newHashSet("environmentId"), riFilter.getEnvironments());
        assertEquals(Sets.newHashSet("modelId"), riFilter.getModels());

        convertAndVerify(riFilter);
    }

    private FirmwareRule createFirmareRule() {
        FirmwareRule firmwareRule = new FirmwareRule();
        firmwareRule.setRule(new Rule());
        firmwareRule.setId("id123");
        firmwareRule.setName("ruleName");
        firmwareRule.setApplicableAction(new RuleAction("boundConfigId123"));
        List<Rule> compoundParts = new ArrayList<>();
        compoundParts.add(BaseTestUtils.createRule(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from("modelId"), null));
        compoundParts.add(BaseTestUtils.createRule(RuleFactory.ENV, StandardOperation.IS, FixedArg.from("environmentId"), Relation.AND));
        firmwareRule.getRule().setCompoundParts(compoundParts);
        firmwareRule.setType(TemplateNames.REBOOT_IMMEDIATELY_FILTER);
        return firmwareRule;
    }

    private void convertAndVerify(RebootImmediatelyFilter filter) {
        FirmwareRule firmwareRule = converter.convertRebootFilterToFirmwareRule(filter);
        RebootImmediatelyFilter converted = converter.convertFirmwareRuleToRebootFilter(firmwareRule);

        Assert.assertEquals(filter, converted);
    }
}