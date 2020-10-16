/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.thucydides.util.rfc;

import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FeatureUtils {
    public static final String RFC_FEATURE = "rfc/feature";

    public static Feature createAndSaveFeature() throws IOException {
        final Feature obj = createFeature();
        HttpClient.post(GenericTestUtils.buildFullUrl(RFC_FEATURE), obj);
        return obj;
    }

    public static Feature createFeature() {
        final Feature obj = new Feature();
        obj.setId(UUID.randomUUID().toString());
        obj.setName("testFeature");
        obj.setFeatureName("testFeature");
        obj.setEffectiveImmediate(true);
        obj.setEnable(true);
        Map<String, String> configProperties = Collections.singletonMap("test123", "test123");
        obj.setConfigData(configProperties);
        return obj;
    }

    public static Feature createAndSaveFeature(String name) throws IOException {
        Feature feature = createFeature();
        feature.setName(name);
        feature.setFeatureName(name);
        HttpClient.post(GenericTestUtils.buildFullUrl(RFC_FEATURE), feature);
        return feature;
    }

    public static List<Feature> createAndSaveFeatureProfiles() throws IOException {
        return Lists.newArrayList(
                createAndSaveFeature("feature123"),
                createAndSaveFeature("feature456")
        );
    }
}
