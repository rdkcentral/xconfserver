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
 * Created: 6/18/14
 */
package com.comcast.xconf.logupload;

import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.ALWAYS)
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
    @JsonIgnore
    private String scheduleStartDate;

    @JsonProperty("urn:settings:CheckSchedule:EndDate")
    @JsonIgnore
    private String scheduleEndDate;

    @JsonProperty("urn:settings:LogUploadSettings:Message")
    private String lusMessage;

    @JsonProperty("urn:settings:LogUploadSettings:Name")
    private String lusName;

    @JsonProperty("urn:settings:LogUploadSettings:NumberOfDays")
    private Integer lusNumberOfDay;

    @JsonProperty("urn:settings:LogUploadSettings:UploadRepositoryName")
    private String lusUploadRepositoryName;

    @JsonProperty("urn:settings:LogUploadSettings:UploadRepository:URL")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String lusUploadRepositoryURLNew;

    @JsonProperty("urn:settings:LogUploadSettings:UploadRepository:uploadProtocol")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String lusUploadRepositoryUploadProtocol;

    @JsonProperty("urn:settings:LogUploadSettings:RepositoryURL")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String lusUploadRepositoryURL;

    @JsonProperty("urn:settings:LogUploadSettings:UploadOnReboot")
    private Boolean lusUploadOnReboot;

    @JsonProperty("urn:settings:LogUploadSettings:UploadImmediately")
    private Boolean uploadImmediately;
    /**
     * Upload flag to indicate if allowed to upload logs or not.
     */
    @JsonProperty("urn:settings:LogUploadSettings:upload")
    private Boolean upload;

    @JsonProperty("urn:settings:LogUploadSettings:LogFiles")
    @JsonIgnore
    private List<LogFile> lusLogFiles;

    @JsonProperty("urn:settings:LogUploadSettings:LogFiles:StartDate")
    @JsonIgnore
    private String lusLogFilesStartDate;

    @JsonProperty("urn:settings:LogUploadSettings:LogFiles:EndDate")
    @JsonIgnore
    private String lusLogFilesEndDate;

    /**
     * For level one logging
     */
    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:cron")
    private String lusScheduleCron;

    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:levelone:cron")
    private String lusScheduleCronL1;

    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:leveltwo:cron")
    private String lusScheduleCronL2;

    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:levelthree:cron")
    private String lusScheduleCronL3;

    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:DurationMinutes")
    private Integer lusScheduleDurationMinutes;

    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:StartDate")
    @JsonIgnore
    private String lusScheduleStartDate;

    @JsonProperty("urn:settings:LogUploadSettings:UploadSchedule:EndDate")
    @JsonIgnore
    private String lusScheduleEndDate;

    @JsonProperty("urn:settings:VODSettings:Name")
    private String vodSettingsName;

    @JsonProperty("urn:settings:VODSettings:LocationsURL")
    private String locationUrl;

    @JsonProperty("urn:settings:VODSettings:SRMIPList")
    private Map<String, String> srmIPList;

    @JsonProperty("urn:settings:TelemetryProfile")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({"applicationType"})
    private PermanentTelemetryProfile telemetryProfile;

    @JsonProperty("urn:settings:SettingType:epon")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> eponSettings;

    @JsonProperty("urn:settings:SettingType:partnersettings")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> partnerSettings;

    public Settings() {
        this.ruleIDs = new HashSet<String>();
    }

    public void addRuleID(String id) {
        ruleIDs.add(id);
    }

    @JsonIgnore
    public String getSchedulerType() {
        return schedulerType;
    }

    public void setSchedulerType(String schedulerType) {
        this.schedulerType = schedulerType;
    }

    public void copyDeviceSettings(Settings settings) {
        groupName = settings.getGroupName();
        checkOnReboot = settings.isCheckOnReboot();
        configurationServiceURL = settings.getConfigurationServiceURL();
        scheduleCron = settings.getScheduleCron();
        scheduleDurationMinutes = settings.getScheduleDurationMinutes();
        scheduleStartDate = settings.getScheduleStartDate();
        scheduleEndDate = settings.getScheduleEndDate();
    }

    public void copyLusSetting(Settings settings, boolean setLUSSettings) {
        if (setLUSSettings) {
            lusMessage = null;
            lusName = settings.getLusName();
            lusNumberOfDay = settings.getLusNumberOfDay();
            lusUploadRepositoryName = settings.getLusUploadRepositoryName();
            lusUploadRepositoryURL = settings.getLusUploadRepositoryURL();
            lusUploadRepositoryURLNew = settings.getLusUploadRepositoryURLNew();
            lusUploadRepositoryUploadProtocol = settings.getLusUploadRepositoryUploadProtocol();
            lusUploadOnReboot = settings.getLusUploadOnReboot();
            lusLogFiles = settings.getLusLogFiles();
            lusLogFilesStartDate = settings.getLusLogFilesStartDate();
            lusLogFilesEndDate = settings.getLusLogFilesEndDate();

            /*lusScheduleCron = settings.getLusScheduleCron();
            lusScheduleCronL1 = settings.getLusScheduleCronL1();
            lusScheduleCronL2 = settings.getLusScheduleCronL2();
            lusScheduleCronL3 = settings.getLusScheduleCronL3();*/

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
            lusUploadRepositoryURLNew = null;
            lusUploadRepositoryUploadProtocol = null;
            lusUploadOnReboot = null;
            lusLogFiles = null;
            lusLogFilesStartDate = null;
            lusLogFilesEndDate = null;

            /*lusScheduleCron = null;
            lusScheduleCronL1 = null;
            lusScheduleCronL2 = null;
            lusScheduleCronL3 = null;*/

            lusScheduleDurationMinutes = null;
            lusScheduleStartDate = null;
            lusScheduleEndDate = null;
            upload = false;
        }
    }

    public void copyVodSettings(Settings settings) {
        vodSettingsName = settings.getVodSettingsName();
        locationUrl = settings.getLocationUrl();
        srmIPList = settings.getSrmIPList();
    }

    public boolean areFull() {
        if (groupName != null && lusName != null && vodSettingsName != null) return true;

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

    public String getLusScheduleCronL1() {
        return lusScheduleCronL1;
    }

    public void setLusScheduleCronL1(String lusScheduleCronL1) {
        this.lusScheduleCronL1 = lusScheduleCronL1;
    }

    @JsonIgnore
    public String getLusScheduleCronL2() {
        return lusScheduleCronL2;
    }

    public void setLusScheduleCronL2(String lusScheduleCronL2) {
        this.lusScheduleCronL2 = lusScheduleCronL2;
    }

    @JsonIgnore
    public String getLusScheduleCronL3() {
        return lusScheduleCronL3;
    }

    public void setLusScheduleCronL3(String lusScheduleCronL3) {
        this.lusScheduleCronL3 = lusScheduleCronL3;
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

    public PermanentTelemetryProfile getTelemetryProfile() {
        return telemetryProfile;
    }

    public void setTelemetryProfile(PermanentTelemetryProfile telemetryProfile) {
        this.telemetryProfile = telemetryProfile;
    }

    @JsonIgnore
    public Map<String, String> getEponSettings() {
        return eponSettings;
    }

    public void setSettingProfiles(final Set<SettingProfile> settingProfiles) {
        if (CollectionUtils.isEmpty(settingProfiles)) {
            return;
        }
        for (final SettingProfile settingProfile : settingProfiles) {
            final Map<String, String> properties = settingProfile.getProperties();
            switch (settingProfile.getSettingType()) {
                case PARTNER_SETTINGS:
                    partnerSettings = properties;
                    break;
                case EPON:
                    eponSettings = properties;
                    break;
            }
        }
    }

    @JsonIgnore
    public Map<String, String> getPartnerSettings() {
        return partnerSettings;
    }

    public String getLusUploadRepositoryURLNew() {
        return lusUploadRepositoryURLNew;
    }

    public void setLusUploadRepositoryURLNew(String lusUploadRepositoryURLNew) {
        this.lusUploadRepositoryURLNew = lusUploadRepositoryURLNew;
    }

    public String getLusUploadRepositoryUploadProtocol() {
        return lusUploadRepositoryUploadProtocol;
    }

    public void setLusUploadRepositoryUploadProtocol(String lusUploadRepositoryUploadProtocol) {
        this.lusUploadRepositoryUploadProtocol = lusUploadRepositoryUploadProtocol;
    }

    public Boolean getUploadImmediately() {
        return uploadImmediately;
    }

    public void setUploadImmediately(Boolean uploadImmediately) {
        this.uploadImmediately = uploadImmediately;
    }

    @Override
    public String toString() {
        return toLogString();
    }

    public String toLogString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(" groupName='").append(groupName).append('\'');
        sb.append(", checkOnReboot=").append(checkOnReboot);
        sb.append(", scheduleCron='").append(scheduleCron).append('\'');
        sb.append(", scheduleDurationMinutes=").append(scheduleDurationMinutes);
        sb.append(", scheduleStartDate='").append(scheduleStartDate).append('\'');
        sb.append(", scheduleEndDate='").append(scheduleEndDate).append('\'');
        sb.append(", lusMessage='").append(lusMessage).append('\'');
        sb.append(", lusName='").append(lusName).append('\'');
        sb.append(", lusNumberOfDay=").append(lusNumberOfDay);
        sb.append(", lusUploadRepositoryName='").append(lusUploadRepositoryName).append('\'');
        sb.append(", lusUploadRepositoryURL='").append(lusUploadRepositoryURL).append('\'');
        sb.append(", lusUploadOnReboot=").append(lusUploadOnReboot);
        sb.append(", lusUploadImmediately=").append(uploadImmediately);
        sb.append(", upload=").append(upload);
        sb.append(", lusLogFiles=").append(lusLogFiles);
        sb.append(", lusLogFilesStartDate='").append(lusLogFilesStartDate).append('\'');
        sb.append(", lusLogFilesEndDate='").append(lusLogFilesEndDate).append('\'');
        sb.append(", lusScheduleCron='").append(lusScheduleCron).append('\'');
        sb.append(", lusScheduleCronL1='").append(lusScheduleCronL1).append('\'');
        sb.append(", lusScheduleCronL2='").append(lusScheduleCronL2).append('\'');
        sb.append(", lusScheduleCronL3='").append(lusScheduleCronL3).append('\'');
        sb.append(", lusScheduleDurationMinutes=").append(lusScheduleDurationMinutes);
        sb.append(", lusScheduleStartDate='").append(lusScheduleStartDate).append('\'');
        sb.append(", lusScheduleEndDate='").append(lusScheduleEndDate).append('\'');
        sb.append(", vodSettingsName='").append(vodSettingsName).append('\'');
        sb.append(", locationUrl='").append(locationUrl).append('\'');
        sb.append(", srmIPList=").append(srmIPList);
        if (telemetryProfile != null) sb.append(", telemetryProfile=").append(telemetryProfile);
        if (eponSettings != null) sb.append(", eponSettings=").append(eponSettings);
        if (partnerSettings != null) sb.append(", partnerSettings=").append(partnerSettings);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Settings settings = (Settings) o;

        if (checkOnReboot != settings.checkOnReboot) return false;
        if (schedulerType != null ? !schedulerType.equals(settings.schedulerType) : settings.schedulerType != null)
            return false;
        if (ruleIDs != null ? !ruleIDs.equals(settings.ruleIDs) : settings.ruleIDs != null) return false;
        if (groupName != null ? !groupName.equals(settings.groupName) : settings.groupName != null) return false;
        if (configurationServiceURL != null ? !configurationServiceURL.equals(settings.configurationServiceURL) : settings.configurationServiceURL != null)
            return false;
        if (scheduleCron != null ? !scheduleCron.equals(settings.scheduleCron) : settings.scheduleCron != null)
            return false;
        if (scheduleDurationMinutes != null ? !scheduleDurationMinutes.equals(settings.scheduleDurationMinutes) : settings.scheduleDurationMinutes != null)
            return false;
        if (scheduleStartDate != null ? !scheduleStartDate.equals(settings.scheduleStartDate) : settings.scheduleStartDate != null)
            return false;
        if (scheduleEndDate != null ? !scheduleEndDate.equals(settings.scheduleEndDate) : settings.scheduleEndDate != null)
            return false;
        if (lusMessage != null ? !lusMessage.equals(settings.lusMessage) : settings.lusMessage != null) return false;
        if (lusName != null ? !lusName.equals(settings.lusName) : settings.lusName != null) return false;
        if (lusNumberOfDay != null ? !lusNumberOfDay.equals(settings.lusNumberOfDay) : settings.lusNumberOfDay != null)
            return false;
        if (lusUploadRepositoryName != null ? !lusUploadRepositoryName.equals(settings.lusUploadRepositoryName) : settings.lusUploadRepositoryName != null)
            return false;
        if (lusUploadRepositoryURLNew != null ? !lusUploadRepositoryURLNew.equals(settings.lusUploadRepositoryURLNew) : settings.lusUploadRepositoryURLNew != null)
            return false;
        if (lusUploadRepositoryUploadProtocol != null ? !lusUploadRepositoryUploadProtocol.equals(settings.lusUploadRepositoryUploadProtocol) : settings.lusUploadRepositoryUploadProtocol != null)
            return false;
        if (lusUploadRepositoryURL != null ? !lusUploadRepositoryURL.equals(settings.lusUploadRepositoryURL) : settings.lusUploadRepositoryURL != null)
            return false;
        if (lusUploadOnReboot != null ? !lusUploadOnReboot.equals(settings.lusUploadOnReboot) : settings.lusUploadOnReboot != null)
            return false;
        if (uploadImmediately != null ? !uploadImmediately.equals(settings.uploadImmediately) : settings.uploadImmediately != null)
            return false;
        if (upload != null ? !upload.equals(settings.upload) : settings.upload != null) return false;
        if (lusLogFiles != null ? !lusLogFiles.equals(settings.lusLogFiles) : settings.lusLogFiles != null)
            return false;
        if (lusLogFilesStartDate != null ? !lusLogFilesStartDate.equals(settings.lusLogFilesStartDate) : settings.lusLogFilesStartDate != null)
            return false;
        if (lusLogFilesEndDate != null ? !lusLogFilesEndDate.equals(settings.lusLogFilesEndDate) : settings.lusLogFilesEndDate != null)
            return false;
        if (lusScheduleCron != null ? !lusScheduleCron.equals(settings.lusScheduleCron) : settings.lusScheduleCron != null)
            return false;
        if (lusScheduleCronL1 != null ? !lusScheduleCronL1.equals(settings.lusScheduleCronL1) : settings.lusScheduleCronL1 != null)
            return false;
        if (lusScheduleCronL2 != null ? !lusScheduleCronL2.equals(settings.lusScheduleCronL2) : settings.lusScheduleCronL2 != null)
            return false;
        if (lusScheduleCronL3 != null ? !lusScheduleCronL3.equals(settings.lusScheduleCronL3) : settings.lusScheduleCronL3 != null)
            return false;
        if (lusScheduleDurationMinutes != null ? !lusScheduleDurationMinutes.equals(settings.lusScheduleDurationMinutes) : settings.lusScheduleDurationMinutes != null)
            return false;
        if (lusScheduleStartDate != null ? !lusScheduleStartDate.equals(settings.lusScheduleStartDate) : settings.lusScheduleStartDate != null)
            return false;
        if (lusScheduleEndDate != null ? !lusScheduleEndDate.equals(settings.lusScheduleEndDate) : settings.lusScheduleEndDate != null)
            return false;
        if (vodSettingsName != null ? !vodSettingsName.equals(settings.vodSettingsName) : settings.vodSettingsName != null)
            return false;
        if (locationUrl != null ? !locationUrl.equals(settings.locationUrl) : settings.locationUrl != null)
            return false;
        if (srmIPList != null ? !srmIPList.equals(settings.srmIPList) : settings.srmIPList != null) return false;
        return !(telemetryProfile != null ? !telemetryProfile.equals(settings.telemetryProfile) : settings.telemetryProfile != null);

    }

    @Override
    public int hashCode() {
        int result = schedulerType != null ? schedulerType.hashCode() : 0;
        result = 31 * result + (ruleIDs != null ? ruleIDs.hashCode() : 0);
        result = 31 * result + (groupName != null ? groupName.hashCode() : 0);
        result = 31 * result + (checkOnReboot ? 1 : 0);
        result = 31 * result + (configurationServiceURL != null ? configurationServiceURL.hashCode() : 0);
        result = 31 * result + (scheduleCron != null ? scheduleCron.hashCode() : 0);
        result = 31 * result + (scheduleDurationMinutes != null ? scheduleDurationMinutes.hashCode() : 0);
        result = 31 * result + (scheduleStartDate != null ? scheduleStartDate.hashCode() : 0);
        result = 31 * result + (scheduleEndDate != null ? scheduleEndDate.hashCode() : 0);
        result = 31 * result + (lusMessage != null ? lusMessage.hashCode() : 0);
        result = 31 * result + (lusName != null ? lusName.hashCode() : 0);
        result = 31 * result + (lusNumberOfDay != null ? lusNumberOfDay.hashCode() : 0);
        result = 31 * result + (lusUploadRepositoryName != null ? lusUploadRepositoryName.hashCode() : 0);
        result = 31 * result + (lusUploadRepositoryURLNew != null ? lusUploadRepositoryURLNew.hashCode() : 0);
        result = 31 * result + (lusUploadRepositoryUploadProtocol != null ? lusUploadRepositoryUploadProtocol.hashCode() : 0);
        result = 31 * result + (lusUploadRepositoryURL != null ? lusUploadRepositoryURL.hashCode() : 0);
        result = 31 * result + (lusUploadOnReboot != null ? lusUploadOnReboot.hashCode() : 0);
        result = 31 * result + (uploadImmediately != null ? uploadImmediately.hashCode() : 0);
        result = 31 * result + (upload != null ? upload.hashCode() : 0);
        result = 31 * result + (lusLogFiles != null ? lusLogFiles.hashCode() : 0);
        result = 31 * result + (lusLogFilesStartDate != null ? lusLogFilesStartDate.hashCode() : 0);
        result = 31 * result + (lusLogFilesEndDate != null ? lusLogFilesEndDate.hashCode() : 0);
        result = 31 * result + (lusScheduleCron != null ? lusScheduleCron.hashCode() : 0);
        result = 31 * result + (lusScheduleCronL1 != null ? lusScheduleCronL1.hashCode() : 0);
        result = 31 * result + (lusScheduleCronL2 != null ? lusScheduleCronL2.hashCode() : 0);
        result = 31 * result + (lusScheduleCronL3 != null ? lusScheduleCronL3.hashCode() : 0);
        result = 31 * result + (lusScheduleDurationMinutes != null ? lusScheduleDurationMinutes.hashCode() : 0);
        result = 31 * result + (lusScheduleStartDate != null ? lusScheduleStartDate.hashCode() : 0);
        result = 31 * result + (lusScheduleEndDate != null ? lusScheduleEndDate.hashCode() : 0);
        result = 31 * result + (vodSettingsName != null ? vodSettingsName.hashCode() : 0);
        result = 31 * result + (locationUrl != null ? locationUrl.hashCode() : 0);
        result = 31 * result + (srmIPList != null ? srmIPList.hashCode() : 0);
        result = 31 * result + (telemetryProfile != null ? telemetryProfile.hashCode() : 0);
        return result;
    }
}

