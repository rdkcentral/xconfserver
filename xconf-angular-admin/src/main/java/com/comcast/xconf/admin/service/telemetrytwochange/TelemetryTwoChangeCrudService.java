/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.admin.service.telemetrytwochange;

import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.change.TelemetryTwoChange;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelemetryTwoChangeCrudService<T extends IPersistable> extends AbstractTelemetryTwoChangeCrudService<TelemetryTwoChange<T>> {

    @Autowired
    private SimpleDao<String, TelemetryTwoChange<T>> changeDao;

    @Autowired
    private AuthService authService;

    public SimpleDao<String, TelemetryTwoChange<T>> getEntityDao() {
        return changeDao;
    }

    @Override
    protected void beforeSaving(TelemetryTwoChange change) {
        if (StringUtils.isBlank(change.getAuthor())) {
            change.setAuthor(authService.getUserNameOrUnknown());
        }
        super.beforeSaving(change);

        getValidator().validateAll(change, getAll());
    }

}