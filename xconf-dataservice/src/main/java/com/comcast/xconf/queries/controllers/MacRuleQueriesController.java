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
 * Author: ikostrov
 * Created: 02.09.15 14:57
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.NamespacedList;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.queries.beans.MacRuleBeanWrapper;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.comcast.xconf.queries.QueriesHelper.nullifyUnwantedFields;
import static com.comcast.xconf.queries.QueryConstants.*;

@RestController
public class MacRuleQueriesController extends BaseQueriesController {

    private static final Logger log = LoggerFactory.getLogger(MacRuleQueriesController.class);

    @Autowired
    private MacRuleService macRuleService;

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private CachedSimpleDao<String, Model> modelDAO;

    @Autowired
    private ModelQueriesService modelService;

    @Autowired
    private FirmwareConfigQueriesService firmwareConfigQueriesService;

    @Autowired
    private GenericNamespacedListLegacyService genericNamespacedListLegacyService;

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_RULES_MAC,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getMACRules(@RequestParam(value = "version", required = false) String apiVersion,
                                      @RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        Set<MacRuleBeanWrapper> beans = macRuleService.getRulesWithMacCondition(applicationType);
        List<MacRuleBean> result = new ArrayList<>();
        for (MacRuleBeanWrapper bean : beans) {

            result.add(nullifyUnwantedFields(wrap(bean, apiVersion)));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_RULES_MAC + "/{ruleName}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getMACRule(@PathVariable String ruleName,
                                     @RequestParam(value = "version", required = false) String apiVersion,
                                     @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(ruleName)) {
            return new ResponseEntity<>("Name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        MacRuleBeanWrapper bean = macRuleService.getRuleWithMacConditionByName(ruleName, applicationType);
        if (bean != null) {
            return new ResponseEntity<>(nullifyUnwantedFields(wrap(bean, apiVersion)), HttpStatus.OK);
        }
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("MAcRule with name " + ruleName + " does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(value = QUERIES_RULES_MAC + "/address/{macAddress}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getMACRulesByMAC(@PathVariable String macAddress,
                                           @RequestParam(value = "version", required = false) String apiVersion,
                                           @RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        List<MacRuleBean> result = new ArrayList<>();
        if (MacAddress.isValid(macAddress)) {
            for (MacRuleBeanWrapper macRule : macRuleService.searchMacRules(macAddress, applicationType)) {
                macRule = wrap(macRule, apiVersion);
                result.add(nullifyUnwantedFields(macRule));
            }
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    protected MacRuleBeanWrapper wrap(MacRuleBeanWrapper bean, String apiVersion) {
        double version = parseVersion(apiVersion);
        if (version >= 2.0) {
            if (StringUtils.isNotBlank(bean.getMacListRef())) {
                NamespacedList nsList = genericNamespacedListLegacyService.getNamespacedList(bean.getMacListRef());
                Set<String> macs = nsList != null ? nsList.getData() : new HashSet<String>();
                bean.setMacList(macs);
            }
            return bean;
        } else {
            bean.setId(null);
            bean.setMacList(null);
            return bean;
        }

    }

    private double parseVersion(String version) {
        try {
            if (StringUtils.isNotBlank(version)) {
                return Double.parseDouble(version);
            }
        } catch (NumberFormatException e) {
            log.warn("Version number is invalid: " + version + ". Using default version=1");
        }
        return 1.0;
    }

    @RequestMapping(method = RequestMethod.POST, value = UPDATE_RULES_MAC)
    public ResponseEntity saveMACRule(@RequestBody MacRuleBean macRule, @RequestParam(required = false) String applicationType) {

        if (StringUtils.isBlank(macRule.getName())) {
            return new ResponseEntity<>("Rule name is empty.", HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isBlank(macRule.getMacListRef())) {
            return new ResponseEntity<>("MAC address list is empty or blank.", HttpStatus.BAD_REQUEST);
        }

        validateApplicationType(applicationType);
        validateRuleName(macRule.getId(), macRule.getName());

        NamespacedList nsList = genericNamespacedListLegacyService.getNamespacedList(macRule.getMacListRef());
        if (nsList == null) {
            return new ResponseEntity<>("Mac list does not exist.", HttpStatus.BAD_REQUEST);
        }

        if (CollectionUtils.isEmpty(macRule.getTargetedModelIds())) {
            return new ResponseEntity<>("Model list is not specified.", HttpStatus.BAD_REQUEST);
        }
        for (String modelId : macRule.getTargetedModelIds()) {
            if (!modelService.isExistModel(modelId)) {
                return new ResponseEntity<>("Model " + modelId + " is not exist", HttpStatus.BAD_REQUEST);
            }
        }

        if (macRule.getFirmwareConfig() == null || StringUtils.isBlank(macRule.getFirmwareConfig().getId())) {
            return new ResponseEntity<>("Firmware configuration is not specified.", HttpStatus.BAD_REQUEST);
        }
        FirmwareConfig firmwareConfig = firmwareConfigQueriesService.getById(macRule.getFirmwareConfig().getId());
        if (!ApplicationType.equals(firmwareConfig.getApplicationType(), applicationType)) {
            return new ResponseEntity<>("ApplicationType of FirmwareConfig and MacRule does not match", HttpStatus.BAD_REQUEST);
        }
        if (!firmwareConfigQueriesService.isValidFirmwareConfigByModelIds(macRule.getTargetedModelIds(), macRule.getFirmwareConfig(), applicationType)) {
            return new ResponseEntity<>("Firmware config does not support this model", HttpStatus.BAD_REQUEST);
        }

        MacRuleBean ruleToUpdate = null;
        for (MacRuleBean rule : macRuleService.getByApplicationType(applicationType)) {
            if (macRule.getName().equals(rule.getName())) {
                ruleToUpdate = rule;
                continue;
            }
            if (StringUtils.equals(macRule.getMacListRef(), rule.getMacListRef())) {
                return new ResponseEntity<>("MAC addresses list is already used in another rule: " + rule.getName(), HttpStatus.BAD_REQUEST);
            }
        }
        HttpStatus status = HttpStatus.CREATED;
        if (ruleToUpdate != null) {
            macRule.setId(ruleToUpdate.getId());
            status = HttpStatus.OK;
        }

        Set<String> models = new HashSet<>(macRule.getTargetedModelIds());
        for (Model model : modelDAO.getAll()) {
            models.remove(model.getId());
        }
        if (!models.isEmpty()) {
            return new ResponseEntity<>("Model list contains not existed models.", HttpStatus.BAD_REQUEST);
        }

        FirmwareConfig firmware = firmwareConfigDAO.getOne(macRule.getFirmwareConfig().getId());
        if (firmware == null) {
            return new ResponseEntity<>("Firmware configuration doesn't exist.", HttpStatus.BAD_REQUEST);
        }

        if (Sets.intersection(firmware.getSupportedModelIds(), macRule.getTargetedModelIds()).isEmpty()) {
            return new ResponseEntity<>("Firmware configuration doesn't support given models.", HttpStatus.BAD_REQUEST);
        }

        if (firmware != null && !ApplicationType.equals(applicationType, firmware.getApplicationType())) {
            return new ResponseEntity<>("ApplicationType of MacRule and FirmwareConfig does not match", HttpStatus.BAD_REQUEST);
        }

        macRule.setFirmwareConfig(firmware);

        macRuleService.save(macRule, applicationType);
        return new ResponseEntity<>(macRule, status);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_RULES_MAC + "/{name}")
    public ResponseEntity deleteMACRule(@PathVariable String name,
                                        @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("Name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        MacRuleBeanWrapper rule = macRuleService.getMacRuleByName(name, applicationType);
        if (rule == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        macRuleService.delete(rule.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
