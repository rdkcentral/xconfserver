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
 * Author: Igor Kostrov
 * Created: 2/1/2016
*/
package com.comcast.xconf.estbfirmware.converter;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.xconf.estbfirmware.DownloadLocationFilter;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DownloadLocationFilterConverterTest extends BaseQueriesControllerTest {

    @Autowired
    private DownloadLocationFilterConverter converter;

    @Test
    public void convertFilterWithTftpConditions() throws Exception {
        DownloadLocationFilter filter = createFilter();
        filter.setHttpLocation(null);

        assertConvertedEquals(filter);
    }

    @Test
    public void convertFilterWithHttpCondition() throws Exception {
        DownloadLocationFilter filter = createFilter();
        filter.setFirmwareLocation(null);
        filter.setIpv6FirmwareLocation(null);

        assertConvertedEquals(filter);
    }

    private void assertConvertedEquals(DownloadLocationFilter filter) {
        FirmwareRule rule = converter.convert(filter);
        DownloadLocationFilter converted = converter.convert(rule);
        Assert.assertEquals(filter, converted);
    }

    private DownloadLocationFilter createFilter() {
        DownloadLocationFilter filter = new DownloadLocationFilter();
        filter.setId("filterID");
        filter.setName("filterName");
        filter.setBoundConfigId("configID");
        filter.setForceHttp(true);
        filter.setFirmwareLocation(IpAddress.parse("1.1.1"));
        filter.setIpv6FirmwareLocation(IpAddress.parse("::1"));
        filter.setHttpLocation("http://comcast.com");
        filter.setIpAddressGroup(createDefaultIpAddressGroupExtended());
        return filter;
    }

}
