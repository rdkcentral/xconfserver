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
 * Created: 11/3/2017
*/
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.Environment;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.queries.QueryConstants;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PercentageBeanControllerTest extends BaseControllerTest {

    @Test
    public void createBeanWithWrongApplicationType() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        PercentageBean percentageBean = createPercentageBean(defaultModelId, defaultEnvironmentId, firmwareConfig);
        String errorMsg = String.format("Current application type %s doesn't match with entity application type: %s", XHOME, percentageBean.getApplicationType());
        performPostRequest(PercentageBeanController.URL_MAPPING, xhomeCookie, percentageBean)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(errorMsg));
    }

    @Test
    public void createBeanWithModelOnly() throws Exception {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        PercentageBean percentageBean = createPercentageBean(defaultModelId, null, firmwareConfig);
        performPostRequest(PercentageBeanController.URL_MAPPING, percentageBean)
                .andExpect(status().isCreated());
    }

    @Test
    public void getPage() throws Exception {
        List<PercentageBean> percentageBeans = createAndSavePercentageBeans(10);
        Collections.sort(percentageBeans);
        mockMvc.perform(post("/" + PercentageBeanController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param("pageSize", "5")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(nullifyUnwantedFields(percentageBeans.subList(0, 5)))));
    }

    @Test
    public void nullValuesAreIgnoredInXmlAndJsonResponse() throws Exception {
        Model model = createAndSaveModel(defaultModelId.toUpperCase());
        Environment env = createAndSaveEnvironment(defaultEnvironmentId.toUpperCase());

        PercentageBean percentageBean = createPercentageBean(model.getId(), env.getId(), defaultPartnerId);
        percentageBeanService.create(percentageBean);

        String fullUrl = "/" + QueryConstants.QUERIES_PERCENTAGE_BEAN + "/" + percentageBean.getId();

        mockMvc.perform(get(fullUrl)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(xpath("/PercentageBean/lastKnownGood", "").doesNotExist());

        mockMvc.perform(get(fullUrl)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastKnownGood").doesNotExist());
    }

    @Test
    public void findByModel() throws Exception {
        List<PercentageBean> percentageBeans = createAndSavePercentageBeans(5);
        Collections.sort(percentageBeans);
        PercentageBean beanToSearch = percentageBeans.get(3);

        assertPercentageBeanResponse(beanToSearch, SearchFields.MODEL, beanToSearch.getModel());
    }

    @Test
    public void findByMinVersion() throws Exception {
        List<PercentageBean> percentageBeans = createAndSavePercentageBeans(5);
        Collections.sort(percentageBeans);
        PercentageBean beanToSearch = percentageBeans.get(3);
        FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne(beanToSearch.getLastKnownGood());

        assertPercentageBeanResponse(beanToSearch, SearchFields.MIN_CHECK_VERSION, firmwareConfig.getFirmwareVersion());
    }

    @Test
    public void findByPartnerId() throws Exception {
        List<PercentageBean> percentageBeans = createAndSavePercentageBeans(5);
        PercentageBean beanToSearch = percentageBeans.get(2);
        String partnerId = (String) beanToSearch.getOptionalConditions().getCondition().getFixedArg().getValue();

        assertPercentageBeanResponse(beanToSearch, SearchFields.FIXED_ARG, partnerId);
    }

    @Test
    public void findByLastKnownGood() throws Exception {
        List<PercentageBean> percentageBeans = createAndSavePercentageBeans(5);
        Collections.sort(percentageBeans);
        PercentageBean beanToSearch = percentageBeans.get(3);
        FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne(beanToSearch.getLastKnownGood());

        assertPercentageBeanResponse(beanToSearch, SearchFields.LAST_KNOWN_GOOD, firmwareConfig.getFirmwareVersion());
    }

    @Test
    public void findByIntermediateVersion() throws Exception {
        List<PercentageBean> percentageBeans = createAndSavePercentageBeans(5);
        Collections.sort(percentageBeans);
        PercentageBean beanToSearch = percentageBeans.get(3);
        FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne(beanToSearch.getLastKnownGood());

        assertPercentageBeanResponse(beanToSearch, SearchFields.INTERMEDIATE_VERSION, firmwareConfig.getFirmwareVersion());
    }

    @Test
    public void findByMinCheckVersion() throws Exception {
        List<PercentageBean> percentageBeans = createAndSavePercentageBeans(5);
        Collections.sort(percentageBeans);
        PercentageBean beanToSearch = percentageBeans.get(3);

        assertPercentageBeanResponse(beanToSearch, SearchFields.MIN_CHECK_VERSION, beanToSearch.getFirmwareVersions().iterator().next());
    }

    @Test
    public void setPartnerIdToUpperCaseIsOperation() throws Exception {
        String partnerId = "lowercasepartnerid";
        PercentageBean percentageBean = createPercentageBean(defaultModelId, defaultEnvironmentId, partnerId);
        percentageBeanService.create(percentageBean);
        PercentageBean one = percentageBeanService.getOne(percentageBean.getId());
        List<String> partnerIds = RuleUtil.getFixedArgsFromRuleByFreeArgAndOperation(one.getOptionalConditions(), RuleFactory.PARTNER_ID, StandardOperation.IS);

        assertFalse(partnerIds.get(0).equals(partnerId));
        assertTrue(partnerIds.get(0).equalsIgnoreCase(partnerId));
    }

    @Test
    public void setPartnerIdToUpperCaseInOperation() throws Exception {
        String partnerId = "lowercasepartnerid";
        PercentageBean percentageBean = createPercentageBean(defaultModelId, defaultEnvironmentId, partnerId);
        Rule partnerRule = Rule.Builder.of(new Condition(RuleFactory.PARTNER_ID, StandardOperation.IN, FixedArg.from(Collections.singletonList(partnerId)))).build();
        percentageBean.setOptionalConditions(partnerRule);
        percentageBeanService.create(percentageBean);
        PercentageBean one = percentageBeanService.getOne(percentageBean.getId());
        List<String> partnerIds = RuleUtil.getFixedArgsFromRuleByFreeArgAndOperation(one.getOptionalConditions(), RuleFactory.PARTNER_ID, StandardOperation.IN);

        assertFalse(partnerIds.get(0).equals(Collections.singletonList(partnerId)));
        assertTrue(partnerIds.get(0).equalsIgnoreCase(Collections.singletonList(partnerId).toString()));
    }

    private void assertPercentageBeanResponse(PercentageBean beanToSearch, String searchKey, String searchValue) throws Exception {
        beanToSearch.getOptionalConditions().setRelation(null);
        mockMvc.perform(post("/" + PercentageBeanController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(Collections.singletonMap(searchKey, searchValue)))
                .param("pageSize", "5")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singletonList(beanToSearch))));
    }

    private PercentageBean createPercentageBean(String model, String environment, FirmwareConfig firmwareConfig) {
        PercentageBean percentageBean = new PercentageBean();
        percentageBean.setId(UUID.randomUUID().toString());
        percentageBean.setName(UUID.randomUUID().toString());
        percentageBean.setModel(model);
        percentageBean.setEnvironment(environment);
        percentageBean.setDistributions(Lists.newArrayList(new RuleAction.ConfigEntry(firmwareConfig.getId(), 10.0, 50.0)));
        percentageBean.setLastKnownGood(firmwareConfig.getId());
        percentageBean.setIntermediateVersion(firmwareConfig.getId());
        percentageBean.setFirmwareCheckRequired(true);
        percentageBean.setFirmwareVersions(Sets.newHashSet(firmwareConfig.getFirmwareVersion()));
        Rule optionalCondition = Rule.Builder.of(new Condition(RuleFactory.PARTNER_ID, StandardOperation.IS, FixedArg.from(UUID.randomUUID().toString()))).build();
        optionalCondition.setRelation(Relation.AND);
        percentageBean.setOptionalConditions(optionalCondition);
        percentageBean.setApplicationType(ApplicationType.STB);
        percentageBean.setActive(true);
        return percentageBean;
    }

    private List<PercentageBean> createAndSavePercentageBeans(Integer size) {
        List<PercentageBean> percentageBeans = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Model model = createAndSaveModel(UUID.randomUUID().toString());
            Environment env = createAndSaveEnvironment(UUID.randomUUID().toString());
            FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig("firmwareConfig_" + i, model.getId(), UUID.randomUUID().toString());
            PercentageBean percentageBean = createPercentageBean(model.getId().toUpperCase(), env.getId().toUpperCase(), firmwareConfig);
            percentageBeanService.create(percentageBean);
            percentageBeans.add(percentageBean);
        }
        return percentageBeans;
    }

    private List<PercentageBean> nullifyUnwantedFields(List<PercentageBean> percentageBeans) {
        for (PercentageBean percentageBean : percentageBeans) {
            percentageBean.getOptionalConditions().setRelation(null);
        }
        return percentageBeans;
    }
}
