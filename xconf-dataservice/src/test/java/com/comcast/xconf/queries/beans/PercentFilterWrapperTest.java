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
 * Author: ikostrov
 * Created: 04.09.15 19:22
*/
package com.comcast.xconf.queries.beans;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.estbfirmware.EnvModelPercentage;
import com.comcast.xconf.estbfirmware.PercentFilterValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.DataBindingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PercentFilterWrapperTest {

    private final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Test
    public void testWrapper() throws Exception {
        PercentFilterValue value = createPercentFilter();
        PercentFilterWrapper wrapper = new PercentFilterWrapper(value);
        Assert.assertEquals(toJSON(value), toJSON(wrapper.toPercentFilterValue()));
    }

    public <T> String toJSON(T entity) {
        final ObjectWriter jsonWriter = mapper.writer();
        try {
            return jsonWriter.writeValueAsString(entity);
        } catch (IOException e) {
            throw new DataBindingException(e);
        }
    }

    private PercentFilterValue createPercentFilter() {
        PercentFilterValue percentFilterValue = new PercentFilterValue();
        percentFilterValue.setId(PercentFilterValue.SINGLETON_ID);
        percentFilterValue.setPercentage(50);
        percentFilterValue.setWhitelist(createIpAddressGroup());
        percentFilterValue.setEnvModelPercentages(createEnvModelPercentages());
        return percentFilterValue;
    }
    protected IpAddressGroupExtended createIpAddressGroup() {
        IpAddressGroupExtended ipAddressGroup = new IpAddressGroupExtended();
        ipAddressGroup.setId("ipAddressGroupId");
        Set<IpAddress> addresses = new HashSet<>();
        addresses.add(new IpAddress("10.11.12.13"));
        ipAddressGroup.setIpAddresses(addresses);
        ipAddressGroup.setName("ipAddressGroupName");
        return ipAddressGroup;
    }

    private Map<String, EnvModelPercentage> createEnvModelPercentages() {
        Map<String, EnvModelPercentage> map = new HashMap<>();
        EnvModelPercentage envModelPercentage = new EnvModelPercentage();
        envModelPercentage.setActive(true);
        envModelPercentage.setFirmwareCheckRequired(false);
        envModelPercentage.setPercentage(33);
        map.put("model1", envModelPercentage);
        return map;
    }
}
