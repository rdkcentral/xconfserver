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
 * Author: Igor Kostrov
 * Created: 3/28/2016
*/
package com.comcast.xconf.thucydides.util.setting;

import com.beust.jcommander.internal.Lists;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.settings.SettingType;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SettingUtils {
    public static final String SETTING_PROFILE_URL = "setting/profile";
    public static final String SETTING_RULE_URL = "setting/rule";

    public static SettingProfile createAndSaveSettingProfile() throws IOException {
        SettingProfile profile = createSettingProfile();
        HttpClient.post(GenericTestUtils.buildFullUrl(SETTING_PROFILE_URL), profile);
        return profile;
    }

    public static List<SettingProfile> createAndSaveSettingProfiles() throws IOException {
        return Lists.newArrayList(
                createAndSaveSettingProfile("profile1"),
                createAndSaveSettingProfile("profile2")
        );
    }

    public static SettingProfile createAndSaveSettingProfile(String name) throws IOException {
        SettingProfile profile = createSettingProfile(name);
        HttpClient.post(GenericTestUtils.buildFullUrl(SETTING_PROFILE_URL), profile);
        return profile;
    }

    public static SettingProfile createAndSaveSettingProfile(String name, SettingType settingType) throws IOException {
        SettingProfile profile = createSettingProfile(name, settingType);
        HttpClient.post(GenericTestUtils.buildFullUrl(SETTING_PROFILE_URL), profile);
        return profile;
    }

    public static SettingRule createAndSaveSettingRule() throws IOException {
        SettingProfile profile = createSettingProfile();
        return createAndSaveSettingRule("SettingRule", profile.getId());
    }

    public static SettingRule createAndSaveSettingRule(String name, String profileId) throws IOException {
        SettingRule rule = createSettingRule(name, profileId);
        HttpClient.post(GenericTestUtils.buildFullUrl(SETTING_RULE_URL), rule);
        return rule;
    }

    public static List<SettingRule> createAndSaveSettingRules(String profileId) throws IOException {
        return Lists.newArrayList(
                createAndSaveSettingRule("rule1", profileId),
                createAndSaveSettingRule("rule2", profileId)
        );
    }

    public static SettingRule createAndSaveSettingRule(String name, String profileId, FreeArg freeArg) throws IOException {
        SettingRule rule = createSettingRule(name, profileId, freeArg);
        HttpClient.post(GenericTestUtils.buildFullUrl(SETTING_RULE_URL), rule);
        return rule;
    }

    public static SettingProfile createSettingProfile() {
        return createSettingProfile("profileName");
    }

    public static SettingProfile createSettingProfile(String name, SettingType settingType) {
        SettingProfile profile = new SettingProfile();
        profile.setId(UUID.randomUUID().toString());
        profile.setSettingType(settingType);
        profile.setSettingProfileId(name);
        profile.setProperties(Collections.singletonMap("key1", "value1"));
        return profile;
    }

    public static SettingProfile createSettingProfile(String name) {
        return createSettingProfile(name, SettingType.EPON);
    }

    public static SettingRule createSettingRule(String name, String configId) {
        SettingRule rule = new SettingRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setName(name);
        rule.setBoundSettingId(configId);
        rule.setRule(createRule(name));
        return rule;
    }

    public static SettingRule createSettingRule(String name, String configId, FreeArg freeArg) {
        SettingRule rule = new SettingRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setName(name);
        rule.setBoundSettingId(configId);
        rule.setRule(createRule(name, freeArg));
        return rule;
    }

    private static Rule createRule(String name, FreeArg freeArg) {
        return Rule.Builder.of(new Condition(freeArg, StandardOperation.IS, FixedArg.from(name))).build();
    }

    private static Rule createRule(String name) {
        return createRule(name, RuleFactory.MODEL);
    }
}
