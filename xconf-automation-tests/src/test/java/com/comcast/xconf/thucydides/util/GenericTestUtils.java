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
 * limitations under the License.=
 *
 * Author: Stanislav Menshykov
 * Created: 03.03.16  12:18
 */
package com.comcast.xconf.thucydides.util;

import com.comcast.hydra.astyanax.data.IPersistable;
import com.google.common.io.Files;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.comcast.xconf.thucydides.util.TestConstants.UX_URL;

public class GenericTestUtils {

    public static FirefoxProfile createProfileNeverAskSaving(String pathToDownloadDir) {
        FirefoxProfile result = new FirefoxProfile();
        result.setPreference("browser.download.folderList", 2);
        result.setPreference("browser.download.manager.showWhenStarting", false);
        result.setPreference("browser.download.dir", pathToDownloadDir);
        result.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/xml, application/json, application/vnd.ms-excel");

        return result;
    }

    public static File createTempDir() {
        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        return tempDir;
    }


    public static String makeSwuStbRequest(String mac, String env, String model, String firmwareVersion) throws Exception {
        return HttpClient.get(TestConstants.SERVER_URL + "dataService/xconf/swu/stb?eStbMac=" + mac + "&env=" + env +
                                                            "&model=" + model + "&firmwareVersion=" + firmwareVersion);
    }

    public static String buildFullUrl(final String relativeUrl) {
        return UX_URL + relativeUrl;
    }

    public static <T extends IPersistable> void deleteEntities(final String url, final Class<T> clazz) throws IOException {
        deleteEntities(url, url, clazz);
    }

    public static <T extends IPersistable> void deleteEntities(final String getUrl, final String deleteUrl, final Class<T> clazz) throws IOException {
        final List<T> data = HttpClient.getAll(buildFullUrl(getUrl), clazz);
        for (T entity : data) {
            HttpClient.delete(buildFullUrl(deleteUrl), entity.getId());
        }
    }
}
