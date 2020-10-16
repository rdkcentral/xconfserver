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
 * Author: Stanislav Menshykov
 * Created: 24.11.15  15:33
 */
package com.comcast.xconf.admin.validator.dcm;

import com.comcast.xconf.logupload.LogFilesGroup;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class LogFilesGroupValidator {

    public String validateOnSave(LogFilesGroup logFilesGroup) {
        if (StringUtils.isBlank(logFilesGroup.getGroupName())) {
            return "LogFilesGroup name must be not empty";
        }

        if (CollectionUtils.isEmpty(logFilesGroup.getLogFileIds())) {
            return "LogFilesGroup must contain at least one logFile id";
        }

        return null;
    }
}
