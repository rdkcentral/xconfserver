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
 *  Author: mdolina
 *  Created: 4:29 PM
 */
package com.comcast.xconf.logupload;

public enum TimeZone {
    HAWAII_ALEUTIAN(5),
    HAST(5),
    ALASKA(4),
    AKST(4),
    PACIFIC(3),
    PST(3),
    MOUNTAIN(2),
    MST(2),
    CENTRAL(1),
    CST(1),
    EASTERN(0),
    EST(0);



    private Integer timeShift;

    private TimeZone(Integer timeShift) {
        this.timeShift = timeShift;
    }

    public Integer getTimeShift() {
        return timeShift;
    }
}



