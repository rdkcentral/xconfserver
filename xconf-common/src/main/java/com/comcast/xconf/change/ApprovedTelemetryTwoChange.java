/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
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
 * Author: Jeyabala Murugan Pethuraj
 * Created: 25.01.2021
 */

package com.comcast.xconf.change;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.IPersistable;

@CF(cfName = "XconfApprovedTelemetryTwoChange", ttl = 432000)
public class ApprovedTelemetryTwoChange<T extends IPersistable> extends TelemetryTwoChange<T> {

    public ApprovedTelemetryTwoChange() {}

    public ApprovedTelemetryTwoChange(TelemetryTwoChange<T> change) {
        this.setId(change.getId());
        this.setEntityId(change.getEntityId());
        this.setEntityType(change.getEntityType());
        this.setApplicationType(change.getApplicationType());
        this.setAuthor(change.getAuthor());
        this.setApprovedUser(change.getApprovedUser());
        this.setOperation(change.getOperation());
        this.setOldEntity(change.getOldEntity());
        this.setNewEntity(change.getNewEntity());
    }
}