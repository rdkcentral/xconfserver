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
 * Author: slavrenyuk
 * Created: 6/5/14
 */
package com.comcast.apps.hesperius.ruleengine.domain.standard;

import com.comcast.apps.hesperius.ruleengine.main.api.Operation;

public interface StandardOperation {

    Operation IS   = Operation.forName("IS");
    Operation GT   = Operation.forName("GT");
    Operation GTE  = Operation.forName("GTE");
    Operation LT   = Operation.forName("LT");
    Operation LTE  = Operation.forName("LTE");
    Operation LIKE = Operation.forName("LIKE");
    Operation IN   = Operation.forName("IN");
    Operation ANY_MATCHED = Operation.forName("ANY_MATCHED");
    Operation PERCENT = Operation.forName("PERCENT");
    Operation EXISTS  = Operation.forName("EXISTS");  // if freeArg exists in context. is suitable for each FreeArgType except StandardFreeArgType.VOID
}
