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
 * Created: 11/10/2015
*/
package com.comcast.xconf.estbfirmware.factory;

import com.comcast.apps.hesperius.ruleengine.domain.additional.AuxFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.Time;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRule;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation.*;

@Component
public class RuleFactory {

    public final static FreeArg MAC = new FreeArg(StandardFreeArgType.STRING, StbContext.ESTB_MAC);
    public final static FreeArg IP = new FreeArg(StandardFreeArgType.STRING, StbContext.IP_ADDRESS);
    public final static FreeArg VERSION = new FreeArg(StandardFreeArgType.STRING, StbContext.FIRMWARE_VERSION);
    public final static FreeArg ENV = new FreeArg(StandardFreeArgType.STRING, StbContext.ENVIRONMENT);
    public final static FreeArg MODEL = new FreeArg(StandardFreeArgType.STRING, StbContext.MODEL);
    public final static FreeArg FIRMWARE_VERSION = new FreeArg(StandardFreeArgType.STRING, StbContext.FIRMWARE_VERSION);
    public final static FreeArg REGEX = new FreeArg(StandardFreeArgType.STRING, StbContext.FIRMWARE_VERSION);
    public final static FreeArg MATCHED_RULE_TYPE = new FreeArg(StandardFreeArgType.STRING, StbContext.MATCHED_RULE_TYPE);
    public final static FreeArg TIME_ZONE = new FreeArg(StandardFreeArgType.STRING, StbContext.TIME_ZONE); // may be "UTC"
    public final static FreeArg TIME = new FreeArg(AuxFreeArgType.TIME, StbContext.TIME);
    public final static FreeArg LOCAL_TIME = new FreeArg(StandardFreeArgType.STRING, StbContext.TIME);
    // required for TimeFilter. it must be added after rules matching
    public final static FreeArg FIRMWARE_DOWNLOAD_PROTOCOL = new FreeArg(StandardFreeArgType.STRING, StbContext.DOWNLOAD_PROTOCOL); // tftp or http
    public final static FreeArg REBOOT_DECOUPLED = new FreeArg(StandardFreeArgType.ANY, StbContext.REBOOT_DECOUPLED);
    public final static FreeArg PARTNER_ID = new FreeArg(StandardFreeArgType.STRING, StbContext.PARTNER_ID);

    private final static FixedArg<?> HTTP = FixedArg.from(FirmwareConfig.DownloadProtocol.http.toString());
    private final static FixedArg<?> UTC = FixedArg.from("UTC");

    public static final Operation IN_LIST = Operation.forName("IN_LIST");
    public static final Operation MATCH = Operation.forName("MATCH");

    public static final Operation RANGE = Operation.forName("RANGE");

    public static Rule newMacRule(String macListName) {
        Rule rule = new Rule();
        rule.setCondition(new Condition(MAC, IN_LIST, FixedArg.from(macListName)));
        return rule;
    }

    public static Rule newIpRule(String listName, String environment, String model) {
        final Rule.Builder builder = Rule.Builder
                .of(new Condition(IP, IN_LIST, FixedArg.from(listName)))
                .and(new Condition(ENV, IS, FixedArg.from(environment)))
                .and(new Condition(MODEL, IS, FixedArg.from(model)));

        return builder.build();
    }

    public static Rule newEnvModelRule(String environment, String model) {
        final Rule.Builder builder = Rule.Builder
                .of(new Condition(ENV, IS, FixedArg.from(environment)))
                .and(new Condition(MODEL, IS, FixedArg.from(model)));

        return builder.build();
    }

    public static Rule newModelRule(String model) {
        final Rule.Builder builder = Rule.Builder
                .of(new Condition(MODEL, IS, FixedArg.from(model)));

        return builder.build();
    }

    public static Rule newIntermediateVersionRule(String environment, String model, String version) {
        final Rule.Builder builder = Rule.Builder
                .of(new Condition(ENV, IS, FixedArg.from(environment)))
                .and(new Condition(MODEL, IS, FixedArg.from(model)))
                .and(Rule.Builder.not(new Condition(VERSION, IS, FixedArg.from(version))));

        return builder.build();
    }

    public static Rule newIpFilter(String listName) {
        return Rule.Builder.of(new Condition(IP, IN_LIST, FixedArg.from(listName))).build();
    }

    public static Rule newGlobalPercentFilter(Double percent, String ipList) {
        List<String> excludedRules = Arrays.asList(TemplateNames.ENV_MODEL_RULE, TemplateNames.MIN_CHECK_RULE, TemplateNames.IV_RULE);
        Rule.Builder builder = Rule.Builder
                .of(Rule.Builder.not(new Condition(MATCHED_RULE_TYPE, IN, FixedArg.from(excludedRules))))
                .and(new Condition(MAC, PERCENT, FixedArg.from(percent)));
        if (ipList != null) {
            builder.and(Rule.Builder.not(new Condition(IP, IN_LIST, FixedArg.from(ipList))));
        }
        return builder.build();
    }

    public static Rule newRiFilter(Set<String> ipAddressGroups, Set<String> macAddresses,
                                   Set<String> environments, Set<String> models) {
        final Rule.Builder riFilterBuilder = Rule.Builder.create();

        if (CollectionUtils.isNotEmpty(ipAddressGroups)) {

            final Rule.Builder inOneOfGroupsRuleBuilder = Rule.Builder.create();

            for (String group : ipAddressGroups) {
                inOneOfGroupsRuleBuilder.or(new Condition(IP, IN_LIST, FixedArg.from(group)));
            }

            riFilterBuilder.and(inOneOfGroupsRuleBuilder.build());
        }
        if (CollectionUtils.isNotEmpty(macAddresses)) {

            riFilterBuilder.and(new Condition(MAC, IN, FixedArg.from(macAddresses)));
        }
        if (CollectionUtils.isNotEmpty(environments)) {

            riFilterBuilder.and(new Condition(ENV, IN, FixedArg.from(environments)));
        }
        if (CollectionUtils.isNotEmpty(models)) {

            riFilterBuilder.and(new Condition(MODEL, IN, FixedArg.from(models)));
        }

        return riFilterBuilder.build();
    }

    public static Rule newRiFilterTemplate() {
        List<String> emptyList = new ArrayList<>();
        String emptyString = "";

        return Rule.Builder.of(new Condition(IP, IN_LIST, FixedArg.from(emptyString)))
                        .and(new Condition(MAC, IN, FixedArg.from(emptyList)))
                        .and(new Condition(ENV, IN, FixedArg.from(emptyList)))
                        .and(new Condition(MODEL, IN, FixedArg.from(emptyList)))
                        .build();
    }

    public static Rule newMinVersionCheckRule(String env, String model, Set<String> versions) {

        return Rule.Builder.of(new Condition(ENV, IS, FixedArg.from(env)))
                        .and(new Condition(MODEL, IS, FixedArg.from(model)))
                        .and(Rule.Builder.not(new Condition(VERSION, IN, FixedArg.from(versions))))
                        .build();
    }

    public static Rule newDownloadLocationFilter(String ipList) {
        return Rule.Builder.of(new Condition(IP, IN_LIST, FixedArg.from(ipList))).build();
    }

    public static Rule newActivationVersionRule(String model, String partnerId) {
        Rule activationRule = Rule.Builder.of(new Condition(MODEL, IS, FixedArg.from(model)))
                .and(new Condition(PARTNER_ID, IS, FixedArg.from(partnerId))).build();
        return activationRule;
    }

    public static Rule newDownloadLocationFilter(String ipList, String downloadProtocol) {
        return Rule.Builder.of(new Condition(IP, IN_LIST, FixedArg.from(ipList)))
                .and(new Condition(FIRMWARE_DOWNLOAD_PROTOCOL, IS, FixedArg.from(downloadProtocol)))
                .build();
    }

    public static Rule newTimeFilter(boolean neverBlockRebootDecoupled, boolean neverBlockHttpDownload,
                                     boolean isLocalTime, String environment, String model, String ipWhiteList,
                                     Time start, Time end) {
        final Condition isUTC = new Condition(TIME_ZONE, IS, UTC);
        final Condition ltEnd = new Condition(LOCAL_TIME, LTE, FixedArg.from(end.toString()));
        final Rule.Builder builder = isLocalTime ? Rule.Builder.of(Rule.Builder.not(isUTC)) : Rule.Builder.of(isUTC);
        builder.and(new Condition(LOCAL_TIME, GTE, FixedArg.from(start.toString())));
        if (start.compareTo(end) < 0) {
            builder.and(ltEnd);
        } else {
            builder.or(ltEnd);
        }
        if (neverBlockRebootDecoupled) {
            builder.and(Rule.Builder.not(new Condition(REBOOT_DECOUPLED, EXISTS, null)));
        }
        if (neverBlockHttpDownload) {
            builder.and(Rule.Builder.not(new Condition(FIRMWARE_DOWNLOAD_PROTOCOL, IS, HTTP)));
        }
        if (ipWhiteList != null) {
            builder.and(Rule.Builder.not(new Condition(IP, IN_LIST, FixedArg.from(ipWhiteList))));
        }
        if (environment != null && model != null) {
            builder.and(Rule.Builder.not(new Condition(ENV, IS, FixedArg.from(environment))));
            builder.or(Rule.Builder.not(new Condition(MODEL, IS, FixedArg.from(model))));
        }
        return builder.build();
    }

    public static FirmwareRule newFirmwareRule(String id, String name, String type, Rule rule, ApplicableAction action, boolean active) {
        FirmwareRule firmwareRule = new FirmwareRule();
        firmwareRule.setRule(rule);
        firmwareRule.setId(id);
        firmwareRule.setActive(active);
        firmwareRule.setName(name);
        firmwareRule.setType(type);
        firmwareRule.setApplicableAction(action);
        return firmwareRule;
    }
}
