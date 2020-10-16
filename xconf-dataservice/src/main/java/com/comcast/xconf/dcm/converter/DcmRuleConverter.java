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
 * Created: 25.05.15 16:00
*/
package com.comcast.xconf.dcm.converter;

import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.dcm.core.Utils;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@Component
public class DcmRuleConverter {

    private static Logger log = LoggerFactory.getLogger(DcmRuleConverter.class);

    private static final String SETTER_PREFIX = "set";

    @Autowired
    private RuleExpressionParser parser;

    public DCMGenericRule convertToRule(final FormulaDataObject formulaDataObject) {
        final DCMGenericRule rule = new DCMGenericRule();

        rule.setId(formulaDataObject.getId());
        rule.setName(formulaDataObject.getName());
        rule.setApplicationType(formulaDataObject.getApplicationType());
        rule.setDescription(formulaDataObject.getDescription());
        rule.setRuleExpression(formulaDataObject.getRuleExpression());
        rule.setPriority(formulaDataObject.getPriority());
        rule.setPercentage(formulaDataObject.getPercentage());
        rule.setPercentageL1(formulaDataObject.getPercentageL1());
        rule.setPercentageL2(formulaDataObject.getPercentageL2());
        rule.setPercentageL3(formulaDataObject.getPercentageL3());

        Rule.copy(createRule(formulaDataObject), rule);

        return rule;
    }

    private Rule createRule(FormulaDataObject dataObject) {
        String expression = dataObject.getRuleExpression();
        return parser.getRule(expression, dataObject);
    }

    public FormulaDataObject convertToFormulaDataObject(final DCMGenericRule rule) {
        if (rule == null) {
            return null;
        }

        final FormulaDataObject formulaDataObject = new FormulaDataObject();

        formulaDataObject.setId(rule.getId());
        formulaDataObject.setName(rule.getName());
        formulaDataObject.setDescription(rule.getDescription());
        formulaDataObject.setRuleExpression(convertRuleToExpression(rule));
        formulaDataObject.setPriority(rule.getPriority());
        formulaDataObject.setPercentage(rule.getPercentage());
        formulaDataObject.setPercentageL1(rule.getPercentageL1());
        formulaDataObject.setPercentageL2(rule.getPercentageL2());
        formulaDataObject.setPercentageL3(rule.getPercentageL3());

        convertRule(rule, formulaDataObject);

        return formulaDataObject;
    }

    private void convertRule(DCMGenericRule dcmRule, FormulaDataObject dataObject) {
        for (Condition condition : RuleUtil.toConditions(dcmRule)) {
            convertCondition(condition, dataObject);
        }
    }

    private void convertCondition(Condition condition, FormulaDataObject dataObject) {
        if (condition == null) {
            return;
        }
        String name = condition.getFreeArg().getName();
        if (FormulaRuleBuilder.PROP_ESTB_IP.equals(name)) {

            dataObject.setEstbIP((String) condition.getFixedArg().getValue());

        } else if (FormulaRuleBuilder.PROP_ESTB_MAC.equals(name)) {

            dataObject.setEstbMacAddress((String) condition.getFixedArg().getValue());

        } else if (FormulaRuleBuilder.PROP_ECM_MAC.equals(name)) {

            dataObject.setEcmMacAddress((String) condition.getFixedArg().getValue());

        } else if (FormulaRuleBuilder.PROP_ENV.equals(name)) {

            Collection collection = getConditionCollection(condition.getFixedArg().getValue());
            dataObject.setEnvList(new ArrayList<String>(collection));
            dataObject.setEnv(Utils.joinOr(collection));

        } else {
            setConditionValue(dataObject, condition, name);
        }
    }

    private void setConditionValue(FormulaDataObject dataObject, Condition condition, String name) {
        try {
            Set set = Sets.newHashSet(getConditionCollection(condition.getFixedArg().getValue()));
            Method setter = getSetter(dataObject, name);
            ReflectionUtils.invokeMethod(setter, dataObject, Utils.joinOr(set));
        } catch (NoSuchMethodException e) {
            log.error("Couldn't setup value into field: " + name, e.toString());
        }
    }

    private Collection getConditionCollection(Object fixedArgValue) {
        if (fixedArgValue instanceof Collection) {
            return (Collection) fixedArgValue;
        } else {
            return Lists.newArrayList(fixedArgValue);
        }
    }

    private String convertRuleToExpression(DCMGenericRule dcmRule) {
        String ruleExpression = "";
        if (!dcmRule.isCompound()) {
            ruleExpression = dcmRule.getCondition().getFreeArg().getName();
        } else {
            for (Rule rule : dcmRule.getCompoundParts()) {
                if (rule.getRelation() != null) {
                    ruleExpression += " " + rule.getRelation() + " ";
                }
                if (rule.isNegated()) {
                    ruleExpression += Token.OperationToken.NOT_OPERATOR + " ";
                }
                ruleExpression += rule.getCondition().getFreeArg().getName();
            }
        }
        return ruleExpression;
    }

    public static Method getSetter(Object obj, String field) throws NoSuchMethodException {
        for(Method method : obj.getClass().getDeclaredMethods()) {
            if(getMethodSimpleName(method).equals(SETTER_PREFIX + StringUtils.capitalize(field)))
                return method;
        }
        throw new NoSuchMethodException();
    }

    public static String getMethodSimpleName(Method method) {
        return method.getName().substring(method.getName().lastIndexOf(".") + 1);
    }

}
