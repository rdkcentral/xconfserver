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
 * Created: 02.06.15 19:08
*/
package com.comcast.xconf.dcm.converter;

import com.comcast.xconf.dcm.core.Utils;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.logupload.Formula;

public class FormulaConverter {

    public Formula convertToFormula(final FormulaDataObject formulaDataObject) {
        final Formula formula = new Formula();

        formula.setId(formulaDataObject.getId());
        formula.setName(formulaDataObject.getName());
        formula.setDescription(formulaDataObject.getDescription());
        formula.setEstbIP(formulaDataObject.getEstbIP());
        formula.setEstbMacAddress(formulaDataObject.getEstbMacAddress());
        formula.setEcmMacAddress(formulaDataObject.getEcmMacAddress());
        formula.setEnv(Utils.propertySplitter(formulaDataObject.getEnv(), "( OR )"));
        formula.setModel(Utils.propertySplitter(formulaDataObject.getModel(), "( OR )"));
        formula.setFirmwareVersion(Utils.propertySplitter(formulaDataObject.getFirmwareVersion(), "( OR )"));
        formula.setControllerId(Utils.propertySplitter(formulaDataObject.getControllerId(), "( OR )"));
        formula.setChannelMapId(Utils.propertySplitter(formulaDataObject.getChannelMapId(), "( OR )"));
        formula.setVodId(Utils.propertySplitter(formulaDataObject.getVodId(), "( OR )"));
        formula.setRuleExpression(formulaDataObject.getRuleExpression());
        formula.setPriority(formulaDataObject.getPriority());
        formula.setPercentage(formulaDataObject.getPercentage());
        formula.setPercentageL1(formulaDataObject.getPercentageL1());
        formula.setPercentageL2(formulaDataObject.getPercentageL2());
        formula.setPercentageL3(formulaDataObject.getPercentageL3());

        return formula;
    }

    public FormulaDataObject convertToFormulaDataObject(final Formula formula) {
        if (formula == null) {
            return null;
        }

        final FormulaDataObject formulaDataObject = new FormulaDataObject();

        formulaDataObject.setId(formula.getId());
        formulaDataObject.setName(formula.getName());
        formulaDataObject.setDescription(formula.getDescription());
        formulaDataObject.setEstbIP(formula.getEstbIP());
        formulaDataObject.setEstbMacAddress(formula.getEstbMacAddress());
        formulaDataObject.setEcmMacAddress(formula.getEcmMacAddress());
        formulaDataObject.setEnv(Utils.joinOr(formula.getEnv()));
        formulaDataObject.setModel(Utils.joinOr(formula.getModel()));
        formulaDataObject.setFirmwareVersion(Utils.joinOr(formula.getFirmwareVersion()));
        formulaDataObject.setControllerId(Utils.joinOr(formula.getControllerId()));
        formulaDataObject.setChannelMapId(Utils.joinOr(formula.getChannelMapId()));
        formulaDataObject.setVodId(Utils.joinOr(formula.getVodId()));
        formulaDataObject.setRuleExpression(formula.getRuleExpression());
        formulaDataObject.setPriority(formula.getPriority());
        formulaDataObject.setPercentage(formula.getPercentage());
        formulaDataObject.setPercentageL1(formula.getPercentageL1());
        formulaDataObject.setPercentageL2(formula.getPercentageL2());
        formulaDataObject.setPercentageL3(formula.getPercentageL3());
        return formulaDataObject;
    }

}
