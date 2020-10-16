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
 *  Created: 2:35 PM
 */
package com.comcast.xconf.queries.controllers;


import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.estbfirmware.PercentageBeanQueriesService;
import com.comcast.xconf.estbfirmware.converter.PercentageBeanConverter;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.queries.QueryConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PercentageBeanQueriesController extends BaseQueriesController {

    private static final Logger log = LoggerFactory.getLogger(PercentageBeanQueriesController.class);

    public static final String API_VERSION_2 = "2";

    @Autowired
    private PercentageBeanQueriesService percentageBeanService;

    @Autowired
    private PercentageBeanConverter percentageBeanConverter;

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_PERCENTAGE_BEAN)
    public ResponseEntity getAll(@RequestParam(required = false) String field,
                                 @RequestParam(required = false) String applicationType) throws IllegalAccessException {
        validateApplicationType(applicationType);
        if (StringUtils.isNotBlank(field)) {
            return new ResponseEntity<>(percentageBeanService.getPercentFilterFieldValues(field, applicationType), HttpStatus.OK);
        }
        List<PercentageBean> percentageBeans = percentageBeanService.getAll(applicationType);
        return new ResponseEntity<>(percentageBeans, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QueryConstants.QUERIES_PERCENTAGE_BEAN + "/{id}")
    public ResponseEntity getOne(@PathVariable String id) {
        PercentageBean percentageBean = percentageBeanService.getOne(id);
        return new ResponseEntity<>(percentageBean, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = QueryConstants.UPDATES_PERCENTAGE_BEAN)
    public ResponseEntity update(@RequestBody PercentageBean percentageBean,
                                 @RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        validateRuleName(percentageBean.getId(), percentageBean.getName());
        percentageBean.setApplicationType(ApplicationType.get(applicationType));
        percentageBeanService.update(percentageBean);
        return new ResponseEntity<>(percentageBean, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATES_PERCENTAGE_BEAN)
    public ResponseEntity create(@RequestBody PercentageBean percentageBean,
                                 @RequestParam(required = false) String applicationType) {
        validateApplicationType(applicationType);
        validateRuleName(percentageBean.getId(), percentageBean.getName());
        percentageBean.setApplicationType(ApplicationType.get(applicationType));
        percentageBeanService.create(percentageBean);
        return new ResponseEntity<>(percentageBean, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = QueryConstants.DELETES_PERCENTAGE_BEAN + "/{id}")
    public ResponseEntity delete(@PathVariable String id) {
        percentageBeanService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
