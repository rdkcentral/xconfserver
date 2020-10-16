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

package com.comcast.xconf.admin.service.change;

import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.change.ApprovedChange;
import com.comcast.xconf.change.Change;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApprovedChangeCrudService<T extends IPersistable> extends AbstractChangeCrudService<ApprovedChange<T>> {

    @Autowired
    private SimpleDao<String, ApprovedChange<T>> approvedDao;

    @Autowired
    private AuthService authService;

    @Override
    public SimpleDao<String, ApprovedChange<T>> getEntityDao() {
        return approvedDao;
    }

    public ApprovedChange<T> saveToApproved(Change<T> change) {
        return super.create(new ApprovedChange<>(change));
    }

    @Override
    protected void beforeSaving(ApprovedChange<T> approvedChange) {
        if (approvedChange != null && StringUtils.isBlank(approvedChange.getApprovedUser())) {
            approvedChange.setApprovedUser(authService.getUserNameOrUnknown());
        }
        super.beforeSaving(approvedChange);
    }

}