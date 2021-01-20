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

import com.comcast.xconf.rfc.FeatureRule;
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
    public void testSortFeatureRulesByPriorityDesc() {

        List<FeatureRule> formulas = createFeatureRules();
        List<FeatureRule> sorted = SortingManager.sortRulesByPriorityAsc(formulas);

        List<Integer> integers = new ArrayList<>();
        for (FeatureRule formula : sorted) {
            integers.add(formula.getPriority());
        }

        for (int i = 0; i < integers.size() - 1; i++) {
            Assert.assertTrue(integers.get(i) < integers.get(i+1));
        }
    }

    private List<FeatureRule> createFeatureRules() {
        ArrayList<FeatureRule> list = new ArrayList<>();
        list.add(createFormula(3));
        list.add(createFormula(1));
        list.add(createFormula(5));
        list.add(createFormula(8));
        list.add(createFormula(12));
        list.add(createFormula(2));
        return list;
    }

    private FeatureRule createFormula(int num) {
        FeatureRule formula = new FeatureRule();
        formula.setName("FeatureRule" + num);
        formula.setPriority(num);
        return formula;
    }
}
