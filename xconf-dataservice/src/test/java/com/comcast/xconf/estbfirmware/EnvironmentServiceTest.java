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
 *  Author: mdolina
 *  Created: 1/21/16 6:54 PM
 */

package com.comcast.xconf.estbfirmware;

import com.comcast.xconf.Environment;
import com.comcast.xconf.dcm.converter.DcmRuleConverter;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EnvironmentServiceTest extends BaseQueriesControllerTest {

    @Autowired
    private EnvironmentQueriesService environmentService;

    @Autowired
    private DcmRuleConverter converter;

    @Test
    public void сheckUsageEnvironmentIsNotUsedAnywhere() throws Exception {
        Environment environment = createEnvironment(defaultEnvironmentId);
        environmentDAO.setOne(environment.getId(), environment);

        assertNull(environmentService.checkUsage(environment.getId()));
    }

    @Test
    public void checkUsageEnvironmentIsUsedInFormula() throws Exception {
        Environment environment = createEnvironment(defaultEnvironmentId);
        environmentDAO.setOne(environment.getId(), environment);
        FormulaDataObject formula = createDefaultFormula();
        DCMGenericRule rule = converter.convertToRule(formula);
        dcmRuleDAO.setOne(rule.getId(), rule);

        assertEquals("ruleType=Formula name=" + formula.getName(), environmentService.checkUsage(environment.getId()));
    }
}