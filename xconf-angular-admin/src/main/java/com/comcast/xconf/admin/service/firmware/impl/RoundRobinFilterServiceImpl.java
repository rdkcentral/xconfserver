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
 *  Created: 2:32 PM
 */
package com.comcast.xconf.admin.service.firmware.impl;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.admin.service.firmware.RoundRobinFilterService;
import com.comcast.xconf.admin.validator.firmware.RoundRobinFilterValidator;
import com.comcast.xconf.estbfirmware.DownloadLocationRoundRobinFilterValue;
import com.comcast.xconf.estbfirmware.LocationFilterService;
import com.comcast.xconf.estbfirmware.SingletonFilterValue;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoundRobinFilterServiceImpl implements RoundRobinFilterService {

    @Autowired
    private CachedSimpleDao<String, SingletonFilterValue> singletonFilterValueDAO;

    @Autowired
    private LocationFilterService locationFilterService;

    @Autowired
    private RoundRobinFilterValidator roundRobinFilterValidator;

    @Autowired
    private FirmwarePermissionService permissionService;

    @Override
    public SingletonFilterValue getOne(String applicationType) {
        if (StringUtils.isBlank(applicationType)) {
            applicationType = permissionService.getReadApplication();
        }
        String filterId = getFilterIdByApplication(applicationType);
        SingletonFilterValue roundRobinFilter = singletonFilterValueDAO.getOne(filterId);
        if (roundRobinFilter == null) {
            roundRobinFilter = new DownloadLocationRoundRobinFilterValue(ApplicationType.get(applicationType));
        }
        return roundRobinFilter;
    }

    @Override
    public SingletonFilterValue save(SingletonFilterValue singletonFilterValue) {
        roundRobinFilterValidator.validate(singletonFilterValue);
        DownloadLocationRoundRobinFilterValue roundRobinFilter = (DownloadLocationRoundRobinFilterValue) singletonFilterValue;
        String applicationType = permissionService.getWriteApplication();
        String filterId = getFilterIdByApplication(applicationType);
        if (StringUtils.isBlank(roundRobinFilter.getApplicationType())) {
            roundRobinFilter.setApplicationType(applicationType);
        }

        if(!StringUtils.equals(filterId, roundRobinFilter.getId())){
            roundRobinFilter.setId(filterId);
        }
        singletonFilterValueDAO.setOne(filterId, roundRobinFilter);
        return roundRobinFilter;
    }

    @Override
    public List<DownloadLocationRoundRobinFilterValue> getAllRoundRobinFilters() {
        List<SingletonFilterValue> allFilters = singletonFilterValueDAO.getAll();
        List<DownloadLocationRoundRobinFilterValue> roundRobinFilters = new ArrayList<>();
        for (SingletonFilterValue filter : allFilters) {
            if (filter instanceof DownloadLocationRoundRobinFilterValue) {
                roundRobinFilters.add((DownloadLocationRoundRobinFilterValue) filter);
            }
        }
        return roundRobinFilters;
    }

    private String getFilterIdByApplication(String applicationType) {
        if (ApplicationType.equals(ApplicationType.STB, applicationType)) {
            return DownloadLocationRoundRobinFilterValue.SINGLETON_ID;
        }

        return applicationType.toUpperCase() + "_" + DownloadLocationRoundRobinFilterValue.SINGLETON_ID;

    }
}
