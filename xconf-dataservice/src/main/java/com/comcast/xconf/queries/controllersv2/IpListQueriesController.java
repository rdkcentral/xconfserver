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
 * Created: 02.11.15  15:35
 */
package com.comcast.xconf.queries.controllersv2;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.queries.controllers.IpAddressGroupsQueriesController;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import com.comcast.xconf.util.IpAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.comcast.xconf.queries.QueryConstants.*;

@RestController
public class IpListQueriesController extends NamespacedlistAbstractController {

    private static final Logger log = LoggerFactory.getLogger(IpAddressGroupsQueriesController.class);

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    @RequestMapping(method = RequestMethod.POST, value = UPDATE_IP_ADDRESS_GROUPS_V2)
    public ResponseEntity createList(@RequestBody GenericNamespacedList ipList) {
        return super.createList(ipList, GenericNamespacedListTypes.IP_LIST);
    }

    @RequestMapping(method = RequestMethod.PUT, value = UPDATE_IP_ADDRESS_GROUPS_V2)
    public ResponseEntity updateList(@RequestBody GenericNamespacedList ipList) {
        return super.updateList(ipList, GenericNamespacedListTypes.IP_LIST);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_IP_ADDRESS_GROUPS_V2,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getLists() {
        return super.getLists(GenericNamespacedListTypes.IP_LIST);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_IP_ADDRESS_GROUPS_V2 + "/byIp/{ipAddress}/",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getListsByIp(@PathVariable String ipAddress) {
        if (!IpAddressUtils.isValidIpAddress(ipAddress)) {
            return new ResponseEntity<>("IpAddress is invalid.", HttpStatus.BAD_REQUEST);
        }

        final List<GenericNamespacedList> result = genericNamespacedListQueriesService.getListsByIp(ipAddress);
        for (GenericNamespacedList list : result) {
            nullifyUnwantedFields(list);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_IP_ADDRESS_GROUPS_V2 + "/byName/{id}/",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getListById(@PathVariable String id) {
        return super.getListById(id, GenericNamespacedListTypes.IP_LIST);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_IP_ADDRESS_GROUPS_V2 + "/{id}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity deleteList(@PathVariable String id) {
        return super.deleteList(id, GenericNamespacedListTypes.IP_LIST);
    }

}
