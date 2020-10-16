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
package com.comcast.xconf.logupload;

import com.comcast.xconf.NamespacedList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: ikostrov
 * Date: 12.01.15
 * Time: 18:23
 */
public class DCMRuleTest {

    public static final String FORMULA_ID = "123456";
    public static final String ECM_MAC = "84:AE:75:55:86:77";
    public static final String CHANNEL_MAP_ID = "1698";
    public static final String START_DATE = "2014-01-01 01:01:01";
    public static final String END_DATE = "2017-01-01 01:01:01";

    @Test
    public void testValidateCronExpression() throws Exception {
        Assert.assertTrue(DCMRule.validate("15 23 * * *"));
        Assert.assertTrue(DCMRule.validate("15 23 "));

        Assert.assertFalse(DCMRule.validate("15"));
        Assert.assertFalse(DCMRule.validate("15 * * *"));
        Assert.assertFalse(DCMRule.validate("* * *"));
    }

    @Test
    public void testApplyRule_MainCron() throws Exception {
        Formula formula = getFormula();

        Settings settings = createSettings();
        Settings output = applyRule(formula, settings);

        assertCronWithDuration(settings.getLusScheduleCron(), output.getLusScheduleCron(), settings.getLusScheduleDurationMinutes());
        Assert.assertNull(output.getLusScheduleCronL1());
        Assert.assertNull(output.getLusScheduleCronL2());
        Assert.assertNull(output.getLusScheduleCronL3());
    }

    @Test
    public void testApplyRule_LevelOne() throws Exception {
        Formula formula = getFormula();
        formula.setPercentageL1(100);

        Settings settings = createSettings();
        Settings output = applyRule(formula, settings);

        assertCronWithDuration(settings.getLusScheduleCron(), output.getLusScheduleCron(), settings.getLusScheduleDurationMinutes());
        assertCronWithDuration(settings.getLusScheduleCronL1(), output.getLusScheduleCronL1(), settings.getLusScheduleDurationMinutes());
        Assert.assertNull(output.getLusScheduleCronL2());
        Assert.assertNull(output.getLusScheduleCronL3());
    }

    @Test
    public void testApplyRule_LevelTwo() throws Exception {
        Formula formula = getFormula();
        formula.setPercentageL2(100);

        Settings settings = createSettings();
        Settings output = applyRule(formula, settings);

        assertCronWithDuration(settings.getLusScheduleCron(), output.getLusScheduleCron(), settings.getLusScheduleDurationMinutes());
        assertCronWithDuration(settings.getLusScheduleCronL2(), output.getLusScheduleCronL2(), settings.getLusScheduleDurationMinutes());
        Assert.assertNull(output.getLusScheduleCronL1());
        Assert.assertNull(output.getLusScheduleCronL3());
    }

    @Test
    public void testApplyRule_LevelThree() throws Exception {
        Formula formula = getFormula();
        formula.setPercentage(0);
        formula.setPercentageL3(100);

        Settings settings = createSettings();
        Settings output = applyRule(formula, settings);

        Assert.assertNull(output.getLusScheduleCron());
        Assert.assertNull(output.getLusScheduleCronL1());
        Assert.assertNull(output.getLusScheduleCronL2());
        assertCronWithDuration(settings.getLusScheduleCronL3(), output.getLusScheduleCronL3(), settings.getLusScheduleDurationMinutes());
    }

    @Test
    public void testDeviceSettingsAndLusAppliedSeparately() throws Exception {
        Formula formula = getFormula();
        formula.setPercentage(0);

        Settings settings = createSettings();
        Settings output = applyRule(formula, settings);
        Assert.assertNull(output.getLusName());
        Assert.assertFalse(output.getUpload());

        Formula formula1 = getFormula();
        formula1.setPercentage(100);
        applyRule(formula1, settings, output);
        Assert.assertNotNull(output.getLusName());
        Assert.assertTrue(output.getUpload());
    }

    private Settings applyRule(Formula formula, Settings settings) {
        Settings output = new Settings();
        applyRule(formula, settings, output);
        assertSettings(settings, output);
        return output;
    }

    private Settings applyRule(Formula formula, Settings settings, Settings output) {
        NamespacedList estbMacList = NamespacedList.newMacList();
        NamespacedList ecmMacList = NamespacedList.newMacList();
        ecmMacList.setData(asSet(ECM_MAC));
        DCMRule rule = new DCMRule(formula, null, estbMacList, ecmMacList, settings);

        rule.apply(getContext(), output);

        return output;
    }

    private LogUploaderContext getContext() {
        LogUploaderContext context = new LogUploaderContext();
        context.setEcmMacAddress(ECM_MAC);
        context.setChannelMapId(CHANNEL_MAP_ID);
        return context;
    }

    private Formula getFormula() {
        Formula formula = new Formula();
        formula.setId(FORMULA_ID);
        formula.setRuleExpression("ecmMacAddress and channelMapId");
        formula.setChannelMapId(asSet(CHANNEL_MAP_ID));
        return formula;
    }

    private void assertSettings(Settings settings, Settings output) {
        Assert.assertTrue(output.getRuleIDs().contains(FORMULA_ID));
        Assert.assertEquals(settings.getGroupName(), output.getGroupName());
        Assert.assertEquals(settings.isCheckOnReboot(), output.isCheckOnReboot());
        assertCronWithDuration(settings.getScheduleCron(), output.getScheduleCron(), settings.getScheduleDurationMinutes());
    }

    private boolean assertCronWithDuration(String expectedCron, String outputCron, Integer duration) {
        // typical expression is '20 15 * * * '
        int expectedMin = Integer.parseInt(expectedCron.split(" ")[0]);
        int outputMin = Integer.parseInt(outputCron.split(" ")[0]);
        Assert.assertTrue(expectedMin <= outputMin);
        Assert.assertTrue(outputMin <= expectedMin + duration);

        // verify hours and date parts
        for (int i = 1; i < 5; i++) {
            String outputHour = outputCron.split(" ")[i];
            String expectedHour = expectedCron.split(" ")[i];
            Assert.assertEquals(expectedHour, outputHour);
        }
        return true;
    }

    private Settings createSettings() {
        Settings settings = new Settings();
        settings.setGroupName("TestGroupName");
        settings.setCheckOnReboot(true);
        settings.setScheduleCron("20 15 * * *");
        settings.setScheduleDurationMinutes(5);
        settings.setScheduleStartDate(START_DATE);
        settings.setScheduleEndDate(END_DATE);

        settings.setLusScheduleCron("20 15 * * *");
        settings.setLusScheduleCronL1("20 15 * * *");
        settings.setLusScheduleCronL2("20 15 * * *");
        settings.setLusScheduleCronL3("20 15 * * *");

        settings.setLusName("TestLusName");
        settings.setLusScheduleDurationMinutes(5);
        settings.setLusScheduleStartDate(START_DATE);
        settings.setLusScheduleEndDate(END_DATE);
        return settings;
    }

    private Set<String> asSet(String value) {
        return new HashSet<>(Arrays.asList(value));
    }
}
