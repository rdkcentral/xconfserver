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
 * <p/>
 * Author: Stanislav Menshykov
 * Created: 1/15/16  2:54 PM
 */
package com.comcast.xconf.priority;

import com.comcast.xconf.logupload.DCMGenericRule;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PriorityUtilsTest {

    @Test
    public void testAddNewItemAndReorganize1() throws Exception {
        List<DCMGenericRule> currentItemsList = createItemsList("formula1", 1, "formula3", 3, "formula2", 2);

        DCMGenericRule newFormula = createFormula("newFormula", 4);
        List<DCMGenericRule> actualResult = PriorityUtils.addNewItemAndReorganize(newFormula, currentItemsList);

        List<DCMGenericRule> expectedResult = Lists.newArrayList(newFormula);
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void testAddNewItemAndReorganizeWithDuplicatePriorities() throws Exception {
        String formulaFrom1To2Name = "formulaFrom1To2";
        String formulaFrom2To3Name = "formulaFrom2To3";
        String formula2Duplicate = "formula2Duplicate";
        List<DCMGenericRule> currentItemsList = createItemsList(formulaFrom1To2Name, 1, formulaFrom2To3Name, 2, formula2Duplicate, 2);

        DCMGenericRule newFormula = createFormula("newFormula", 1);
        List<DCMGenericRule> actualResult = PriorityUtils.addNewItemAndReorganize(newFormula, currentItemsList);

        List<DCMGenericRule> expectedResult = Lists.newArrayList(newFormula);
        expectedResult.addAll(createItemsList(formulaFrom1To2Name, 2, formulaFrom2To3Name, 3, formula2Duplicate, 4));
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void testAddNewItemAndReorganize2() throws Exception {
        String formulaFrom1To2Name = "formulaFrom1To2";
        String formulaFrom2To3Name = "formulaFrom2To3";
        String formulaFrom3To4Name = "formulaFrom3To4";
        List<DCMGenericRule> currentItemsList = createItemsList(formulaFrom1To2Name, 1, formulaFrom3To4Name, 3, formulaFrom2To3Name, 2);

        DCMGenericRule newFormula = createFormula("newFormula", 1);
        List<DCMGenericRule> actualResult = PriorityUtils.addNewItemAndReorganize(newFormula, currentItemsList);

        List<DCMGenericRule> expectedResult = Lists.newArrayList(newFormula);
        expectedResult.addAll(createItemsList(formulaFrom1To2Name, 2, formulaFrom2To3Name, 3, formulaFrom3To4Name, 4));
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void testAddNewItemAndReorganize3() throws Exception {
        String newFormulaName = "newFormula";
        String formula1Name = "formula1";
        String formulaFrom2To3Name = "formulaFrom2To3";
        String formulaFrom3To4Name = "formulaFrom3To4";
        List<DCMGenericRule> currentItemsList = createItemsList(formula1Name, 1, formulaFrom2To3Name, 2, formulaFrom3To4Name, 3);

        DCMGenericRule newFormula = createFormula(newFormulaName, 2);
        List<DCMGenericRule> actualResult = PriorityUtils.addNewItemAndReorganize(newFormula, currentItemsList);

        List<DCMGenericRule> expectedResult = createItemsList(newFormulaName, 2, formulaFrom2To3Name, 3, formulaFrom3To4Name, 4);
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void addingNewItemWithInvalidPrioritySetsLowestPriority() throws Exception {
        String newFormulaName = "newFormula";
        List<DCMGenericRule> currentItemsList = createItemsList("formula2", 2, "formula1", 1, "formula3", 3);
        Integer invalidPriority = 0;

        DCMGenericRule newFormulaToSave = createFormula(newFormulaName, invalidPriority);
        List<DCMGenericRule> actualResult = PriorityUtils.addNewItemAndReorganize(newFormulaToSave, currentItemsList);

        DCMGenericRule newFormulaToExpect = createFormula(newFormulaName, 4);
        List<DCMGenericRule> expectedResult = Lists.newArrayList(newFormulaToExpect);
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void testUpdateItemByPriorityAndReorganize() throws Exception {
        String formulaFrom1To2Name = "formulaFrom1To2";
        String formulaFrom2To1Name = "formulaFrom2To1";
        String formula3Name = "formula3";
        String updatedFormulaName = "updatedFormula";
        Integer priorityOfFormulaToUpdate = 2;
        List<DCMGenericRule> currentItemsList = createItemsList(formulaFrom1To2Name, 1, formula3Name, 3, formulaFrom2To1Name, 2);

        DCMGenericRule formulaForUpdate = createFormula(updatedFormulaName, 1);
        List<DCMGenericRule> actualResult = PriorityUtils.updateItemByPriorityAndReorganize(formulaForUpdate, currentItemsList, priorityOfFormulaToUpdate);

        List<DCMGenericRule> expectedResult = Lists.newArrayList(createFormula(updatedFormulaName, 1), createFormula(formulaFrom1To2Name, 2));
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void updatingItemByPriorityWithInvalidPrioritySetsLowestPriority() throws Exception {
        String formulaFrom1To3Name = "formulaFrom1To3";
        String formulaFrom2To1Name = "formulaFrom2To1";
        String formulaFrom3To2Name = "formulaFrom3To2";
        String updatedFormulaName = "updatedFormula";
        Integer priorityOfFormulaToUpdate = 1;
        Integer invalidPriority = 42;
        List<DCMGenericRule> currentItemsList = createItemsList(formulaFrom2To1Name, 2, formulaFrom1To3Name, 1, formulaFrom3To2Name, 3);

        DCMGenericRule formulaForUpdate = createFormula(updatedFormulaName, invalidPriority);
        List<DCMGenericRule> actualResult = PriorityUtils.updateItemByPriorityAndReorganize(formulaForUpdate, currentItemsList, priorityOfFormulaToUpdate);

        List<DCMGenericRule> expectedResult = createItemsList(formulaFrom2To1Name, 1, formulaFrom3To2Name, 2, updatedFormulaName, 3);
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void testUpdatePriorities() throws Exception {
        String formula1Name = "formula1";
        String formulaFrom2To3Name = "formulaFrom2To3";
        String formulaFrom3To2Name = "formulaFrom3To2";
        Integer oldPriority = 2;
        Integer newPriority = 3;
        List<DCMGenericRule> itemsList = createItemsList(formula1Name, 1, formulaFrom2To3Name, 2, formulaFrom3To2Name, 3);

        List<DCMGenericRule> actualResult = PriorityUtils.updatePriorities(itemsList, oldPriority, newPriority);

        List<DCMGenericRule> expectedResult = Lists.newArrayList(createFormula(formulaFrom3To2Name, 2), createFormula(formulaFrom2To3Name, 3));
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void testPackPriorities() throws Exception {
        String formulaFrom2to1Name = "formulaFrom2To1";
        String formulaFrom4To2Name = "formulaFrom4To2";
        String formulaFrom5To3Name = "formulaFrom5To3";
        List<DCMGenericRule> itemsList = createItemsList(formulaFrom4To2Name, 4, formulaFrom5To3Name, 5, formulaFrom2to1Name, 2);

        List<DCMGenericRule> actualResult = PriorityUtils.packPriorities(itemsList);

        List<DCMGenericRule> expectedResult = createItemsList(formulaFrom2to1Name, 1, formulaFrom4To2Name, 2, formulaFrom5To3Name, 3);
        assertThat(expectedResult, is(actualResult));
    }

    @Test
    public void verifyReturningOnlyChangedPrioritiesAfterPackOperation() {
        List<DCMGenericRule> formulas = createFormulas(10);
        List<DCMGenericRule> changedFormulas = PriorityUtils.packPriorities(formulas);
        assertEquals(0, changedFormulas.size());

        formulas.remove(3);
        List<DCMGenericRule> changedFormulasAfterDeleteOne = PriorityUtils.packPriorities(formulas);
        assertEquals(6, changedFormulasAfterDeleteOne.size());
    }

    List<DCMGenericRule> createFormulas(Integer numberOfFormulas) {
        List<DCMGenericRule> formulas = new ArrayList<>();
        for (int i = 0; i < numberOfFormulas; i++) {
            DCMGenericRule formula = new DCMGenericRule();
            formula.setPriority(i + 1);
            formulas.add(formula);
        }
        return formulas;
    }


    private DCMGenericRule createFormula(String name, Integer priority) {
        DCMGenericRule formula = new DCMGenericRule();
        formula.setName(name);
        formula.setPriority(priority);

        return formula;
    }

    private List<DCMGenericRule> createItemsList(String name1, Integer priority1,
                                                 String name2, Integer priority2,
                                                 String name3, Integer priority3) {
        DCMGenericRule item1 = createFormula(name1, priority1);
        DCMGenericRule item2 = createFormula(name2, priority2);
        DCMGenericRule item3 = createFormula(name3, priority3);

        return Lists.newArrayList(item1, item2, item3);
    }
}
