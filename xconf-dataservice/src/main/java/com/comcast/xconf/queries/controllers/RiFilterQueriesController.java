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
 * Created: 28.08.15 15:46
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.Environment;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.queries.QueryConstants;
import com.comcast.xconf.service.GenericNamespacedListLegacyService;
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

/**
 * Controller, which implements Restful XCONF API to manage RebootImmediatelyFilter.
 */
@RestController
public class RiFilterQueriesController extends BaseQueriesController {

    private static final Logger log = LoggerFactory.getLogger(RiFilterQueriesController.class);

    @Autowired
    private RebootImmediatelyFilterService riFilterService;

    @Autowired
    private CachedSimpleDao<String, Environment> environmentDAO;

    @Autowired
    private CachedSimpleDao<String, Model> modelDAO;

    @Autowired
    private GenericNamespacedListLegacyService genericNamespacedListLegacyService;

    @Autowired
    private ModelQueriesService modelService;

    @Autowired
    private EnvironmentQueriesService environmentService;

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_RI,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getRiFilters(@RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        Set<RebootImmediatelyFilter> beans = riFilterService.getByApplicationType(applicationType);
        return new ResponseEntity<>(new ArrayList<>(beans), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_RI + "/{filterName}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getRiFilter(@PathVariable String filterName,
                                      @RequestParam(value = "version", required = false) String apiVersion,
                                      @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(filterName)) {
            return new ResponseEntity<>("Name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        RebootImmediatelyFilter filter = getFilterByName(filterName, applicationType);
        if (filter != null) {
            return new ResponseEntity<>(filter, HttpStatus.OK);
        }
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("RebootImmediatelyFilter with name " + filterName + " does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATES_FILTERS_RI)
    public ResponseEntity saveRiFilter(@RequestBody RebootImmediatelyFilter filter, @RequestParam(required = false) String applicationType) {

        if (StringUtils.isBlank(filter.getName())) {
            return new ResponseEntity<>("Rule name is empty.", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        validateRuleName(filter.getId(), filter.getName());
        if (StringUtils.isEmpty(filter.getMacAddresses()) && CollectionUtils.isEmpty(filter.getIpAddressGroups())
            && CollectionUtils.isEmpty(filter.getEnvironments()) && CollectionUtils.isEmpty(filter.getModels())) {
            return new ResponseEntity<>("Please specify at least one of filter criteria.", HttpStatus.BAD_REQUEST);
        }
        if (filter.getModels() != null) {
            Set<String> modelIds = new HashSet<>();
            for (String modelId : filter.getModels()) {
                modelIds.add(modelId.toUpperCase());
            }
            filter.setModels(modelIds);
            for (String modelId : filter.getModels()) {
                if (!modelService.isExistModel(modelId)) {
                    return new ResponseEntity<>("Model " + modelId + " is not exist", HttpStatus.BAD_REQUEST);
                }
            }
        }

        if (filter.getEnvironments() != null) {
            Set<String> environments = new HashSet<>();
            for (String env : filter.getEnvironments()) {
                environments.add(env.toUpperCase());
            }
            filter.setEnvironments(environments);

            for (String envId : filter.getEnvironments()) {
                if (!environmentService.isExistEnvironment(envId)) {
                    return new ResponseEntity<>("Environment " + envId + " is not exist", HttpStatus.BAD_REQUEST);
                }
            }

        }
        if (CollectionUtils.isNotEmpty(filter.getIpAddressGroups())) {
            for (IpAddressGroup ipAddressGroup : filter.getIpAddressGroups()) {
                if (ipAddressGroup != null && genericNamespacedListLegacyService.isChangedIpAddressGroup(ipAddressGroup)) {
                    return new ResponseEntity<>("IP address group is not matched by existed IP address group", HttpStatus.BAD_REQUEST);
                }
            }
        }

        if (StringUtils.isNotBlank(filter.getMacAddresses())) {
            List<String> invalidMacs = new ArrayList<>();
            for (String ma : filter.getMacAddresses().split("[\\s,]+")) {
                if (!MacAddress.isValid(ma)) {
                    invalidMacs.add(ma);
                }
            }
            if (!invalidMacs.isEmpty()) {
                return new ResponseEntity<>("Please enter a valid MAC address or whitespace delimited list of MAC addresses.", HttpStatus.BAD_REQUEST);
            }
        }
        HttpStatus status = HttpStatus.CREATED;
        RebootImmediatelyFilter filterToUpdate = null;
        for (RebootImmediatelyFilter f : riFilterService.getAllRebootFiltersFromDB()) {
            if (f.getName().equals(filter.getName())) {
                filterToUpdate = f;
            }
        }
        if (filterToUpdate != null) {
            filter.setId(filterToUpdate.getId());
            status = HttpStatus.OK;
        }

        try {
            riFilterService.save(filter, applicationType);
            return new ResponseEntity<>(filter, status);
        } catch (Exception e) {
            log.error("Could not save RebootImmediatelyFilter", e);
            return new ResponseEntity<>("Could not save RebootImmediatelyFilter", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes filter by id or name.
     * @param name filter name
     * @return OK status if deleted
     */
    @RequestMapping(method = RequestMethod.DELETE, value = QueryConstants.DELETES_FILTERS_RI + "/{name}")
    public ResponseEntity deleteFilter(@PathVariable String name, @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("Please provide name.", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        for (RebootImmediatelyFilter filter: riFilterService.getByApplicationType(applicationType)) {
            if (StringUtils.equals(name, filter.getName())) {
                riFilterService.delete(filter.getId());
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private RebootImmediatelyFilter getFilterByName(String filterName, String applicationType) {
        Set<RebootImmediatelyFilter> riFilters = riFilterService.getByApplicationType(applicationType);
        for (RebootImmediatelyFilter filter : riFilters) {
            if (StringUtils.equals(filterName, filter.getName())) {
                return filter;
            }
        }
        return null;
    }
}
