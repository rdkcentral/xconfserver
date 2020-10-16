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
 * Created: 6/7/2016
*/
package com.comcast.xconf.filter;

import com.comcast.xconf.contextconfig.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.lang.reflect.Field;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestContext.class})
public class UpdateDeleteApiFilterTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private Filter apiFilter = new UpdateDeleteApiFilter();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(apiFilter, "/*").build();
    }

    @Test
    public void testDeletePathWhenApiEnabled() throws Exception {
        enableApi(true);
        performDeleteRequestAndVerifyStatus(204);
    }

    @Test
    public void testDeletePathWhenApiDisabled() throws Exception {
        enableApi(false);
        performDeleteRequestAndVerifyStatus(403);
    }

    private void performDeleteRequestAndVerifyStatus(int status) throws Exception {
        mockMvc.perform(
                delete("/delete/rules/ips/123/")
        )
                .andExpect(status().is(status));
    }

    @Test
    public void testGetPathWhenApiEnabled() throws Exception {
        enableApi(true);
        performGetRequest();
    }

    @Test
    public void testGetPathWhenApiDisabled() throws Exception {
        enableApi(false);
        performGetRequest();
    }

    private void performGetRequest() throws Exception {
        mockMvc.perform(
                get("/queries/rules/ips/")
        )
                .andExpect(status().is(200));
    }

    private void enableApi(boolean value)  throws NoSuchFieldException, IllegalAccessException {
        Field field = ReflectionUtils.findField(UpdateDeleteApiFilter.class, "enableUpdateDeleteAPI");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, apiFilter, value);
    }
}
