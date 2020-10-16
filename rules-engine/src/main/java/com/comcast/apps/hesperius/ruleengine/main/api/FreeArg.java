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

/**
 *
 */
public class FreeArg {

    protected FreeArgType type;
    protected String name;

    public FreeArg() {
    }

    public FreeArg(FreeArgType type, String name) {
        this.type = type;
        this.name = name;
    }

    public FreeArgType getType() {
        return type;
    }

    public void setType(FreeArgType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FreeArg)) return false;

        FreeArg freeArg = (FreeArg) o;

        if (name != null ? !name.equals(freeArg.name) : freeArg.name != null) return false;
        if (type != null ? !type.equals(freeArg.type) : freeArg.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
