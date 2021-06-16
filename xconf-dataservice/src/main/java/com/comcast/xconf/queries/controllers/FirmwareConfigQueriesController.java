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

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.FirmwareConfigQueriesService;
import com.comcast.xconf.estbfirmware.ModelQueriesService;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.queries.QueryConstants;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class FirmwareConfigQueriesController extends BaseQueriesController {

    public static final String MAX_ALLOWED_NUMBER_OF_PROPERTIES_ERR_MSG_TEMPLATE = "Max allowed number of properties is %s";
    public static final int MAX_ALLOWED_NUMBER_OF_PROPERTIES = 20;

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private ModelQueriesService modelService;

    @Autowired
    private FirmwareConfigQueriesService firmwareConfigQueriesService;

    private static final Logger log = LoggerFactory.getLogger(FirmwareConfigQueriesController.class);


    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FIRMWARES, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public ResponseEntity getFirmwareConfigs(@RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        List<FirmwareConfig> configs = firmwareConfigQueriesService.getByApplicationType(applicationType)
                .stream().map(QueriesHelper::nullifyUnwantedFields)
                .collect(Collectors.toList());
        return new ResponseEntity<>(configs, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FIRMWARES + "/model/{modelId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public ResponseEntity getFirmwareConfigsByModel(@PathVariable String modelId,
                                                    @RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        List<FirmwareConfig> firmwareConfigs = firmwareConfigQueriesService.getFirmwareConfigsByModelIdsAndApplication(Sets.newHashSet(modelId), applicationType)
                .stream()
                .map(QueriesHelper::nullifyUnwantedFields).collect(Collectors.toList());
        return new ResponseEntity<>(firmwareConfigs, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FIRMWARES + "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getFirmwareConfigById(@PathVariable String id, @RequestParam(value = "version", required = false) String apiVersion) {
        if (StringUtils.isBlank(id)) {
            return new ResponseEntity<>("Id is empty", HttpStatus.BAD_REQUEST);
        }
        FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne(id);
        if (firmwareConfig != null) {
            return ResponseEntity.ok(QueriesHelper.nullifyUnwantedFields(firmwareConfig));
        }
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("FirmwareConfig with id " + id + " does not exist", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(null);
    }

    @RequestMapping(method = RequestMethod.PUT, value = QueryConstants.UPDATE_FIRMWARES, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity updateFirmwareConfig(@RequestBody FirmwareConfig firmwareConfig,
                                               @RequestParam(required = false) String applicationType) {
        String errorMessage = validateFirmwareConfig(firmwareConfig, applicationType);
        if (StringUtils.isNotBlank(errorMessage)) {
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
        firmwareConfig.setApplicationType(ApplicationType.get(applicationType));
        Set<String> modelIds = new HashSet<>();
        for (String modelId : firmwareConfig.getSupportedModelIds()) {
            modelIds.add(modelId.toUpperCase());
        }
        firmwareConfig.setSupportedModelIds(modelIds);

        if (firmwareConfigDAO.getOne(firmwareConfig.getId()) == null) {
            return new ResponseEntity<>("Firmware Config with current id: " + firmwareConfig.getId() + " does not exist", HttpStatus.NOT_FOUND);
        }
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        return new ResponseEntity<>(firmwareConfig, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATE_FIRMWARES, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity createFirmwareConfig(@RequestBody FirmwareConfig firmwareConfig,
                                               @RequestParam(required = false) String applicationType) {
        String errorMessage = validateFirmwareConfig(firmwareConfig, applicationType);
        if (StringUtils.isNotBlank(errorMessage)) {
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
        firmwareConfig.setApplicationType(ApplicationType.get(applicationType));
        if (!firmwareConfigQueriesService.validateName(firmwareConfig, applicationType)) {
            return new ResponseEntity<>("This description is already used", HttpStatus.BAD_REQUEST);
        }
        Set<String> modelIds = new HashSet<>();
        for (String modelId : firmwareConfig.getSupportedModelIds()) {
            modelIds.add(modelId.toUpperCase());
        }
        firmwareConfig.setSupportedModelIds(modelIds);

        for (String modelId : firmwareConfig.getSupportedModelIds()) {
            if (!modelService.isExistModel(modelId)) {
                return new ResponseEntity<>("Model: " + modelId + " is not exist", HttpStatus.BAD_REQUEST);
            }
        }

        if (StringUtils.isBlank(firmwareConfig.getId())) {
            firmwareConfig.setId(UUID.randomUUID().toString());
        }
        if (StringUtils.isNotBlank(applicationType)) {
            firmwareConfig.setApplicationType(applicationType);
        }
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);

        return new ResponseEntity<>(firmwareConfig, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = QueryConstants.DELETE_FIRMWARES + "/{id}")
    public ResponseEntity deleteFirmwareConfig(@PathVariable String id) {
        if (StringUtils.isBlank(id)) {
            return new ResponseEntity<>("Id is empty", HttpStatus.BAD_REQUEST);
        }

        if (firmwareConfigDAO.getOne(id) != null) {
            firmwareConfigDAO.deleteOne(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.QUERIES_FIRMWARES + "/bySupportedModels", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getFirmwareConfigsBySupportedModels(@RequestBody Set<String> modelIds, @RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        if (modelIds != null && CollectionUtils.isNotEmpty(modelIds)) {
            List<FirmwareConfig> firmwareConfigs = firmwareConfigQueriesService.getFirmwareConfigsByModelIdsAndApplication(modelIds, applicationType);
            for (FirmwareConfig firmwareConfig : firmwareConfigs) {
                QueriesHelper.nullifyUnwantedFields(firmwareConfig);
            }
            return ResponseEntity.ok(firmwareConfigs);
        }
        return new ResponseEntity<>("Model list is empty", HttpStatus.BAD_REQUEST);
    }

    private String validateFirmwareConfig(FirmwareConfig firmwareConfig, String applicationType) {
        if(firmwareConfig == null) {
            return "Firmware config is not present";
        }
        if (StringUtils.isBlank(firmwareConfig.getDescription())) {
            return "Description is empty";
        }
        if (StringUtils.isBlank(firmwareConfig.getFirmwareFilename())) {
            return "File name is empty";
        }
        if (StringUtils.isBlank(firmwareConfig.getFirmwareVersion())) {
            return "Version is empty";
        }
        if (StringUtils.isNotBlank(applicationType) && !ApplicationType.isValid(applicationType)) {
            return "ApplicationType " + applicationType + " is not valid";
        }
        if (CollectionUtils.isEmpty(firmwareConfig.getSupportedModelIds())) {
            return "Supported model list is empty";
        }
        if (StringUtils.isNotBlank(applicationType) && !ApplicationType.isValid(applicationType)) {
            return "ApplicationType is not valid";
        }
        for (String modelId : firmwareConfig.getSupportedModelIds()) {
            if (!modelService.isExistModel(modelId)) {
                return "Model: " + modelId + " is not exist";
            }
        }

        Map<String, String> properties = firmwareConfig.getProperties();
        if (MapUtils.isNotEmpty(properties)) {
            for (Map.Entry<String, String> parameter : firmwareConfig.getProperties().entrySet()) {
                if (StringUtils.isBlank(parameter.getKey())) {
                    throw new ValidationRuntimeException("Key is empty");
                }
            }
        }

        if (MapUtils.isNotEmpty(properties) && properties.size() > MAX_ALLOWED_NUMBER_OF_PROPERTIES) {
            throw new ValidationRuntimeException(String.format(MAX_ALLOWED_NUMBER_OF_PROPERTIES_ERR_MSG_TEMPLATE, MAX_ALLOWED_NUMBER_OF_PROPERTIES));
        }

        return null;
    }
}
