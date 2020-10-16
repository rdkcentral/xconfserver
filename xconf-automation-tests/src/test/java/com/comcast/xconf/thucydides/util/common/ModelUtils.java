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
 * Created: 3/21/16  2:16 PM
 */
package com.comcast.xconf.thucydides.util.common;

import com.beust.jcommander.internal.Lists;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;

import java.io.IOException;
import java.util.List;


public class ModelUtils {
    private static final String MODEL_URL = "model";

    public static String defaultModelId = "MODELID";

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(MODEL_URL, Model.class);
    }

    public static Model createAndSaveDefaultModel() throws Exception {
        Model result = createDefaultModel();
        HttpClient.post(GenericTestUtils.buildFullUrl(MODEL_URL), result);

        return result;
    }

    public static Model createDefaultModel() {
        Model result = new Model();
        result.setId(defaultModelId);
        result.setDescription("someDescription");

        return result;
    }

    public static Model createAndSaveModel(String modelId) throws IOException {
        Model model = createDefaultModel();
        model.setId(modelId);
        HttpClient.post(GenericTestUtils.buildFullUrl(MODEL_URL), model);

        return model;
    }

    public static Model createAndSaveModel(String modelId, String description) throws IOException {
        Model model = new Model();
        model.setId(modelId);
        model.setDescription(description);
        HttpClient.post(GenericTestUtils.buildFullUrl(MODEL_URL), model);

        return model;
    }

    public static List<Model> createAndSaveModels() throws IOException {
        return Lists.newArrayList(
            createAndSaveModel("modelId123", "description123"),
            createAndSaveModel("modelId456", "description456")
        );
    }
}
