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
package com.comcast.xconf.admin.controller.setting;

import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.settings.SettingType;
import com.fasterxml.jackson.core.type.TypeReference;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SettingTestPageControllerTest extends BaseControllerTest {

    private List<SettingRule> eponSettingRuleList = new ArrayList<>();
    private List<SettingRule> partnerSettingRuleList = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        //creates profile with setting type: EPON
        SettingProfile profileWithEponType = createAndSaveSettingProfile("ProfileNameWithEpon", SettingType.EPON);
        //creates rule of the profile. This rule contains key:firmwareVersion and value:TestVersion
        eponSettingRuleList.add(createAndSaveSettingRule(RuleFactory.VERSION, "TestVersion", profileWithEponType.getId()));

        SettingProfile profileWithPartnerSettingsType = createAndSaveSettingProfile("ProfileNameWithPartnerSettings", SettingType.PARTNER_SETTINGS);
        partnerSettingRuleList.add(createAndSaveSettingRule(RuleFactory.MODEL, "TestModel", profileWithPartnerSettingsType.getId()));
    }

    @Test
    public void getEponSettingRule() throws Exception {

        MockHttpServletResponse response = createAndSendRule(eponSettingRuleList, "epon")
            .andExpect(status().isOk())
                .andReturn().getResponse();

        boolean hasSettingRule = hasSettingRule(response.getContentAsString(), eponSettingRuleList);
        Assert.assertTrue(hasSettingRule);
    }

    @Test
    public void getPartnerSettingRule() throws Exception {

        MockHttpServletResponse response = createAndSendRule(partnerSettingRuleList, "partnersettings")
                .andExpect(status().isOk())
                .andReturn().getResponse();

        boolean hasSettingRule = hasSettingRule(response.getContentAsString(), partnerSettingRuleList);
        Assert.assertTrue(hasSettingRule);
    }

    @Test
    public void getAllSettingRule() throws Exception {

        List<SettingRule> settingRuleList = new ArrayList<>();
        settingRuleList.addAll(eponSettingRuleList);
        settingRuleList.addAll(partnerSettingRuleList);

        MockHttpServletResponse response = createAndSendRule(settingRuleList, "epon", "partnersettings")
                .andExpect(status().isOk())
                .andReturn().getResponse();

        boolean hasSettingRule = hasSettingRule(response.getContentAsString(), settingRuleList);
        Assert.assertTrue(hasSettingRule);
    }

    private boolean hasSettingRule(String content, List<SettingRule> createdRulesList) throws Exception {
        JSONObject json = (JSONObject) new JSONParser().parse(content);
        Map <String, List<SettingRule>> settingRuleMap = mapper.readValue(json.get("result").toString(), new TypeReference<Map <String, List<SettingRule>>>() {});

        if (settingRuleMap.size() <= 0) {
            return false;
        }

        for (Map.Entry entry : settingRuleMap.entrySet()) {
            List<SettingRule> settingRules = (List<SettingRule>) entry.getValue();
            // validates if size of setting rules list more than 0
            if (settingRules.size() <= 0) return false;

            // failed if setting rule was not found
            for (SettingRule settingRule : settingRules) {
                boolean foundSettingRule = false;
                for (SettingRule createdRule : createdRulesList) {
                    if (createdRule.compareTo(settingRule) == 0) {
                        foundSettingRule = true;
                        break;
                    }
                }
                if (!foundSettingRule) return false;
            }
        }

        return true;
    }

    private ResultActions createAndSendRule(List<SettingRule> settingRulesList, String... settingTypes) throws Exception {
        StringBuffer url = new StringBuffer(SettingTestPageController.URL_MAPPING);
        url.append("?");
        for (int i = 0; i < settingTypes.length; i++) {
            url.append("settingType=");
            url.append(settingTypes[i]);

            if (i < settingTypes.length - 1) {
                url.append("&");
            }
        }

        Map<String, String> map = new HashMap<>();
        for (SettingRule settingRule : settingRulesList) {
            Condition condition = settingRule.getRule().getCondition();
            map.put(condition.getFreeArg().getName(), condition.getFixedArg().getValue().toString());
        }

        return  performPostRequest(url.toString(), map);
    }
}
