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
 *  Author: mdolina
 *  Created: 12:32 PM
 */
package com.comcast.xconf.estbfirmware.evaluation.percentfilter;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.Environment;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.FirmwareConfigFacade;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BasePercentFilterTestCase extends BaseQueriesControllerTest {

    protected static Model model =  null;
    protected static Environment environment = null;
    protected static GenericNamespacedList macList = null;
    protected static FirmwareConfig firmwareConfig = null;
    protected static FirmwareConfig contextConfig = null;
    protected static FirmwareRuleTemplate envModelRuleTemplate = null;
    protected static FirmwareRule envModelFirmwareRule = null;

    protected void initPercentConditions() {
        model =  createModel("ENV_MODEL_RULE_MODEL_ID");
        modelDAO.setOne(model.getId(), model);

        environment = createEnvironment("ENV_MODEL_RULE_ENVIRONMENT_ID");
        environmentDAO.setOne(environment.getId(), environment);

        macList = createGenericNamespacedList("envModelRuleMacListId", GenericNamespacedListTypes.MAC_LIST, "AA:BB:CC:AA:BB:CC");
        genericNamespacedListDAO.setOne(macList.getId(), macList);

        firmwareConfig = createAndSaveFirmwareConfig("version", model.getId(), FirmwareConfig.DownloadProtocol.http);
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);

        contextConfig = createAndSaveFirmwareConfig("contextVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        firmwareConfigDAO.setOne(contextConfig.getId(), contextConfig);

        envModelRuleTemplate = createFirmwareRuleTemplate(TemplateNames.ENV_MODEL_RULE, createEnvModelRule(environment.getId(), model.getId(), macList.getId()), createRuleAction(ApplicableAction.Type.RULE_TEMPLATE, firmwareConfig.getId()));
        firmwareRuleTemplateDao.setOne(envModelRuleTemplate.getId(), envModelRuleTemplate);

        envModelFirmwareRule = createEnvModelFirmwareRule("envModelRuleName", firmwareConfig.getId(), environment.getId(), model.getId(), macList.getId());
        firmwareRuleDao.setOne(envModelFirmwareRule.getId(), envModelFirmwareRule);
    }

    protected FirmwareConfigFacade createAndNullifyFirmwareConfigFacade(FirmwareConfig firmwareConfig) {
        firmwareConfig.setRebootImmediately(true);
        FirmwareConfigFacade firmwareConfigFacade = new FirmwareConfigFacade(firmwareConfig);
        firmwareConfigFacade.getProperties().remove("id");
        firmwareConfigFacade.getProperties().remove("description");
        firmwareConfigFacade.getProperties().remove("supportedModelIds");
        firmwareConfigFacade.getProperties().remove("updated");
        return firmwareConfigFacade;
    }

    protected void performAndVerifyRequest(FirmwareConfig firmwareConfigForRequest, HttpStatus status, FirmwareConfigFacade expectedConfig) throws Exception {
        ResultActions resultActions = null;
        if (firmwareConfigForRequest !=  null) {
            resultActions = mockMvc.perform(get("/xconf/swu/stb")
                    .param("eStbMac", macList.getData().iterator().next())
                    .param("env", environment.getId())
                    .param("model", model.getId())
                    .param("firmwareVersion", firmwareConfigForRequest.getFirmwareVersion()))
                    .andExpect(status().is(status.value()));
        } else {
            resultActions = mockMvc.perform(get("/xconf/swu/stb")
                    .param("eStbMac", macList.getData().iterator().next())
                    .param("env", environment.getId())
                    .param("model", model.getId()))
                    .andExpect(status().is(status.value()));
        }
        verifyResponseContent(resultActions, status, expectedConfig);
    }

    private void verifyResponseContent(ResultActions resultActions, HttpStatus status, FirmwareConfigFacade expectedConfig) throws Exception {
        if (status().isOk().equals(status().is(status.value())) || status().isCreated().equals(status().is(status.value()))) {
            resultActions.andExpect(content().json(JsonUtil.toJson(expectedConfig)));
        }
    }
}
