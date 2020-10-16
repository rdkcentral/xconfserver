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
package com.comcast.xconf;

import com.comcast.xconf.util.RequestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: ikostrov
 * Date: 09.10.14
 * Time: 18:16
 */
public class RequestUtilTest {

    private RequestUtil requestUtil;

    @Before
    public void setUp() {
        requestUtil = new RequestUtil(Collections.singletonList(RequestUtil.XFF_HEADER_NAME));
    }

    @Test
    public void testXFF() throws Exception {
        String ip = "1.1.1.1";
        assertXffHeader(ip, ip);
        assertXffHeader("  " + ip, ip);
        assertXffHeader("  " + ip + " ", ip);
        assertXffHeader(String.format("%s, proxy1, proxy2", ip), ip);
        assertXffHeader(String.format(" %s  , proxy1, proxy2", ip), ip);

        // for empty header should be null response
        assertXffHeader("", null);
        assertXffHeader("  ", null);
        assertXffHeader("  , proxy1", null);
        assertXffHeader(null, null);
    }

    private void assertXffHeader(String xffHeader, String answer) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(RequestUtil.XFF_HEADER_NAME)).thenReturn(xffHeader);
        String ip = requestUtil.grepIpAddressFromXFF(request);
        Assert.assertEquals(ip, answer);
    }
}
