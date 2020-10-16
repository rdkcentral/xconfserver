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
 * Created: 31.08.15 15:36
*/
package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.ModelQueriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static com.comcast.xconf.queries.QueriesHelper.nullifyUnwantedFields;
import static com.comcast.xconf.queries.QueryConstants.*;

/**
 * Controller, which implements Restful XCONF API to manage Models and Environments.
 */
@RestController
public class ModelQueriesController extends BaseQueriesController {

    private static final Logger log = LoggerFactory.getLogger(ModelQueriesController.class);

    @Autowired
    private ModelQueriesService modelService;

    @Autowired
    private CachedSimpleDao<String, Model> modelDAO;

    @RequestMapping(method = RequestMethod.POST, value = UPDATE_MODELS)
    public ResponseEntity createModel(@RequestBody Model model) {
        modelService.getValidator().validate(model);
        Model existedModel = modelDAO.getOne(model.getId());
        if (existedModel != null) {
            return new ResponseEntity<>("Model with current name already exists", HttpStatus.CONFLICT);
        }
        modelDAO.setOne(model.getId(), model);

        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, value = UPDATE_MODELS)
    public ResponseEntity update(@RequestBody Model model) {
        modelService.getValidator().validate(model);
        Model existingModel = modelDAO.getOne(model.getId());
        if (Objects.isNull(existingModel)) {
            return new ResponseEntity<>(model.getId() + " model does not exist", HttpStatus.NOT_FOUND);
        }
        modelDAO.setOne(model.getId(), model);

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_MODELS,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getModels() {
        List<Model> models = modelDAO.getAll();
        for (Model model : models) {
            nullifyUnwantedFields(model);
        }
        return new ResponseEntity<>(models, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = QUERIES_MODELS + "/{id}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getModel(@PathVariable String id, @RequestParam(value = "version", required = false) String apiVersion) {
        Model model = modelDAO.getOne(id);

        if (model == null
                && ApiVersionUtils.greaterOrEqual(apiVersion, 3.0f)) {
            return new ResponseEntity<>("Model with id " + id + " does not exist", HttpStatus.NOT_FOUND);
        }
        if (model == null) {
            return ResponseEntity.ok(null);
        }
        return new ResponseEntity<>(nullifyUnwantedFields(model), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_MODELS + "/{id}")
    public ResponseEntity deleteModel(@PathVariable String id) {
        Model model = modelDAO.getOne(id);
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        String usage = modelService.checkUsage(id);
        if (usage != null) {
            return new ResponseEntity<>("Can't delete model. It's used by " + usage, HttpStatus.BAD_REQUEST);
        }
        modelDAO.deleteOne(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
