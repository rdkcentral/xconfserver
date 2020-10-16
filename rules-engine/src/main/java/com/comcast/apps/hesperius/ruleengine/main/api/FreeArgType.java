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
package com.comcast.apps.hesperius.ruleengine.main.api;

import com.comcast.apps.hesperius.ruleengine.main.api.internal.Marker;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;


/**
 * @see com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType
 */
@JsonSerialize(using = ToStringSerializer.class)
public final class FreeArgType extends Marker {

    public static FreeArgType forName(String name) {
        return new FreeArgType(name);
    }

    private FreeArgType(String name) {
        super(name);
    }

    @Override
    protected boolean validateType(Object o) {
        return o instanceof FreeArgType;
    }
}
