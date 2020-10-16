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
 * Created: 25.05.15 21:16
*/
package com.comcast.xconf.dcm.converter;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.util.RuleUtil;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DcmRuleConverterTest {

    private DcmRuleConverter converter = new DcmRuleConverter();

    @Before
    public void setUp() throws Exception {
        Field parserField = ReflectionUtils.findField(DcmRuleConverter.class, "parser");
        parserField.setAccessible(true);
        RuleExpressionParser parser = new RuleExpressionParser();
        ReflectionUtils.setField(parserField, converter, parser);

        Field builderField = ReflectionUtils.findField(RuleExpressionParser.class, "builder");
        builderField.setAccessible(true);
        ReflectionUtils.setField(builderField, parser, new FormulaRuleBuilder());
    }

    @Test
    public void testConverter() throws Exception {
        FormulaDataObject dataObject = createDataObject();
        DCMGenericRule rule = converter.convertToRule(dataObject);
        System.out.println("rule: " + rule.toString());

        FormulaDataObject convertedObject = converter.convertToFormulaDataObject(rule);
        JSONAssert.assertEquals(JsonUtil.toJson(dataObject), JsonUtil.toJson(convertedObject), true);
    }

    @Test
    public void testConvertRuleExpression() throws Exception {
        FormulaDataObject dataObject = createSimpleFormula();
        DCMGenericRule dcmRule = converter.convertToRule(dataObject);
        dcmRule.getCompoundParts().get(0).setNegated(true);
        dcmRule.getCompoundParts().get(0).setCondition(createCondition("firmwareVersion", "testVersion"));
        FormulaDataObject convertedDataObject = converter.convertToFormulaDataObject(dcmRule);

        assertNull(convertedDataObject.getModel());
        assertNotNull("firmwareVersion", convertedDataObject.getFirmwareVersion());
        assertEquals("NOT firmwareVersion AND estbMacAddress", convertedDataObject.getRuleExpression());
    }

    /*IS operation is not supported in an old admin, IN operation is used instead of it*/
    @Test
    public void testConverterWithUnsupportedIsOperation() {
        FormulaDataObject formula = createSimpleFormula();
        DCMGenericRule rule = converter.convertToRule(formula);
        List<Rule> compoundParts = rule.getCompoundParts();
        compoundParts.get(0).getCondition().setOperation(Operation.forName("IS"));
        compoundParts.get(0).getCondition().setFixedArg(FixedArg.from("model"));

        assertEquals(formula, converter.convertToFormulaDataObject(rule));
    }

    @Test
    public void testRuleNegation() {
        FormulaDataObject formula = createDataObject();
        DCMGenericRule rule = converter.convertToRule(formula);
        for (Rule rule1: rule.getCompoundParts()) {
            if (!rule1.isCompound() && rule1.getCondition().getFreeArg().getName().equals("ecmMacAddress")) {
                assertEquals(true, rule1.isNegated());
            }
        }
    }

    @Test
    public void convertRuleWithMatchOperation() {
        FormulaDataObject formula = new FormulaDataObject();
        formula.setId("id123");
        formula.setName("name");
        formula.setModel("model.*");
        formula.setPercentage(100);
        formula.setFirmwareVersion("firmwareVersion");
        formula.setRuleExpression("model AND firmwareVersion");
        DCMGenericRule dcmRule = converter.convertToRule(formula);
        Iterable<Condition> conditions = RuleUtil.toConditions(dcmRule);

        for(Condition condition : conditions) {
            if (RuleFactory.MODEL.equals(condition.getFreeArg())) {
                assertEquals(RuleFactory.MATCH, condition.getOperation());
            } else {
                assertEquals(StandardOperation.IS, condition.getOperation());
            }
        }

        assertEquals(formula, converter.convertToFormulaDataObject(dcmRule));
    }

    private FormulaDataObject createDataObject() {
        FormulaDataObject object = new FormulaDataObject();

        object.setId("id123");
        object.setName("SomeName");
        object.setDescription("SomeDescription");
        object.setPercentage(50);
        object.setPercentageL1(50);
        object.setPercentageL2(50);
        object.setPercentageL3(50);

        object.setEnv("QA");
        object.setEnvList(Arrays.asList("QA"));
        object.setEcmMacAddress("macList1");
        object.setEstbMacAddress("macList2");
        object.setModel("123 OR 456");
        object.setControllerId("controller123");
        object.setChannelMapId("map123");
        object.setFirmwareVersion("10.11.12");

        object.setRuleExpression("env AND NOT ecmMacAddress OR estbMacAddress OR model AND controllerId OR NOT channelMapId AND firmwareVersion");
        return object;
    }

    FormulaDataObject createSimpleFormula() {
        FormulaDataObject formula = new FormulaDataObject();
        formula.setId("formulaId");
        formula.setName("testName");
        formula.setPercentage(100);
        formula.setModel("model");
        formula.setEstbMacAddress("macList");
        formula.setRuleExpression("model AND estbMacAddress");
        return formula;
    }

    private Condition createCondition(String name, String value) {
        Condition condition = new Condition();
        condition.setFreeArg(new FreeArg(StandardFreeArgType.STRING, name));
        condition.setOperation(StandardOperation.IN);
        condition.setFixedArg(FixedArg.from(value));
        return condition;
    }
}
