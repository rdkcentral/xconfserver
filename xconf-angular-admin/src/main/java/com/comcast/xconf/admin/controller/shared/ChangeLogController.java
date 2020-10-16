/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.admin.controller.shared;

import com.comcast.xconf.admin.service.shared.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ChangeLogController.URL_MAPPING)
public class ChangeLogController {

    public static final String URL_MAPPING = "api/changelog";

    private ChangeLogService changeLogService;

    @Autowired
    public ChangeLogController(ChangeLogService changeLogService) {
        this.changeLogService = changeLogService;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Long, List<Change>> getChangeLog() {
        return changeLogService.getChangeLog();
    }

    public static class Change {
        String changedKey;
        String operationType;
        String cfName;
        private String userName;

        public Change() {}

        public Change(String changedKey, String operationType, String cfName, String userName) {
            this.changedKey = changedKey;
            this.operationType = operationType;
            this.cfName = cfName;
            this.userName = userName;
        }

        public String getChangedKey() {
            return changedKey;
        }

        public void setChangedKey(String changedKey) {
            this.changedKey = changedKey;
        }

        public String getOperationType() {
            return operationType;
        }

        public void setOperationType(String operationType) {
            this.operationType = operationType;
        }

        public String getCfName() {
            return cfName;
        }

        public void setCfName(String cfName) {
            this.cfName = cfName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
}