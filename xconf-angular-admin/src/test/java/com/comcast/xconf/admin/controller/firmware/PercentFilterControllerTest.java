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
 * Created: 10/11/2017
*/
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.GlobalPercentage;
import com.comcast.xconf.estbfirmware.PercentFilterVo;
import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.EndsWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PercentFilterControllerTest extends BaseControllerTest {

    @Before
    public void setUp() throws Exception {
        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate(TemplateNames.GLOBAL_PERCENT);
        firmwareRuleTemplate.setRule(RuleFactory.newGlobalPercentFilter(100.0, null));
        firmwareRuleTemplateDao.setOne(TemplateNames.GLOBAL_PERCENT, firmwareRuleTemplate);
    }

    @Test
    public void createGlobalPercentage_BothApplicationTypes() throws Exception {
        Set<String> permissions = new HashSet<>(STB_PERMISSIONS);
        permissions.addAll(XHOME_PERMISSIONS);
        when(firmwarePermissionService.getPermissions()).thenReturn(permissions);
        GlobalPercentage xhomePercentage = createGlobalPercentage(ApplicationType.XHOME, 90.0);
        GlobalPercentage stbPercentage = createGlobalPercentage(ApplicationType.STB, 80.0);

        Cookie xhomeCookie = new Cookie("applicationType", ApplicationType.XHOME);
        Cookie stbCookie = new Cookie("applicationType", ApplicationType.STB);
        performPostRequest(xhomePercentage, xhomeCookie);
        performPostRequest(stbPercentage, stbCookie);

        performGetRequest(stbPercentage, stbCookie);
        performGetRequest(xhomePercentage, xhomeCookie);
    }

    @Test
    public void createGlobalPercentage_MissingType() throws Exception {
        Cookie xhomeCookie = new Cookie("applicationType", ApplicationType.XHOME);
        GlobalPercentage defaultPercentage = createGlobalPercentage(ApplicationType.XHOME, 100.0);

        performGetRequest(defaultPercentage, xhomeCookie);
    }

    @Test
    public void exportGlobalPercentage() throws Exception {
        Cookie xhomeCookie = new Cookie("applicationType", ApplicationType.XHOME);

        GlobalPercentage defaultPercentage = createGlobalPercentage(ApplicationType.XHOME, 100.0);
        PercentFilterVo percentFilterVo = new PercentFilterVo(defaultPercentage, new ArrayList<PercentageBean>());
        mockMvc.perform(
                get("/" + PercentFilterController.URL_MAPPING + "/globalPercentage")
                        .param("export", "export")
                        .cookie(xhomeCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(getExportHeadersExistMatcher("Content-Disposition", "Content-Type"))
                .andExpect(header().string("Content-Disposition", new EndsWith(ApplicationType.XHOME + ".json")))
                .andExpect(content().json(JsonUtil.toJson(percentFilterVo)));
    }

    private ResultMatcher getExportHeadersExistMatcher(final String... headers) {
        return mvcResult -> {
            for (String header : headers) {
                Assert.assertTrue("Header "  + header + " doesn't exist", mvcResult.getResponse().getHeaderNames().contains(header));
            }
        };
    }

    private void performGetRequest(GlobalPercentage percentage, Cookie cookie) throws Exception {
        mockMvc.perform(
                get("/" + PercentFilterController.URL_MAPPING)
                        .cookie(cookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(percentage)));
    }

    private void performPostRequest(GlobalPercentage percentage, Cookie cookie) throws Exception {
        mockMvc.perform(
                post("/" + PercentFilterController.URL_MAPPING)
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(percentage)))
                .andExpect(status().isOk());
    }

    private GlobalPercentage createGlobalPercentage(String applicationType, Double percentage) {
        GlobalPercentage globalPercentage = GlobalPercentage.forApplication(applicationType);
        globalPercentage.setPercentage(percentage);
        return globalPercentage;
    }
}
