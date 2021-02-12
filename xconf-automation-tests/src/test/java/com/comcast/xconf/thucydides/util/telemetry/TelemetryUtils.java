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
 * Author: rdolomansky
 * Created: 3/14/16  8:32 PM
 */
package com.comcast.xconf.thucydides.util.telemetry;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.UploadProtocol;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;
import com.comcast.xconf.thucydides.util.RuleUtils;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class TelemetryUtils {
    public static final String PERMANENT_PROFILE_URL = "telemetry/profile";
    public static final String TARGETING_RULE_URL = "telemetry/rule";


    public static PermanentTelemetryProfile createAndSavePermanentProfile() throws IOException {
        final PermanentTelemetryProfile obj = createPermanentTelemetryProfile();
        HttpClient.post(GenericTestUtils.buildFullUrl(PERMANENT_PROFILE_URL), obj);
        return obj;
    }

    public static TelemetryRule createAndSaveTargetingRule() throws IOException {
        final PermanentTelemetryProfile telemetryProfile = createAndSavePermanentProfile();
        final TelemetryRule obj = createPermanentTargetingRule(telemetryProfile.getId());
        HttpClient.post(GenericTestUtils.buildFullUrl(TARGETING_RULE_URL), obj);
        return obj;
    }

    public static TelemetryRule createAndSaveTargetingRule(String profileId, String ruleName, Condition condition) throws IOException {
        TelemetryRule telemetryRule = createPermanentTargetingRule(profileId);
        telemetryRule.setId(UUID.randomUUID().toString());
        telemetryRule.setName(ruleName);
        telemetryRule.setCondition(condition);
        HttpClient.post(GenericTestUtils.buildFullUrl(TARGETING_RULE_URL), telemetryRule);
        return telemetryRule;
    }

    public static PermanentTelemetryProfile createPermanentTelemetryProfile() {
        final PermanentTelemetryProfile obj = new PermanentTelemetryProfile();
        obj.setId(UUID.randomUUID().toString());
        obj.setName("testName");
        obj.setSchedule("123");
        obj.setUploadProtocol(UploadProtocol.HTTP);
        obj.setUploadRepository("localhost.com");
        obj.setTelemetryProfile(Lists.newArrayList(doTelemetryElement()));

        return obj;
    }

    public static PermanentTelemetryProfile createAndSavePermanentTelemetryProfile(String profileName) throws IOException {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();
        profile.setName(profileName);
        HttpClient.post(GenericTestUtils.buildFullUrl(PERMANENT_PROFILE_URL), profile);
        return profile;
    }

    private static TelemetryProfile.TelemetryElement doTelemetryElement() {
        final TelemetryProfile.TelemetryElement telemetryElement = new TelemetryProfile.TelemetryElement();
        telemetryElement.setHeader("1");
        telemetryElement.setContent("1");
        telemetryElement.setType("1");
        telemetryElement.setPollingFrequency("1");

        return telemetryElement;
    }

    public static TelemetryRule createPermanentTargetingRule(String boundTelemetryId) {
        final TelemetryRule telemetryRule = new TelemetryRule();
        telemetryRule.setId(UUID.randomUUID().toString());
        telemetryRule.setName("testName");
        telemetryRule.setBoundTelemetryId(boundTelemetryId);
        telemetryRule.setCondition(new Condition(RuleFactory.MODEL,
                StandardOperation.IS, FixedArg.from("1.1.1.1")));

        return telemetryRule;
    }

    public static List<PermanentTelemetryProfile> createAndSavePermanentTelemetryProfiles() throws IOException {
        return Lists.newArrayList(
            createAndSavePermanentTelemetryProfile("profile123"),
            createAndSavePermanentTelemetryProfile("profile456")
        );
    }

    public static List<TelemetryRule> createAndSaveTargetingRules(String permanentProfileId) throws IOException {
        return Lists.newArrayList(
                createAndSaveTargetingRule(permanentProfileId, "targetingRule123",
                        RuleUtils.createCondition(RuleFactory.VERSION, StandardOperation.IS, "firmwareVersion")),
                createAndSaveTargetingRule(permanentProfileId, "targetingRule456",
                        RuleUtils.createCondition(RuleFactory.MAC, StandardOperation.IS, "AA:BB:CC:DD:EE:FF"))
        );
    }

    public static TelemetryRule createAndSaveTargetingRule(String boundTelemetryId, List<Condition> conditions) throws IOException {
        TelemetryRule targetingRule = createPermanentTargetingRule(boundTelemetryId);
        targetingRule.setCompoundParts(RuleUtils.createRule(conditions));
        targetingRule.setCondition(null);
        HttpClient.post(GenericTestUtils.buildFullUrl(TARGETING_RULE_URL), targetingRule);
        return targetingRule;
    }

}
