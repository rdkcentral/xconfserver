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

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.estbfirmware.FirmwareRule;
import com.comcast.xconf.estbfirmware.MacRuleBean;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.converter.NgRuleConverter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class MacRuleLegacyConverterTest extends BaseQueriesControllerTest{

    @Autowired
    private NgRuleConverter ngRuleConverter;

    private String macListName = "macAddress";
    private String macAddressValue = "AA:AA:AA:AA:AA:AA";
    private Model model = createModel("model");

    @Test
    public void convertFirmwareRuleToMacRuleByMultipleConditions() throws Exception {
        GenericNamespacedList macList = createGenericNamespacedList(macListName, GenericNamespacedListTypes.MAC_LIST, macAddressValue);
        genericNamespacedListDAO.setOne(macList.getId(), macList);
        modelDAO.setOne(model.getId(), model);
        FirmwareRule firmwareRule = createMacRule();
        MacRuleBean macRuleBean = MacRuleLegacyConverter.convertFirmwareRuleToMacRuleBeanWrapper(ngRuleConverter.convertOld(firmwareRule));

        assertEquals(firmwareRule.getId(), macRuleBean.getId());
        assertEquals(firmwareRule.getName(), macRuleBean.getName());
        assertEquals(macListName, macRuleBean.getMacListRef());
    }

    private FirmwareRule createMacRule() {
        FirmwareRule firmwareRule = new FirmwareRule();
        firmwareRule.setId("id111");
        firmwareRule.setName("macRuleName");
        firmwareRule.setType(FirmwareRule.RuleType.MAC_RULE);
        firmwareRule.setCompoundParts(new ArrayList<Rule>());
        firmwareRule.getCompoundParts().add(createRule(null, RuleFactory.MAC, RuleFactory.IN_LIST, macListName));
        firmwareRule.getCompoundParts().add(createRule(null, RuleFactory.MODEL, StandardOperation.IS, model.getId()));
        return firmwareRule;
    }
}