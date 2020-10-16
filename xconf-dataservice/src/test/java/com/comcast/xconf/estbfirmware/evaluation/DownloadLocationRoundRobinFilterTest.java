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
 * Created: 04.05.2016
*/
package com.comcast.xconf.estbfirmware.evaluation;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.xconf.estbfirmware.DownloadLocationRoundRobinFilterValue;
import com.comcast.xconf.estbfirmware.EstbFirmwareContext;
import com.comcast.xconf.estbfirmware.FirmwareConfigFacade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.comcast.xconf.util.RequestUtil.XCONF_USER_AGENT;
import static com.comcast.xconf.estbfirmware.Capabilities.*;
import static com.comcast.xconf.estbfirmware.FirmwareConfig.DownloadProtocol;


public class DownloadLocationRoundRobinFilterTest {

    public static final String HTTP_FULL_URL_LOCATION = "http://comcast.com";
    public static final String HTTP_LOCATION = "comcast.com";
    public static final String IP_ADDRESS = "192.168.1.1";

    @Test
    public void verifyHttpWhenRCDLAndConnectionIsNotSecure() throws Exception {
        FirmwareConfigFacade config = createConfig(DownloadProtocol.http);

        EstbFirmwareContext.Converted context = createContext(RCDL.toString());
        context.setXconfHttpHeader(XCONF_USER_AGENT);
        boolean result = DownloadLocationRoundRobinFilter.filter(config, createFilter(), context);

        Assert.assertTrue(result);
        Assert.assertEquals(HTTP_LOCATION, config.getFirmwareLocation());
        Assert.assertEquals(DownloadProtocol.http.toString(), config.getFirmwareDownloadProtocol());
    }

    @Test
    public void verifyFullHttpWhenRcdlAndSupportFullUrlAndConnectionIsNotSecure() throws Exception {
        FirmwareConfigFacade config = createConfig(DownloadProtocol.http);

        EstbFirmwareContext.Converted context = createContext(RCDL.toString(), supportsFullHttpUrl.toString());
        context.setXconfHttpHeader(XCONF_USER_AGENT);
        boolean result = DownloadLocationRoundRobinFilter.filter(config, createFilter(), context);

        Assert.assertTrue(result);
        Assert.assertEquals(HTTP_FULL_URL_LOCATION, config.getFirmwareLocation());
        Assert.assertEquals(DownloadProtocol.http.toString(), config.getFirmwareDownloadProtocol());
    }

    @Test
    public void verifyHttpWhenNoRCDL() throws Exception {
        FirmwareConfigFacade config = createConfig(DownloadProtocol.http);

        EstbFirmwareContext.Converted context = createContext(rebootDecoupled.toString());
        boolean result = DownloadLocationRoundRobinFilter.filter(config, createFilter(), context);

        Assert.assertTrue(result);
        Assert.assertEquals(HTTP_LOCATION, config.getFirmwareLocation());
        Assert.assertEquals(DownloadProtocol.http.toString(), config.getFirmwareDownloadProtocol());
    }

    private DownloadLocationRoundRobinFilterValue createFilter() {
        DownloadLocationRoundRobinFilterValue filter = new DownloadLocationRoundRobinFilterValue();
        filter.setHttpLocation(HTTP_LOCATION);
        filter.setHttpFullUrlLocation(HTTP_FULL_URL_LOCATION);
        filter.setLocations(Arrays.asList(newLocation(IP_ADDRESS)));
        return filter;
    }

    private FirmwareConfigFacade createConfig() {
        return createConfig(DownloadProtocol.tftp);
    }

    private FirmwareConfigFacade createConfig(DownloadProtocol protocol) {
        FirmwareConfigFacade config = new FirmwareConfigFacade();
        config.setFirmwareDownloadProtocol(protocol);
        return config;
    }

    private EstbFirmwareContext.Converted createContext(String... capabilities) {
        EstbFirmwareContext context = new EstbFirmwareContext();
        context.setModel("X1");
        context.setIpAddress("1.1.1.1");
        context.seteStbMac("11:22:33:44:55:66");
        context.setCapabilities(Arrays.asList(capabilities));
        return context.convert();
    }

    private DownloadLocationRoundRobinFilterValue.Location newLocation(String ip) {
        DownloadLocationRoundRobinFilterValue.Location location = new DownloadLocationRoundRobinFilterValue.Location();
        location.setLocationIp(IpAddress.parse(ip));
        location.setPercentage(100);
        return location;
    }
}
