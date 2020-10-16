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
 * Created: 3/21/16  1:52 PM
 */
package com.comcast.xconf.thucydides.util.firmware;

import com.beust.jcommander.internal.Lists;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;
import com.comcast.xconf.thucydides.util.common.ModelUtils;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FirmwareConfigUtils {
    private final static String FIRMWARE_CONFIG_URL = "firmwareconfig";

    public static String defaultDescription = "firmwareConfigDescription";
    public static String defaultVersion = "firmwareConfigVersion";
    public static String defaultFileName = "firmwareConfigFileName";
    public static String defaultId = UUID.fromString("1-2-3-4-5").toString();

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(FIRMWARE_CONFIG_URL, FirmwareConfig.class);
        ModelUtils.doCleanup();
    }

    public static FirmwareConfig createAndSaveDefaultFirmwareConfig() throws Exception {
        ModelUtils.createAndSaveDefaultModel();
        FirmwareConfig result = createDefaultFirmwareConfig();
        HttpClient.post(GenericTestUtils.buildFullUrl(FIRMWARE_CONFIG_URL), result);

        return result;
    }

    public static FirmwareConfig createAndSaveFirmwareConfig(String id, String firmwareVersion) throws IOException {
        FirmwareConfig firmwareConfig = createDefaultFirmwareConfig();
        firmwareConfig.setId(id);
        firmwareConfig.setFirmwareVersion(firmwareVersion);
        firmwareConfig.setDescription(firmwareVersion + "_description");
        HttpClient.post(GenericTestUtils.buildFullUrl(FIRMWARE_CONFIG_URL), firmwareConfig);
        return firmwareConfig;
    }

    public static FirmwareConfig createDefaultFirmwareConfig() {
        FirmwareConfig result = new FirmwareConfig();
        result.setDescription(defaultDescription);
        result.setId(defaultId);
        result.setFirmwareFilename(defaultFileName);
        result.setFirmwareVersion(defaultVersion);
        result.setSupportedModelIds(Collections.singleton(ModelUtils.defaultModelId));

        return result;
    }

    public static FirmwareConfig createAndSaveFirmwareConfig(String id, String firmwareVersion, String description, Set<String> modelIds) throws IOException {
        FirmwareConfig firmwareConfig = new FirmwareConfig();
        firmwareConfig.setId(id);
        firmwareConfig.setFirmwareVersion(firmwareVersion);
        firmwareConfig.setDescription(description);
        firmwareConfig.setSupportedModelIds(modelIds);
        firmwareConfig.setFirmwareFilename(defaultFileName);
        for (String modelId : modelIds) {
            ModelUtils.createAndSaveModel(modelId);
        }
        HttpClient.post(GenericTestUtils.buildFullUrl(FIRMWARE_CONFIG_URL), firmwareConfig);

        return firmwareConfig;
    }

    public static List<FirmwareConfig> createAndSaveFirmwareConfigs() throws IOException {
        FirmwareConfig firmwareConfig1 = createAndSaveFirmwareConfig(
                "id123", "firmwareVersion1", "description1", Sets.newHashSet("MODEL1", "MODEL2")
        );

        FirmwareConfig firmwareConfig2 = createAndSaveFirmwareConfig(
                "id456", "firmwareVersion2", "description2", Sets.newHashSet("MODEL3", "MODEL4")
        );
        return Lists.newArrayList(firmwareConfig1, firmwareConfig2);
    }
}
