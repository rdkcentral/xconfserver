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
package com.comcast.xconf.contextconfig;

import com.comcast.apps.dataaccess.cache.DaoFactory;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.xconf.Environment;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.NamespacedList;
import com.comcast.xconf.change.ApprovedChange;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.logupload.*;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.telemetry.*;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatastoreContext {

    @Autowired
    private DaoFactory daoFactory;

    @Bean
    CachedSimpleDao<String, LogFile> logFileDAO() {
        return daoFactory.createCachedSimpleDao(String.class, LogFile.class);
    }

    @Bean
    CachedSimpleDao<String, Feature> featureDAO() {
        return daoFactory.createCachedSimpleDao(String.class, Feature.class);
    }

    @Bean
    CachedSimpleDao<String, FeatureRule> featureRuleDAO() {
        return daoFactory.createCachedSimpleDao(String.class, FeatureRule.class);
    }

    @Bean
    CachedSimpleDao<String, com.comcast.xconf.firmware.FirmwareRule> firmwareRuleDao() {
        return daoFactory.createCachedSimpleDao(String.class, com.comcast.xconf.firmware.FirmwareRule.class);
    }

    @Bean
    CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao() {
        return daoFactory.createCachedSimpleDao(String.class, FirmwareRuleTemplate.class);
    }

    @Bean
    CachedSimpleDao<String, Model> modelDAO() {
        return daoFactory.createCachedSimpleDao(String.class, Model.class);
    }

    @Bean
    CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO() {
        return daoFactory.createCachedSimpleDao(String.class, FirmwareConfig.class);
    }

    @Bean
    CachedSimpleDao<String, LogUploadSettings> logUploadSettingsDAO() {
        return daoFactory.createCachedSimpleDao(String.class, LogUploadSettings.class);
    }

    @Bean
    CachedSimpleDao<String, LogFilesGroup> logFilesGroupDAO() {
        return daoFactory.createCachedSimpleDao(String.class, LogFilesGroup.class);
    }

    @Bean
    CachedSimpleDao<String, LogFileList> logFileListDAO() {
        return daoFactory.createCachedSimpleDao(String.class, LogFileList.class);
    }

    @Bean
    CachedSimpleDao<String, IpAddressGroupExtended> ipAddressGroupDAO() {
        return daoFactory.createCachedSimpleDao(String.class, IpAddressGroupExtended.class);
    }

    @Bean
    CachedSimpleDao<String, Environment> environmentDAO() {
        return daoFactory.createCachedSimpleDao(String.class, Environment.class);
    }

    @Bean
    CachedSimpleDao<String, VodSettings> vodSettingsDAO() {
        return daoFactory.createCachedSimpleDao(String.class, VodSettings.class);
    }

    @Bean
    CachedSimpleDao<String, UploadRepository> uploadRepositoryDAO() {
        return daoFactory.createCachedSimpleDao(String.class, UploadRepository.class);
    }

    @Bean
    CachedSimpleDao<String, DeviceSettings> deviceSettingsDAO() {
        return daoFactory.createCachedSimpleDao(String.class, DeviceSettings.class);
    }

    @Bean
    CachedSimpleDao<String, SingletonFilterValue> singletonFilterValueDAO() {
        return daoFactory.createCachedSimpleDao(String.class, SingletonFilterValue.class);
    }

    @Bean
    ListingDao<String, String, LastConfigLog> lastConfigLogDAO() {
        return daoFactory.createListingDao(LastConfigLog.class);
    }

    @Bean
    ListingDao<String, String, ConfigChangeLog> configChangeLogDAO() {
        return daoFactory.createListingDao(ConfigChangeLog.class);
    }

    @Bean
    CachedSimpleDao<String, NamespacedList> namespacedListDAO() {
        return daoFactory.createCachedSimpleDao(String.class, NamespacedList.class);
    }

    @Bean
    CachedSimpleDao<String, GenericNamespacedList> genericNamespacedListDAO() {
        return daoFactory.createCachedSimpleDao(String.class, GenericNamespacedList.class);
    }

    @Bean
    SimpleDao<String, GenericNamespacedList> nonCachedNamespacedListDao() {
        return daoFactory.createSimpleDao(GenericNamespacedList.class);
    }

    @Bean
    CachedSimpleDao<TimestampedRule, TelemetryProfile> temporaryTelemetryProfileDAO() {
        return daoFactory.createCachedSimpleDao(TimestampedRule.class, TelemetryProfile.class);
    }

    @Bean
    CachedSimpleDao<String, PermanentTelemetryProfile> permanentTelemetryDAO() {
        return daoFactory.createCachedSimpleDao(String.class, PermanentTelemetryProfile.class);
    }

    @Bean
    CachedSimpleDao<String, TelemetryRule> telemetryRuleDAO() {
        return daoFactory.createCachedSimpleDao(String.class, TelemetryRule.class);
    }

    @Bean
    CachedSimpleDao<String, DCMGenericRule> dcmRuleDAO() {
        return daoFactory.createCachedSimpleDao(String.class, DCMGenericRule.class);
    }

    @Bean
    CachedSimpleDao<String, SettingProfile> settingProfileDao() {
        return daoFactory.createCachedSimpleDao(String.class, SettingProfile.class);
    }

    @Bean
    CachedSimpleDao<String, SettingRule> settingRuleDAO() {
        return daoFactory.createCachedSimpleDao(String.class, SettingRule.class);
    }

    @Bean
    SimpleDao<String, Change> changeDao() {
        return daoFactory.createSimpleDao(Change.class);
    }

    @Bean
    SimpleDao<String, ApprovedChange> approvedDao() {
        return daoFactory.createSimpleDao(ApprovedChange.class);
    }

    @Bean
    CachedSimpleDao<String, TelemetryTwoProfile> telemetryTwoProfileDAO() {
        return daoFactory.createCachedSimpleDao(String.class, TelemetryTwoProfile.class);
    }

    @Bean
    CachedSimpleDao<String, TelemetryTwoRule> telemetryTwoRuleDAO() {
        return daoFactory.createCachedSimpleDao(String.class, TelemetryTwoRule.class);
    }
}
