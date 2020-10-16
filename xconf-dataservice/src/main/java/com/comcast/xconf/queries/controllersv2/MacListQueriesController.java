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
 * Author: Stanislav Menshykov
 * Created: 02.11.15  12:11
 */
package com.comcast.xconf.queries.controllersv2;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.queries.beans.StringListWrapper;
import com.comcast.xconf.queries.controllers.NsListQueriesController;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.comcast.xconf.GenericNamespacedListTypes.MAC_LIST;
import static com.comcast.xconf.queries.QueryConstants.*;

@RestController
public class MacListQueriesController extends NamespacedlistAbstractController{

    private static final Logger log = LoggerFactory.getLogger(NsListQueriesController.class);

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_NS_LISTS_V2,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getLists() {
        return super.getLists(GenericNamespacedListTypes.MAC_LIST);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_NS_LISTS_V2 + "/byId/{listId}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getListById(@PathVariable String listId) {
        return super.getListById(listId, GenericNamespacedListTypes.MAC_LIST);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_NS_LISTS_V2 + "/byMacPart/{macPart}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getListByMac(@PathVariable final String macPart) {
        final List<GenericNamespacedList> result = genericNamespacedListQueriesService.getListsByMacPart(macPart);
        if (CollectionUtils.isNotEmpty(result)) {
            for (GenericNamespacedList list : result) {
                nullifyUnwantedFields(list);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(method = RequestMethod.POST, value = UPDATE_NS_LISTS_V2)
    public ResponseEntity saveList(@RequestBody GenericNamespacedList macList) {
        return super.createList(macList, GenericNamespacedListTypes.MAC_LIST);
    }

    @RequestMapping(method = RequestMethod.PUT, value = UPDATE_NS_LISTS_V2)
    public ResponseEntity updateList(@RequestBody GenericNamespacedList macList) {
        return super.updateList(macList, GenericNamespacedListTypes.MAC_LIST);
    }

    @RequestMapping(method = RequestMethod.POST, value = {UPDATE_NS_LISTS_V2 + "/{listId}/addData",
            UPDATE_NS_LISTS_LEGACY + "/{listId}/addData"})
    public ResponseEntity addListData(@PathVariable String listId, @RequestBody StringListWrapper macList) {
        try {
            return ResponseEntity.ok(nullifyUnwantedFields(genericNamespacedListQueriesService.addNamespacedListData(listId, MAC_LIST, macList)));
        } catch (Exception e) {
            log.error("Could not save NamespacedList", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = {UPDATE_NS_LISTS_V2 + "/{listId}/removeData",
            UPDATE_NS_LISTS_LEGACY + "/{listId}/removeData"})
    public ResponseEntity removeListData(@PathVariable String listId, @RequestBody StringListWrapper macList) {
        try {
            return ResponseEntity.ok(nullifyUnwantedFields(genericNamespacedListQueriesService.removeNamespacedListData(listId, MAC_LIST, macList)));
        } catch (Exception e) {
            log.error("Could not save NamespacedList", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_NS_LISTS_V2 + "/{id}")
    public ResponseEntity deleteList(@PathVariable String id) {
        return super.deleteList(GenericNamespacedListTypes.MAC_LIST, id);
    }
}
