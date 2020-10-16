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
 *  Created: 12/25/15 1:13 PM
 */

package com.comcast.xconf.dcm.formula;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.logupload.LogFile;
import com.comcast.xconf.logupload.VodSettings;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
public class DCMTestCase28 extends BaseDcmFormulaTestCase {

    private final String defaultFormulaId = "formulaId28";
    private FormulaDataObject formula = createFormula();
    private VodSettings vodSettings = createVodSettings();
    private List<LogFile> logFiles = createLogFileList();
    private List<GenericNamespacedList> namespacedLists = createNamespacedLists();

    @Before()
    public void init() throws Exception {
        initDcmFormula(formula);
        initLists(namespacedLists, logFiles);
        initDcmSettings(null, null, vodSettings);
    }

    @Test
    public void testBehaviorWhenOnlyDeviceSettingsIsPresent() throws Exception {
        mockMvc.perform(get("/loguploader/getSettings?ecmMacAddress=2C:6E:56:8A:CA:96"))
                .andExpect(status().isOk())
                .andExpect(equalResponse(Lists.newArrayList(defaultFormulaId)));
    }

    private VodSettings createVodSettings() {
        VodSettings vodSettings = new VodSettings();
        vodSettings.setId(defaultFormulaId);
        vodSettings.setName("guneet_VOD_TC28");
        vodSettings.setLocationsURL("http://foo.com");
        vodSettings.setSrmIPList(new HashMap<String, String>());
        return vodSettings;
    }

    private List<LogFile> createLogFileList() {
        LogFile logFile1 = new LogFile();
        logFile1.setId("XQA_LogFile_001");
        logFile1.setName("XQA_LogFile_001");
        logFile1.setDeleteOnUpload(false);
        return Lists.newArrayList(logFile1);
    }

    private FormulaDataObject createFormula() {
        FormulaDataObject formula = new FormulaDataObject();
        formula.setId(defaultFormulaId);
        formula.setName("formula28");
        formula.setDescription("guneet_formula_TC28");
        formula.setPercentage(100);
        formula.setPriority(15);
        formula.setEcmMacAddress("ecmMacListFormula28");
        formula.setRuleExpression("ecmMacAddress");
        return formula;
    }

    private List<GenericNamespacedList> createNamespacedLists() {
        List<GenericNamespacedList> namespacedLists = new ArrayList<>();
        namespacedLists.add(createGenericNamespacedList("ecmMacListFormula28", GenericNamespacedListTypes.MAC_LIST, "2C:6E:56:8A:CA:96"));
        return namespacedLists;
    }
}
