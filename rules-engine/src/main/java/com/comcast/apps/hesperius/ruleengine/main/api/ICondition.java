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
 * Created: 5/29/14
 */
package com.comcast.apps.hesperius.ruleengine.main.api;

import com.comcast.apps.hesperius.ruleengine.main.api.internal.IReadonlyCondition;

/**
 * Condition without boolean relations.
 *
 * E.g. "stbIp is 127.0.0.1", where:
 * - "stbIp" is a free arg {@link #getFreeArg()} of type "ip address" {@link FreeArg#getType()} with name "stbIp" {@link FreeArg#getName()}
 * - 'is" obviously represents an operation {@link #getOperation()}
 * - "127.0.0.1" is a fixed arg {@link #getFixedArg()}
 */
public interface ICondition extends IReadonlyCondition {

    @Override
    FreeArg getFreeArg();

    void setFreeArg(FreeArg freeArg);

    @Override
    Operation getOperation();

    void setOperation(Operation operation);

    @Override
    FixedArg getFixedArg();

    void setFixedArg(FixedArg fixedArg);
}
