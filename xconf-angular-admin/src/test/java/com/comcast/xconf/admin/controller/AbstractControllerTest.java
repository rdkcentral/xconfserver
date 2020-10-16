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
 * Author: Yury Stagit
 * Created: 12/14/16  12:00 PM
 */
package com.comcast.xconf.admin.controller;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.firmware.ApplicationType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;
import org.mockito.internal.matchers.EndsWith;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractControllerTest<T extends IPersistable> extends BaseControllerTest {

    protected List<T> entityList = new ArrayList<>();

    @Before
    public void onInit() throws Exception {
        entityList.add(createEntity());
    }

    @After
    public void onClean() throws Exception{
        for (T entity : entityList) {
            performDeleteRequest(getUrlMapping() + "/" + entity.getId());
        }
        entityList.clear();
    }

    @Test
    public void testNotFoundEntity() throws Exception {
        T entity = entityList.get(0);

        ResultActions resultActions  = performGetRequest("/" + getUrlMapping() + "/" + entity.getId(), null);
        validateNotFoundException(resultActions, entity.getId());
    }

    @Test
    public void testNotFoundEntityWhenUpdating() throws Exception {
        T entity = entityList.get(0);

        ResultActions resultActions  = performPutRequest(getUrlMapping(), entity);
        validateNotFoundException(resultActions, entity.getId());
    }

    @Test
    public void testNotFoundEntityWhenDeleting() throws Exception {
        T entity = entityList.get(0);

        ResultActions resultActions  = performDeleteRequest(getUrlMapping() + "/" + entityList.get(0).getId());
        validateNotFoundException(resultActions, entity.getId());
    }

    @Test
    public void testCreatingEntity() throws Exception {
        T entity = entityList.get(0);

        performPostRequestAndVerify(getUrlMapping(), entity);
        performGetRequest("/" + getUrlMapping() + "/" + entity.getId(), null)
                .andExpect(status().isOk()).andExpect(content().json(JsonUtil.toJson(entity)));
    }

    @Test
    public void testCreatingExistingEntity() throws Exception {
        T entity = entityList.get(0);

        performPostRequestAndVerify(getUrlMapping(), entity);

        ResultActions resultActions  = performPostRequest(getUrlMapping(), entity);
        validateEntityExistsException(resultActions, entity.getId());
    }

    @Test
    public void testUpdatingEntity() throws Exception {
        T entity = entityList.get(0);
        
        performPostRequestAndVerify(getUrlMapping(), entity);

        entity = updateEntity(entityList.get(0));
        performPutRequestAndVerify(getUrlMapping(), entity);
        ResultActions resultActions  = performGetRequest("/" + getUrlMapping() + "/" + entity.getId(), null)
                .andExpect(status().isOk());
        assertEntity(resultActions, entity);
    }

    @Test
    public void testDeletingEntity() throws Exception {
        T entity = entityList.get(0);

        performPostRequestAndVerify(getUrlMapping(), entity);

        performDeleteRequestAndVerify(getUrlMapping() + "/" + entity.getId());

        ResultActions resultActions  = performGetRequest("/" + getUrlMapping() + "/" + entity.getId(), null);
        validateNotFoundException(resultActions, entity.getId());
    }

    @Test
    public void testGetAllEntities() throws Exception {

        int countEntities = 9;
        for (int i = 0; i < countEntities; i++) {
            T entity = createEntity();
            entityList.add(entity);
        }

        for (T entity : entityList) {
            performPostRequestAndVerify(getUrlMapping(), entity);
        }

        ResultActions resultActions  = performGetRequest("/" + getUrlMapping(), null)
                .andExpect(status().isOk());

        Collections.sort(entityList, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        assertEntity(resultActions, entityList);
    }

    @Test
    public void testExportOne() throws Exception {

        T entity = entityList.get(0);
        performPostRequestAndVerify(getUrlMapping(), entity);

        String applicationTypeSuffix = "_" + ApplicationType.STB + ".json";
        performGetRequest("/" + getUrlMapping() + "/" + entity.getId(), Collections.singletonMap("export", ""))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singleton(entity))))
                .andExpect(header().string("Content-Disposition", new EndsWith(applicationTypeSuffix)))
                .andExpect(header().string("Content-Disposition", new Contains(getOneEntityExportName() + entity.getId())));
    }

    @Test
    public void testExportAll() throws Exception {

        T entity = entityList.get(0);
        performPostRequestAndVerify(getUrlMapping(), entity);

        String applicationTypeSuffix = "_" + ApplicationType.STB + ".json";
        performGetRequest("/" + getUrlMapping(), Collections.singletonMap("export", ""))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singleton(entity))))
                .andExpect(header().string("Content-Disposition", new EndsWith(applicationTypeSuffix)))
                .andExpect(header().string("Content-Disposition", new Contains(getAllEntitiesExportName())));
    }

    public abstract String getUrlMapping();

    public abstract T createEntity() throws Exception;

    public abstract T updateEntity(T entity) throws Exception;

    public abstract void assertEntity(ResultActions resultActions, Object objectToAssert) throws Exception;

    public abstract String getOneEntityExportName();

    public abstract String getAllEntitiesExportName();

}
