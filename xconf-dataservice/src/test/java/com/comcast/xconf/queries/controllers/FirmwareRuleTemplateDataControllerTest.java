/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.search.SearchFields;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.comcast.xconf.util.ImportHelper.IMPORTED;
import static com.comcast.xconf.util.ImportHelper.NOT_IMPORTED;

public class FirmwareRuleTemplateDataControllerTest extends BaseQueriesControllerTest {

    @Before
    @After
    public void cleanUp() {
        for (FirmwareRuleTemplate template : firmwareRuleTemplateDao.getAll()) {
            firmwareRuleTemplateDao.deleteOne(template.getId());
        }
    }

    @Test
    public void importAll() throws Exception {
        List<FirmwareRuleTemplate> templates = createTemplates(10);
        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put(IMPORTED, getTemplateNames(templates));
        expectedResult.put(NOT_IMPORTED, new ArrayList<String>());

        performPostAndVerifyResponse(FirmwareRuleTemplateDataController.API_URL + "/importAll", templates, expectedResult);
    }

    @Test
    public void importAllWithException() throws Exception {
        FirmwareRuleTemplate templateWithException = createTemplate(UUID.randomUUID().toString(), null, new Rule(), 1);
        List<FirmwareRuleTemplate> templatesToImport = Collections.singletonList(templateWithException);
        Map<String, List<String>> expectedResult = new HashMap<>();
        expectedResult.put(IMPORTED, new ArrayList<String>());
        expectedResult.put(NOT_IMPORTED, getTemplateNames(templatesToImport));

        performPostAndVerifyResponse(FirmwareRuleTemplateDataController.API_URL + "/importAll", templatesToImport, expectedResult);

    }

    @Test
    public void getAll() throws Exception {
        List<FirmwareRuleTemplate> templates = save(createTemplates(10));

        performGetAndVerify(FirmwareRuleTemplateDataController.API_URL + "/filtered", new HashMap<String, String>(), templates);
    }

    @Test
    public void getByType() throws Exception {
        List<FirmwareRuleTemplate> templates = save(createTemplates(3));
        Map<String, String> searchParams = Collections.singletonMap(SearchFields.NAME.toLowerCase(), templates.get(0).getName());
        String url = FirmwareRuleTemplateDataController.API_URL + "/filtered";

        performGetAndVerify(url, searchParams, Collections.singletonList(templates.get(0)));
        performGetAndVerify(url, new HashMap<String, String>(), templates);
    }

    private FirmwareRuleTemplate createTemplate(String id, String firmwareConfigId, Rule rule, Integer priority) {
        FirmwareRuleTemplate template = new FirmwareRuleTemplate();
        template.setId(id);
        template.setRule(rule);
        template.setApplicableAction(createRuleAction(ApplicableAction.Type.RULE_TEMPLATE, firmwareConfigId));
        template.setPriority(priority);
        return template;
    }

    private List<FirmwareRuleTemplate> createTemplates(Integer size) {
        List<FirmwareRuleTemplate> templates = new ArrayList<>();
        Model model = createAndSaveModel(defaultModelId);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId(), FirmwareConfig.DownloadProtocol.http);
        for(int i = 0; i < size; i++) {
            FirmwareRuleTemplate template = createTemplate(UUID.randomUUID().toString(), firmwareConfig.getId(), createRule("key" + i, "value" + i), i + 1);
            templates.add(template);
        }
        return templates;
    }

    private List<FirmwareRuleTemplate> save(List<FirmwareRuleTemplate> templates) {
        for (FirmwareRuleTemplate template : templates) {
            firmwareRuleTemplateDao.setOne(template.getId(), template);
        }
        return templates;
    }

    private List<String> getTemplateNames(List<FirmwareRuleTemplate> templates) {
        List<String> names = new ArrayList<>();
        for (FirmwareRuleTemplate template : templates) {
            names.add(template.getName());
        }
        return names;
    }
}