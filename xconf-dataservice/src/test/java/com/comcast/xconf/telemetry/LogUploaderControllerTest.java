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
 * Author: obaturynskyi
 * Created: 13.05.2015  14:02
 */
package com.comcast.xconf.telemetry;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.*;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.comcast.xconf.dcm.ruleengine.LogUploaderController;
import com.comcast.xconf.dcm.ruleengine.TelemetryProfileService;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.*;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.settings.SettingType;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.logupload.telemetry.TimestampedRule;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.codehaus.jackson.JsonProcessingException;
import com.google.common.collect.Sets;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.comcast.xconf.firmware.ApplicationType.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


public class LogUploaderControllerTest extends BaseQueriesControllerTest {
    private static final String MAPPING = "/loguploader/";

    @Autowired
    private CachedSimpleDao<TimestampedRule, TelemetryProfile> temporaryTelemetryProfileDAO;
    @Autowired
    private TelemetryProfileService telemetryProfileService;
    @Autowired
    private LogUploaderController logUploaderController;

    private static final String ID = "myId";


    @Autowired
    protected WebApplicationContext wac;

    protected MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @Before
    @After
    public void cleanData() throws NoSuchMethodException {
        super.cleanData();
        for (TimestampedRule rule : temporaryTelemetryProfileDAO.asLoadingCache().asMap().keySet()) {
            temporaryTelemetryProfileDAO.deleteOne(rule);
        }
    }

    @Test
    public void getSettingsReturnsEntityWhenCheckNowTrueTest() {
        TelemetryProfile telemetryProfile = new TelemetryProfile();
        telemetryProfile.setId(ID);
        telemetryProfile.setExpires(DateTime.now(DateTimeZone.UTC).getMillis());
        final String value = "1A:08:38:B9:CE:13";
        TimestampedRule timestampedRule = telemetryProfileService.createTelemetryProfile("estbMacAddress", value, telemetryProfile);
        assertNotNull(timestampedRule);
        TelemetryProfile telemetryProfileResponse = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(timestampedRule).orNull();
        assertNotNull(telemetryProfileResponse);
        assertEquals(telemetryProfile, telemetryProfileResponse);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.10.10");
        request.addHeader("X-Forwarded-For", "30.30.30.30");
        final Map<String, String> context = new HashMap<String, String>(){{
            put(LogUploaderContext.ESTB_MAC, value);
        }};
        ResponseEntity responseEntity = logUploaderController.getSettings(request, true, "1.0", Sets.newHashSet("any"), context);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(telemetryProfile, responseEntity.getBody());
    }

    @Test
    public void getSettingsReturns404WhenCheckNowTrueWhenProfileExpiredTest() throws InterruptedException {
        TelemetryProfile telemetryProfile = new TelemetryProfile();
        telemetryProfile.setId(ID);
        telemetryProfile.setExpires(0);
        final String value = "1A:08:38:B9:CE:13";
        TimestampedRule timestampedRule = telemetryProfileService.createTelemetryProfile("estbMacAddress", value, telemetryProfile);
        assertNotNull(timestampedRule);
        TelemetryProfile telemetryProfileResponse = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(timestampedRule).orNull();
        assertNotNull(telemetryProfileResponse);
        assertEquals(telemetryProfile, telemetryProfileResponse);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.10.10");
        request.addHeader("X-Forwarded-For", "30.30.30.30");
        final Map<String, String> context = new HashMap<String, String>(){{
            put(LogUploaderContext.ESTB_MAC, value);
        }};

        Thread.sleep(CompleteTestSuite.timeToWaitExpiration);

        ResponseEntity responseEntity = logUploaderController.getSettings(request, true, "1.0", Sets.newHashSet("any"), context);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("<h2>404 NOT FOUND</h2><div> telemetry profile not found</div>", responseEntity.getBody());
    }

    @Test
    public void getSettingsReturnsSettingsWithPermanentProfileWhenCheckNowFalseTest() {
        final String mac = "1A:08:38:B9:CE:13";
        TelemetryRule rule = new TelemetryRule();
        rule.setId(ID);
        rule.setName("myRuleName");
        String boundId = "myBoundTelemetryId";
        rule.setBoundTelemetryId(boundId);
        Condition condition = new Condition(new FreeArg(StandardFreeArgType.STRING, "estbMacAddress"), StandardOperation.IS, FixedArg.from(mac));
        rule.setCondition(condition);
        telemetryRuleDAO.setOne(rule.getId(), rule);
        PermanentTelemetryProfile permanentTelemetryProfile = new PermanentTelemetryProfile();
        permanentTelemetryProfile.setId(boundId);
        permanentTelemetryProfile.setName("myPermanentTelemetryProfileName");
        permanentTelemetryDAO.setOne(boundId, permanentTelemetryProfile);
        DCMGenericRule dcmRule = new DCMGenericRule();
        dcmRule.setId("formulaId");
        dcmRule.setCondition(condition);
        dcmRuleDAO.setOne(dcmRule.getId(), dcmRule);
        String ip = "11.11.11.11";
        IpAddressGroupExtended ipAddressGroupExtended = new IpAddressGroupExtended();
        ipAddressGroupExtended.setId(ip);
        ipAddressGroupExtended.setName("ipAddressGroupExtendedName");
        Set<IpAddress> addresses = new HashSet<>();
        addresses.add(new IpAddress(ip));
        ipAddressGroupExtended.setIpAddresses(addresses);
        GenericNamespacedList ipList = GenericNamespacedListsConverter.convertFromIpAddressGroupExtended(ipAddressGroupExtended);
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);
        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setId(mac);
        Set<String> namespacedListDataSet = new HashSet<>();
        namespacedListDataSet.add(mac);
        namespacedList.setData(namespacedListDataSet);
        GenericNamespacedList macList = GenericNamespacedListsConverter.convertFromNamespacedList(namespacedList);
        genericNamespacedListDAO.setOne(macList.getId(), macList);
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId(dcmRule.getId());
        deviceSettings.setName("deviceSettingsName");
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setCheckOnReboot(true);
        deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId(dcmRule.getId());
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettingsDAO.setOne(logUploadSettings.getId(), logUploadSettings);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.10.10");
        request.addHeader("X-Forwarded-For", ip);
        final Map<String, String> context = new HashMap<String, String>(){{
            put(LogUploaderContext.ESTB_MAC, mac);
            put(LogUploaderContext.ECM_MAC, mac);
        }};

        ResponseEntity responseEntity = logUploaderController.getSettings(request, false, "1.0", Sets.newHashSet("any"), context);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Settings responseSettings = (Settings) responseEntity.getBody();
        assertEquals(deviceSettings.getName(), responseSettings.getGroupName());
        assertEquals(1, responseSettings.getRuleIDs().size());
        assertEquals(dcmRule.getId(), responseSettings.getRuleIDs().toArray()[0]);
        assertEquals(permanentTelemetryProfile, responseSettings.getTelemetryProfile());
    }

    @Test
    public void testTempThenPermTelemetryForCheckNow() throws Exception{
        String permanentRuleID = UUID.randomUUID().toString();
        final String mac = "00:1C:B3:09:85:15";
        String ip = "11.11.11.11";
        final PermanentTelemetryProfile permanentTelemetryProfile = (PermanentTelemetryProfile) createTelemetry("ptp1", System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2));
        final TelemetryProfile temporaryTelemetryProfile = createTelemetry("ttp1", System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2));
        final TimestampedRule permanentRule = createRule(System.currentTimeMillis(), "estbMacAddress", "00:1C:B3:09:85:15");
        final TelemetryRule permanentTelemetryRule  = new TelemetryRule();
        Rule.copy(permanentRule, permanentTelemetryRule);
        permanentTelemetryRule.setBoundTelemetryId(permanentRuleID);
        permanentTelemetryRule.setId(permanentRuleID);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.10.10");
        request.addHeader("X-Forwarded-For", ip);
        final Map<String, String> context = new HashMap<String, String>(){{
            put(LogUploaderContext.ESTB_MAC, mac);
            put(LogUploaderContext.ECM_MAC, mac);
        }};

        telemetryProfileService.createTelemetryProfile("estbMacAddress", "00:1C:B3:09:85:15", temporaryTelemetryProfile);
        telemetryRuleDAO.setOne(permanentRuleID, permanentTelemetryRule);
        permanentTelemetryDAO.setOne(permanentRuleID, permanentTelemetryProfile);

        final ResponseEntity response = logUploaderController.getSettings(request, true, "2", Sets.newHashSet("any"), context);
        assertEquals(((TelemetryProfile)response.getBody()).getName(), "ttp1");

        final ResponseEntity responsePermanentTelemetry = logUploaderController.getSettings(request, true, "2", Sets.newHashSet("any"), context);
        assertEquals(((TelemetryProfile)responsePermanentTelemetry.getBody()).getName(), "ptp1");

        telemetryRuleDAO.deleteOne(permanentRuleID);
        permanentTelemetryDAO.deleteOne(permanentRuleID);

        final ResponseEntity response404 = logUploaderController.getSettings(request, true, "2", Sets.newHashSet("any"), context);

        assertEquals(response404.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void checkSettingsFieldMissing() throws Exception {
        DCMGenericRule dcmGenericRule = createDcmGenericRule();
        dcmRuleDAO.setOne(dcmGenericRule.getId(), dcmGenericRule);
        Model model = new Model();
        model.setId("MODELID");
        modelDAO.setOne(model.getId(), model);
        VodSettings vodSettings = createVodSettings();
        vodSettingsDAO.setOne(vodSettings.getId(), vodSettings);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.10.10");
        request.addHeader("X-Forwarded-For", "11.11.11.11");
        Map<String, String> context = new HashMap<>(Collections.singletonMap("model", "MODELID"));
        ResponseEntity response = logUploaderController.getSettings(request, null, null, null, context);
        Settings settings = (Settings) response.getBody();

        assertNull(settings.getScheduleStartDate());
        assertNull(settings.getScheduleEndDate());
        assertNull(settings.getLusLogFiles());
        assertNull(settings.getLusLogFilesStartDate());
        assertNull(settings.getLusLogFilesEndDate());
        assertNull(settings.getLusScheduleStartDate());
        assertNull(settings.getLusScheduleEndDate());
    }

    @Test
    public void getSettingsWithEponAndPartnerSettingTypes() throws Exception {
        createAndSaveEponSetting();
        createAndSavePartnerSetting();

        mockMvc.perform(
                get(MAPPING + "getSettings?version=2.1&settingType=epon&test_key=test_value&test2_key=test2_value&settingType=partnersettings")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("urn:settings:SettingType:epon.*", hasSize(1))
                )
                .andExpect(
                        jsonPath("urn:settings:SettingType:partnersettings.*", hasSize(1))
                )
                .andReturn();

    }

    @Test
    public void getSettingsWithEponSettingType() throws Exception {
        createAndSaveEponSetting();

        mockMvc.perform(
                get(MAPPING + "getSettings?version=2.1&settingType=epon&test_key=test_value&test2_key=test2_value")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("urn:settings:SettingType:epon.*", hasSize(1))
                )
                .andExpect(
                        jsonPath("$", Matchers.not(Matchers.hasKey("urn:settings:SettingType:partnersettings")))
                )
                .andReturn();
    }

    @Test
    public void getSettingsWithPartnerSettingType() throws Exception {
        createAndSavePartnerSetting();

        mockMvc.perform(
                get(MAPPING + "getSettings?version=2.1&test_key=test_value&test2_key=test2_value&settingType=partnersettings")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$", Matchers.not(Matchers.hasKey("urn:settings:SettingType:epon")))
                )
                .andExpect(
                        jsonPath("urn:settings:SettingType:partnersettings.*", hasSize(1))
                )
                .andReturn();
    }

    @Test
    public void getSettingsWithoutTelemetryProfileApplicationType() throws Exception {
        DCMGenericRule dcmGenericRule = createDcmGenericRule();
        dcmRuleDAO.setOne(dcmGenericRule.getId(), dcmGenericRule);

        initDcmSettings(dcmGenericRule, STB);

        mockMvc.perform(get(MAPPING + "getSettings?model=MODELID")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("urn:settings:TelemetryProfile.applicationType").doesNotExist());
    }

    @Test
    public void getSettingsWhenPartnerIsCaseInsensitive() throws Exception {
        DCMGenericRule dcmGenericRule = createDcmGenericRule();
        dcmGenericRule.setCondition(createPartnerRule("PARTNERID").getCondition());
        dcmRuleDAO.setOne(dcmGenericRule.getId(), dcmGenericRule);

        initDcmSettings(dcmGenericRule, STB);

        assertSettingByPartnerRequest(MAPPING + "getSettings?partnerId=PartnerId");
        assertSettingByPartnerRequest(MAPPING + "getSettings?partnerId=partnerid");
        assertSettingByPartnerRequest(MAPPING + "getSettings?partnerId=PARTNERID");
    }

    @Test
    public void getSettingIsNotReturnIdAndComponentOfTelemetryElement() throws Exception {
        DCMGenericRule dcmGenericRule = createDcmGenericRule();
        dcmRuleDAO.setOne(dcmGenericRule.getId(), dcmGenericRule);

        initDcmSettings(dcmGenericRule, STB);

        mockMvc.perform(get(MAPPING + "getSettings").param(LogUploaderContext.MODEL, "MODELID")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urn:settings:TelemetryProfile.telemetryProfile[:1].id").doesNotExist())
                .andExpect(jsonPath("$.urn:settings:TelemetryProfile.telemetryProfile[:1].component").doesNotExist())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void verifyTelemetry2Response() throws Exception {
        DCMGenericRule dcmGenericRule = createDcmGenericRule();
        dcmRuleDAO.setOne(dcmGenericRule.getId(), dcmGenericRule);

        initDcmSettings(dcmGenericRule, STB);

        mockMvc.perform(get(MAPPING + "getT2Settings").param(LogUploaderContext.MODEL, "MODELID")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urn:settings:TelemetryProfile.telemetryProfile[:1].type").value("<event>"))
                .andExpect(jsonPath("$.urn:settings:TelemetryProfile.telemetryProfile[:1].content").value(getComponentOfExistingTelemetryProfile()))
                .andExpect(jsonPath("$.urn:settings:TelemetryProfile.telemetryProfile[:1].component").doesNotExist());
    }

    @Test
    public void verifyTelemetry2ResponseByStbApplication() throws Exception {
        verifyT2ResponseByApplication(STB);
    }

    @Test
    public void verifyTelemetry2ResponseByXhomeApplication() throws Exception {
        verifyT2ResponseByApplication(XHOME);
    }

    @Test
    public void verifyTelemetry2ResponseByRdcCloudApplication() throws Exception {
        verifyT2ResponseByApplication(RDKCLOUD);
    }

    @Test
    public void getTelemetryTwoProfilesTest() throws Exception {
        TelemetryTwoProfile telemetryTwoProfile = createTelemetryTwoProfile("ID", "TestProfile");
        telemetryTwoProfileDAO.setOne(telemetryTwoProfile.getId(), telemetryTwoProfile);

        final String model = "TEST";
        TelemetryTwoRule telemetryTwoRule = new TelemetryTwoRule();
        telemetryTwoRule.setId(ID);
        telemetryTwoRule.setName("myRuleName");
        List<String> boundTelemetryIds = new ArrayList<>();
        boundTelemetryIds.add(telemetryTwoProfile.getId());
        telemetryTwoRule.setBoundTelemetryIds(boundTelemetryIds);

        telemetryTwoRule.setCondition(BaseTestUtils.createCondition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from(model)));
        telemetryTwoRuleDAO.setOne(telemetryTwoRule.getId(), telemetryTwoRule);

        assertNotNull(telemetryTwoRule);
        assertNotNull(telemetryTwoProfile);

        mockMvc.perform(
                get(MAPPING + "getTelemetryProfiles?model=TEST")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk());
    }

    @Test
    public void getTelemetryTwoProfilesNotFoundTest() throws Exception {
        TelemetryTwoProfile telemetryTwoProfile = createTelemetryTwoProfile("ID", "TestProfile");
        telemetryTwoProfileDAO.setOne(telemetryTwoProfile.getId(), telemetryTwoProfile);

        final String model = "TEST";
        TelemetryTwoRule telemetryTwoRule = new TelemetryTwoRule();
        telemetryTwoRule.setId(ID);
        telemetryTwoRule.setName("myRuleName");
        List<String> boundTelemetryIds = new ArrayList<>();
        boundTelemetryIds.add(telemetryTwoProfile.getId());
        telemetryTwoRule.setBoundTelemetryIds(boundTelemetryIds);

        telemetryTwoRule.setCondition(BaseTestUtils.createCondition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from(model)));
        telemetryTwoRuleDAO.setOne(telemetryTwoRule.getId(), telemetryTwoRule);

        assertNotNull(telemetryTwoRule);
        assertNotNull(telemetryTwoProfile);

        mockMvc.perform(
                get(MAPPING + "getTelemetryProfiles?model=NOMATCH")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
    }

    private void verifyT2ResponseByApplication(String applicationType) throws Exception {
        DCMGenericRule dcmGenericRule = createDcmGenericRule();
        dcmGenericRule.setApplicationType(applicationType);
        dcmRuleDAO.setOne(dcmGenericRule.getId(), dcmGenericRule);

        initDcmSettings(dcmGenericRule, applicationType);

        mockMvc.perform(get(MAPPING + "getT2Settings/" + applicationType).param(LogUploaderContext.MODEL, "MODELID")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urn:settings:TelemetryProfile.telemetryProfile[:1].type").value("<event>"))
                .andExpect(jsonPath("$.urn:settings:TelemetryProfile.telemetryProfile[:1].content").value(getComponentOfExistingTelemetryProfile()))
                .andExpect(jsonPath("$.urn:settings:TelemetryProfile.telemetryProfile[:1].component").doesNotExist());
    }

    private String getComponentOfExistingTelemetryProfile() {
        TelemetryProfile telemetryProfile = permanentTelemetryDAO.getAll().get(0);
        return telemetryProfile.getTelemetryProfile().get(0).getComponent();
    }

    private void assertSettingByPartnerRequest(String url) throws Exception {
        mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("urn:settings:GroupName").value(defaultDeviceSettingName))
                .andExpect(jsonPath("urn:settings:LogUploadSettings:Name").value(defaultLogUploadSettingName));
    }

    private void initDcmSettings(DCMGenericRule dcmGenericRule, String applicationType) {
        DeviceSettings deviceSettings = createDeviceSettings(dcmGenericRule.getId());
        deviceSettings.setApplicationType(applicationType);
        deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);

        UploadRepository uploadRepository = createUploadRepository();
        uploadRepository.setApplicationType(applicationType);
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);

        LogUploadSettings logUploadSettings = createLogUploadSettings(dcmGenericRule.getId(), uploadRepository.getId());
        logUploadSettings.setApplicationType(applicationType);
        logUploadSettingsDAO.setOne(logUploadSettings.getId(), logUploadSettings);

        PermanentTelemetryProfile telemetryProfile = createPermanentTelemetryProfile();
        telemetryProfile.setApplicationType(applicationType);
        permanentTelemetryDAO.setOne(telemetryProfile.getId(), telemetryProfile);

        TelemetryRule telemetryRule = createTelemetryRule(telemetryProfile.getId(), dcmGenericRule.getCondition());
        telemetryRule.setApplicationType(applicationType);
        telemetryRuleDAO.setOne(telemetryRule.getId(), telemetryRule);
    }

    private void createAndSaveEponSetting() {
        final SettingProfile eponSettingProfile = new SettingProfile();
        eponSettingProfile.setId(UUID.randomUUID().toString());
        eponSettingProfile.setSettingProfileId("profile_epon");
        eponSettingProfile.setSettingType(SettingType.EPON);

        final Map<String, String> properties = new HashMap<>();
        properties.put("epon_property_key", "epon_property_value");

        eponSettingProfile.setProperties(properties);
        settingProfileDao.setOne(eponSettingProfile.getId(), eponSettingProfile);

        final SettingRule eponSettingRule = new SettingRule();
        eponSettingRule.setId(UUID.randomUUID().toString());
        eponSettingRule.setName("epon_setting_rule");
        eponSettingRule.setBoundSettingId(eponSettingProfile.getId());
        eponSettingRule.setRule(
                Rule.Builder.create().and(
                        new Condition(
                                new FreeArg(StandardFreeArgType.STRING, "test_key"),
                                StandardOperation.IS,
                                FixedArg.from("test_value")
                        )
                ).build()
        );
        settingRuleDAO.setOne(eponSettingRule.getId(), eponSettingRule);
    }

    private void createAndSavePartnerSetting() {
        final SettingProfile partnerSettingsSettingProfile = new SettingProfile();
        partnerSettingsSettingProfile.setId("profile_partnersettings");
        partnerSettingsSettingProfile.setSettingType(SettingType.PARTNER_SETTINGS);

        final Map<String, String> properties = new HashMap<>();
        properties.put("partnersettings_property_key", "partnersettings_property_value");

        partnerSettingsSettingProfile.setProperties(properties);
        settingProfileDao.setOne(partnerSettingsSettingProfile.getId(), partnerSettingsSettingProfile);

        final SettingRule partnerSettingsSettingRule = new SettingRule();
        partnerSettingsSettingRule.setId(UUID.randomUUID().toString());
        partnerSettingsSettingRule.setName("epon_setting_rule");
        partnerSettingsSettingRule.setBoundSettingId(partnerSettingsSettingProfile.getId());
        partnerSettingsSettingRule.setRule(
                Rule.Builder.create().and(
                        new Condition(
                                new FreeArg(StandardFreeArgType.STRING, "test2_key"),
                                StandardOperation.IS,
                                FixedArg.from("test2_value")
                        )
                ).build()
        );
        settingRuleDAO.setOne(partnerSettingsSettingRule.getId(), partnerSettingsSettingRule);
    }

    private TimestampedRule createRule(long timestamp, String contextKey, String value) {
        final TimestampedRule rule = new TimestampedRule();
        com.comcast.apps.hesperius.ruleengine.main.impl.Rule.Builder.of(new Condition(new FreeArg(StandardFreeArgType.STRING, contextKey), StandardOperation.IS, FixedArg.from(value))).copyTo(rule);
        rule.setTimestamp(timestamp);
        return rule;
    }

    private TelemetryProfile createTelemetry(String name, long expires) {
        final TelemetryProfile telemetryProfile = new PermanentTelemetryProfile();
        telemetryProfile.setExpires(expires);
        telemetryProfile.setName(name);
        telemetryProfile.setSchedule("*");
        telemetryProfile.setUploadProtocol(UploadProtocol.S3);
        telemetryProfile.setUploadRepository("s3://repo1.local");
        telemetryProfile.setTelemetryProfile(new ArrayList<TelemetryProfile.TelemetryElement>());
        return telemetryProfile;
    }

    private DCMGenericRule createDcmGenericRule() {
        DCMGenericRule rule = new DCMGenericRule();
        rule.setId("id123");
        rule.setName("name");
        rule.setPercentage(100);
        rule.setCondition(BaseTestUtils.createCondition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from("MODELID")));
        rule.setRuleExpression("model");
        return rule;
    }

    private DCMGenericRule createDcmGenericRule(String id, Condition condition) {
        DCMGenericRule rule = new DCMGenericRule();
        rule.setId(id);
        rule.setCondition(condition);
        return rule;
    }

    private VodSettings createVodSettings() {
        VodSettings vodSettings = new VodSettings();
        vodSettings.setId("id123");
        vodSettings.setName("vodSettings");
        vodSettings.setLocationsURL("http://foo.com");
        vodSettings.setSrmIPList(new HashMap<String, String>());
        return vodSettings;
    }

    private TelemetryTwoProfile createTelemetryTwoProfile(String id, String name) {
        TelemetryTwoProfile telemetryTwoprofile = new TelemetryTwoProfile();
        telemetryTwoprofile.setId(id);
        telemetryTwoprofile.setName(name);
        telemetryTwoprofile.setJsonconfig("{\n" +
                "    \"Description\": \"Telemetry 2.0 test\",\n" +
                "    \"Version\": \"0.2\",\n" +
                "    \"Protocol\": \"HTTP\",\n" +
                "    \"EncodingType\": \"JSON\",\n" +
                "    \"ReportingInterval\": 180,\n" +
                "    \"TimeReference\": \"0001-01-01T00:00:00Z\",\n" +
                "    \"Parameter\": [{\n" +
                "        \"type\": \"dataModel\",\n" +
                "        \"name\": \"TestMac\",\n" +
                "        \"reference\": \"Device.ABC\"\n" +
                "    }],\n" +
                "    \"HTTP\": {\n" +
                "        \"URL\": \"https://test.com/\",\n" +
                "        \"Compression\": \"None\",\n" +
                "        \"Method\": \"POST\",\n" +
                "        \"RequestURIParameter\": [{\n" +
                "            \"Name\": \"profileName\",\n" +
                "            \"Reference\": \"Test.Name\"\n" +
                "        }, {\n" +
                "            \"Name\": \"testVersion\",\n" +
                "            \"Reference\": \"Test.Version\"\n" +
                "        }]\n" +
                "    },\n" +
                "    \"JSONEncoding\": {\n" +
                "        \"ReportFormat\": \"NameValuePair\",\n" +
                "        \"ReportTimestamp\": \"None\"\n" +
                "    }\n" +
                "}");
        telemetryTwoprofile.setApplicationType(STB);
        return telemetryTwoprofile;
    }
}
