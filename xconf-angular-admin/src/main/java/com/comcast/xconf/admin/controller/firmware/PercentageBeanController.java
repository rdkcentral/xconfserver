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
 *  Created: 6:14 PM
 */
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.estbfirmware.PercentFilterVo;
import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.estbfirmware.PercentageBeanService;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.service.firmware.FirmwareRuleService;
import com.comcast.xconf.shared.controller.AbstractController;
import com.comcast.xconf.shared.service.AbstractService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(PercentageBeanController.URL_MAPPING)
public class PercentageBeanController extends AbstractController<PercentageBean> {

    public static final String URL_MAPPING = "api/percentfilter/percentageBean";

    @Autowired
    private PercentageBeanService percentageBeanService;

    @Autowired
    private FirmwareRuleService firmwareRuleService;

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.ENV_MODEL_PERCENTAGE_BEAN.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ENV_MODEL_PERCENTAGE_BEANS.getName();
    }

    @Override
    public AbstractService<PercentageBean> getService() {
        return percentageBeanService;
    }

    @RequestMapping(method = RequestMethod.GET, params = "export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportAll() {
        List<PercentageBean> percentageBeens = percentageBeanService.getAll();
        HttpHeaders headers = Utils.createContentDispositionHeader(getAllEntitiesExportName() + percentageBeanService.getApplicationTypeSuffix());
        return new ResponseEntity<>(new PercentFilterVo(null, percentageBeens), headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}", params = "export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportOne(@PathVariable String id) {
        PercentageBean percentageBean = percentageBeanService.getOne(id);
        HttpHeaders headers = Utils.createContentDispositionHeader(getOneEntityExportName() + percentageBean.getId() + percentageBeanService.getApplicationTypeSuffix());
        return new ResponseEntity<>(new PercentFilterVo(null, Collections.singletonList(percentageBean)), headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/asRule/{id}", params = "export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportOneAsRule(@PathVariable String id) {
        FirmwareRule percentageBeanRule = firmwareRuleService.getOne(id);
        HttpHeaders headers = Utils.createContentDispositionHeader(ExportFileNames.ENV_MODEL_PERCENTAGE_AS_RULE.getName() + percentageBeanRule.getId() + percentageBeanService.getApplicationTypeSuffix());
        return new ResponseEntity<>(Lists.newArrayList(percentageBeanRule), headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/allAsRules", params = "export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportAllAsRules() {
        Iterable<String> keys = percentageBeanService.getEntityDAO().getKeys();
        List<FirmwareRule> percentageBeansAsRule = firmwareRuleService.getEntityDAO().getAll(Sets.newHashSet(keys));
        HttpHeaders headers = Utils.createContentDispositionHeader(ExportFileNames.ENV_MODEL_PERCENTAGE_AS_RULES.getName() + percentageBeanService.getApplicationTypeSuffix());
        return new ResponseEntity<>(percentageBeansAsRule, headers, HttpStatus.OK);
    }

}