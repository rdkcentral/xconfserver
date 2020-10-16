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
 * Created: 03.09.15 16:29
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.Environment;
import com.comcast.xconf.estbfirmware.EnvironmentQueriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.comcast.xconf.queries.QueriesHelper.nullifyUnwantedFields;
import static com.comcast.xconf.queries.QueryConstants.*;

/**
 * Controller, which implements Restful XCONF API to manage Environments.
 */
@RestController
public class EnvironmentQueriesController extends BaseQueriesController {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentQueriesController.class);

    @Autowired
    private EnvironmentQueriesService environmentService;

    @Autowired
    private CachedSimpleDao<String, Environment> environmentDAO;

    @RequestMapping(method = RequestMethod.POST, value = UPDATE_ENVIRONMENTS)
    public ResponseEntity createEnvironment(@RequestBody Environment env) {
        environmentService.getValidator().validate(env);

        Environment existed = environmentDAO.getOne(env.getId());
        if (existed != null) {
            return new ResponseEntity<>("Environment with current name already exists", HttpStatus.CONFLICT);
        }
        environmentDAO.setOne(env.getId(), env);
        return new ResponseEntity<>(env, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_ENVIRONMENTS,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getEnvironments() {
        List<Environment> environments = environmentDAO.getAll();
        for (Environment env : environments) {
            nullifyUnwantedFields(env);
        }
        return new ResponseEntity<>(environments, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_ENVIRONMENTS + "/{id}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getEnvironment(@PathVariable String id, @RequestParam(value = "version", required = false) String apiVersion) {
        Environment env = environmentDAO.getOne(id);
        if (env != null) {
            return new ResponseEntity<>(nullifyUnwantedFields(env), HttpStatus.OK);
        }
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("Environment with id " + id + " does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Environment does not exist", HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_ENVIRONMENTS + "/{id}")
    public ResponseEntity deleteEnvironment(@PathVariable String id) {
        Environment env = environmentDAO.getOne(id);
        if (env == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        String usage = environmentService.checkUsage(id);
        if (usage != null) {
            return new ResponseEntity<>("Environment is used: " + usage, HttpStatus.BAD_REQUEST);
        }
        environmentDAO.deleteOne(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
