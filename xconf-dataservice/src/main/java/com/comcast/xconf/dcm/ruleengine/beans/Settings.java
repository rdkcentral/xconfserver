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
package com.comcast.xconf.dcm.ruleengine.beans;

import com.comcast.xconf.logupload.LogFile;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Kaba
 */

public class Settings {

    private static final String DEFAULT_LOG_UPLOAD_SETTINGS_MESSAGE = "Don't upload your logs, but check for updates on this schedule.";

    private String schedulerType;


    @JsonIgnore
    private Set<String> ruleIDs;

    @JsonProperty("urn:settings:GroupName")
    private String groupName;

    @JsonProperty("urn:settings:CheckOnReboot")
    private boolean checkOnReboot;

    //@JsonProperty("urn:settings:ConfigurationServiceURL")
    @Deprecated
    private String configurationServiceURL;

    @JsonProperty("urn:settings:CheckSchedule:cron")
    private String scheduleCron;

    @JsonProperty("urn:settings:CheckSchedule:DurationMinutes")
    private Integer scheduleDurationMinutes;

    @JsonProperty("urn:settings:CheckSchedule:StartDate")
    private String scheduleStartDate;

    @JsonProperty("urn:settings:CheckSchedule:EndDate")
    private String scheduleEndDate;

    @JsonProperty("urn:settings:LogUploadSettings:Message")
    private String lusMessage;

    @JsonProperty("urn:settings:LogUploadSettings:Name")
    private String lusName;

    @JsonProperty("urn:settings:LogUploadSettings:NumberOfDays")
    private Integer lusNumberOfDay;

    @JsonProperty("urn:settings:LogUploadSettings:UploadRepositoryName")
    private String lusUploadRepositoryName;

    @JsonProperty("urn:settings:LogUploadSettings:RepositoryURL")
    private String lusUploadRepositoryURL;

    @JsonProperty("urn:settings:LogUploadSettings:UploadOnReboot")
    private Boolean lusUploadOnReboot;
    /**
     * Upload flag to indicate if allowed to upload logs or not.
     */
    @JsonProperty("urn:settings:LogUploadSettings:upload")
    private Boolean upload;

    @JsonProperty("urn:settings:LogUploadSettings:LogFiles")
    private List<LogFile> lusLogFiles;

    @JsonProperty("urn:settings:LogUploadSettings:LogFiles:StartDate")
    private String lusLogFilesStartDate;

    @JsonProperty("urn:settings:LogUploadSettings:LogFiles:EndDate")
    private String lusLogFilesEndDate;

    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:cron")
    private String lusScheduleCron;

    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:DurationMinutes")
    private Integer lusScheduleDurationMinutes;

    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:StartDate")
    private String lusScheduleStartDate;

    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:EndDate")
    private String lusScheduleEndDate;

    @JsonProperty("urn:settings:VODSettings:Name")
    private String vodSettingsName;

    @JsonProperty("urn:settings:VODSettings:LocationsURL")
    private String locationUrl;

    @JsonProperty("urn:settings:VODSettings:SRMIPList")
    private Map<String, String> srmIPList;


    public Settings() {
        this.ruleIDs = new HashSet<String>();
    }

    public void addRuleID (String id) {
        ruleIDs.add(id);
    }

    @JsonIgnore
    public String getSchedulerType() {
        return schedulerType;
    }

    public void setSchedulerType(String schedulerType) {
        this.schedulerType = schedulerType;
    }

    public void copyDeviceSettings (Settings settings, boolean setLUSSettings) {
        groupName = settings.getGroupName();
        checkOnReboot = settings.isCheckOnReboot();
        configurationServiceURL = settings.getConfigurationServiceURL();
        scheduleCron = settings.getScheduleCron();
        scheduleDurationMinutes = settings.getScheduleDurationMinutes();
        scheduleStartDate = settings.getScheduleStartDate();
        scheduleEndDate = settings.getScheduleEndDate();
        if (setLUSSettings) {
            lusMessage = null;
            lusName = settings.getLusName();
            lusNumberOfDay = settings.getLusNumberOfDay();
            lusUploadRepositoryName = settings.getLusUploadRepositoryName();
            lusUploadRepositoryURL = settings.getLusUploadRepositoryURL();
            lusUploadOnReboot = settings.getLusUploadOnReboot();
            lusLogFiles = settings.getLusLogFiles();
            lusLogFilesStartDate = settings.getLusLogFilesStartDate();
            lusLogFilesEndDate = settings.getLusLogFilesEndDate();
            lusScheduleCron = settings.getLusScheduleCron();
            lusScheduleDurationMinutes = settings.getLusScheduleDurationMinutes();
            lusScheduleStartDate = settings.getLusScheduleStartDate();
            lusScheduleEndDate = settings.getLusScheduleEndDate();
            upload = true;
        } else {
            lusMessage = DEFAULT_LOG_UPLOAD_SETTINGS_MESSAGE;
            lusName = null;
            lusNumberOfDay = null;
            lusUploadRepositoryName = null;
            lusUploadRepositoryURL = null;
            lusUploadOnReboot = null;
            lusLogFiles = null;
            lusLogFilesStartDate = null;
            lusLogFilesEndDate = null;
            lusScheduleCron = null;
            lusScheduleDurationMinutes = null;
            lusScheduleStartDate = null;
            lusScheduleEndDate = null;
            upload = false;
        }
    }

    public void copyVodSettings (Settings settings) {
        vodSettingsName = settings.getVodSettingsName();
        locationUrl = settings.getLocationUrl();
        srmIPList = settings.getSrmIPList();
    }

    public boolean areFull() {
        if (groupName != null && vodSettingsName != null ) return true;

        return false;
    }

    @JsonIgnore
    public Set<String> getRuleIDs() {
        return ruleIDs;
    }

    public void setRuleIDs(Set<String> ruleIDs) {
        this.ruleIDs = ruleIDs;
    }

    @JsonIgnore
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @JsonIgnore
    public boolean isCheckOnReboot() {
        return checkOnReboot;
    }

    public void setCheckOnReboot(boolean checkOnReboot) {
        this.checkOnReboot = checkOnReboot;
    }

    @JsonIgnore
    @Deprecated
    public String getConfigurationServiceURL() {
        return configurationServiceURL;
    }

    @Deprecated
    public void setConfigurationServiceURL(String configurationServiceURL) {
        this.configurationServiceURL = configurationServiceURL;
    }

    @JsonIgnore
    public String getScheduleCron() {
        return scheduleCron;
    }

    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }

    @JsonIgnore
    public Integer getScheduleDurationMinutes() {
        return scheduleDurationMinutes;
    }

    public void setScheduleDurationMinutes(Integer scheduleDurationMinutes) {
        this.scheduleDurationMinutes = scheduleDurationMinutes;
    }

    @JsonIgnore
    public String getScheduleStartDate() {
        return scheduleStartDate;
    }

    public void setScheduleStartDate(String scheduleStartDate) {
        this.scheduleStartDate = scheduleStartDate;
    }

    @JsonIgnore
    public String getScheduleEndDate() {
        return scheduleEndDate;
    }

    public void setScheduleEndDate(String scheduleEndDate) {
        this.scheduleEndDate = scheduleEndDate;
    }

    @JsonIgnore
    public String getLusMessage() {
        return lusMessage;
    }

    public void setLusMessage(String lusMessage) {
        this.lusMessage = lusMessage;
    }

    @JsonIgnore
    public String getLusName() {
        return lusName;
    }

    public void setLusName(String lusName) {
        this.lusName = lusName;
    }

    @JsonIgnore
    public Integer getLusNumberOfDay() {
        return lusNumberOfDay;
    }

    public void setLusNumberOfDay(Integer lusNumberOfDay) {
        this.lusNumberOfDay = lusNumberOfDay;
    }

    @JsonIgnore
    public String getLusUploadRepositoryName() {
        return lusUploadRepositoryName;
    }

    public void setLusUploadRepositoryName(String lusUploadRepositoryName) {
        this.lusUploadRepositoryName = lusUploadRepositoryName;
    }

    @JsonIgnore
    public String getLusUploadRepositoryURL() {
        return lusUploadRepositoryURL;
    }

    public void setLusUploadRepositoryURL(String lusUploadRepositoryURL) {
        this.lusUploadRepositoryURL = lusUploadRepositoryURL;
    }

    @JsonIgnore
    public Boolean getUpload() {
        return upload;
    }

    public void setUpload(Boolean upload) {
        this.upload = upload;
    }
    @JsonIgnore
    public Boolean getLusUploadOnReboot() {
        return lusUploadOnReboot;
    }

    public void setLusUploadOnReboot(Boolean lusUploadOnReboot) {
        this.lusUploadOnReboot = lusUploadOnReboot;
    }

    @JsonIgnore
    public List<LogFile> getLusLogFiles() {
        return lusLogFiles;
    }

    public void setLusLogFiles(List<LogFile> lusLogFiles) {
        this.lusLogFiles = lusLogFiles;
    }

    @JsonIgnore
    public String getLusLogFilesStartDate() {
        return lusLogFilesStartDate;
    }

    public void setLusLogFilesStartDate(String lusLogFilesStartDate) {
        this.lusLogFilesStartDate = lusLogFilesStartDate;
    }

    @JsonIgnore
    public String getLusLogFilesEndDate() {
        return lusLogFilesEndDate;
    }

    public void setLusLogFilesEndDate(String lusLogFilesEndDate) {
        this.lusLogFilesEndDate = lusLogFilesEndDate;
    }

    @JsonIgnore
    public String getLusScheduleCron() {
        return lusScheduleCron;
    }

    public void setLusScheduleCron(String lusScheduleCron) {
        this.lusScheduleCron = lusScheduleCron;
    }

    @JsonIgnore
    public Integer getLusScheduleDurationMinutes() {
        return lusScheduleDurationMinutes;
    }

    public void setLusScheduleDurationMinutes(Integer lusScheduleDurationMinutes) {
        this.lusScheduleDurationMinutes = lusScheduleDurationMinutes;
    }

    @JsonIgnore
    public String getLusScheduleStartDate() {
        return lusScheduleStartDate;
    }

    public void setLusScheduleStartDate(String lusScheduleStartDate) {
        this.lusScheduleStartDate = lusScheduleStartDate;
    }

    @JsonIgnore
    public String getLusScheduleEndDate() {
        return lusScheduleEndDate;
    }

    public void setLusScheduleEndDate(String lusScheduleEndDate) {
        this.lusScheduleEndDate = lusScheduleEndDate;
    }

    @JsonIgnore
    public String getVodSettingsName() {
        return vodSettingsName;
    }

    public void setVodSettingsName(String vodSettingsName) {
        this.vodSettingsName = vodSettingsName;
    }

    @JsonIgnore
    public String getLocationUrl() {
        return locationUrl;
    }

    public void setLocationUrl(String locationUrl) {
        this.locationUrl = locationUrl;
    }

    @JsonIgnore
    public Map<String, String> getSrmIPList() {
        return srmIPList;
    }

    public void setSrmIPList(Map<String, String> srmIPList) {
        this.srmIPList = srmIPList;
    }
}
