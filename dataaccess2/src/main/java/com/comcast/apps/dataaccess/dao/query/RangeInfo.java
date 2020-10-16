/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */
package com.comcast.apps.dataaccess.dao.query;


public class RangeInfo<N> {
    private N startValue;
    private N endValue;

    public RangeInfo() {}

    public RangeInfo(N startValue, N endValue) {
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public N getStartValue() {
        return startValue;
    }

    public void setStartValue(N startValue) {
        this.startValue = startValue;
    }

    public N getEndValue() {
        return endValue;
    }

    public void setEndValue(N endValue) {
        this.endValue = endValue;
    }

}
