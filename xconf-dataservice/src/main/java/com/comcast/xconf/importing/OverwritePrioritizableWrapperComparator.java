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
 * limitations under the License.=
 *
 * Author: Stanislav Menshykov
 * Created: 19.01.16  12:17
 */
package com.comcast.xconf.importing;


import com.comcast.xconf.priority.Prioritizable;

import java.util.Comparator;

public class OverwritePrioritizableWrapperComparator<T extends Prioritizable> implements Comparator<OverwriteWrapper<T>> {
    @Override
    public int compare(OverwriteWrapper<T> left, OverwriteWrapper<T> right) {
        int leftPriority = (left != null && left.getEntity() != null && left.getEntity().getPriority() != null) ? left.getEntity().getPriority() : 0;
        int rightPriority = (right != null && right.getEntity() != null && right.getEntity().getPriority() != null) ? right.getEntity().getPriority() : 0;

        return leftPriority - rightPriority;
    }
}
