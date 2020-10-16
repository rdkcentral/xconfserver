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
 * Author: rdolomansky
 * Created: 3/25/15  1:23 PM
 */
package com.comcast.xconf.shared.controller;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.shared.domain.EntityMessage;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.shared.utils.LoggingUtils;
import com.comcast.xconf.shared.utils.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class AbstractController<T extends IPersistable & Comparable> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected AuthService authService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody T entity) {
        getService().create(entity);
        log("created", entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity update(@RequestBody T entity) {
        getService().update(entity);
        log("updated", entity);
        return ResponseEntity.ok(entity);
    }

    @RequestMapping(value = "/entities", method = RequestMethod.POST)
    public Map<String, Object> createEntitiesPackage(@RequestBody List<T> entities) {
        Map<String, Object> entitiesMap = new HashMap<>();
        for (T entity : entities) {
            try {
                create(entity);
                entitiesMap.put(entity.getId()
                        , new EntityMessage(EntityMessage.ENTITY_STATUS.SUCCESS, entity.getId()));
            } catch (Exception e) {
                entitiesMap.put(entity.getId()
                        , new EntityMessage(EntityMessage.ENTITY_STATUS.FAILURE, e.getMessage()));
            }
        }
        return entitiesMap;
    }

    @RequestMapping(value = "/entities", method = RequestMethod.PUT)
    public Map<String, Object> updateEntitiesPackage(@RequestBody List<T> entities) {
        Map<String, Object> entitiesMap = new HashMap<>();
        for (T entity : entities) {
            try {
                update(entity);
                entitiesMap.put(entity.getId()
                        , new EntityMessage(EntityMessage.ENTITY_STATUS.SUCCESS, entity.getId()));
            } catch (Exception e) {
                entitiesMap.put(entity.getId()
                        , new EntityMessage(EntityMessage.ENTITY_STATUS.FAILURE, e.getMessage()));
            }
        }
        return entitiesMap;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getAllEntities() {
        return ResponseEntity.ok(getService().getAll());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/page")
    public ResponseEntity getEntities(
            @RequestParam final int pageNumber,
            @RequestParam final int pageSize) {
        List<T> entities = getService().getAll();

        return new ResponseEntity<>(PageUtils.getPage(entities, pageNumber, pageSize), Utils.createNumberOfItemsHttpHeaders(entities), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, params = "export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportAll() {
        List<T> entities = getService().getAll();
        HttpHeaders headers = Utils.createContentDispositionHeader(getAllEntitiesExportName());
        return new ResponseEntity<>(entities, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity getEntity(@PathVariable String id) {
        return ResponseEntity.ok(getService().getOne(id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}", params = "export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportOne(@PathVariable String id) {
        T entity = getService().getOne(id);
        HttpHeaders headers = Utils.createContentDispositionHeader(getOneEntityExportName() + entity.getId());
        return new ResponseEntity<>(Collections.singleton(entity), headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity deleteOne(@PathVariable String id) {
        T entity = getService().delete(id);
        log("deleted", entity);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/filtered", method = RequestMethod.POST)
    public ResponseEntity getFiltered(
            @RequestBody(required = false) Map<String, String> searchContext,
            @RequestParam final Integer pageSize,
            @RequestParam final Integer pageNumber
    ) {
        List<T> entities = getService().findByContext(searchContext);
        return new ResponseEntity<>(PageUtils.getPage(entities, pageNumber, pageSize),
                Utils.createNumberOfItemsHttpHeaders(entities), HttpStatus.OK);
    }

    protected void log(String strMethod, T entity) {
        LoggingUtils.log(logger, Level.INFO, "Successfully " + strMethod + " {} with info: {}", authService.getUserName(), entity.getClass().getSimpleName(), JsonUtil.toJson(entity));
    }

    public abstract String getOneEntityExportName();

    public abstract String getAllEntitiesExportName();

    public abstract AbstractService<T> getService();
}
