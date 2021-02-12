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
 * Created: 7/30/14
 */
package com.comcast.xconf;

import com.comcast.xconf.priority.Prioritizable;
import com.google.common.collect.Ordering;

import java.util.Comparator;
import java.util.List;

public class SortingManager {

    public static <T extends Prioritizable>List<T> sortRulesByPriorityAsc(Iterable<T> formulas) {
        Comparator<T> byPriorityDesc = (left, right) -> {
            int leftPriority = (left != null && left.getPriority() != null) ? left.getPriority() : 0;
            int rightPriority = (right != null && right.getPriority() != null) ? right.getPriority() : 0;
            return leftPriority - rightPriority;
        };

        return Ordering.from(byPriorityDesc).sortedCopy(formulas);
    }
}
