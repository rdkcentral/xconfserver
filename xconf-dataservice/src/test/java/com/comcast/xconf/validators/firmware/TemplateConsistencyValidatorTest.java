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
 * Created: 7/8/2016
*/
package com.comcast.xconf.validators.firmware;

import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.internal.matchers.Contains;
import org.springframework.beans.factory.annotation.Autowired;

public class TemplateConsistencyValidatorTest extends BaseQueriesControllerTest {

    @org.junit.Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private TemplateConsistencyValidator validator;

    @Test
    public void verifyFailed_ExtraFreeArgInRule() throws Exception {
        Rule ipRule = RuleFactory.newIpRule("IpListName", "QA", "X1");
        Rule template = RuleFactory.newEnvModelRule("env", "model");

        expectedException.expect(ValidationRuntimeException.class);
        expectedException.expectMessage(new Contains(StbContext.IP_ADDRESS));

        validator.validate(ipRule, template);
    }

    @Test
    public void verifyOK_ValidRule() throws Exception {
        Rule envModelRule = RuleFactory.newEnvModelRule("QA", "X1");
        Rule template = RuleFactory.newEnvModelRule("env", "model");

        validator.validate(envModelRule, template);
    }

    @Test
    public void verifyOK_ExtraFreeArgInTemplate() throws Exception {
        Rule envModelRule = RuleFactory.newEnvModelRule("QA", "X1");
        Rule template = RuleFactory.newIpRule("list", "env", "model");

        validator.validate(envModelRule, template);
    }
}
