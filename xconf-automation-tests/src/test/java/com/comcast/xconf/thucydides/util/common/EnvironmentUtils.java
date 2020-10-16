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
 * Created: 3/21/16  2:15 PM
 */
package com.comcast.xconf.thucydides.util.common;

import com.beust.jcommander.internal.Lists;
import com.comcast.xconf.Environment;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;

import java.io.IOException;
import java.util.List;


public class EnvironmentUtils {
    private static final String ENVIRONMENT_URL = "environment";

    public static String defaultEnvId = "ENVID";

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(ENVIRONMENT_URL, Environment.class);
    }

    public static Environment createAndSaveDefaultEnvironment() throws Exception {
        Environment result = createDefaultEnvironment();
        HttpClient.post(GenericTestUtils.buildFullUrl(ENVIRONMENT_URL), result);

        return result;
    }

    public static Environment createDefaultEnvironment() {
        Environment result = new Environment();
        result.setId(defaultEnvId);
        result.setDescription("someDescription");

        return result;
    }

    public static Environment createAndSaveEnvironment(String id) throws IOException {
        Environment environment = createDefaultEnvironment();
        environment.setId(id);
        HttpClient.post(GenericTestUtils.buildFullUrl(ENVIRONMENT_URL), environment);
        return environment;
    }

    public static Environment createAndSaveEnvironment(String id, String description) throws IOException {
        Environment environment = new Environment();
        environment.setId(id);
        environment.setDescription(description);
        HttpClient.post(GenericTestUtils.buildFullUrl(ENVIRONMENT_URL), environment);
        return environment;
    }

    public static List<Environment> createAndSaveEnvironments() throws IOException {
        return Lists.newArrayList(
                createAndSaveEnvironment("envId123", "description123"),
                createAndSaveEnvironment("envId456", "description456")
        );
    }
}
