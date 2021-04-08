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
 *  Created: 12/21/15 3:15 PM
 */

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.dcm.ruleengine.LogFileService;
import com.comcast.xconf.logupload.LogFile;
import com.comcast.xconf.logupload.LogUploadSettings;
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class LogFileQueriesController {

    @Autowired
    private CachedSimpleDao<String, LogFile> logFileDAO;

    @Autowired
    private CachedSimpleDao<String, LogUploadSettings> logUploadSettingsDAO;

    @Autowired
    private LogFileService logFileService;

    private static final Logger log = LoggerFactory.getLogger(LogFileQueriesController.class);


    @RequestMapping(method = RequestMethod.POST, value = QueryConstants.UPDATES_LOG_FILE)
    public ResponseEntity create(@RequestBody LogFile logFile) {

        if (logFile == null) {
            return new ResponseEntity<>("Log file is empty", HttpStatus.BAD_REQUEST);
        }

        if (!isValidName(logFile)) {
            return new ResponseEntity<>("Name is already used", HttpStatus.BAD_REQUEST);
        }

        try {
            String id = logFile.getId();
            if (id == null || id.isEmpty()) {
                id = UUID.randomUUID().toString();
                logFile.setId(id);
                logFileDAO.setOne(logFile.getId(), logFile);
            } else {
                logFileDAO.setOne(logFile.getId(), logFile);
                updateLogUploadSettingsAndLogFileGroups(logFile);
            }

        } catch (Exception e) {
            log.error(e.toString(), e);
            return new ResponseEntity("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(logFile, HttpStatus.CREATED);
    }

    private boolean isValidName(final LogFile logFile) {
        if (StringUtils.isBlank(logFile.getName())) {
            return false;
        }
        final LogFile lf = getLogFileByName(StringUtils.trim(logFile.getName()));
        if (lf != null && !lf.getId().equals(logFile.getId())) {
            return false;
        }
        return true;
    }

    private void updateLogUploadSettingsAndLogFileGroups(LogFile logFile) {
        List<LogUploadSettings> listLogUploadSettings = logUploadSettingsDAO.getAll(Integer.MAX_VALUE/100);
        for (LogUploadSettings logUploadSettings : listLogUploadSettings) {
            List<LogFile> indexesLogFiles = logFileService.getAll(logUploadSettings.getId());
            Iterator<LogFile> logFileIterator = indexesLogFiles.iterator();
            while(logFileIterator.hasNext()) {
                LogFile item = logFileIterator.next();
                if (item.getId().equals(logFile.getId())){
                    logFileService.setOne(logUploadSettings.getId(),logFile);
                }
            }
        }
    }

    private LogFile getLogFileByName(final String name) {
        for (final Map.Entry<String, Optional<LogFile>> entry : logFileDAO.asLoadingCache().asMap().entrySet()) {
            if (!entry.getValue().isPresent()) {
                continue;
            }
            final LogFile logFile = entry.getValue().get();
            if (logFile.getName().equals(name)) {
                logFile.setId(entry.getKey());
                return logFile;
            }
        }
        return null;
    }
}
