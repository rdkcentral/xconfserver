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
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.xconf.CompleteTestSuite;
import com.comcast.xconf.dcm.ruleengine.TelemetryProfileService;
import com.comcast.xconf.logupload.UploadProtocol;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.logupload.telemetry.TimestampedRule;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static org.junit.Assert.*;

public class TelemetryProfileServiceTest extends BaseQueriesControllerTest {

    @Autowired
    private CachedSimpleDao<TimestampedRule, TelemetryProfile> temporaryTelemetryProfileDAO;

    @Autowired
    private TelemetryProfileService telemetryProfileService;

    private static String ATTRIBUTE = "myAttribute";
    private static String VALUE = "myExpectedValue";
    private static String ID = "myId";


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
    public void createTelemetryProfileTest() {
        TelemetryProfile telemetryProfile = new TelemetryProfile();
        telemetryProfile.setExpires(DateTime.now(DateTimeZone.UTC).getMillis());
        telemetryProfile.setId(ID);
        telemetryProfile.setName("MyName");
        telemetryProfile.setSchedule("never");
        telemetryProfile.setUploadProtocol(UploadProtocol.HTTPS);
        telemetryProfile.setUploadRepository("localhost");
        List<TelemetryProfile.TelemetryElement> elements = new ArrayList<>();
        TelemetryProfile.TelemetryElement telemetryElement = new TelemetryProfile.TelemetryElement();
        telemetryElement.setContent("MyContent");
        telemetryElement.setHeader("MyHeader");
        telemetryElement.setPollingFrequency("daily");
        telemetryElement.setType("MyType");
        elements.add(telemetryElement);
        telemetryProfile.setTelemetryProfile(elements);

        TimestampedRule timestampedRule = telemetryProfileService.createTelemetryProfile(ATTRIBUTE, VALUE, telemetryProfile);

        assertEquals(ATTRIBUTE, timestampedRule.getCondition().getFreeArg().getName());
        assertEquals(StandardFreeArgType.STRING, timestampedRule.getCondition().getFreeArg().getType());
        assertEquals(VALUE, timestampedRule.getCondition().getFixedArg().getValue());
        assertEquals(StandardOperation.IS, timestampedRule.getCondition().getOperation());
        assertNull(timestampedRule.getCompoundParts());
        TelemetryProfile telemetryProfileResponse = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(timestampedRule).orNull();
        assertEquals(telemetryProfile, telemetryProfileResponse);
    }


    @Test
    public void dropTelemetryForTest() {
        TelemetryProfile telemetryProfile = new TelemetryProfile();
        telemetryProfile.setId(ID);
        TimestampedRule timestampedRule = telemetryProfileService.createTelemetryProfile(ATTRIBUTE, VALUE, telemetryProfile);
        assertNotNull(timestampedRule);
        TelemetryProfile telemetryProfileResponse = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(timestampedRule).orNull();
        assertNotNull(telemetryProfileResponse);
        assertEquals(telemetryProfile, telemetryProfileResponse);

        List<TelemetryProfile> droppedTelemetryProfiles = telemetryProfileService.dropTelemetryFor(ATTRIBUTE, VALUE);

        assertNotNull(droppedTelemetryProfiles);
        assertTrue(droppedTelemetryProfiles.contains(telemetryProfileResponse));
        Optional<TelemetryProfile> noTelemetryProfile = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(timestampedRule);
        assertNull(noTelemetryProfile);
    }

    @Test
    public void getTemporaryProfileForContextExpiredTest() throws InterruptedException {
        TelemetryProfile telemetryProfile = new TelemetryProfile();
        telemetryProfile.setId(ID);
        telemetryProfile.setExpires(0);
        TimestampedRule timestampedRule = telemetryProfileService.createTelemetryProfile(ATTRIBUTE, VALUE, telemetryProfile);
        assertNotNull(timestampedRule);
        TelemetryProfile telemetryProfileResponse = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(timestampedRule).orNull();
        assertNotNull(telemetryProfileResponse);
        assertEquals(telemetryProfile, telemetryProfileResponse);
        Map<String, String> context = new HashMap<>();
        context.put(ATTRIBUTE, VALUE);
        Thread.sleep(CompleteTestSuite.timeToWaitExpiration);
        TelemetryProfile telemetryProfileExpired = telemetryProfileService.getTemporaryProfileForContext(context);

        assertNull(telemetryProfileExpired);
    }

    @Test
    public void getTemporaryProfileForContextNotExpiredTest() {
        TelemetryProfile telemetryProfile = new TelemetryProfile();
        telemetryProfile.setId(ID);
        telemetryProfile.setExpires(DateTime.now(DateTimeZone.UTC).getMillis());
        TimestampedRule timestampedRule = telemetryProfileService.createTelemetryProfile(ATTRIBUTE, VALUE, telemetryProfile);
        assertNotNull(timestampedRule);
        TelemetryProfile telemetryProfileResponse = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(timestampedRule).orNull();
        assertNotNull(telemetryProfileResponse);
        assertEquals(telemetryProfile, telemetryProfileResponse);
        Map<String, String> context = new HashMap<>();
        context.put(ATTRIBUTE, VALUE);

        TelemetryProfile telemetryProfileNotExpired = telemetryProfileService.getTemporaryProfileForContext(context);

        assertNotNull(telemetryProfileNotExpired);
        assertEquals(telemetryProfileNotExpired, telemetryProfileResponse);
    }

    @Test
    public void getTemporaryProfileForContextInvalidContextTest() {
        Map<String, String> context = new HashMap<>();
        context.put(ATTRIBUTE, VALUE);

        TelemetryProfile telemetryProfileNotExisting = telemetryProfileService.getTemporaryProfileForContext(context);

        assertNull(telemetryProfileNotExisting);
    }

    @Test
    public void getPermanentProfileForContextOneRuleTest() {
        TelemetryRule rule = new TelemetryRule();
        rule.setId(ID);
        rule.setName("myRuleName");
        String boundId = "myBoundTelemetryId";
        rule.setBoundTelemetryId(boundId);
        Condition condition = new Condition(new FreeArg(StandardFreeArgType.STRING, ATTRIBUTE), StandardOperation.IS, FixedArg.from(VALUE));
        rule.setCondition(condition);
        telemetryRuleDAO.setOne(rule.getId(), rule);
        PermanentTelemetryProfile permanentTelemetryProfile = new PermanentTelemetryProfile();
        permanentTelemetryProfile.setId(boundId);
        permanentTelemetryProfile.setName("myPermanentTelemetryProfileName");
        permanentTelemetryDAO.setOne(boundId, permanentTelemetryProfile);
        Map<String, String> context = new HashMap<>();
        context.put(ATTRIBUTE, VALUE);

        TelemetryProfile permanentProfileForContext = telemetryProfileService.getPermanentProfileForContext(context);

        assertNotNull(permanentProfileForContext);
        assertEquals(permanentTelemetryProfile, permanentProfileForContext);
    }

    @Test
    public void getPermanentProfileForContextReturnIsRuleWithHigherPriorityAmongIsAndLikeRulesTest() {
        TelemetryRule rule1 = new TelemetryRule();
        rule1.setId("id1");
        rule1.setName("name1");
        rule1.setBoundTelemetryId("myBoundTelemetryId1");
        String attr1 = "attr1";
        String value1 = "value1";
        Condition condition = new Condition(new FreeArg(StandardFreeArgType.STRING, attr1), StandardOperation.IS, FixedArg.from(value1));
        rule1.setCondition(condition);
        telemetryRuleDAO.setOne(rule1.getId(), rule1);
        TelemetryRule rule2 = new TelemetryRule();
        rule2.setId("id2");
        rule2.setName("name2");
        rule2.setBoundTelemetryId("myBoundTelemetryId2");
        String attr2 = "attr2";
        String value2 = "value2";
        Condition condition2 = new Condition(new FreeArg(StandardFreeArgType.STRING, attr2), StandardOperation.LIKE, FixedArg.from(value2));
        rule2.setCondition(condition2);
        telemetryRuleDAO.setOne(rule2.getId(), rule2);
        PermanentTelemetryProfile permanentTelemetryProfile1 = new PermanentTelemetryProfile();
        permanentTelemetryProfile1.setId(rule1.getBoundTelemetryId());
        permanentTelemetryProfile1.setName("myPermanentTelemetryProfileName1");
        permanentTelemetryDAO.setOne(permanentTelemetryProfile1.getId(), permanentTelemetryProfile1);
        PermanentTelemetryProfile permanentTelemetryProfile2 = new PermanentTelemetryProfile();
        permanentTelemetryProfile2.setId(rule2.getBoundTelemetryId());
        permanentTelemetryProfile2.setName("myPermanentTelemetryProfileName2");
        permanentTelemetryDAO.setOne(permanentTelemetryProfile2.getId(), permanentTelemetryProfile2);
        Map<String, String> context = new HashMap<>();
        context.put(attr1, value1);
        context.put(attr2, value2);

        TelemetryProfile permanentProfileForContext = telemetryProfileService.getPermanentProfileForContext(context);

        assertNotNull(permanentProfileForContext);
        assertEquals(permanentTelemetryProfile1, permanentProfileForContext);
    }

    @Test
    public void getPermanentProfileForContextReturnLikeRuleWithHigherPriorityAmongPercentAndLikeRulesTest() {
        TelemetryRule rule1 = new TelemetryRule();
        rule1.setId("id1");
        rule1.setName("name1");
        rule1.setBoundTelemetryId("myBoundTelemetryId1");
        String attr1 = "attr1";
        Double value1 = 3.0;
        Condition condition = new Condition(new FreeArg(StandardFreeArgType.STRING, attr1), StandardOperation.PERCENT, FixedArg.from(value1));
        rule1.setCondition(condition);
        telemetryRuleDAO.setOne(rule1.getId(), rule1);
        TelemetryRule rule2 = new TelemetryRule();
        rule2.setId("id2");
        rule2.setName("name2");
        rule2.setBoundTelemetryId("myBoundTelemetryId2");
        String attr2 = "attr2";
        String value2 = "value2";
        Condition condition2 = new Condition(new FreeArg(StandardFreeArgType.STRING, attr2), StandardOperation.LIKE, FixedArg.from(value2));
        rule2.setCondition(condition2);
        telemetryRuleDAO.setOne(rule2.getId(), rule2);
        PermanentTelemetryProfile permanentTelemetryProfile1 = new PermanentTelemetryProfile();
        permanentTelemetryProfile1.setId(rule1.getBoundTelemetryId());
        permanentTelemetryProfile1.setName("myPermanentTelemetryProfileName1");
        permanentTelemetryDAO.setOne(permanentTelemetryProfile1.getId(), permanentTelemetryProfile1);
        PermanentTelemetryProfile permanentTelemetryProfile2 = new PermanentTelemetryProfile();
        permanentTelemetryProfile2.setId(rule2.getBoundTelemetryId());
        permanentTelemetryProfile2.setName("myPermanentTelemetryProfileName2");
        permanentTelemetryDAO.setOne(permanentTelemetryProfile2.getId(), permanentTelemetryProfile2);
        Map<String, String> context = new HashMap<>();
        context.put(attr1, value1.toString());
        context.put(attr2, value2);

        TelemetryProfile permanentProfileForContext = telemetryProfileService.getPermanentProfileForContext(context);

        assertNotNull(permanentProfileForContext);
        assertEquals(permanentTelemetryProfile2, permanentProfileForContext);
    }

    @Test
    public void getPermanentProfileForContextReturnCompoundRuleAmongSimpleAndCompoundRulesTest() {
        TelemetryRule rule1 = new TelemetryRule();
        rule1.setId("id1");
        rule1.setName("name1");
        rule1.setBoundTelemetryId("myBoundTelemetryId1");
        List<com.comcast.apps.hesperius.ruleengine.main.impl.Rule> compoundParts = new ArrayList<>();
        TelemetryRule ruleIn1 = new TelemetryRule();
        ruleIn1.setId("idIn1");
        ruleIn1.setName("nameIn1");
        ruleIn1.setBoundTelemetryId("myBoundTelemetryIdIn1");
        String attrIn1 = "attrIn1";
        Double valueIn1 = 3.0;
        Condition condition = new Condition(new FreeArg(StandardFreeArgType.STRING, attrIn1), StandardOperation.PERCENT, FixedArg.from(valueIn1));
        ruleIn1.setCondition(condition);
        compoundParts.add(ruleIn1);
        TelemetryRule ruleIn2 = new TelemetryRule();
        ruleIn2.setId("idIn2");
        ruleIn2.setName("nameIn2");
        ruleIn2.setBoundTelemetryId("myBoundTelemetryIdIn2");
        String attrIn2 = "attrIn2";
        String valueIn2 = "valueIn2";
        condition = new Condition(new FreeArg(StandardFreeArgType.STRING, attrIn2), StandardOperation.LIKE, FixedArg.from(valueIn2));
        ruleIn2.setCondition(condition);
        ruleIn2.setRelation(Relation.OR);
        compoundParts.add(ruleIn2);
        rule1.setCompoundParts(compoundParts);
        telemetryRuleDAO.setOne(rule1.getId(), rule1);
        TelemetryRule rule2 = new TelemetryRule();
        rule2.setId("id2");
        rule2.setName("name2");
        rule2.setBoundTelemetryId("myBoundTelemetryId2");
        String attr2 = "attr2";
        String value2 = "value2";
        Condition condition2 = new Condition(new FreeArg(StandardFreeArgType.STRING, attr2), StandardOperation.IS, FixedArg.from(value2));
        rule2.setCondition(condition2);
        telemetryRuleDAO.setOne(rule2.getId(), rule2);
        PermanentTelemetryProfile permanentTelemetryProfile1 = new PermanentTelemetryProfile();
        permanentTelemetryProfile1.setId(rule1.getBoundTelemetryId());
        permanentTelemetryProfile1.setName("myPermanentTelemetryProfileName1");
        permanentTelemetryDAO.setOne(permanentTelemetryProfile1.getId(), permanentTelemetryProfile1);
        PermanentTelemetryProfile permanentTelemetryProfile2 = new PermanentTelemetryProfile();
        permanentTelemetryProfile2.setId(rule2.getBoundTelemetryId());
        permanentTelemetryProfile2.setName("myPermanentTelemetryProfileName2");
        permanentTelemetryDAO.setOne(permanentTelemetryProfile2.getId(), permanentTelemetryProfile2);
        Map<String, String> context = new HashMap<>();
        context.put(attrIn1, valueIn1.toString());
        context.put(attrIn2, valueIn2);
        context.put(attr2, value2);

        TelemetryProfile permanentProfileForContext = telemetryProfileService.getPermanentProfileForContext(context);

        assertNotNull(permanentProfileForContext);
        assertEquals(permanentTelemetryProfile1, permanentProfileForContext);
    }

    @Test
    public void getAvailableDescriptorsTest() {
        TelemetryRule rule = new TelemetryRule();
        rule.setId("id");
        rule.setName("name");
        rule.setBoundTelemetryId("myBoundTelemetryId");
        String attr1 = "attr";
        String value1 = "value";
        Condition condition = new Condition(new FreeArg(StandardFreeArgType.STRING, attr1), StandardOperation.IS, FixedArg.from(value1));
        rule.setCondition(condition);
        telemetryRuleDAO.setOne(rule.getId(), rule);

        List<TelemetryRule.PermanentTelemetryRuleDescriptor> descriptors = telemetryProfileService.getAvailableDescriptors(STB);

        assertEquals(1, descriptors.size());
        TelemetryRule.PermanentTelemetryRuleDescriptor descriptor = descriptors.get(0);
        assertEquals(rule.getId(), descriptor.getRuleId());
        assertEquals(rule.getName(), descriptor.getRuleName());
    }

    @Test
    public void getAvailableProfileDescriptorsTest() {
        TelemetryRule rule = new TelemetryRule();
        rule.setId("id");
        rule.setName("name");
        rule.setBoundTelemetryId("myBoundTelemetryId");
        String attr = "attr";
        String value = "value";
        Condition condition = new Condition(new FreeArg(StandardFreeArgType.STRING, attr), StandardOperation.IS, FixedArg.from(value));
        rule.setCondition(condition);
        telemetryRuleDAO.setOne(rule.getId(), rule);
        PermanentTelemetryProfile permanentTelemetryProfile = new PermanentTelemetryProfile();
        permanentTelemetryProfile.setId(rule.getBoundTelemetryId());
        permanentTelemetryProfile.setName("myPermanentTelemetryProfileName1");
        permanentTelemetryDAO.setOne(permanentTelemetryProfile.getId(), permanentTelemetryProfile);

        List<PermanentTelemetryProfile.TelemetryProfileDescriptor> profileDescriptors = telemetryProfileService.getAvailableProfileDescriptors(STB);

        assertEquals(1, profileDescriptors.size());
        PermanentTelemetryProfile.TelemetryProfileDescriptor profileDescriptor = profileDescriptors.get(0);
        assertEquals(permanentTelemetryProfile.getId(), profileDescriptor.getId());
        assertEquals(permanentTelemetryProfile.getName(), profileDescriptor.getName());
    }

    /*@Test
    @Ignore
    public void expireTemporaryTelemetryRulesDeletesRuleAfter60secondsTest() throws ValidationException, InterruptedException {
        TelemetryProfile telemetryProfile = new TelemetryProfile();
        telemetryProfile.setId(ID);
        telemetryProfile.setExpires(0);
        TimestampedRule timestampedRule = telemetryProfileService.createTelemetryProfile(ATTRIBUTE, VALUE, telemetryProfile);
        assertNotNull(timestampedRule);
        TelemetryProfile telemetryProfileResponse = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(timestampedRule).orNull();
        assertNotNull(telemetryProfileResponse);
        assertEquals(telemetryProfile, telemetryProfileResponse);

        telemetryProfileService.expireTemporaryTelemetryRules();

        Thread.sleep(30000L);
        Optional<TelemetryProfile> telemetryProfileStillExisting = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(timestampedRule);
        assertNotNull(telemetryProfileStillExisting);
        Thread.sleep(31000L);
        Optional<TelemetryProfile> noTelemetryProfile = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(timestampedRule);
        assertNull(noTelemetryProfile);
    }*/

    @Test
    public void createRuleForAttributeTest() {
        TimestampedRule timestampedRule = telemetryProfileService.createRuleForAttribute(ATTRIBUTE, VALUE);

        assertEquals(ATTRIBUTE, timestampedRule.getCondition().getFreeArg().getName());
        assertEquals(StandardFreeArgType.STRING, timestampedRule.getCondition().getFreeArg().getType());
        assertEquals(VALUE, timestampedRule.getCondition().getFixedArg().getValue());
        assertEquals(StandardOperation.IS, timestampedRule.getCondition().getOperation());
        assertNull(timestampedRule.getCompoundParts());
    }

    @Test
    public void testLatestTelemetryOnly() {
        temporaryTelemetryProfileDAO.setOne(createMacRule(System.currentTimeMillis(), "00:1C:B3:09:85:15"), createTelemetry("tp1", System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)));
        temporaryTelemetryProfileDAO.setOne(createMacRule(System.currentTimeMillis() + 10, "00:1C:B3:09:85:15"), createTelemetry("tp2", System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)));
        temporaryTelemetryProfileDAO.setOne(createMacRule(System.currentTimeMillis() + 20, "00:1C:B3:09:85:15"), createTelemetry("tp3", System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)));

        final Map<String, String> context = new HashMap<String, String>() {{
            put("estbMacAddress", "00:1C:B3:09:85:15");
        }};

        final TelemetryProfile telemetry = telemetryProfileService.getTemporaryProfileForContext(context);

        assertEquals(telemetry.getName(), "tp3");

        final TelemetryProfile noTelemetry = telemetryProfileService.getTemporaryProfileForContext(context);

        assertNull(noTelemetry);


    }

    private TimestampedRule createMacRule(long timestamp, String macAddress) {
        final TimestampedRule rule = new TimestampedRule();
        com.comcast.apps.hesperius.ruleengine.main.impl.Rule.Builder.of(new Condition(new FreeArg(StandardFreeArgType.STRING, "estbMacAddress"), StandardOperation.IS, FixedArg.from(macAddress))).copyTo(rule);
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
}
