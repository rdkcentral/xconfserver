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
package com.comcast.xconf.queries.controllers;

import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.queries.QueryConstants;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class EnvModelRuleQueriesController extends BaseQueriesController {

    @Autowired
    EnvModelRuleService envModelRuleService;

    @Autowired
    private EnvironmentQueriesService environmentService;

    @Autowired
    private ModelQueriesService modelService;

    @Autowired
    private FirmwareConfigQueriesService firmwareConfigQueriesService;

    private static final Logger log = LoggerFactory.getLogger(EnvModelRuleQueriesController.class);


    @RequestMapping(value= QueryConstants.QUERIES_RULES_ENV_MODEL, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getEnvModelRules(@RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        List<EnvModelRuleBean> beans = new ArrayList<>(envModelRuleService.getByApplicationType(applicationType));
        for (EnvModelRuleBean bean : beans) {
            bean.setFirmwareConfig(QueriesHelper.nullifyUnwantedFields(bean.getFirmwareConfig()));
        }
        return new ResponseEntity<>(beans, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_RULES_ENV_MODEL + "/{name}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getEnvModelRule(@PathVariable String name,
                                          @RequestParam(value = "version", required = false) String apiVersion,
                                          @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("EnvModelRule name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        EnvModelRuleBean envModelRule = envModelRuleService.getOneByName(name, applicationType);

        if (envModelRule == null
                && ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("EnvModelRule with name " + name + " does not exist", HttpStatus.NOT_FOUND);
        }
        if (envModelRule == null) {
            return ResponseEntity.ok(null);
        }
        envModelRule.setFirmwareConfig(QueriesHelper.nullifyUnwantedFields(envModelRule.getFirmwareConfig()));
        return new ResponseEntity<>(envModelRule, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATE_RULES_ENV_MODEL, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity updateEnvModelRule(@RequestBody EnvModelRuleBean bean, @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(bean.getName())) {
            return new ResponseEntity<>("Name is empty", HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isBlank(bean.getEnvironmentId())) {
            return new ResponseEntity<>("Environment is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        validateRuleName(bean.getId(), bean.getName());
        if (!environmentService.isExistEnvironment(bean.getEnvironmentId())) {
            return new ResponseEntity<>("Environment is not exist", HttpStatus.BAD_REQUEST);
        }
        bean.setEnvironmentId(bean.getEnvironmentId().toUpperCase());
        if (StringUtils.isBlank(bean.getModelId())) {
            return new ResponseEntity<>("Model is empty", HttpStatus.BAD_REQUEST);
        }
        if (!modelService.isExistModel(bean.getModelId())) {
            return new ResponseEntity<>("Model is not exist", HttpStatus.BAD_REQUEST);
        }
        FirmwareConfig firmwareConfig = bean.getFirmwareConfig();
        if (firmwareConfig != null && StringUtils.isNotBlank(firmwareConfig.getId())) {
            firmwareConfig = firmwareConfigQueriesService.getById(firmwareConfig.getId());
        }
        if (firmwareConfig != null && !ApplicationType.equals(applicationType, firmwareConfig.getApplicationType())) {
            return new ResponseEntity<>("ApplicationType of EnvModelRule and FirmwareConfig does not match", HttpStatus.BAD_REQUEST);
        }
        if (firmwareConfig != null && !firmwareConfigQueriesService.isValidFirmwareConfigByModelIds(Sets.newHashSet(bean.getModelId()), firmwareConfig, applicationType)) {
            return new ResponseEntity<>("FirmwareConfig does not support this model", HttpStatus.BAD_REQUEST);
        }
        if (firmwareConfig != null) {
            bean.setFirmwareConfig(firmwareConfig);
        }

        bean.setModelId(bean.getModelId().toUpperCase());
        for (EnvModelRuleBean ruleBean : envModelRuleService.getByApplicationType(applicationType)) {
            if (ruleBean.getName().equalsIgnoreCase(bean.getName()) && !ruleBean.getId().equalsIgnoreCase(bean.getId())) {
                return new ResponseEntity<>("Name is already used", HttpStatus.BAD_REQUEST);
            }
            if (ruleBean.getEnvironmentId().equalsIgnoreCase(bean.getEnvironmentId()) &&
                    ruleBean.getModelId().equalsIgnoreCase(bean.getModelId())) {
                return new ResponseEntity<>("Env/Model overlap with rule: " + ruleBean.getName(), HttpStatus.BAD_REQUEST);
            }
        }

        envModelRuleService.save(bean, applicationType);
        return new ResponseEntity<>(bean, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = QueryConstants.DELETE_RULES_ENV_MODEL + "/{name}")
    public ResponseEntity deleteEnvModelRuleBean(@PathVariable String name,
                                                 @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("Name is required", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        EnvModelRuleBean rule = envModelRuleService.getOneByName(name, applicationType);
        if (rule != null) {
            envModelRuleService.delete(rule.getId());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
