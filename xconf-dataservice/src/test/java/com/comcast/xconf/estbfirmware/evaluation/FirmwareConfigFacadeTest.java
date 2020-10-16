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
 * Created: 2/22/2016
*/
package com.comcast.xconf.estbfirmware.evaluation;

import com.comcast.xconf.estbfirmware.FirmwareConfigFacade;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;

import javax.xml.bind.DataBindingException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class FirmwareConfigFacadeTest {

    private ObjectMapper mapper = (new ObjectMapper()).setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Test
    public void testSerialize() throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", "value");
        map.put("key2", true);
        map.put("key3", 10);
        FirmwareConfigFacade facade = new FirmwareConfigFacade(map);
        ObjectWriter jsonWriter = mapper.writer();

        try {
            String json  = jsonWriter.writeValueAsString(facade);
            System.out.println("json: " + json);

            Object o = mapper.reader(FirmwareConfigFacade.class).readValue(json);
            System.out.println("Object: " + o);
        } catch (IOException var3) {
            throw new DataBindingException(var3);
        }
    }
}
