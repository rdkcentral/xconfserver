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
package com.comcast.xconf.admin.controller.rfc;

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.AbstractControllerTest;
import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.controller.rfc.feature.FeatureController;
import com.comcast.xconf.admin.utils.TestDataBuilder;
import com.comcast.xconf.dcm.ruleengine.TelemetryProfileService;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureExport;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;
import org.mockito.internal.matchers.EndsWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

public class FeatureControllerTest extends AbstractControllerTest<Feature> {

    @Override
    public String getUrlMapping() {
        return FeatureController.URL_MAPPING;
    }

    @Override
    public Feature createEntity() throws Exception {
        return TestDataBuilder.createFeature();
    }

    @Override
    public Feature updateEntity(Feature feature) throws Exception {
        return TestDataBuilder.modifyFeature(feature, "new-" + feature.getId());
    }

    @Override
    public void assertEntity(ResultActions resultActions, Object feature) throws Exception {
        resultActions.andExpect(content().json(JsonUtil.toJson(feature)));
    }

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.FEATURE.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_FEATURES.getName();
    }

    @Test
    @Override
    public void testExportOne() throws Exception {
        Feature entity = entityList.get(0);
        performPostRequestAndVerify(getUrlMapping(), entity);

        String applicationTypeSuffix = "_" + ApplicationType.STB + ".json";

        performGetRequest("/" + getUrlMapping() + "/" + entity.getId(), Collections.singletonMap("export", ""))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singleton(new FeatureExport(entity)))))
                .andExpect(header().string("Content-Disposition", new EndsWith(applicationTypeSuffix)))
                .andExpect(header().string("Content-Disposition", new Contains(getOneEntityExportName() + entity.getId())));
    }

    @Test
    @Override
    public void testExportAll() throws Exception {
        Feature entity = entityList.get(0);
        performPostRequestAndVerify(getUrlMapping(), entity);

        String applicationTypeSuffix = "_" + ApplicationType.STB + ".json";

        performGetRequest("/" + getUrlMapping(), Collections.singletonMap("export", ""))
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtil.toJson(Collections.singleton(new FeatureExport(entity)))))
                .andExpect(header().string("Content-Disposition", new EndsWith(applicationTypeSuffix)))
                .andExpect(header().string("Content-Disposition", new Contains(getAllEntitiesExportName())));
    }

    @Test
    public void entityWithTheSameIdIsNotOverriddenInOtherApplicationTypeDuringCreating() throws Exception {
        Feature stbFeature = entityList.get(0);
        performPostRequestAndVerify(getUrlMapping(), stbFeature);

        Feature xHomeFeature = CloneUtil.clone(stbFeature);
        xHomeFeature.setApplicationType(ApplicationType.XHOME);

        mockMvc.perform(post("/" + FeatureController.URL_MAPPING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(xHomeFeature)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Entity with id: " + stbFeature.getId() + " already exists in stb application"));
    }
}
