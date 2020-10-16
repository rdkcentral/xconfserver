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
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.queries.QueryConstants;
import com.comcast.xconf.queries.beans.EnvModelPercentageWrapper;
import com.comcast.xconf.queries.beans.PercentFilterWrapper;
import com.comcast.xconf.queries.beans.TimeFilterWrapper;
import com.comcast.xconf.service.GenericNamespacedListLegacyService;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Controller, which implements Restful XCONF API to manage blocking filters: Ip Filter, Time Filter and Percent Filter.
 */
@RestController
public class BlockingFilterQueriesController extends BaseQueriesController {

    private static final Logger log = LoggerFactory.getLogger(BlockingFilterQueriesController.class);

    @Autowired
    private PercentFilterService percentFilterService;

    @Autowired
    private IpFilterService ipFilterService;

    @Autowired
    private TimeFilterService timeFilterService;

    @Autowired
    private GenericNamespacedListLegacyService genericNamespacedListLegacyService;

    @Autowired
    private EnvModelRuleService envModelRuleService;

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;


    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_IPS, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getAllIpFilters(@RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        return new ResponseEntity<>(new ArrayList<>(ipFilterService.getByApplicationType(applicationType)), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_IPS + "/{name}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getIpFilterById(@PathVariable String name,
                                          @RequestParam(value = "version", required = false) String apiVersion,
                                          @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("Filter name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        IpFilter ipFilter = ipFilterService.getIpFilterByName(name, applicationType);
        if (ipFilter != null) {
            return new ResponseEntity<>(ipFilter, HttpStatus.OK);
        }
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("IpFilter with name " + name + " does not exist",HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATE_FILTERS_IPS, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity updateIpFilter(@RequestBody IpFilter ipFilter, @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(ipFilter.getName())) {
            return new ResponseEntity<>("IpFilter name is empty", HttpStatus.BAD_REQUEST);
        }
        if (ipFilter.getIpAddressGroup() == null) {
            return new ResponseEntity<>("IpAddressGroup is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        validateRuleName(ipFilter.getId(), ipFilter.getName());
        if (genericNamespacedListLegacyService.isChangedIpAddressGroup(ipFilter.getIpAddressGroup())) {
            return new ResponseEntity<>("IP address group is not matched by existed IP address group", HttpStatus.BAD_REQUEST);
        }
        ipFilterService.save(ipFilter, applicationType);
        return new ResponseEntity<>(ipFilter, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = QueryConstants.DELETE_FILTERS_IPS + "/{name}")
    public ResponseEntity removeIpFilter(@PathVariable String name,
                                         @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("Name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        IpFilter ipFilter = ipFilterService.getIpFilterByName(name, applicationType);
        if (ipFilter != null) {
            ipFilterService.delete(ipFilter.getId());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_PERCENT,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public ResponseEntity getPercentFilter(@RequestParam(required = false) String field,
                                           @RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        if (StringUtils.isBlank(field)) {
            PercentFilterWrapper filter = new PercentFilterWrapper(percentFilterService.get(applicationType));
            return new ResponseEntity<>(QueriesHelper.nullifyUnwantedFields(percentFilterService.toHumanReadableForm(filter)), HttpStatus.OK);
        } else {
            try {
                Map<String, Set<Object>> fieldValues = percentFilterService.getPercentFilterFieldValues(field, applicationType);
                return new ResponseEntity<>(fieldValues, HttpStatus.OK);
            } catch (IllegalAccessException e) {
                log.error("Exception: " + e.getMessage());
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (EntityNotFoundException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }
        }
    }


    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATE_FILTERS_PERCENT, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity updatePercentFilter(@RequestBody PercentFilterWrapper filter, @RequestParam(required = false) String applicationType) {
        if (String.valueOf(filter.getPercentage()).contains("-")) {
            return new ResponseEntity<>("Percentage has negative value", HttpStatus.BAD_REQUEST);
        }
        if (filter.getPercentage() < 0 || filter.getPercentage() > 100) {
            return new ResponseEntity<>("Percentage should be within [0, 100]", HttpStatus.BAD_REQUEST);
        }
        if (filter.getWhitelist() != null && genericNamespacedListLegacyService.isChangedIpAddressGroup(filter.getWhitelist())) {
            return new ResponseEntity<>("IP address group is not matched by existed IP address group", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        if (filter.getEnvModelPercentageWrappers() != null) {
            for (EnvModelPercentageWrapper percentage : filter.getEnvModelPercentageWrappers()) {
                if (percentage != null) {
                    Set<String> firmwareVersions = percentage.getFirmwareVersions();
                    if (percentage.isFirmwareCheckRequired() && (firmwareVersions == null || firmwareVersions.isEmpty())) {
                        return new ResponseEntity<>("FirmwareVersion is required", HttpStatus.BAD_REQUEST);
                    }

                    String lastKnownGood = percentage.getLastKnownGood();
                    if (StringUtils.isNotBlank(lastKnownGood)) {
                        if (percentage.getPercentage() == 100.0) {
                            return new ResponseEntity<>("Can't set LastKnownGood when percentage=100", HttpStatus.BAD_REQUEST);
                        }
                        if (!percentage.isActive()) {
                            return new ResponseEntity<>("Can't set LastKnownGood when filter is not active", HttpStatus.BAD_REQUEST);
                        }
                        String configId = getFirmwareConfigId(lastKnownGood, applicationType);
                        if (configId == null) {
                            return new ResponseEntity<>("No version in firmware configs matches LastKnownGood value: " + lastKnownGood, HttpStatus.BAD_REQUEST);
                        } else {
                            percentage.setLastKnownGood(configId);
                        }
                    }

                    String intermediateVersion = percentage.getIntermediateVersion();
                    if (StringUtils.isNotBlank(intermediateVersion)) {
                        if (! percentage.isFirmwareCheckRequired()) {
                            return new ResponseEntity<>("Can't set IntermediateVersion when firmware check is disabled", HttpStatus.BAD_REQUEST);
                        }
                        String configId = getFirmwareConfigId(intermediateVersion, applicationType);
                        if (configId == null) {
                            return new ResponseEntity<>("No version in firmware configs matches IntermediateVersion value: " + intermediateVersion, HttpStatus.BAD_REQUEST);
                        }

                        percentage.setIntermediateVersion(configId);
                    }

                    if (String.valueOf(percentage.getPercentage()).contains("-")) {
                        return new ResponseEntity<>("Percentage has negative value", HttpStatus.BAD_REQUEST);
                    }
                    if (percentage.getPercentage() < 0 || percentage.getPercentage() > 100) {
                        return new ResponseEntity<>("percentage should be within [0, 100]", HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }

        percentFilterService.save(filter.toPercentFilterValue(), applicationType);
        return new ResponseEntity<>(filter, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_TIME, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getTimeFilters(@RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        ArrayList<TimeFilterWrapper> results = new ArrayList<>();
        for (TimeFilter timeFilter : timeFilterService.getByApplicationType(applicationType)) {
            results.add(new TimeFilterWrapper(timeFilter));
        }
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_TIME + "/{name}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getTimeFilterById(@PathVariable String name,
                                            @RequestParam(value = "version", required = false) String apiVersion,
                                            @RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        TimeFilter timeFilter = timeFilterService.getByName(name, applicationType);
        if (timeFilter != null) {
            return new ResponseEntity<>(new TimeFilterWrapper(timeFilter), HttpStatus.OK);
        }
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("TimeFilter with name " + name + " does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATE_FILTERS_TIME, consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity updateTimeFilter(@RequestBody TimeFilterWrapper filter,
                                           @RequestParam(required = false) String applicationType) {
        if (filter == null) {
            return new ResponseEntity<>("Filter is null", HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isBlank(filter.getName())) {
            return new ResponseEntity<>("Name is blank", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        validateRuleName(filter.getId(), filter.getName());
        TimeFilter timeFilterByName = timeFilterService.getByName(filter.getName(), applicationType);
        if (StringUtils.isBlank(filter.getId())
                && timeFilterByName != null) {
            return new ResponseEntity<>("Name is already used", HttpStatus.BAD_REQUEST);
        }

        if (filter.getIpWhitelist() != null && genericNamespacedListLegacyService.isChangedIpAddressGroup(filter.getIpWhitelist())) {
            return new ResponseEntity<>("IP address group is not matched by existed IP address group", HttpStatus.BAD_REQUEST);
        }
        if (filter.getEnvModelWhitelist() != null && !envModelRuleService.isExistEnvModelRule(filter.getEnvModelWhitelist(), applicationType)) {
            return new ResponseEntity<>("Env/Model does not match with existed Env/Model", HttpStatus.BAD_REQUEST);
        }
        if (filter.getEnvModelWhitelist() != null
                && filter.getEnvModelWhitelist().getEnvironmentId() != null
                && filter.getEnvModelWhitelist().getModelId() != null) {
            filter.getEnvModelWhitelist().setEnvironmentId(filter.getEnvModelWhitelist().getEnvironmentId().toUpperCase());
            filter.getEnvModelWhitelist().setModelId(filter.getEnvModelWhitelist().getModelId().toUpperCase());
        }
        timeFilterService.save(filter.toTimeFilter(), applicationType);
        return new ResponseEntity<>(filter, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = QueryConstants.DELETE_FILTERS_TIME + "/{name}")
    public ResponseEntity removeTimeFilter(@PathVariable String name,
                                           @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("Name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        TimeFilter filter = timeFilterService.getByName(name, applicationType);
        if (filter != null) {
            timeFilterService.delete(filter.getId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    private String getFirmwareConfigId(String version, String applicationType) {
        Iterable<FirmwareConfig> configs = Optional.presentInstances(firmwareConfigDAO.asLoadingCache().asMap().values());
        for (FirmwareConfig config : configs) {
            if (StringUtils.equals(version, config.getFirmwareVersion()) && ApplicationType.equals(applicationType, config.getApplicationType())) {
                return config.getId();
            }
        }
        return null;
    }
}
