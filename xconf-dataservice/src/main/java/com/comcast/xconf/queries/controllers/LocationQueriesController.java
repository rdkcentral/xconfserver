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
import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.queries.QueryConstants;
import com.comcast.xconf.queries.beans.DownloadLocationFilterWrapper;
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

@RestController
public class LocationQueriesController extends BaseQueriesController {

    @Autowired
    private DownloadLocationFilterService downloadLocationFilterService;

    @Autowired
    private CachedSimpleDao<String, SingletonFilterValue> singletonFilterValueDAO;

    @Autowired
    private LocationFilterService locationFilterService;

    @Autowired
    private GenericNamespacedListLegacyService genericNamespacedListLegacyService;

    @Autowired
    private ModelQueriesService modelService;

    @Autowired
    private EnvironmentQueriesService environmentService;

    private static final Logger log = LoggerFactory.getLogger(LocationQueriesController.class);

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_LOCATION, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getLocationFilters(@RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        List<DownloadLocationFilterWrapper> beans = new ArrayList<>();
        for(DownloadLocationFilter filter : downloadLocationFilterService.getByApplicationType(applicationType)) {
            beans.add(new DownloadLocationFilterWrapper(filter));
        }
        return new ResponseEntity<>(beans, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_LOCATION + "/byName/{name}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public ResponseEntity getLocationFilterByName(@PathVariable String name,
                                                  @RequestParam(value = "version", required = false) String apiVersion,
                                                  @RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        log.info("getLocationFilter ruleName=" + name);
        if (StringUtils.isNotEmpty(name)) {
            Set<DownloadLocationFilter> beans = downloadLocationFilterService.getByApplicationType(applicationType);
            for (DownloadLocationFilter bean : beans) {
                if (name.equals(bean.getName())) {
                    return ResponseEntity.ok(new DownloadLocationFilterWrapper(bean));
                }
            }
        }
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("LocationFilter with name " + name + " does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_LOCATION + "/{name}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getLocationFilterById(@PathVariable String name,
                                                @RequestParam(value = "version", required = false) String apiVersion,
                                                @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("Name is empty", HttpStatus.OK);
        }
        validateApplicationType(applicationType);
        DownloadLocationFilter downloadLocationFilter = downloadLocationFilterService.getOneDwnLocationFilterFromDBByName(name, applicationType);
        if (downloadLocationFilter != null) {
            return new ResponseEntity<>(new DownloadLocationFilterWrapper(downloadLocationFilter), HttpStatus.OK);
        }
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("LocationFilter with id " + name + " does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATE_FILTERS_LOCATION, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity updateLocationFilter(@RequestBody DownloadLocationFilterWrapper locationFilter, @RequestParam(required = false) String applicationType) {
        DownloadLocationFilter existedFilter = downloadLocationFilterService.getOneDwnLocationFilterFromDBByName(locationFilter.getName(), applicationType);
        if (existedFilter != null && StringUtils.isBlank(locationFilter.getId())) {
            return new ResponseEntity<>("Name is already used", HttpStatus.BAD_REQUEST);
        }

        if (locationFilter.getIpAddressGroup() == null && CollectionUtils.isEmpty(locationFilter.getEnvironments()) && CollectionUtils.isEmpty(locationFilter.getModels())) {
            return new ResponseEntity<>("Condition is required", HttpStatus.BAD_REQUEST);
        }

        if (locationFilter.getIpAddressGroup() == null && CollectionUtils.isNotEmpty(locationFilter.getEnvironments()) && CollectionUtils.isEmpty(locationFilter.getModels())) {
            return new ResponseEntity<>("Models are required", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        validateRuleName(locationFilter.getId(), locationFilter.getName());
        if (locationFilter.getModels() != null) {
            Set<String> modelIds = new HashSet<>();
            for (String modelId : locationFilter.getModels()) {
                modelIds.add(modelId.toUpperCase());
            }
            locationFilter.setModels(modelIds);

            for (String modelId : locationFilter.getModels()) {
                if (!modelService.isExistModel(modelId)) {
                    return new ResponseEntity<>("Model " + modelId + " is not exist", HttpStatus.BAD_REQUEST);
                }
            }
        }

        if (locationFilter.getIpAddressGroup() != null && genericNamespacedListLegacyService.isChangedIpAddressGroup(locationFilter.getIpAddressGroup())) {
            return new ResponseEntity<>("IP address group is not matched by existed IP address group", HttpStatus.BAD_REQUEST);
        }

        if (locationFilter.getIpAddressGroup() == null && CollectionUtils.isNotEmpty(locationFilter.getModels()) && CollectionUtils.isEmpty(locationFilter.getEnvironments())) {
            return new ResponseEntity<>("Environments are required", HttpStatus.BAD_REQUEST);
        }

        if (locationFilter.getEnvironments() != null) {
            Set<String> environments = new HashSet<>();
            for (String env : locationFilter.getEnvironments()) {
                environments.add(env.toUpperCase());
            }
            locationFilter.setEnvironments(environments);

            for (String envId : locationFilter.getEnvironments()) {
                if (!environmentService.isExistEnvironment(envId)) {
                    return new ResponseEntity<>("Environment " + envId + "is not exist", HttpStatus.BAD_REQUEST);
                }
            }
        }

        if (Boolean.FALSE.equals(locationFilter.getForceHttp()) && locationFilter.getIpv6FirmwareLocation() != null && locationFilter.getFirmwareLocation() == null) {
            return new ResponseEntity<>("If you are not forcing HTTP, you can't use IPv6 without IPv4 location", HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isBlank(locationFilter.getHttpLocation()) && locationFilter.getFirmwareLocation() == null) {
            return new ResponseEntity<>("Any location are required", HttpStatus.BAD_REQUEST);
        }

        if (Boolean.TRUE.equals(locationFilter.getForceHttp()) && StringUtils.isBlank(locationFilter.getHttpLocation())) {
            return new ResponseEntity<>("HTTP location is required", HttpStatus.BAD_REQUEST);
        }

        if (locationFilter.getFirmwareLocation() != null) {
            if (locationFilter.getFirmwareLocation().isIpv6()) {
                return new ResponseEntity<>("Version is invalid", HttpStatus.BAD_REQUEST);
            } else if (locationFilter.getFirmwareLocation().isCidrBlock()) {
                return new ResponseEntity<>("IP addresss is invalid", HttpStatus.BAD_REQUEST);
            }
        }

        if (locationFilter.getIpv6FirmwareLocation() != null) {
            if (!locationFilter.getIpv6FirmwareLocation().isIpv6()) {
                return new ResponseEntity<>("Version is invalid", HttpStatus.BAD_REQUEST);
            } else if (locationFilter.getIpv6FirmwareLocation().isCidrBlock()) {
                return new ResponseEntity<>("IP address is invalid", HttpStatus.BAD_REQUEST);
            }
        }

        downloadLocationFilterService.save(locationFilter, applicationType);
        return new ResponseEntity<>(new DownloadLocationFilterWrapper(locationFilter), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = QueryConstants.DELETE_FILTERS_LOCATION + "/{name}")
    public ResponseEntity deleteLocationFilter(@PathVariable String name,
                                               @RequestParam(required = false) String applicationType) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("Name is empty", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        DownloadLocationFilter filter = downloadLocationFilterService.getOneDwnLocationFilterFromDBByName(name, applicationType);
        if (filter != null) {
            downloadLocationFilterService.delete(filter.getId());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_FILTERS_DOWNLOADLOCATION, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getDownloadLocationRoundRobinFilter(@RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        String id = getRoundRobinIdByApplicationType(applicationType);
        SingletonFilterValue singletonFilterValue = singletonFilterValueDAO.getOne(id);
        if (singletonFilterValue == null) {
            singletonFilterValue = new DownloadLocationRoundRobinFilterValue();
        }
        singletonFilterValue = QueriesHelper.nullifyUnwantedFields(singletonFilterValue);
        return new ResponseEntity<>(singletonFilterValue, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATE_FILTERS_DOWNLOADLOCATION, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity updateDownloadLocationRoundRobinFilter(@RequestBody DownloadLocationRoundRobinFilterValue filter, @RequestParam(required = false) String applicationType) {

        if (!locationFilterService.isValidUrl(filter.getHttpFullUrlLocation())) {
            return new ResponseEntity<>("Location URL is not valid", HttpStatus.BAD_REQUEST);
        }
        validateApplicationType(applicationType);
        filter.setApplicationType(ApplicationType.get(applicationType));

        // set locations to prevent npe
        if (filter.getLocations() == null) {
            filter.setLocations(new ArrayList<DownloadLocationRoundRobinFilterValue.Location>());
        }
        if (filter.getIpv6locations() == null) {
            filter.setIpv6locations(new ArrayList<DownloadLocationRoundRobinFilterValue.Location>());
        }

        boolean ipv6InIpv4List = false;
        for (DownloadLocationRoundRobinFilterValue.Location location : filter.getLocations()) {
            if (location.getLocationIp().isIpv6()) {
                ipv6InIpv4List = true;
                break;
            }
        }

        boolean ipv4InIpv6List = false;
        for (DownloadLocationRoundRobinFilterValue.Location location : filter.getIpv6locations()) {
            if (!location.getLocationIp().isIpv6()) {
                ipv4InIpv6List = true;
                break;
            }
        }
        if (ipv4InIpv6List || ipv6InIpv4List) {
            return new ResponseEntity<>("IP address has an invalid version", HttpStatus.BAD_REQUEST);
        }

        int percentage = 0;
        for (DownloadLocationRoundRobinFilterValue.Location location : filter.getLocations()) {
            percentage += location.getPercentage();
        }

        int ipv6Percentage = 0;
        for (DownloadLocationRoundRobinFilterValue.Location location : filter.getIpv6locations()) {
            ipv6Percentage += location.getPercentage();
        }

        if (percentage != 100) {
            return new ResponseEntity<>("Summary IPv4 percentage should be 100", HttpStatus.BAD_REQUEST);
        }

        if (ipv6Percentage != 100 && filter.getIpv6locations() != null && filter.getIpv6locations().size() > 0) {
            return new ResponseEntity<>("Summary IPv6 percentage should be 100", HttpStatus.BAD_REQUEST);
        }

        Set<String> ipSet = new HashSet<String>();
        for (DownloadLocationRoundRobinFilterValue.Location loc : filter.getLocations()) {
            ipSet.add(loc.getLocationIp().toString());
        }

        Set<String> ipv6Set = new HashSet<String>();
        for (DownloadLocationRoundRobinFilterValue.Location loc : filter.getIpv6locations()) {
            ipv6Set.add(loc.getLocationIp().toString());
        }

        if (ipSet.size() < filter.getLocations().size() || ipv6Set.size() < filter.getIpv6locations().size()) {
            return new ResponseEntity<>("Locations are duplicated", HttpStatus.BAD_REQUEST);
        }

        try {
            filter.setId(getRoundRobinIdByApplicationType(applicationType));
            singletonFilterValueDAO.setOne(filter.getId(), filter);
        } catch (Exception e) {
            log.error("Unable to save DownloadLocationRoundRobin filter", e);
            return new ResponseEntity<>("Unable to save DownloadLocationRoundRobin: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        filter = QueriesHelper.nullifyUnwantedFields(filter);
        return new ResponseEntity<>(filter, HttpStatus.OK);
    }

    private String getRoundRobinIdByApplicationType(String applicationType) {
        if (ApplicationType.equals(ApplicationType.STB, applicationType)) {
            return DownloadLocationRoundRobinFilterValue.SINGLETON_ID;
        }
        return applicationType.toUpperCase() + "_" + DownloadLocationRoundRobinFilterValue.SINGLETON_ID;
    }
}
