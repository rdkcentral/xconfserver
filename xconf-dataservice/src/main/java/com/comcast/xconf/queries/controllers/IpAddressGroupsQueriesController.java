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
 * Created: 01.09.15 15:22
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.comcast.xconf.queries.beans.StringListWrapper;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
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

import static com.comcast.xconf.GenericNamespacedListTypes.IP_LIST;
import static com.comcast.xconf.queries.QueryConstants.*;
/**
 * Controller which implements Restful XCONF API to manage Ip Address Groups.
 */
@RestController
public class IpAddressGroupsQueriesController {

    private static final Logger log = LoggerFactory.getLogger(IpAddressGroupsQueriesController.class);

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = UPDATE_IP_ADDRESS_GROUPS)
    public ResponseEntity saveIpAddressGroup(@RequestBody IpAddressGroupExtended group) {
        if (StringUtils.isBlank(group.getName()) || !group.getName().matches("^[-a-zA-Z0-9_.' ]+$")) {
            return new ResponseEntity<>("Name is invalid.", HttpStatus.BAD_REQUEST);
        }

        try {
            final GenericNamespacedList ipList = GenericNamespacedListsConverter.convertFromIpAddressGroupExtended(group);
            if (genericNamespacedListQueriesService.getOneByTypeNonCached(ipList.getId(), GenericNamespacedListTypes.IP_LIST) != null) {
                genericNamespacedListQueriesService.updateNamespacedList(ipList, GenericNamespacedListTypes.IP_LIST);
            } else {
                genericNamespacedListQueriesService.createNamespacedList(ipList, GenericNamespacedListTypes.IP_LIST);
            }

            return new ResponseEntity<>(group, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_IP_ADDRESS_GROUPS,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<IpAddressGroupExtended>> getIpAddressGroups() {
        List<IpAddressGroupExtended> environments = GenericNamespacedListsConverter.convertToListOfIpAddressGroups(
                genericNamespacedListQueriesService.getAllByType(GenericNamespacedListTypes.IP_LIST));

        for (IpAddressGroupExtended env : environments) {
            env.setUpdated(null);
        }
        return new ResponseEntity<>(environments, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_IP_ADDRESS_GROUPS + "/byIp/{ipAddress}/",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getIpAddressGroupsByIp(@PathVariable String ipAddress) {
        if (!IpAddress.isValid(ipAddress)) {
            return new ResponseEntity<>("IpAddress is invalid.", HttpStatus.BAD_REQUEST);
        }
        List<IpAddressGroupExtended> result = GenericNamespacedListsConverter.convertToListOfIpAddressGroups(
                genericNamespacedListQueriesService.getListsByIp(ipAddress));
        for (IpAddressGroupExtended env : result) {
            env.setUpdated(null);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_IP_ADDRESS_GROUPS + "/byName/{name}/",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getIpAddressGroupsByName(@PathVariable String name, @RequestParam(value = "version", required = false) String apiVersion) {
        if (StringUtils.isBlank(name)) {
            return new ResponseEntity<>("Name is blank.", HttpStatus.BAD_REQUEST);
        }
        List<IpAddressGroupExtended> resultArray = new ArrayList<>();
        GenericNamespacedList groupByName = genericNamespacedListQueriesService.getOneByType(name, GenericNamespacedListTypes.IP_LIST);

        if (groupByName == null
                && ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("IpAddressGroup with name " + name + " does not exist", HttpStatus.NOT_FOUND);
        }
        if (groupByName != null) {
            resultArray.add(prepareResponse(groupByName));
        }
        return new ResponseEntity<>(resultArray, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_IP_ADDRESS_GROUPS + "/{id}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity deleteIpAddressGroup(@PathVariable String id) {
        GenericNamespacedList listToDelete = genericNamespacedListQueriesService.getOneByType(id, GenericNamespacedListTypes.IP_LIST);
        if (listToDelete == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        try {
            genericNamespacedListQueriesService.deleteNamespacedList(GenericNamespacedListTypes.IP_LIST, id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = UPDATE_IP_ADDRESS_GROUPS + "/{listId}/addData")
    public ResponseEntity addListData(@PathVariable String listId, @RequestBody StringListWrapper ipList) {
        try {
            return ResponseEntity.ok(prepareResponse(genericNamespacedListQueriesService.addNamespacedListData(listId, IP_LIST, ipList)));
        } catch (Exception e) {
            log.error("Could not save IpAddressGroup", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = UPDATE_IP_ADDRESS_GROUPS + "/{listId}/removeData")
    public ResponseEntity removeListData(@PathVariable String listId, @RequestBody StringListWrapper ipList) {
        try {
            return ResponseEntity.ok(prepareResponse(genericNamespacedListQueriesService.removeNamespacedListData(listId, IP_LIST, ipList)));
        } catch (Exception e) {
            log.error("Could not save IpAddressGroup", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private static IpAddressGroupExtended prepareResponse(GenericNamespacedList nsList) {
        return nullifyUnwantedFields(GenericNamespacedListsConverter.convertToIpAddressGroupExtended(nsList));
    }

    public static IpAddressGroupExtended nullifyUnwantedFields(IpAddressGroupExtended ipAddressGroup) {
        ipAddressGroup.setUpdated(null);
        return ipAddressGroup;
    }

}
