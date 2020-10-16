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
 *  Created: 12/18/15 12:59 PM
 */

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.dcm.core.Utils;
import com.comcast.xconf.logupload.VodSettings;
import com.comcast.xconf.queries.QueryConstants;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(QueryConstants.UPDATES_VOD_SETTINGS)
public class VodSettingsQueriesController {

    @Autowired
    private CachedSimpleDao<String, VodSettings> vodSettingsDAO;

    private static final Logger log = LoggerFactory.getLogger(VodSettingsQueriesController.class);
    private static final String IP_ADDRESS =    "^(([01]?[1-9][0-9]?|2[0-4][0-9]|25[0-5])\\.){3}([01]?[0-9][0-9]?|2[0-4][0-9]|25[0-5])$";
    private static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody VodSettings vodSettings) {

        if (vodSettings == null) {
            return new ResponseEntity("VodSettings is not present", HttpStatus.BAD_REQUEST);
        }

        if (!Utils.isValidUrl(vodSettings.getLocationsURL())) {
            return new ResponseEntity<>("LocationURL is not valid", HttpStatus.BAD_REQUEST);
        }

        String nameErrorMessage = validateName(vodSettings);
        if (StringUtils.isNotBlank(nameErrorMessage)) {
            return new ResponseEntity(nameErrorMessage, HttpStatus.BAD_REQUEST);
        }


        if (!validateIPList(vodSettings)) {
            return new ResponseEntity<>("IP list is not valid", HttpStatus.BAD_REQUEST);
        }


        Map<String, String> mapIPs = Utils.combineListsIntoMap(vodSettings.getIpNames(), vodSettings.getIpList());
        vodSettings.setSrmIPList(mapIPs);

        if (mapIPs.isEmpty() && vodSettings.getLocationsURL().isEmpty()) {
            return new ResponseEntity("You should add also LocationsURL or SRMIPList", HttpStatus.BAD_REQUEST);
        }

        try {
            String id = vodSettings.getId();
            if (id == null || id.isEmpty()) {
                id = UUID.randomUUID().toString();
                vodSettings.setId(id);
            }
            vodSettingsDAO.setOne(vodSettings.getId(), vodSettings);
        } catch (Exception e) {
            log.error(e.toString(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(vodSettings, HttpStatus.CREATED);
    }

    private String validateName(final VodSettings vodSettings) {
        if (StringUtils.isBlank(vodSettings.getName())) {
            return "Name is empty";
        }
        final VodSettings vs = getVodSettingsByName(StringUtils.trim(vodSettings.getName()));
        if (vs != null && !vs.getId().equals(vodSettings.getId())) {
            return "Name is already used";
        }
        return null;
    }

    private VodSettings getVodSettingsByName(final String name) {
        for (final Optional<VodSettings> entity : vodSettingsDAO.asLoadingCache().asMap().values()) {
            if (!entity.isPresent()) {
                continue;
            }
            final VodSettings vodSettings = entity.get();
            if (vodSettings.getName().equals(name)) {
                return vodSettings;
            }
        }
        return null;
    }

    private boolean validateIPList(VodSettings vodSettings) {
        if ( vodSettings == null || (vodSettings.getIpNames() == null ^ vodSettings.getIpList() == null) ) {return false;}
        if (vodSettings.getIpNames() == null && vodSettings.getIpList() == null) return true;
        if (vodSettings.getIpNames().size() != vodSettings.getIpList().size() ) return false;
        for (String itemIP : vodSettings.getIpList()) {
            if (!validIP(itemIP)) {
                return false;
            }
        }
        return true;
    }

    private boolean validIP(String address) {
        if (address == null)
            return false;
        Matcher matcher = addressPattern.matcher(address);
        return matcher.matches();
    }
}
