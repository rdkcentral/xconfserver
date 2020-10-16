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
 *  Created: 1/14/16 7:52 PM
 */

package com.comcast.xconf.queries.controllersv2;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.queries.controllers.NsListQueriesController;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class NamespacedlistAbstractController {
    private static final Logger log = LoggerFactory.getLogger(NsListQueriesController.class);

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    public ResponseEntity getLists(final String namespacedListType) {
        final List<GenericNamespacedList> result = genericNamespacedListQueriesService.getAllByType(namespacedListType);
        for (GenericNamespacedList nsList : result) {
            nullifyUnwantedFields(nsList);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public ResponseEntity getListById(String listId, String namespacedListType) {
        return new ResponseEntity<>(nullifyUnwantedFields(genericNamespacedListQueriesService.getOneByType(listId, namespacedListType)), HttpStatus.OK);
    }

    public ResponseEntity createList(GenericNamespacedList namespacedList, String namespacedListType) {
        try {
            genericNamespacedListQueriesService.createNamespacedList(namespacedList, namespacedListType);
            return new ResponseEntity<>(nullifyUnwantedFields(namespacedList), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity updateList(GenericNamespacedList namespacedList, String namespacedListType) {
        try {
            genericNamespacedListQueriesService.updateNamespacedList(namespacedList, namespacedListType);
            return new ResponseEntity<>(nullifyUnwantedFields(namespacedList), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    public ResponseEntity deleteList(String namespacedListType, String id) {
        try {
            genericNamespacedListQueriesService.deleteNamespacedList(namespacedListType, id);
            return new ResponseEntity<>("Successfully deleted " + id, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public GenericNamespacedList nullifyUnwantedFields(GenericNamespacedList genericNamespacedList) {
        genericNamespacedList.setUpdated(null);
        genericNamespacedList.setTypeName(null);
        return genericNamespacedList;
    }
}
