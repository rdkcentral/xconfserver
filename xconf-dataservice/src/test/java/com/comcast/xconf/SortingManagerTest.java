/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf;

import com.comcast.xconf.logupload.Formula;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ikostrov
 * Date: 30.07.14
 * Time: 14:41
 */
public class SortingManagerTest {

    @Test
    public void testSortFormulasByPriorityDesc() throws Exception {

        List<Formula> formulas = createFormulas();
        List<Formula> sorted = SortingManager.sortFormulasByPriorityAsc(formulas);

        List<Integer> integers = new ArrayList<Integer>();
        for (Formula formula : sorted) {
            integers.add(formula.getPriority());
        }

        System.out.println("SOrted list: " + integers);

        for (int i = 0; i < integers.size() - 1; i++) {
            Assert.assertTrue(integers.get(i) < integers.get(i+1));
        }
    }

    private List<Formula> createFormulas() {
        ArrayList<Formula> list = new ArrayList<Formula>();
        list.add(createFormula(3));
        list.add(createFormula(1));
        list.add(createFormula(5));
        list.add(createFormula(8));
        list.add(createFormula(12));
        list.add(createFormula(2));
        return list;
    }

    private Formula createFormula(int num) {
        Formula formula = new Formula();
        formula.setName("Formula" + num);
        formula.setPriority(num);
        return formula;
    }
}
