/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureExport;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.service.rfc.FeatureDataService;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FeatureDataControllerTest extends BaseQueriesControllerTest {

    @Autowired
    private FeatureDataService featureDataService;

    @Test
    public void getOne() throws Exception {
        Feature feature = createAndSaveFeature();
        mockMvc.perform(get(FeatureDataController.API_URL + "/{id}", feature.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(new FeatureExport(feature))));
    }

    @Test
    public void create() throws Exception {
        Feature feature = createFeature();
        mockMvc.perform(post(FeatureDataController.API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(feature))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertNotNull(featureDAO.getOne(feature.getId()));
    }

    @Test
    public void throwEntityExistsExceptionIfFeatureWithExistingIdIsCreated() throws Exception {
        Feature feature = createAndSaveFeature();

        mockMvc.perform(post(FeatureDataController.API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(feature))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("\"Entity with id: " + feature.getId() + " already exists\""));
    }

    @Test
    public void update() throws Exception {
        Feature feature = createAndSaveFeature();
        Feature updatedFeature = createFeature();
        updatedFeature.setName("changedName");
        updatedFeature.setId(feature.getId());

        mockMvc.perform(put(FeatureDataController.API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(updatedFeature))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertEquals(updatedFeature.getName(), featureDAO.getOne(updatedFeature.getId()).getName());
    }

    @Test
    public void deleteOne() throws Exception {
        Feature feature = createAndSaveFeature();
        assertNotNull(featureDAO.getOne(feature.getId()));

        mockMvc.perform(delete(FeatureDataController.API_URL + "/{id}", feature.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void importAll() throws Exception {
        List<Feature> features = createFeatures();
        assertEquals(0, featureDAO.getAll().size());

        mockMvc.perform(post(FeatureDataController.API_URL + "/importAll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(features))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertEquals(features.size(), featureDAO.getAll().size());
    }

    @Test
    public void getFilteredByName() throws Exception {
        List<Feature> features = createAndSaveFeatures();

        mockMvc.perform(get(FeatureDataController.API_URL + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.NAME, features.get(0).getName())
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(new FeatureExport(features.get(0))))));
    }

    @Test
    public void getFilteredByInstance() throws Exception {
        List<Feature> features = createAndSaveFeatures();

        mockMvc.perform(get(FeatureDataController.API_URL + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.FEATURE_INSTANCE, features.get(0).getFeatureName())
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(new FeatureExport(features.get(0))))));
    }

    @Test
    public void getFilteredByKey() throws Exception {
        List<Feature> features = createAndSaveFeatures();
        Map<String, String> configData = features.get(0).getConfigData();
        String key = configData.keySet().iterator().next();

        mockMvc.perform(get(FeatureDataController.API_URL + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.FREE_ARG, key)
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(new FeatureExport(features.get(0))))));
    }

    @Test
    public void getFilteredByValue() throws Exception {
        List<Feature> features = createAndSaveFeatures();
        Map<String, String> configData = features.get(0).getConfigData();
        String value = configData.values().iterator().next();

        mockMvc.perform(get(FeatureDataController.API_URL + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.FIXED_ARG, value)
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(new FeatureExport(features.get(0))))));
    }

    @Test
    public void getFilteredByKeyAndValue() throws Exception {
        List<Feature> features = createAndSaveFeatures();
        Map<String, String> configData = features.get(0).getConfigData();
        String key = configData.keySet().iterator().next();
        String value = configData.values().iterator().next();

        mockMvc.perform(get(FeatureDataController.API_URL + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param(SearchFields.FREE_ARG, key)
                .param(SearchFields.FIXED_ARG, value)
                .param(SearchFields.APPLICATION_TYPE, STB))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Lists.newArrayList(new FeatureExport(features.get(0))))));
    }

    private List<Feature> createFeatures() {
        return Lists.newArrayList(createFeature(), createFeature());
    }

    private List<Feature> createAndSaveFeatures() {
        List<Feature> features = createFeatures();
        for (Feature feature : features) {
            featureDataService.create(feature);
        }
        return features;
    }
}
