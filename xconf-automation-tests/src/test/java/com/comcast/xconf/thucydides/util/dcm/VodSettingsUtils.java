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
 * Created: 3/29/16  12:29 PM
 */
package com.comcast.xconf.thucydides.util.dcm;

import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.VodSettings;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class VodSettingsUtils {
    private static final String VOD_SETTINGS_URL = "dcm/vodsettings";

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(VOD_SETTINGS_URL, VodSettings.class);
        FormulaUtils.doCleanup();
    }

    public static VodSettings createAndSaveDefaultVodSettings() throws Exception {
        VodSettings result = createDefaultVodSettings();
        HttpClient.post(GenericTestUtils.buildFullUrl(VOD_SETTINGS_URL), result);

        return result;
    }

    public static VodSettings createDefaultVodSettings() throws Exception {
        DCMGenericRule formula = FormulaUtils.createAndSaveDefaultFormula();
        VodSettings result = createVodSettingsWithoutFormula(formula.getId());

        return result;
    }

    public static VodSettings createVodSettingsWithoutFormula(String id) {
        VodSettings vodSettings = new VodSettings();
        vodSettings.setId(id);
        vodSettings.setName("vodSettingsName");
        vodSettings.setLocationsURL("http://test.com");
        vodSettings.setIpNames(Collections.singletonList("ip"));
        vodSettings.setIpList(Collections.singletonList("1.1.1.1"));

        return vodSettings;
    }

    public static VodSettings createAndSaveVodSettings(String name) throws Exception {
        VodSettings vodSettings = createVodSettingsWithoutFormula(UUID.randomUUID().toString());
        vodSettings.setName(name);
        HttpClient.post(GenericTestUtils.buildFullUrl(VOD_SETTINGS_URL), vodSettings);
        return vodSettings;
    }

    public static List<VodSettings> createAndSaveVodSettingsList() throws Exception {
        return Lists.newArrayList(
                createAndSaveVodSettings("vodSettings1"),
                createAndSaveVodSettings("vodSettings2")
        );
    }
}
