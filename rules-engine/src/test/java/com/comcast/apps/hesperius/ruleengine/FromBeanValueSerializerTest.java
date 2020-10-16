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
 * Author: rdolomansky
 * Created: 5/23/16  7:35 PM
 */
package com.comcast.apps.hesperius.ruleengine;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class FromBeanValueSerializerTest {

    private static final ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testWithIpAddress() throws Exception {
        final FixedArg fixedArg = FixedArg.FromBean.from(IpAddress.parse("1.1.1.1"));
        final String valueWithIpAddress = mapper.writeValueAsString(fixedArg);
        check(valueWithIpAddress, IpAddress.class, "\"1.1.1.1\"", fixedArg);
    }

    @Test
    public void testWithInteger() throws Exception {
        final FixedArg fixedArg = FixedArg.FromBean.from(new Integer(15));
        final String valueWithInteger = mapper.writeValueAsString(fixedArg);
        check(valueWithInteger, Integer.class, "15", fixedArg);
    }

    @Test
    public void testWithLong() throws Exception {
        final FixedArg fixedArg = FixedArg.FromBean.from(new Long(15l));
        final String valueWithLong = mapper.writeValueAsString(fixedArg);
        check(valueWithLong, Long.class, "15", fixedArg);
    }

    @Test
    public void testWithFloat() throws Exception {
        final FixedArg fixedArg = FixedArg.FromBean.from(new Float(15.15f));
        final String valueWithFloat = mapper.writeValueAsString(fixedArg);
        check(valueWithFloat, Float.class, "15.15", fixedArg);
    }

    @Test
    public void testWithDouble() throws Exception {
        final FixedArg fixedArg = FixedArg.FromBean.from(new Double(15.15));
        final String valueWithDouble = mapper.writeValueAsString(fixedArg);
        check(valueWithDouble, Double.class, "15.15", fixedArg);
    }

    private void check(final String sourceJsonString, final Class<?> expectedClass, final String expectedJsonString,
            final FixedArg sourceFixedArg) {
        Assert.assertEquals(sourceJsonString,
                "{\"bean\":{\"value\":{\"" + expectedClass.getCanonicalName() + "\":" + expectedJsonString + "}}}");
        try {
            final FixedArg fixedArg = mapper.readValue(sourceJsonString, FixedArg.class);
            Assert.assertEquals(sourceFixedArg, fixedArg);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

}
