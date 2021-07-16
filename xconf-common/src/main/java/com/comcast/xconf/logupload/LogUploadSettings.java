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
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.CfNames;
import com.comcast.xconf.firmware.ApplicationType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.collections.comparators.NullComparator;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@CF(cfName = CfNames.LogUpload.LOG_UPLOAD_SETTINGS)
public class LogUploadSettings extends XMLPersistable implements Comparable<LogUploadSettings>, Applicationable {

    public static final String[] MODE_TO_GET_LOG_FILES = {"LogFiles", "LogFilesGroup", "AllLogFiles"};
    @NotBlank
    private String name;
    @NotNull
    private Boolean uploadOnReboot;
    @NotNull
    @Min(value=0)
    private Integer numberOfDays = 0;
    @NotNull
    private Boolean areSettingsActive;
    @NotNull
    @Valid
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
    public Schedule schedule = new Schedule();
    private List<String> logFileIds;
    private String logFilesGroupId;
    /**
     * We need this modeToGetLogFiles property to know where/(where from) we should to save/load list of log files
     * modeToGetLogFiles = LogFiles : we should save/load log files from/to CF LogFileList associating to logUploadSettings.Id
     * modeToGetLogFiles = LogFilesGroup : we should save/load log files from/to CF LogFileList associating to LogFilesGroup.Id
     * modeToGetLogFiles = AllLogFiles : we should load all log files from CF LogFiles
     */
    @NotBlank
    private String modeToGetLogFiles;
    @NotBlank
    private String uploadRepositoryId;

    private Boolean activeDateTimeRange;
    private String fromDateTime;
    private String toDateTime;

    private String applicationType = ApplicationType.STB;

    public LogUploadSettings() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getUploadOnReboot() {
        return uploadOnReboot;
    }

    public void setUploadOnReboot(Boolean uploadOnReboot) {
        this.uploadOnReboot = uploadOnReboot;
    }

    public Integer getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public Boolean getAreSettingsActive() {
        return areSettingsActive;
    }

    public void setAreSettingsActive(Boolean areSettingsActive) {
        this.areSettingsActive = areSettingsActive;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public List<String> getLogFileIds() {
        return logFileIds;
    }

    public void setLogFileIds(List<String> logFileIds) {
        this.logFileIds = logFileIds;
    }

    public String getLogFilesGroupId() {
        return logFilesGroupId;
    }

    public void setLogFilesGroupId(String logFilesGroupId) {
        this.logFilesGroupId = logFilesGroupId;
    }

    public String getModeToGetLogFiles() {
        return modeToGetLogFiles;
    }

    public void setModeToGetLogFiles(String modeToGetLogFiles) {
        this.modeToGetLogFiles = modeToGetLogFiles;
    }

    public String getUploadRepositoryId() {
        return uploadRepositoryId;
    }

    public void setUploadRepositoryId(String uploadRepositoryId) {
        this.uploadRepositoryId = uploadRepositoryId;
    }

    public Boolean getActiveDateTimeRange() {
        return activeDateTimeRange;
    }

    public void setActiveDateTimeRange(Boolean activeDateTimeRange) {
        this.activeDateTimeRange = activeDateTimeRange;
    }

    public String getFromDateTime() {
        return fromDateTime;
    }

    public void setFromDateTime(String fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public String getToDateTime() {
        return toDateTime;
    }

    public void setToDateTime(String toDateTime) {
        this.toDateTime = toDateTime;
    }

    @Override
    public String getApplicationType() {
        return applicationType;
    }

    @Override
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogUploadSettings that = (LogUploadSettings) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (uploadOnReboot != null ? !uploadOnReboot.equals(that.uploadOnReboot) : that.uploadOnReboot != null)
            return false;
        if (numberOfDays != null ? !numberOfDays.equals(that.numberOfDays) : that.numberOfDays != null) return false;
        if (areSettingsActive != null ? !areSettingsActive.equals(that.areSettingsActive) : that.areSettingsActive != null)
            return false;
        if (schedule != null ? !schedule.equals(that.schedule) : that.schedule != null) return false;
        if (logFileIds != null ? !logFileIds.equals(that.logFileIds) : that.logFileIds != null) return false;
        if (logFilesGroupId != null ? !logFilesGroupId.equals(that.logFilesGroupId) : that.logFilesGroupId != null)
            return false;
        if (modeToGetLogFiles != null ? !modeToGetLogFiles.equals(that.modeToGetLogFiles) : that.modeToGetLogFiles != null)
            return false;
        if (uploadRepositoryId != null ? !uploadRepositoryId.equals(that.uploadRepositoryId) : that.uploadRepositoryId != null)
            return false;
        if (activeDateTimeRange != null ? !activeDateTimeRange.equals(that.activeDateTimeRange) : that.activeDateTimeRange != null)
            return false;
        if (fromDateTime != null ? !fromDateTime.equals(that.fromDateTime) : that.fromDateTime != null) return false;
        if (toDateTime != null ? !toDateTime.equals(that.toDateTime) : that.toDateTime != null) return false;
        if (applicationType != null ? !applicationType.equals(that.applicationType) : that.applicationType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (uploadOnReboot != null ? uploadOnReboot.hashCode() : 0);
        result = 31 * result + (numberOfDays != null ? numberOfDays.hashCode() : 0);
        result = 31 * result + (areSettingsActive != null ? areSettingsActive.hashCode() : 0);
        result = 31 * result + (schedule != null ? schedule.hashCode() : 0);
        result = 31 * result + (logFileIds != null ? logFileIds.hashCode() : 0);
        result = 31 * result + (logFilesGroupId != null ? logFilesGroupId.hashCode() : 0);
        result = 31 * result + (modeToGetLogFiles != null ? modeToGetLogFiles.hashCode() : 0);
        result = 31 * result + (uploadRepositoryId != null ? uploadRepositoryId.hashCode() : 0);
        result = 31 * result + (activeDateTimeRange != null ? activeDateTimeRange.hashCode() : 0);
        result = 31 * result + (fromDateTime != null ? fromDateTime.hashCode() : 0);
        result = 31 * result + (toDateTime != null ? toDateTime.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(LogUploadSettings o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }
}
