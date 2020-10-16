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
 * Author: Stanislav Menshykov
 * Created: 3/25/16  11:10 AM
 */
package com.comcast.xconf.thucydides.util.dcm;

import com.beust.jcommander.internal.Lists;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.estbfirmware.logupload.LogUploadArgs;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;
import com.comcast.xconf.thucydides.util.RuleUtils;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class FormulaUtils {
    private static final String FORMULA_URL = "dcm/formula";

    public static String defaultId = UUID.fromString("1-2-3-4-5").toString();

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(FORMULA_URL, DCMGenericRule.class);
    }

    public static DCMGenericRule createAndSaveDefaultFormula() throws Exception {
        DCMGenericRule result = createDefaultFormula();
        HttpClient.post(GenericTestUtils.buildFullUrl(FORMULA_URL), result);

        return result;
    }

    public static DCMGenericRule createAndSaveFormula(String name, Integer priority, Condition condition) throws IOException {
        DCMGenericRule formula = new DCMGenericRule();
        formula.setId(UUID.randomUUID().toString());
        formula.setName(name);
        formula.setDescription(name + "description");
        formula.setPercentage(100);
        formula.setPriority(priority);
        formula.setCondition(condition);
        HttpClient.post(GenericTestUtils.buildFullUrl(FORMULA_URL), formula);
        return formula;
    }

    public static DCMGenericRule createDefaultFormula() {
        DCMGenericRule result = new DCMGenericRule();
        result.setId(defaultId);
        result.setCondition(RuleUtils.createCondition(LogUploadArgs.ESTB_IP, StandardOperation.IS, "1.1.1.1"));
        result.setName("formulaName");
        result.setDescription("formulaDescription");
        result.setPriority(1);
        result.setPercentage(100);
        result.setPercentageL1(0);
        result.setPercentageL2(100);
        result.setPercentageL3(0);

        return result;
    }

    public static List<DCMGenericRule> createAndSaveFormulas() throws Exception {
        return Lists.newArrayList(
            createAndSaveFormula("formulaId123", 1, RuleUtils.createCondition(LogUploadArgs.ESTB_MAC, StandardOperation.IS, "AA:AA:AA:AA:AA:AA")),
            createAndSaveFormula("formulaId456", 2, RuleUtils.createCondition(LogUploadArgs.ESTB_IP, StandardOperation.IS, "10.10.10.10"))
        );
    }
}
