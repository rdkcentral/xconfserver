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
 * Created: 2/10/16  3:54 PM
 */
package com.comcast.xconf;

import com.comcast.apps.dataaccess.cache.dao.ChangedKeysProcessingDaoImpl;
import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.xconf.dcm.ruleengine.LogFileService;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.admin.contextconfig.TestContextConfig;
import com.comcast.xconf.admin.service.telemetry.PermanentTelemetryProfileService;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.PercentageBeanService;
import com.comcast.xconf.estbfirmware.SingletonFilterValue;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.logupload.*;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import com.comcast.xconf.service.firmware.ActivationVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestContextConfig.class})
public class BaseIntegrationTest {

    public static final HashSet<String> STB_PERMISSIONS = Sets.newHashSet("read-common", "write-common", "read-firmware-stb", "write-firmware-stb", "read-dcm-stb", "write-dcm-stb", "read-telemetry-stb", "write-telemetry-stb");
    public static final HashSet<String> XHOME_PERMISSIONS = Sets.newHashSet("read-firmware-xhome", "write-firmware-xhome", "read-dcm-xhome", "write-dcm-xhome", "read-telemetry-xhome", "write-telemetry-xhome");

    @Autowired
    protected WebApplicationContext wac;

    protected MockMvc mockMvc;

    @Autowired
    protected CachedSimpleDao<String, Model> modelDAO;

    @Autowired
    protected CachedSimpleDao<String, Environment> environmentDAO;

    @Autowired
    protected CachedSimpleDao<String, LogFile> logFileDAO;

    @Autowired
    protected CachedSimpleDao<String, LogFilesGroup> logFilesGroupDAO;

    @Autowired
    protected CachedSimpleDao<String, DCMGenericRule> dcmRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, UploadRepository> uploadRepositoryDAO;

    @Autowired
    protected CachedSimpleDao<String, LogUploadSettings> logUploadSettingsDAO;

    @Autowired
    protected CachedSimpleDao<String, DeviceSettings> deviceSettingsDAO;

    @Autowired
    protected CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    @Autowired
    protected CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    protected CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    protected CachedSimpleDao<String, VodSettings> vodSettingsDAO;

    @Autowired
    protected CachedSimpleDao<String, SettingProfile> settingProfileDao;

    @Autowired
    protected CachedSimpleDao<String, SettingRule> settingRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, PermanentTelemetryProfile> permanentTelemetryDAO;

    @Autowired
    protected CachedSimpleDao<String, TelemetryRule> telemetryRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, TelemetryTwoProfile> telemetryTwoProfileDAO;

    @Autowired
    protected CachedSimpleDao<String, TelemetryTwoRule> telemetryTwoRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, FeatureRule> featureRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, Feature> featureDAO;

    @Autowired
    protected CachedSimpleDao<String, GenericNamespacedList> genericNamespacedListDAO;

    @Autowired
    protected CachedSimpleDao<String, SingletonFilterValue> singletonFilterValueDAO;

    @Autowired
    protected LogFileService indexesLogFilesDAO;

    @Autowired
    protected GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    @Autowired
    protected ChangedKeysProcessingDaoImpl changeLogDao;

    @Autowired
    @Deprecated
    protected CachedSimpleDao<String, IpAddressGroupExtended> ipAddressGroupDAO;

    @Autowired
    @Deprecated
    protected CachedSimpleDao<String, NamespacedList> namespacedListDAO;

    @Autowired
    protected FirmwarePermissionService firmwarePermissionService;

    @Autowired
    protected DcmPermissionService dcmPermissionService;

    @Autowired
    private TelemetryPermissionService telemetryPermissionService;

    @Autowired
    protected ActivationVersionService activationVersionService;

    @Autowired
    protected PercentageBeanService percentageBeanService;

    @Autowired
    protected PermanentTelemetryProfileService telemetryProfileService;

    @Autowired
    protected SimpleDao<String, GenericNamespacedList> nonCachedNamespacedListDao;

    protected ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();
        when(firmwarePermissionService.getPermissions()).thenReturn(STB_PERMISSIONS);
        when(dcmPermissionService.getPermissions()).thenReturn(STB_PERMISSIONS);
        when(telemetryPermissionService.getPermissions()).thenReturn(STB_PERMISSIONS);

    }

    @Before
    @After
    public void cleanData() {
        List<? extends CachedSimpleDao<String, ? extends IPersistable>> daoList = Arrays.asList(
                modelDAO, environmentDAO, logFileDAO, logFilesGroupDAO, dcmRuleDAO, uploadRepositoryDAO,
                logUploadSettingsDAO, deviceSettingsDAO, firmwareRuleTemplateDao, firmwareConfigDAO,
                firmwareRuleDao, vodSettingsDAO, permanentTelemetryDAO, telemetryRuleDAO, featureRuleDAO,
                genericNamespacedListDAO, singletonFilterValueDAO, settingProfileDao, settingRuleDAO,
                ipAddressGroupDAO, namespacedListDAO,
                featureDAO, telemetryTwoProfileDAO, telemetryTwoRuleDAO
        );
        cleanData(daoList);
    }

    protected void cleanData(List<? extends CachedSimpleDao<String, ? extends IPersistable>> daoList) {
        for (CachedSimpleDao<String, ? extends IPersistable> dao : daoList) {
            for (IPersistable iPersistable : dao.getAll()) {
                dao.deleteOne(iPersistable.getId());
            }
        }
        for (GenericNamespacedList genericNamespacedList : nonCachedNamespacedListDao.getAll(Integer.MAX_VALUE)) {
            nonCachedNamespacedListDao.deleteOne(genericNamespacedList.getId());
        }

    }
}
