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
import com.comcast.xconf.service.GenericNamespacedListLegacyService;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
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
public class IpRuleQueriesController extends BaseQueriesController {

    @Autowired
    private IpRuleService ipRuleService;

    @Autowired
    private GenericNamespacedListLegacyService genericNamespacedListLegacyService;

    @Autowired
    private ModelQueriesService modelService;

    @Autowired
    private EnvironmentQueriesService environmentService;

    @Autowired
    private FirmwareConfigQueriesService firmwareConfigQueriesService;

    private static final Logger log = LoggerFactory.getLogger(IpRuleQueriesController.class);

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_RULES_IPS, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getIPRules(@RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        List<IpRuleBean> ipRules = new ArrayList<>(ipRuleService.getByApplicationType(applicationType));
        for (IpRuleBean bean : ipRules) {
            bean.setFirmwareConfig(QueriesHelper.nullifyUnwantedFields(bean.getFirmwareConfig()));
        }
        return new ResponseEntity<>(ipRules, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_RULES_IPS + "/{ruleName}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getIpRuleById(@PathVariable String ruleName,
                                        @RequestParam(value = "version", required = false) String apiVersion,
                                        @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(ruleName)) {
            return new ResponseEntity<>("Name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        IpRuleBean ipRuleByName = getIpRuleByName(ruleName, applicationType);
        if (ipRuleByName != null) {
            ipRuleByName.setFirmwareConfig(QueriesHelper.nullifyUnwantedFields(ipRuleByName.getFirmwareConfig()));
            return new ResponseEntity<>(ipRuleByName, HttpStatus.OK);
        }
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("IpRule with name " + ruleName + " does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_RULES_IPS + "/byIpAddressGroup/{ipAddressGroupName}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getIpRuleByIpAddressGroup(@PathVariable String ipAddressGroupName,
                                                    @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(ipAddressGroupName)) {
            return new ResponseEntity<>("IpAddressGroup id is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        List<IpRuleBean> ipRules = getIpRuleByIpAddressGroupName(ipAddressGroupName, applicationType);
        if (CollectionUtils.isNotEmpty(ipRules)) {
            return new ResponseEntity<>(ipRules, HttpStatus.OK);
        }

        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATE_RULES_IPS, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity updateIpRule(@RequestBody IpRuleBean ipRuleBean, @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(ipRuleBean.getName())) {
            return new ResponseEntity<>("Name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        validateRuleName(ipRuleBean.getId(), ipRuleBean.getName());
        if (StringUtils.isBlank(ipRuleBean.getEnvironmentId())) {
            return new ResponseEntity<>("Environment id is empty", HttpStatus.BAD_REQUEST);
        }
        ipRuleBean.setEnvironmentId(ipRuleBean.getEnvironmentId().toUpperCase());
        if (!environmentService.isExistEnvironment(ipRuleBean.getEnvironmentId())) {
            return new ResponseEntity<>("Environment " + ipRuleBean.getEnvironmentId() + " is not exist", HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isBlank(ipRuleBean.getModelId())) {
            return new ResponseEntity<>("Model id is empty", HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isNotBlank(applicationType) && !ApplicationType.isValid(applicationType)) {
            return new ResponseEntity<>("ApplicationType is not valid", HttpStatus.BAD_REQUEST);
        }
        ipRuleBean.setModelId(ipRuleBean.getModelId().toUpperCase());
        if (!modelService.isExistModel(ipRuleBean.getModelId())) {
            return new ResponseEntity<>("Model " + ipRuleBean.getModelId() + "is not exist", HttpStatus.BAD_REQUEST);
        }
        FirmwareConfig firmwareConfig = ipRuleBean.getFirmwareConfig();
        if (firmwareConfig != null && StringUtils.isNotBlank(firmwareConfig.getId())) {
            firmwareConfig = firmwareConfigQueriesService.getById(firmwareConfig.getId());
        }
        if (firmwareConfig != null && !ApplicationType.equals(applicationType, firmwareConfig.getApplicationType())) {
            return new ResponseEntity<>("ApplicationType of FirmwareRule and FirmwareConfig does not match", HttpStatus.BAD_REQUEST);
        }
        if (firmwareConfig != null && !firmwareConfigQueriesService.isValidFirmwareConfigByModelIds(Sets.newHashSet(ipRuleBean.getModelId()), firmwareConfig, applicationType)) {
            return new ResponseEntity<>("Firmware config does not support this model", HttpStatus.BAD_REQUEST);
        }
        if (firmwareConfig != null && !ApplicationType.equals(applicationType, firmwareConfig.getApplicationType())) {
            return new ResponseEntity<>("ApplicationType of FirmwareRule and FirmwareConfig does not match", HttpStatus.BAD_REQUEST);
        }
        if (ipRuleBean.getIpAddressGroup() == null) {
            return new ResponseEntity<>("Ip address group is not specified", HttpStatus.BAD_REQUEST);
        }
        if (ipRuleBean.getIpAddressGroup() != null && genericNamespacedListLegacyService.isChangedIpAddressGroup(ipRuleBean.getIpAddressGroup())) {
            return new ResponseEntity<>("IP address group is not matched by existed IP address group", HttpStatus.BAD_REQUEST);
        }

        for (IpRuleBean oldBean : ipRuleService.getByApplicationType(applicationType)) {
            if (StringUtils.equals(oldBean.getName(), ipRuleBean.getName())) {
                if (StringUtils.isBlank(ipRuleBean.getId())) {
                    ipRuleBean.setId(oldBean.getId());
                } else if (!StringUtils.equals(oldBean.getId(), ipRuleBean.getId())) {
                    return new ResponseEntity<>("Ip rule with current name exists.", HttpStatus.BAD_REQUEST);
                } // else id is set correctly
                break;
            }
        }
        ipRuleService.save(ipRuleBean, applicationType);
        return new ResponseEntity<>(ipRuleBean, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = QueryConstants.DELETE_RULES_IPS + "/{name}")
    public ResponseEntity deleteIpRule(@PathVariable String name,
                                       @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("Name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        IpRuleBean bean = getIpRuleByName(name, applicationType);
        if (bean != null) {
            ipRuleService.delete(bean.getId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private IpRuleBean getIpRuleByName(String ruleName, String applicationType) {
        for (IpRuleBean rule : ipRuleService.getByApplicationType(applicationType)) {
            if (StringUtils.equals(ruleName, rule.getName())) {
                return rule;
            }
        }
        return null;
    }

    private List<IpRuleBean> getIpRuleByIpAddressGroupName(String ipAddressGroupName, String applicationType) {
        List<IpRuleBean> ipRules = new ArrayList<>();
        for (IpRuleBean rule : ipRuleService.getByApplicationType(applicationType)) {
            if (rule.getIpAddressGroup() != null && ipAddressGroupName.equals(rule.getIpAddressGroup().getName())) {
                ipRules.add(nullifyUnwantedFields(rule));
            }
        }
        return ipRules;
    }

    private IpRuleBean nullifyUnwantedFields(IpRuleBean bean) {
        if (bean.getFirmwareConfig() != null) {
            QueriesHelper.nullifyUnwantedFields(bean.getFirmwareConfig());
        }
        return bean;
    }

}
