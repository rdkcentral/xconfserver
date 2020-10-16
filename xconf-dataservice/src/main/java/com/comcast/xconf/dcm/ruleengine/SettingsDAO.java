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
 * Created: 6/20/14
 */
package com.comcast.xconf.dcm.ruleengine;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.DaoUtil;
import com.comcast.xconf.logupload.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SettingsDAO {

    @Autowired
    private CachedSimpleDao<String, DeviceSettings> deviceSettingsDAO;
    @Autowired
    private CachedSimpleDao<String, LogUploadSettings> logUploadSettingsDAO;
    @Autowired
    private LogFileService indexesLogFilesDAO;
    @Autowired
    private CachedSimpleDao<String, LogFile> logFileDAO;
    @Autowired
    private CachedSimpleDao<String, UploadRepository> uploadRepositoryDAO;
    @Autowired
    private CachedSimpleDao<String, VodSettings> vodSettingsDAO;

    public Settings get(String id) {
        Settings settings = new Settings();
        DeviceSettings deviceSettings = DaoUtil.getFromCache(deviceSettingsDAO, id);
        LogUploadSettings logUploadSettings = DaoUtil.getFromCache(logUploadSettingsDAO, id);
        if (deviceSettings != null && deviceSettings.getSettingsAreActive() &&
                logUploadSettings != null && logUploadSettings.getAreSettingsActive()) {
            settings.setGroupName(deviceSettings.getName());
            settings.setCheckOnReboot(deviceSettings.getCheckOnReboot());

            ConfigurationServiceURL configurationServiceURL = deviceSettings.getConfigurationServiceURL();
            if (configurationServiceURL != null) {
                settings.setConfigurationServiceURL(configurationServiceURL.getUrl());
            }

            Schedule checkSchedule = deviceSettings.getSchedule();
            settings.setScheduleCron(checkSchedule.getExpression());
            settings.setScheduleDurationMinutes(checkSchedule.getTimeWindowMinutes());
            settings.setLusName(logUploadSettings.getName());
            settings.setLusNumberOfDay(logUploadSettings.getNumberOfDays());

            String uploadRepositoryId = logUploadSettings.getUploadRepositoryId();
            if (uploadRepositoryId != null && !uploadRepositoryId.isEmpty()) {
                UploadRepository uploadRepository = uploadRepositoryDAO.getOne(uploadRepositoryId);
                if (uploadRepository != null) {
                    settings.setLusUploadRepositoryName(uploadRepository.getName());
                    settings.setLusUploadRepositoryURL(buildUrl(uploadRepository.getProtocol(), uploadRepository.getUrl()));
                    settings.setLusUploadRepositoryURLNew(uploadRepository.getUrl());
                    settings.setLusUploadRepositoryUploadProtocol(uploadRepository.getProtocol().name());
                }
            }
            settings.setLusUploadOnReboot(logUploadSettings.getUploadOnReboot());

            if (logUploadSettings.getModeToGetLogFiles() != null) {
                List<LogFile> listLogFilesForLogUplSettings = null;
                if (logUploadSettings.getModeToGetLogFiles().equals(LogUploadSettings.MODE_TO_GET_LOG_FILES[0])) {
                    listLogFilesForLogUplSettings = indexesLogFilesDAO.getAll(logUploadSettings.getId(), Integer.MAX_VALUE/100);
                } else if (logUploadSettings.getModeToGetLogFiles().equals(LogUploadSettings.MODE_TO_GET_LOG_FILES[1])) {
                    String keyFileGroup = logUploadSettings.getLogFilesGroupId();
                    if (keyFileGroup != null && !keyFileGroup.isEmpty()) {
                        listLogFilesForLogUplSettings = indexesLogFilesDAO.getAll(keyFileGroup, Integer.MAX_VALUE/100);
                    }
                } else if (logUploadSettings.getModeToGetLogFiles().equals(LogUploadSettings.MODE_TO_GET_LOG_FILES[2])) {
                    listLogFilesForLogUplSettings = logFileDAO.getAll();
                }

                settings.setLusLogFiles(listLogFilesForLogUplSettings);
            }

            Schedule uploadSchedule = logUploadSettings.getSchedule();

            settings.setLusScheduleCron(uploadSchedule.getExpression());
            settings.setLusScheduleCronL1(uploadSchedule.getExpressionL1());
            settings.setLusScheduleCronL2(uploadSchedule.getExpressionL2());
            settings.setLusScheduleCronL3(uploadSchedule.getExpressionL3());

            settings.setLusScheduleDurationMinutes(uploadSchedule.getTimeWindowMinutes());
            settings.setSchedulerType(uploadSchedule.getType());
        }

        VodSettings vodSettings = DaoUtil.getFromCache(vodSettingsDAO, id);
        if (vodSettings != null) {
            settings.setVodSettingsName(vodSettings.getName());
            settings.setLocationUrl(vodSettings.getLocationsURL());
            settings.setSrmIPList(vodSettings.getSrmIPList());
        }
        return settings;
    }

    public static String buildUrl(UploadProtocol protocol, String host) {
        return (host.contains("://") || protocol == null) ? host : protocol.toString().toLowerCase() + "://"  + host;
    }
}
