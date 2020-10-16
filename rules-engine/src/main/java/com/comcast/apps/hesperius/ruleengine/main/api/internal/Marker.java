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
package com.comcast.apps.hesperius.ruleengine.main.api.internal;

/**
 * The idea is subclasses will be final and will implement {@link #validateType(Object)} according to it javadoc.
 * Thus subclasses can be considered as strong typed text constant.
 *
 * @see com.comcast.apps.hesperius.ruleengine.main.api.Operation
 * @see com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType
 */
public abstract class Marker {

    protected final String name;

    protected Marker(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        this.name = name;
    }

    /**
     * public class MyMarker extends Marker {
     *     protected boolean validateType(Object o) {
     *         return o instanceof MyMarker;
     *     }
     * }
     */
    protected abstract boolean validateType(Object o);

    @Override
    public final String toString() {
        return name;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!validateType(o)) {
            return false;
        }
        return name.equals(((Marker)o).name);
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }
}
