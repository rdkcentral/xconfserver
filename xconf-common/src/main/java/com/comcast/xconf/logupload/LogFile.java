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
import com.comcast.apps.dataaccess.annotation.CompositeCF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.CfNames;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import javax.annotation.Nullable;

import org.apache.commons.collections.comparators.NullComparator;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

@CF(cfName = CfNames.LogUpload.LOG_FILE)
@CompositeCF(
        cfName = CfNames.LogUpload.INDEXED_LOG_FILES,
        comparatorTypeAlias = "CompositeType(UTF8Type, UTF8Type)",
        dao = "IndexesLogFilesDAO"
)
public class LogFile extends XMLPersistable implements Comparable<LogFile> {
    @NotBlank
    private String name;
    @NotNull
    private Boolean deleteOnUpload;

    public LogFile() {
    }

    public LogFile(String name, Boolean deleteOnUpload, String url) {
        this.name = name;
        this.deleteOnUpload = deleteOnUpload;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getDeleteOnUpload() {
        return deleteOnUpload;
    }

    public void setDeleteOnUpload(Boolean deleteOnUpload) {
        this.deleteOnUpload = deleteOnUpload;
    }

    @JsonProperty
    @Override
    public String getId(){
        return super.getId();
    }

    @JsonIgnore
    @JsonProperty
    @Override
    public Date getUpdated(){
        return super.getUpdated();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogFile logFile = (LogFile) o;

        if (deleteOnUpload != null ? !deleteOnUpload.equals(logFile.deleteOnUpload) : logFile.deleteOnUpload != null)
            return false;
        if (name != null ? !name.equals(logFile.name) : logFile.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (deleteOnUpload != null ? deleteOnUpload.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{name:'").append(name).append('\'');
        sb.append(", deleteOnUpload:").append(deleteOnUpload);
        sb.append('}');
        return sb.toString();
    }

    public static final Maps.EntryTransformer LOG_FILE_TRANSFORMER = new Maps.EntryTransformer<String, Optional<LogFile>, LogFile>() {
        @Override
        public LogFile transformEntry(String key, Optional<LogFile> value) {
            LogFile output = value.orNull();
            if (value.isPresent()) {
                output.setId(key);
            }
            return value.orNull();
        }
    };

    @Override
    public int compareTo(LogFile o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }
}
