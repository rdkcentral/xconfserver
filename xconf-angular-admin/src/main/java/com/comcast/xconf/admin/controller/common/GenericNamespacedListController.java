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
 *  Created: 11/20/15 7:04 PM
 */
package com.comcast.xconf.admin.controller.common;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.admin.service.common.GenericNamespacedListService;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.shared.controller.AbstractController;
import com.comcast.xconf.shared.service.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(GenericNamespacedListController.URL_MAPPING)
public class GenericNamespacedListController extends AbstractController<GenericNamespacedList> {

    public static final String URL_MAPPING = "api/genericnamespacedlist";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericNamespacedListService genericNamespacedListService;

    @Override
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity getEntity(@PathVariable String id) {
        return ResponseEntity.ok(genericNamespacedListService.getOneNonCached(id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/ids")
    public ResponseEntity getNamespacedListsIds() {
        return new ResponseEntity<>(genericNamespacedListService.getNamespacedListsIds(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{typeName}/ids")
    public ResponseEntity getNamespacedListsIdsByType(@PathVariable String typeName) {
        return new ResponseEntity<>(genericNamespacedListService.getNamespacedListsIdsByType(typeName), HttpStatus.OK);
    }

    @RequestMapping(value = "/{newId}", method = RequestMethod.PUT)
    public ResponseEntity updateNamespacedList(
            @RequestBody GenericNamespacedList list,
            @PathVariable String newId) {
        GenericNamespacedList updatedList = genericNamespacedListService.update(list, newId);
        log("updated", updatedList);
        return new ResponseEntity<>(QueriesHelper.nullifyUnwantedFields(updatedList), HttpStatus.OK);
    }

    @RequestMapping(value = "/all/{typeName}", params = "export", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity exportAllNamespacedLists(@PathVariable final String typeName) {
        HttpHeaders headers = Utils.createContentDispositionHeader(ExportFileNames.ALL.getName() + typeName + "S");
        return new ResponseEntity<>(genericNamespacedListService.getAllByType(typeName), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/ipAddressGroups", method = RequestMethod.GET)
    public ResponseEntity getAllIpAddressGroups() {
        return new ResponseEntity<>(genericNamespacedListService.getAllIpAddressGroups(), HttpStatus.OK);
    }

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.NAMESPACEDLIST.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_NAMESPACEDLISTS.getName();
    }

    @Override
    public AbstractService<GenericNamespacedList> getService() {
        return genericNamespacedListService;
    }

}
