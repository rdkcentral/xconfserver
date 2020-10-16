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
 * Author: pbura
 * Created: 15/06/2015  15:40
 */

package com.comcast.xconf.dcm.ruleengine;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.logupload.UploadProtocol;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.logupload.telemetry.TimestampedRule;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation.IS;
import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;


public class TelemetryProfileControllerTest extends BaseQueriesControllerTest {

    @Autowired
    private CachedSimpleDao<TimestampedRule, TelemetryProfile> temporaryTelemetryProfileDAO;
    @Autowired
    private TelemetryProfileController telemetryProfileController;

    @Before
    @After
    public void cleanData() throws NoSuchMethodException {
        super.cleanData();
        Set<TimestampedRule> keys = temporaryTelemetryProfileDAO.asLoadingCache().asMap().keySet();
        if (keys != null) {
            for (TimestampedRule key : keys) {
                temporaryTelemetryProfileDAO.deleteOne(key);
            }
        }
    }

    @Test
    public void testCreateTelemetryEntryFor() throws Exception {
        final ResponseEntity re = telemetryProfileController.createTelemetryEntryFor("test","test-value", createTelemetry("tp1", System.currentTimeMillis()+ TimeUnit.HOURS.toMillis(2)));
        Assert.assertEquals(re.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getAvailableDescriptorsByApplicationType() throws Exception {
        Map<String, PermanentTelemetryProfile> telemetryProfiles = createAndSavePermanentTelemetryProfiles(STB, XHOME);
        String url = "/telemetry/getAvailableTelemetryDescriptors";

        performGetWithApplication(url, "", Collections.singletonList(TelemetryProfileService.convertToprofileDescriptor.apply(telemetryProfiles.get(STB))));

        performGetWithApplication(url, STB, Collections.singletonList(TelemetryProfileService.convertToprofileDescriptor.apply(telemetryProfiles.get(STB))));

        performGetWithApplication(url, XHOME, Collections.singletonList(TelemetryProfileService.convertToprofileDescriptor.apply(telemetryProfiles.get(XHOME))));
    }

    @Test
    public void getAvailableTelemetryRuleDescriptorsByApplicationType() throws Exception {
        Map<String, PermanentTelemetryProfile> telemetryProfiles = createAndSavePermanentTelemetryProfiles(STB, XHOME);
        Map<String, TelemetryRule> telemetryRules = createAndSaveTelemetryRules(STB, XHOME, telemetryProfiles);

        String url = "/telemetry/getAvailableRuleDescriptors";

        performGetWithApplication(url, "", Collections.singletonList(TelemetryProfileService.convertToDescriptor.apply(telemetryRules.get(STB))));

        performGetWithApplication(url, STB, Collections.singletonList(TelemetryProfileService.convertToDescriptor.apply(telemetryRules.get(STB))));

        performGetWithApplication(url, XHOME, Collections.singletonList(TelemetryProfileService.convertToDescriptor.apply(telemetryRules.get(XHOME))));
    }

    private TimestampedRule createRule(long timestamp, String contextKey,String value) {
        final TimestampedRule rule = new TimestampedRule();
        com.comcast.apps.hesperius.ruleengine.main.impl.Rule.Builder.of(new Condition(new FreeArg(StandardFreeArgType.STRING, contextKey), IS, FixedArg.from(value))).copyTo(rule);
        rule.setTimestamp(timestamp);
        return rule;
    }

    private TelemetryProfile createTelemetry(String name, long expires) {
        final TelemetryProfile telemetryProfile = new TelemetryProfile();
        telemetryProfile.setExpires(expires);
        telemetryProfile.setName(name);
        telemetryProfile.setSchedule("*");
        telemetryProfile.setUploadProtocol(UploadProtocol.S3);
        telemetryProfile.setUploadRepository("s3://repo1.local");
        telemetryProfile.setTelemetryProfile(new ArrayList<TelemetryProfile.TelemetryElement>());
        return telemetryProfile;
    }

    private Map<String, PermanentTelemetryProfile> createAndSavePermanentTelemetryProfiles(String stbName, String xhomeName) throws Exception {
        Map<String, PermanentTelemetryProfile> profiles = new HashMap<>();
        PermanentTelemetryProfile stbProfile = createPermanentTelemetryProfile();
        stbProfile.setName(stbName);
        stbProfile.setApplicationType(STB);
        permanentTelemetryDAO.setOne(stbProfile.getId(), stbProfile);
        profiles.put(STB, stbProfile);

        PermanentTelemetryProfile xhomeProfile = createPermanentTelemetryProfile();
        xhomeProfile.setName(xhomeName);
        xhomeProfile.setApplicationType(XHOME);
        permanentTelemetryDAO.setOne(xhomeProfile.getId(), xhomeProfile);
        profiles.put(XHOME, xhomeProfile);

        return profiles;
    }

    private Map<String, TelemetryRule> createAndSaveTelemetryRules(String stbName, String xhomeName, Map<String, PermanentTelemetryProfile> profiles) {
        Map<String, TelemetryRule> telemetryRules = new HashMap<>();
        TelemetryRule stbTelemetryRule = createTelemetryRule(stbName, profiles.get(STB).getId(), STB);
        telemetryRuleDAO.setOne(stbTelemetryRule.getId(), stbTelemetryRule);
        telemetryRules.put(STB, stbTelemetryRule);

        TelemetryRule telemetryRule = createTelemetryRule(xhomeName, profiles.get(XHOME).getId(), XHOME);
        telemetryRuleDAO.setOne(telemetryRule.getId(), telemetryRule);
        telemetryRules.put(XHOME, telemetryRule);

        return telemetryRules;
    }
}