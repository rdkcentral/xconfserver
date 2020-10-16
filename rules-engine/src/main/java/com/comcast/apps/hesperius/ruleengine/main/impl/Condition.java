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
package com.comcast.apps.hesperius.ruleengine.main.impl;

import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.ICondition;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;

/**
 * @see ICondition
 */
public class Condition implements ICondition {
    protected FreeArg freeArg;
    protected Operation operation;

    protected FixedArg fixedArg;

    public Condition() {
    }

    public Condition(FreeArg freeArg, Operation operation, FixedArg fixedArg) {
        this.freeArg = freeArg;
        this.operation = operation;
        this.fixedArg = fixedArg;
    }

    @Override
    public FreeArg getFreeArg() {
        return freeArg;
    }

    @Override
    public void setFreeArg(FreeArg freeArg) {
        this.freeArg = freeArg;
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Override
    public FixedArg getFixedArg() {
        return fixedArg;
    }

    @Override
    public void setFixedArg(FixedArg fixedArg) {
        this.fixedArg = fixedArg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Condition)) return false;

        Condition condition = (Condition) o;

        if (fixedArg != null ? !fixedArg.equals(condition.fixedArg) : condition.fixedArg != null) return false;
        if (freeArg != null ? !freeArg.equals(condition.freeArg) : condition.freeArg != null) return false;
        if (operation != null ? !operation.equals(condition.operation) : condition.operation != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return operation != null ? operation.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder();

        res.append(getFreeArg().getName())
                .append(" ")
                .append(getOperation())
                .append(" ")
                .append(getFixedArg().getValue());

        return res.toString();
    }
}