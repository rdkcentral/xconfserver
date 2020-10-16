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
 * Created: 3/24/2016
*/
package com.comcast.xconf.admin.controller.setting;

import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingType;
import com.comcast.xconf.search.SearchFields;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static com.comcast.xconf.admin.controller.setting.SettingProfileController.URL_MAPPING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SettingProfileControllerTest extends BaseControllerTest {

    @Test
    public void createSettingProfile() throws Exception {
        SettingProfile profile = createSettingProfile("ProfileName");

        performPostRequestAndVerify(SettingProfileController.URL_MAPPING, profile);

        String path = URL_MAPPING + "/" + profile.getId();
        performRequestAndVerifyResponse(path, profile);
    }

    @Test
    public void updateSettingProfile() throws Exception {
        SettingProfile profile = createAndSaveSettingProfile("ProfileName");
        profile.setSettingType(SettingType.PARTNER_SETTINGS);
        performPutRequestAndVerify(URL_MAPPING, profile);

        String path = URL_MAPPING + "/" + profile.getId();
        performRequestAndVerifyResponse(path, profile);
    }

    @Test
    public void getAllSettingProfiles() throws Exception {
        SettingProfile profile1 = createAndSaveSettingProfile("ProfileName1");
        SettingProfile profile2 = createAndSaveSettingProfile("ProfileName2");
        SettingProfile profile3 = createAndSaveSettingProfile("ProfileName3");
        String expectedNumberOfItems = "3";
        List<SettingProfile> expectedResult = Arrays.asList(profile1, profile2);

        performRequestAndVerifyResponse(URL_MAPPING, Arrays.asList(profile1, profile2, profile3));
        MockHttpServletResponse response = performGetRequestAndVerifyResponse("/" + URL_MAPPING + "/page",
                new HashMap<String, String>(){{
                    put("pageNumber", "1");
                    put("pageSize", "2");
                }}, expectedResult).andReturn().getResponse();

        final Object actualNumberOfItems = response.getHeaderValue("numberOfItems");
        assertEquals(expectedNumberOfItems, actualNumberOfItems);
    }

    @Test
    public void exportOneTelemetryProfile() throws Exception {
        SettingProfile profile = createAndSaveSettingProfile("ProfileName");

        performExportRequestAndVerifyResponse("/" + URL_MAPPING + "/" + profile.getId(), Collections.singleton(profile), ApplicationType.STB);
    }

    @Test
    public void exportAllTelemetryProfiles() throws Exception {
        SettingProfile profile1 = createAndSaveSettingProfile("ProfileName1");
        SettingProfile profile2 = createAndSaveSettingProfile("ProfileName2");
        SettingProfile profile3 = createAndSaveSettingProfile("ProfileName3");
        List<SettingProfile> expectedResult = Arrays.asList(profile1, profile2, profile3);

        performExportRequestAndVerifyResponse("/" + URL_MAPPING, expectedResult, ApplicationType.STB);
    }

    @Test
    public void deleteTelemetryProfile() throws Exception {
        SettingProfile profile = createAndSaveSettingProfile("ProfileName");

        performDeleteRequestAndVerify(URL_MAPPING + "/" + profile.getId());

        performRequestAndVerifyResponse(URL_MAPPING, Collections.EMPTY_LIST);
    }

    @Test
    public void deletionIsForbidden_ProfileIsUsedInSettingRule() throws Exception {
        SettingProfile profile = createAndSaveSettingProfile("ProfileName");
        createAndSaveSettingRule("SettingRule", profile.getId());

        MvcResult actualResult = mockMvc.perform(
                delete("/" + URL_MAPPING + "/" + profile.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();

        final Exception actualException = actualResult.getResolvedException();
        assertEquals(EntityConflictException.class, actualException.getClass());
        assertTrue(actualException.getMessage().contains("Can't delete profile as it's used in setting rule"));
    }

    @Test
    public void createIsForbidden_NameIsUsed() throws Exception {
        createAndSaveSettingProfile("ProfileName");
        SettingProfile profile2 = createSettingProfile("ProfileName");

        MvcResult actualResult = performPostRequest(URL_MAPPING, profile2).andReturn();

        final Exception actualException = actualResult.getResolvedException();
        assertEquals(EntityConflictException.class, actualException.getClass());
        assertTrue(actualException.getMessage().contains("SettingProfile with such settingProfileId exists"));
    }

    @Test
    public void searchSettingProfile() throws Exception {
        SettingProfile profile1 = createAndSaveSettingProfile("Profile123");
        SettingProfile profile2 = createAndSaveSettingProfile("Profile456");

        List<SettingProfile> expectedResult = Arrays.asList(profile1);

        Map<String, String> contex = new HashMap<>();
        contex.put(SearchFields.NAME, "123");

        mockMvc.perform(post("/" + URL_MAPPING + "/filtered").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(contex))
                .param("pageNumber", "1")
                .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(expectedResult)));

        contex.clear();
        contex.put(SearchFields.NAME, "456");
        expectedResult = Arrays.asList(profile2);

        mockMvc.perform(post("/" + URL_MAPPING + "/filtered").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(contex))
                .param("pageNumber", "1")
                .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(expectedResult)));

        contex.clear();
        contex.put(SearchFields.NAME, "profile");
        expectedResult = Arrays.asList(profile1, profile2);

        mockMvc.perform(post("/" + URL_MAPPING + "/filtered").contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(contex))
                .param("pageNumber", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(expectedResult)));
    }
}
