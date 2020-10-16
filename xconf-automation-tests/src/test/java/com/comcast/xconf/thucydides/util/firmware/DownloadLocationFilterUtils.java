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
 * <p>
 * Author: Stanislav Menshykov
 * Created: 3/17/16  10:45 AM
 */
package com.comcast.xconf.thucydides.util.firmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.xconf.estbfirmware.DownloadLocationRoundRobinFilterValue;
import com.comcast.xconf.thucydides.util.HttpClient;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.common.ModelUtils;

import java.util.ArrayList;

public class DownloadLocationFilterUtils {
    public static final String UPDATES_DOWNLOAD_LOCATION_FILTER_URL = TestConstants.SERVER_URL + "dataService/updates/filters/downloadlocation";

    public static void doCleanup() throws Exception {
        ModelUtils.doCleanup();
    }

    public static DownloadLocationRoundRobinFilterValue createDefaultRoundRobinFilter() throws Exception {
        DownloadLocationRoundRobinFilterValue result = new DownloadLocationRoundRobinFilterValue();
        result.setHttpLocation("test.com");
        result.setHttpFullUrlLocation("http://test.com");
        result.setLocations(new ArrayList<DownloadLocationRoundRobinFilterValue.Location>(){{
            DownloadLocationRoundRobinFilterValue.Location location = new DownloadLocationRoundRobinFilterValue.Location();
            location.setLocationIp(new IpAddress("1.1.1.1"));
            location.setPercentage(100);
            add(location);
        }});
        result.setIpv6locations(new ArrayList<DownloadLocationRoundRobinFilterValue.Location>() {{
            DownloadLocationRoundRobinFilterValue.Location location = new DownloadLocationRoundRobinFilterValue.Location();
            location.setLocationIp(new IpAddress("1::1"));
            location.setPercentage(100);
            add(location);
        }});

        return result;
    }

    public static DownloadLocationRoundRobinFilterValue createAndSaveDefaultRoundRobinFilter() throws Exception {
        DownloadLocationRoundRobinFilterValue result = createDefaultRoundRobinFilter();
        HttpClient.post(UPDATES_DOWNLOAD_LOCATION_FILTER_URL, result);

        return result;
    }
}
