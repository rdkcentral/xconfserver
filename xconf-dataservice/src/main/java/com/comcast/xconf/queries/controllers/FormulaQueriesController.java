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
 *  Created: 12/18/15 12:58 PM
 */

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.dcm.converter.DcmRuleConverter;
import com.comcast.xconf.dcm.core.Utils;
import com.comcast.xconf.dcm.manager.web.FormulaDataObject;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.annotation.Compare;
import com.comcast.xconf.queries.QueryConstants;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Controller
@RequestMapping(QueryConstants.UPADATES_FORMULA)
public class FormulaQueriesController {

    @Autowired
    private CachedSimpleDao<String, DCMGenericRule> dcmRuleDAO;

    @Autowired
    private DcmRuleConverter dcmRuleConverter;

    private static final Logger log = LoggerFactory.getLogger(FormulaQueriesController.class);


    @RequestMapping(method = RequestMethod.POST, value = "/{newPriority}")
    public ResponseEntity create(@RequestBody FormulaDataObject formula,
                                 @PathVariable Integer newPriority) {
        makeModelUpperCase(formula);
        String percentageErrorMessage = validatePercentage(formula);
        if (StringUtils.isNotBlank(percentageErrorMessage)) {
            return new ResponseEntity<>(percentageErrorMessage, HttpStatus.BAD_REQUEST);
        }

        String nameErrorMessage = validateName(formula);
        if (StringUtils.isNotBlank(nameErrorMessage)) {
            return new ResponseEntity<>(nameErrorMessage, HttpStatus.BAD_REQUEST);
        }

        envListToEnvString(formula);

        String ruleExpressionErrorMessage = validateRuleExpression(formula);
        if (StringUtils.isNotBlank(ruleExpressionErrorMessage)) {
            return new ResponseEntity(ruleExpressionErrorMessage, HttpStatus.BAD_REQUEST);
        }

        boolean isNewFormula = false;

        if (formula.getId() == null || formula.getId().isEmpty()) {
            formula.setId(UUID.randomUUID().toString());
            isNewFormula = true;
        }

        try {
            List<FormulaDataObject> formulaList = convertToFormulaDataObjects(dcmRuleDAO.getAll(Integer.MAX_VALUE / 100));
            Integer oldPriority = formula.getPriority();
            if (oldPriority == null) {
                oldPriority = formulaList.size()+1;
                formula.setPriority(oldPriority);
            }

            envListToEnvString(formula);

            if (formula.getPriority().equals(newPriority)) {
                final DCMGenericRule newFormatFormula = convertToFormula(formula);
                dcmRuleDAO.setOne(newFormatFormula.getId(), newFormatFormula);
            }
            else {
                Collections.sort(formulaList);
                if (isNewFormula) {
                    formulaList.add(formulaList.size(), formula);
                }
                if (formulaList.get(oldPriority-1).getId().equals(formula.getId())) {
                    reorganizePriorities(formulaList, oldPriority, newPriority);
                } else {
                    return new ResponseEntity<>("Can't to save the formula! Someone other user changed priority for this formula, try to do it again!", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(formula, HttpStatus.CREATED);
    }

    private void makeModelUpperCase(FormulaDataObject formula) {
        String model = formula.getModel();
        if (StringUtils.isNotBlank(model)) {
            formula.setModel(model.toUpperCase());
        }
    }

    private String validatePercentage(FormulaDataObject formula) {
        int p1 = formula.getPercentageL1() != null ? formula.getPercentageL1() : 0;
        int p2 = formula.getPercentageL2() != null ? formula.getPercentageL2() : 0;
        int p3 = formula.getPercentageL3() != null ? formula.getPercentageL3() : 0;
        int sum = p1 + p2 + p3;
        if (sum > 100 || sum < 0) {
            return "Total sum L1+L2+L3 should NOT be less than 0 or greater than 100";
        }
        return null;
    }

    private String validateName(final FormulaDataObject formulaDataObject) {
        final FormulaDataObject fdo = convertToFormulaDataObject(getFormulaByName(StringUtils.trim(formulaDataObject.getName())));
        if (fdo != null && !fdo.getId().equals(formulaDataObject.getId())) {
            return "Name is already used";
        }
        return null;
    }

    private DCMGenericRule getFormulaByName(final String name) {
        for (final Optional<DCMGenericRule> entity : dcmRuleDAO.asLoadingCache().asMap().values()) {
            if (!entity.isPresent()) {
                continue;
            }
            final DCMGenericRule formula = entity.get();
            if (formula.getName().equals(name)) {
                return formula;
            }
        }
        return null;
    }

    private FormulaDataObject envStringToEnvList(FormulaDataObject formula) {
        if (formula.getEnv() != null) {
            String [] env = formula.getEnv().split(" OR ");
            ArrayList<String> envList = new ArrayList<String>(Arrays.asList(env));
            formula.setEnvList(envList);
        }
        return formula;
    }

    private FormulaDataObject envListToEnvString(FormulaDataObject formula) {
        List<String> envList = formula.getEnvList();
        String env = new String();
        if (envList != null && !envList.isEmpty()) {
            for (String item :envList) {
                env += item + " OR ";
            }
            env = env.substring(0, env.length()-4);
        }
        formula.setEnv(env);
        return formula;
    }

    public String validateRuleExpression(FormulaDataObject formula) {

        ExpressionParser parser = new SpelExpressionParser();
        String ruleExpression = formula.getRuleExpression();

        if (ruleExpression == null || ruleExpression.isEmpty()) {
            return " Rule expression should not be empty!";
        }
        ruleExpression = " "+ruleExpression+" ";
        envListToEnvString(formula);

        StringBuilder body = new StringBuilder();

        Field[] fields = formula.getClass().getDeclaredFields();
        List<String> removeFields = new ArrayList<String>();
        List<String> includeFields = new ArrayList<String>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Compare.class)) {
                Method getterFormula = Utils.getGetter(FormulaDataObject.class, field.getName());
                String returnedValue = (String) Utils.invokeGetter(formula, getterFormula);

                if (ruleExpression.contains(field.getName()) && (returnedValue == null || returnedValue.isEmpty())) {
                    removeFields.add(field.getName());
                } else if (!ruleExpression.contains(field.getName()) && (returnedValue != null &&!returnedValue.isEmpty())) {
                    includeFields.add(field.getName());
                }
                ruleExpression = ruleExpression.replaceAll(" "+field.getName()+" ", " true ");
            }
        }
        if (!removeFields.isEmpty()) {
            if (removeFields.size() == 1) {
                body.append("This field is empty, so please remove it from rule expression: ");
            } else {
                body.append("These fields are empty, so please remove them from rule expression: ");
            }
            body.append(removeFields).append(".");
        }
        if (!includeFields.isEmpty()) {
            if (includeFields.size() == 1) {
                body.append(" This field is not empty, so please include it in rule expression: ");
            } else {
                body.append(" These fields are not empty, so please include them in rule expression: ");
            }
            body.append(includeFields).append(".");
        }
        ruleExpression = ruleExpression.replaceAll(" NOT|not "," ! ");
        ruleExpression = ruleExpression.replaceAll(" OR|or "," AND ");
        try {
            if (body.length() == 0) {
                @SuppressWarnings("unused")
                Boolean result = parser.parseExpression(ruleExpression).getValue(Boolean.class);
            } else {
                return body.toString();
            }
        } catch (Exception  e) {
            // something wrong with rule expression
            if (body.length() == 0) {
                body.append("Something wrong with rule expression!");
            }
            return body.toString();
        }
        return null;
    }

    public String packPriorities() {
        /**
         * if we're had removed one DCMGenericRule, we should update priorities.
         * For example:
         * Here is priorities after deleting DCMGenericRule with ID=4:
         * 1  2  3  5  6
         * Here is priorities after we made packPriorities(); :
         * 1  2  3  4  5
         */
        List<DCMGenericRule> formulaList = dcmRuleDAO.getAll(Integer.MAX_VALUE / 100);

        Collections.sort(formulaList);
        int priority = 1;
        for (DCMGenericRule formula : formulaList) {
            formula.setPriority(priority);
            priority++;
        }

        for (final DCMGenericRule f : formulaList) {
            dcmRuleDAO.setOne(f.getId(), f);
        }

        return  null;
    }

    public List<FormulaDataObject> getAllFormulasFromDB() {
        return convertToFormulaDataObjects(dcmRuleDAO.getAll());
    }

    private DCMGenericRule convertToFormula(final FormulaDataObject formulaDataObject) {
        return dcmRuleConverter.convertToRule(formulaDataObject);
    }

    // for jsp
    private FormulaDataObject convertToFormulaDataObject(final DCMGenericRule formula) {
        if (formula == null) {
            return null;
        }
        return dcmRuleConverter.convertToFormulaDataObject(formula);
    }

    private List<FormulaDataObject> convertToFormulaDataObjects(final List<DCMGenericRule> formulas) {
        final List<FormulaDataObject> result = new ArrayList<FormulaDataObject>();
        for (final DCMGenericRule formula : formulas) {
            result.add(convertToFormulaDataObject(formula));
        }
        return result;
    }

    public List<FormulaDataObject> reorganizePriorities(
            List<FormulaDataObject> formulaList, Integer oldPriority, Integer newPriority) {
        /**
         * When we want to change priority for one DCMGenericRule
         * we should to displace other Formulas.
         * If new priority is higher than old priority,
         * we are making displace down for all Formulas
         * which have lower priority than the new priority.
         * If new priority is lower than old priority,
         * we are making displace up for all Formulas
         * which have higher priority than the new priority.
         */

        FormulaDataObject formula = formulaList.get(oldPriority-1);
        formula.setPriority(newPriority);

        if (oldPriority < newPriority) {
            for (int i = oldPriority; i<=newPriority-1; i++ ) {
                FormulaDataObject buf = formulaList.get(i);
                buf.setPriority(i);
                formulaList.set(i-1, buf);
            }
        }

        if (oldPriority > newPriority) {
            for (int i = oldPriority-2; i>=newPriority-1; i-- ) {
                FormulaDataObject buf = formulaList.get(i);
                buf.setPriority(i+2);
                formulaList.set(i + 1, buf);
            }
        }

        formulaList.set(newPriority-1, formula);

        for (final FormulaDataObject f : formulaList) {
            final DCMGenericRule newFormatFormula = convertToFormula(f);
            dcmRuleDAO.setOne(newFormatFormula.getId(), newFormatFormula);
        }

        return formulaList;
    }

}
