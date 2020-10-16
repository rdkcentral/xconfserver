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
 * Created: 11/9/2015
*/
package com.comcast.xconf.estbfirmware.factory;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.Time;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.ConfigNames;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.DefinePropertiesTemplateAction;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.comcast.xconf.firmware.DefinePropertiesTemplateAction.PropertyValue;
import static com.comcast.xconf.firmware.DefinePropertiesTemplateAction.ValidationType;

@Component
public class TemplateFactory {

    public static final String EMPTY_NAME = "";
    public static final Double DEFAULT_PERCENT = 0d;
    public static final String TRUE = Boolean.TRUE.toString();
    public static final List<String> EMPTY_LIST = new ArrayList<>();
    public static final Set<String> EMPTY_SET = new HashSet<>();

    /* RULE TEMPLATES */
    public FirmwareRuleTemplate createMacRuleTemplate() {
        return createRuleTemplate(TemplateNames.MAC_RULE, RuleFactory.newMacRule(EMPTY_NAME), EMPTY_LIST, 1);
    }

    public FirmwareRuleTemplate createIpRuleTemplate() {
        return createRuleTemplate(TemplateNames.IP_RULE, RuleFactory.newIpRule(EMPTY_NAME, EMPTY_NAME, EMPTY_NAME), EMPTY_LIST, 2);
    }

    public FirmwareRuleTemplate createEnvModelRuleTemplate() {
        return createRuleTemplate(TemplateNames.ENV_MODEL_RULE, RuleFactory.newEnvModelRule(EMPTY_NAME, EMPTY_NAME), EMPTY_LIST, 5);
    }

    public FirmwareRuleTemplate createIntermediateVersionRuleTemplate() {
        List<String> byPassFilters = Lists.newArrayList(TemplateNames.GLOBAL_PERCENT, TemplateNames.TIME_FILTER);
        Rule rule = RuleFactory.newIntermediateVersionRule(EMPTY_NAME, EMPTY_NAME, EMPTY_NAME);
        return createRuleTemplate(TemplateNames.IV_RULE, rule, byPassFilters, 3);
    }

    public FirmwareRuleTemplate createMinCheckRuleTemplate() {
        List<String> byPassFilters = Lists.newArrayList(TemplateNames.GLOBAL_PERCENT, TemplateNames.TIME_FILTER);
        Rule rule = RuleFactory.newMinVersionCheckRule(EMPTY_NAME, EMPTY_NAME, EMPTY_SET);
        return createRuleTemplate(TemplateNames.MIN_CHECK_RULE, rule, byPassFilters, 4);
    }

    /* BLOCKING FILTER TEMPLATES */
    public FirmwareRuleTemplate createIpFilterTemplate() {
        return createBlockingFilterTemplate(TemplateNames.IP_FILTER, RuleFactory.newIpFilter(EMPTY_NAME), 2);
    }

    public FirmwareRuleTemplate createTimeFilterTemplate() {
        Rule rule = RuleFactory.newTimeFilter(
                true, true, false,
                EMPTY_NAME, EMPTY_NAME, EMPTY_NAME,
                new Time("01:00"), new Time("02:00"));
        return createBlockingFilterTemplate(TemplateNames.TIME_FILTER, rule, 1);
    }

    public FirmwareRuleTemplate createGlobalPercentTemplate() {
        return createBlockingFilterTemplate(TemplateNames.GLOBAL_PERCENT, RuleFactory.newGlobalPercentFilter(DEFAULT_PERCENT, EMPTY_NAME), 1);
    }

    /* DEFINE PROPERTIES TEMPLATES */
    public FirmwareRuleTemplate createRiFilterTemplate() {
        Map<String, PropertyValue> map = new HashMap<>();
        map.put(ConfigNames.REBOOT_IMMEDIATELY, PropertyValue.create(TRUE, false, ValidationType.BOOLEAN));
        return createDefinePropertiesTemplate(TemplateNames.REBOOT_IMMEDIATELY_FILTER, RuleFactory.newRiFilterTemplate(), map, EMPTY_LIST, 2);
    }

    public FirmwareRuleTemplate createMinCheckRiTemplate() {
        Map<String, PropertyValue> map = new HashMap<>();
        map.put(ConfigNames.REBOOT_IMMEDIATELY, PropertyValue.create(TRUE, true, ValidationType.BOOLEAN));
        List<String> byPassFilters = Lists.newArrayList(TemplateNames.GLOBAL_PERCENT, TemplateNames.TIME_FILTER);
        Rule rule = RuleFactory.newMinVersionCheckRule(EMPTY_NAME, EMPTY_NAME, EMPTY_SET);
        return createDefinePropertiesTemplate(TemplateNames.MIN_CHECK_RI, rule, map, byPassFilters, 3);
    }

    public FirmwareRuleTemplate createDownloadLocationTemplate() {
        Map<String, PropertyValue> map = new HashMap<>();
        map.put(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, PropertyValue.create("tftp", false, ValidationType.STRING));
        map.put(ConfigNames.FIRMWARE_LOCATION, PropertyValue.create("", false, ValidationType.STRING));
        map.put(ConfigNames.IPV6_FIRMWARE_LOCATION, PropertyValue.create("", true, ValidationType.STRING));
        Rule rule = RuleFactory.newDownloadLocationFilter(EMPTY_NAME);
        return createDefinePropertiesTemplate(TemplateNames.DOWNLOAD_LOCATION_FILTER, rule, map, null, 1);
    }

    public FirmwareRuleTemplate createActivationVersionTemplate() {
        Map<String, PropertyValue> map = new HashMap<>();
        map.put(ConfigNames.REBOOT_IMMEDIATELY, PropertyValue.create("false", false, ValidationType.BOOLEAN));
        Rule rule = RuleFactory.newActivationVersionRule(EMPTY_NAME, EMPTY_NAME);
        FirmwareRuleTemplate template = createDefinePropertiesTemplate(TemplateNames.ACTIVATION_VERSION, rule, map, null, 4);
        template.setEditable(false);
        return template;
    }

    private FirmwareRuleTemplate createRuleTemplate(String id, Rule rule, List<String> byPassFilters, int priority) {
        FirmwareRuleTemplate template = new FirmwareRuleTemplate();
        template.setId(id);
        template.setRule(rule);
        template.setPriority(priority);
        template.setApplicableAction(new ApplicableAction(ApplicableAction.Type.RULE_TEMPLATE));
        template.setByPassFilters(byPassFilters);
        return template;
    }

    private FirmwareRuleTemplate createBlockingFilterTemplate(String id, Rule rule, int priority) {
        FirmwareRuleTemplate template = new FirmwareRuleTemplate();
        template.setId(id);
        template.setRule(rule);
        template.setPriority(priority);
        template.setApplicableAction(new ApplicableAction(ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE));
        return template;
    }

    private FirmwareRuleTemplate createDefinePropertiesTemplate(String id, Rule rule,
                                                                Map<String, PropertyValue> properties,
                                                                List<String> byPassFilters, int priority) {
        FirmwareRuleTemplate template = new FirmwareRuleTemplate();
        template.setId(id);
        template.setRule(rule);
        template.setPriority(priority);
        template.setApplicableAction(new DefinePropertiesTemplateAction(properties, byPassFilters));
        return template;
    }
}
