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
 * Created: 01.09.15 19:15
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.NamespacedList;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.comcast.xconf.queries.beans.StringListWrapper;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
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

import static com.comcast.xconf.GenericNamespacedListTypes.MAC_LIST;
import static com.comcast.xconf.queries.QueryConstants.*;

@RestController
public class NsListQueriesController {

    private static final Logger log = LoggerFactory.getLogger(NsListQueriesController.class);

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;


    @RequestMapping(method = RequestMethod.GET, value = QUERIES_NS_LISTS,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getLists() {

        List<NamespacedList> all = GenericNamespacedListsConverter.convertToListOfNamespacedLists(
                genericNamespacedListQueriesService.getAllByType(GenericNamespacedListTypes.MAC_LIST)
        );
        List<NamespacedList> result = new ArrayList<>();
        for (NamespacedList nsList : all) {
            result.add(nullifyUnwantedFields(nsList));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_NS_LISTS + "/byId/{listId}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getListByID(@PathVariable String listId, @RequestParam(value = "version", required = false) String apiVersion) {
        GenericNamespacedList macList = genericNamespacedListQueriesService.getOneByType(listId, GenericNamespacedListTypes.MAC_LIST);
        if (macList != null) {
            final NamespacedList data = GenericNamespacedListsConverter.convertToNamespacedList(macList);

            if (data != null) {
                return new ResponseEntity<>(nullifyUnwantedFields(data), HttpStatus.OK);
            }
        }
        if (macList == null
                && ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("NamespacedList with id " + listId + " does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_NS_LISTS + "/byMacPart/{macPart}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getListByMac(@PathVariable final String macPart) {
        final List<NamespacedList> allNamespacedLists = GenericNamespacedListsConverter.convertToListOfNamespacedLists(
                genericNamespacedListQueriesService.getListsByMacPart(macPart));
        if (allNamespacedLists != null && CollectionUtils.isNotEmpty(allNamespacedLists)) {
            List<NamespacedList> result = new ArrayList<>();
            for (NamespacedList nsList : allNamespacedLists) {
                result.add(nullifyUnwantedFields(nsList));
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(method = RequestMethod.POST, value = UPDATE_NS_LISTS)
    public ResponseEntity saveNamespacedList(@RequestBody NamespacedList macList) {
        if (StringUtils.isBlank(macList.getId())  || !macList.getId().matches("^[-a-zA-Z0-9_.' ]+$")) {
            return new ResponseEntity<>("Name is invalid.", HttpStatus.BAD_REQUEST);
        }

        try {
            GenericNamespacedList listToSave = GenericNamespacedListsConverter.convertFromNamespacedList(macList);
            if (genericNamespacedListQueriesService.getOneByTypeNonCached(listToSave.getId(), GenericNamespacedListTypes.MAC_LIST) != null) {
                genericNamespacedListQueriesService.updateNamespacedList(listToSave, GenericNamespacedListTypes.MAC_LIST);
            } else {
                genericNamespacedListQueriesService.createNamespacedList(listToSave, GenericNamespacedListTypes.MAC_LIST);
            }

            return new ResponseEntity<>(nullifyUnwantedFields(macList), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = {UPDATE_NS_LISTS + "/{listId}/addData",
                                                          UPDATE_NS_LISTS_LEGACY + "/{listId}/addData"})
    public ResponseEntity addNamespacedListData(@PathVariable String listId, @RequestBody StringListWrapper macList) {
        try {
            return new ResponseEntity<>(nullifyUnwantedFields(GenericNamespacedListsConverter.convertToNamespacedList(
                    genericNamespacedListQueriesService.addNamespacedListData(listId, MAC_LIST, macList)
            )), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = {UPDATE_NS_LISTS + "/{listId}/removeData",
                                                          UPDATE_NS_LISTS_LEGACY + "/{listId}/removeData"})
    public ResponseEntity removeNamespacedListData(@PathVariable String listId, @RequestBody StringListWrapper macList) {
        try {
            return new ResponseEntity<>(nullifyUnwantedFields(GenericNamespacedListsConverter.convertToNamespacedList(
                    genericNamespacedListQueriesService.removeNamespacedListData(listId, MAC_LIST, macList)
            )), HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_NS_LISTS + "/{id}")
    public ResponseEntity deleteNamespacedList(@PathVariable String id) {
        GenericNamespacedList listToDelete = genericNamespacedListQueriesService.getOneByType(id, GenericNamespacedListTypes.MAC_LIST);
        if (listToDelete == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            listToDelete.setUpdated(null);
        }
        // todo check usages
        /*String usage = namespacedListService.checkUsage(id);
        if (usage != null) {
            return new ResponseEntity<>("NamespacedList is used: " + usage, HttpStatus.BAD_REQUEST);
        }*/
        try {
            genericNamespacedListQueriesService.deleteNamespacedList(GenericNamespacedListTypes.MAC_LIST, listToDelete.getId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static NamespacedList nullifyUnwantedFields(NamespacedList namespacedList) {
        namespacedList.setUpdated(null);
        namespacedList.setTypeName(null);
        return namespacedList;
    }
}
