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
 *  Created: 12/18/15 1:00 PM
 */

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.dcm.core.Utils;
import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.logupload.Schedule;
import com.comcast.xconf.queries.QueryConstants;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(QueryConstants.UPDATES_DEVICE_SETTINGS)
public class DeviceSettingsQueriesController {

    private static final Logger log = LoggerFactory.getLogger(DeviceSettingsQueriesController.class);

    @Autowired
    public CachedSimpleDao<String, DeviceSettings> deviceSettingsDAO;

    @RequestMapping(method = RequestMethod.POST, value = "/{scheduleTimezone}")
    public ResponseEntity create(@RequestBody DeviceSettings deviceSettings,
                               @PathVariable String scheduleTimezone) {

        Schedule schedule = deviceSettings.getSchedule();
        if (StringUtils.isBlank(schedule.getStartDate()) || !Utils.isValidDate(schedule.getStartDate())) {
            return new ResponseEntity<>("Start day is invalid", HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isBlank(schedule.getEndDate()) || !Utils.isValidDate(schedule.getEndDate())) {
            return new ResponseEntity<>("End date is invalid", HttpStatus.BAD_REQUEST);
        }
        if (Utils.compareDates(schedule.getStartDate(), schedule.getEndDate()) >= 0) {
            return new ResponseEntity<>("Start date is greater/equal to End date", HttpStatus.BAD_REQUEST);
        }

        String nameErrorMessage = validateName(deviceSettings);
        if (StringUtils.isNotBlank(nameErrorMessage)) {
            return new ResponseEntity<>(nameErrorMessage, HttpStatus.BAD_REQUEST);
        }

        try {
            String id = deviceSettings.getId();
            if (id == null || id.isEmpty()) {
                id = UUID.randomUUID().toString();
                deviceSettings.setId(id);
                deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);
            } else {
                if ((scheduleTimezone!=null) && (!scheduleTimezone.equals("UTC"))) {
                    schedule.setStartDate(Utils.converterDateTimeToUTC(schedule.getStartDate(), scheduleTimezone));
                    schedule.setEndDate(Utils.converterDateTimeToUTC(schedule.getEndDate(), scheduleTimezone));
                }
                deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);
            }
        } catch (Exception e) {
            log.error(e.toString());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(deviceSettings, HttpStatus.CREATED);
    }

    private String validateName(final DeviceSettings deviceSettings) {
        final DeviceSettings ds = getDeviceSettingsByName(StringUtils.trim(deviceSettings.getName()));
        if (ds != null && !ds.getId().equals(deviceSettings.getId())) {
            return "Name is already used";
        }
        return null;
    }

    private DeviceSettings getDeviceSettingsByName(final String name) {
        for (final Optional<DeviceSettings> entity : deviceSettingsDAO.asLoadingCache().asMap().values()) {
            if (!entity.isPresent()) {
                continue;
            }
            final DeviceSettings deviceSettings = entity.get();
            if (deviceSettings.getName().equals(name)) {
                return deviceSettings;
            }
        }
        return null;
    }
}
