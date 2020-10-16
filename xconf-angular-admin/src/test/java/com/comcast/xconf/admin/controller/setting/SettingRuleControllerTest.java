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
 * Author: Igor Kostrov
 * Created: 3/25/2016
*/
package com.comcast.xconf.admin.controller.setting;

import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.comcast.xconf.admin.controller.setting.SettingRuleController.URL_MAPPING;
import static org.junit.Assert.assertEquals;

public class SettingRuleControllerTest extends BaseControllerTest {

    private SettingProfile profile;

    @Before
    public void setUp() throws Exception {
        profile = createAndSaveSettingProfile("ProfileName");
    }

    @Test
    public void createSettingRule() throws Exception {
        SettingRule rule = createSettingRule("RuleName", profile.getId());

        performPostRequestAndVerify(URL_MAPPING, rule);

        String path = "/" + URL_MAPPING + "/" + rule.getId();
        performRequestAndVerifyResponse(path, rule);
    }

    @Test
    public void updateSettingRule() throws Exception {
        SettingRule rule = createAndSaveSettingRule("RuleName", profile.getId());
        rule.setRule(createRule("NewValue"));

        performPutRequestAndVerify("/" + URL_MAPPING, rule);

        String path = URL_MAPPING + "/" + rule.getId();
        performRequestAndVerifyResponse(path, rule);
    }

    @Test
    public void getAllSettingRules() throws Exception {
        SettingRule rule1 = createAndSaveSettingRule("RuleName1", profile.getId());
        SettingRule rule2 = createAndSaveSettingRule("RuleName2", profile.getId());
        SettingRule rule3 = createAndSaveSettingRule("RuleName3", profile.getId());
        String expectedNumberOfItems = "3";
        List<SettingRule> expectedResult = Arrays.asList(rule1, rule2);

        performRequestAndVerifyResponse("/" + URL_MAPPING, Arrays.asList(rule1, rule2, rule3));
        MockHttpServletResponse response = performGetRequestAndVerifyResponse("/" + URL_MAPPING + "/page",
                new HashMap<String, String>(){{
                    put("pageNumber", "1");
                    put("pageSize", "2");
                }}, expectedResult).andReturn().getResponse();

        final Object actualNumberOfItems = response.getHeaderValue("numberOfItems");
        assertEquals(expectedNumberOfItems, actualNumberOfItems);
    }

    @Test
    public void exportOneTelemetryRule() throws Exception {
        SettingRule rule = createAndSaveSettingRule("RuleName", profile.getId());

        performExportRequestAndVerifyResponse("/" + URL_MAPPING + "/" + rule.getId(), Collections.singleton(rule), ApplicationType.STB);
    }

    @Test
    public void exportAllTelemetryRules() throws Exception {
        SettingRule rule1 = createAndSaveSettingRule("RuleName1", profile.getId());
        SettingRule rule2 = createAndSaveSettingRule("RuleName2", profile.getId());
        SettingRule rule3 = createAndSaveSettingRule("RuleName3", profile.getId());
        List<SettingRule> expectedResult = Arrays.asList(rule1, rule2, rule3);

        performExportRequestAndVerifyResponse("/" + URL_MAPPING, expectedResult, ApplicationType.STB);
    }

    @Test
    public void deleteTelemetryRule() throws Exception {
        SettingRule rule = createAndSaveSettingRule("RuleName", profile.getId());

        performDeleteRequestAndVerify(URL_MAPPING + "/" + rule.getId());

        performRequestAndVerifyResponse(URL_MAPPING, Collections.EMPTY_LIST);
    }

    @Test
    public void createIsForbidden_NameIsUsed() throws Exception {
        createAndSaveSettingRule("RuleName", profile.getId());
        SettingRule rule2 = createSettingRule("RuleName", profile.getId());

        MvcResult actualResult = performPostRequest(URL_MAPPING, rule2).andReturn();

        final Exception actualException = actualResult.getResolvedException();
        assertEquals(EntityConflictException.class, actualException.getClass());
        assertEquals("Name is already used", actualException.getMessage());
    }

    @Test
    public void createIsForbidden_RuleHasDuplicate() throws Exception {
        SettingRule rule1 = createAndSaveSettingRule("RuleName", profile.getId());
        SettingRule rule2 = createSettingRule("RuleName", profile.getId());
        rule2.setName("NewRuleName");

        MvcResult actualResult = performPostRequest(URL_MAPPING, rule2).andReturn();

        final Exception actualException = actualResult.getResolvedException();
        assertEquals(RuleValidationException.class, actualException.getClass());
        assertEquals("Rule has duplicate: " + rule1.getName(), actualException.getMessage());
    }
}
