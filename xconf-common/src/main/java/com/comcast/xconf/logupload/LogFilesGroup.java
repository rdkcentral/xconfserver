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
 * Author: slavrenyuk
 * Created: 4/29/14
 */
package com.comcast.xconf.logupload;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.CfNames;
import org.apache.commons.collections.comparators.NullComparator;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@CF(cfName = CfNames.LogUpload.LOG_FILES_GROUPS)
public class LogFilesGroup extends XMLPersistable implements Comparable<LogFilesGroup> {
    @NotBlank
    private String groupName;
    @NotNull
    @Size(min=2)
    private List<String> logFileIds;

    public LogFilesGroup() {
    }

    public LogFilesGroup(String groupName, List<String> logFileIds) {
        this.groupName = groupName;
        this.logFileIds = logFileIds;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getLogFileIds() {
        return logFileIds;
    }

    public void setLogFileIds(List<String> logFileIds) {
        this.logFileIds = logFileIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogFilesGroup that = (LogFilesGroup) o;

        if (groupName != null ? !groupName.equals(that.groupName) : that.groupName != null) return false;
        if (logFileIds != null ? !logFileIds.equals(that.logFileIds) : that.logFileIds != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = groupName != null ? groupName.hashCode() : 0;
        result = 31 * result + (logFileIds != null ? logFileIds.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(LogFilesGroup o) {
        String name1 = (groupName != null) ? groupName.toLowerCase() : null;
        String name2 = (o != null && o.groupName != null) ? o.groupName.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }
}
