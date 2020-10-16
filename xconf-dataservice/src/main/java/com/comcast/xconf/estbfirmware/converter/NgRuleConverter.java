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
 * Created: 11/26/2015
*/
package com.comcast.xconf.estbfirmware.converter;

import com.comcast.apps.hesperius.ruleengine.domain.additional.AuxFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.TimeFilter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.estbfirmware.legacy.LegacyConverterHelper;
import com.comcast.xconf.estbfirmware.legacy.RebootImmediatelyLegacyConverter;
import com.comcast.xconf.estbfirmware.legacy.TimeFilterLegacyConverter;
import com.comcast.xconf.firmware.BlockingFilterAction;
import com.comcast.xconf.firmware.DefinePropertiesAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.comcast.xconf.estbfirmware.FirmwareRule.RuleType.*;

@Component
public class NgRuleConverter {

    private static final Logger log = LoggerFactory.getLogger(NgRuleConverter.class);

    @Autowired
    private TimeFilterLegacyConverter timeFilterLegacyConverter;

    @Autowired
    private RebootImmediatelyLegacyConverter rebootImmediatelyLegacyConverter;

    @Autowired
    private TimeFilterConverter timeFilterConverter;

    public FirmwareRule convertOld(com.comcast.xconf.estbfirmware.FirmwareRule oldRule) {
        FirmwareRule converted = new FirmwareRule();
        converted.setId(oldRule.getId());
        converted.setName(oldRule.getName());
        converted.setUpdated(oldRule.getUpdated());
        converted.setRule(Rule.Builder.copy(oldRule));

        switch (oldRule.getType()) {
            case MAC_RULE:
                converted.setType(TemplateNames.MAC_RULE);
                converted.setApplicableAction(new RuleAction(oldRule.getBoundConfigId()));
                break;
            case IP_RULE:
                converted.setType(TemplateNames.IP_RULE);
                changeIpConditionIntoNewFormat(converted.getRule());
                converted.setApplicableAction(new RuleAction(oldRule.getBoundConfigId()));
                break;
            case ENV_MODEL_RULE:
                converted.setType(TemplateNames.ENV_MODEL_RULE);
                converted.setApplicableAction(new RuleAction(oldRule.getBoundConfigId()));
                break;
            case IP_FILTER:
                converted.setType(TemplateNames.IP_FILTER);
                changeIpConditionIntoNewFormat(converted.getRule());
                converted.setApplicableAction(new BlockingFilterAction());
                break;
            case TIME_FILTER:
                converted.setType(TemplateNames.TIME_FILTER);
                converted.setRule(convertTimeFilterConditions(oldRule));
                converted.setApplicableAction(new BlockingFilterAction());
                break;
            case REBOOT_IMMEDIATELY_FILTER:
                converted.setType(TemplateNames.REBOOT_IMMEDIATELY_FILTER);
                converted.setRule(fixNestedCompoundPartsOfRIRule(oldRule));
                changeIpConditionIntoNewFormat(converted.getRule());
                changeMacConditionIntoNewFormat(converted.getRule());
                converted.setApplicableAction(new DefinePropertiesAction(asMap("rebootImmediately", "true")));
                break;
            case DOWNLOAD_LOCATION_FILTER:
                // will be converted separately
                return null;
            default:
                log.error("Can't convert rule with unknown type: " + oldRule.getType() + ", id: " + oldRule.getId());
                return null;
        }
        return converted;
    }

    public com.comcast.xconf.estbfirmware.FirmwareRule convertNew(FirmwareRule newRule) {
        com.comcast.xconf.estbfirmware.FirmwareRule converted = new com.comcast.xconf.estbfirmware.FirmwareRule();
        converted.setId(newRule.getId());
        converted.setName(newRule.getName());
        converted.setUpdated(newRule.getUpdated());
        Rule.copy(newRule.getRule(), converted);
        switch (newRule.getType()) {
            case TemplateNames.MAC_RULE:
                converted.setType(MAC_RULE);
                converted.setBoundConfigId(getConfigId(newRule));
                break;
            case TemplateNames.IP_RULE:
                converted.setType(IP_RULE);
                converted.setBoundConfigId(getConfigId(newRule));
                break;
            case TemplateNames.ENV_MODEL_RULE:
                converted.setType(ENV_MODEL_RULE);
                converted.setBoundConfigId(getConfigId(newRule));
                break;
            case TemplateNames.REBOOT_IMMEDIATELY_FILTER:
                converted.setType(REBOOT_IMMEDIATELY_FILTER);
                break;
            case TemplateNames.TIME_FILTER:
                converted.setType(TIME_FILTER);
                break;
            case TemplateNames.IP_FILTER:
                converted.setType(IP_FILTER);
                break;
            default:
                log.error("Can't convert rule with unknown type: " + newRule.getType());
        }
        return converted;
    }

    private String getConfigId(FirmwareRule newRule) {
        return ((RuleAction) newRule.getApplicableAction()).getConfigId();
    }

    private void changeIpConditionIntoNewFormat(Rule rule) {
        RuleUtil.convertConditions(rule, new Predicate<Condition>() {
            @Override
            public boolean apply(Condition condition) {
                if (LegacyConverterHelper.isLegacyIpCondition(condition)) {
                    IpAddressGroup group = (IpAddressGroup) condition.getFixedArg().getValue();
                    condition.getFixedArg().setValue(group.getName());
                    condition.setFreeArg(RuleFactory.IP);
                    condition.setOperation(RuleFactory.IN_LIST);
                    return true;
                }
                return false;
            }
        });
    }

    private void changeMacConditionIntoNewFormat(Rule rule) {
        RuleUtil.convertConditions(rule, new Predicate<Condition>() {
            @Override
            public boolean apply(Condition condition) {
                if (condition.getFreeArg() != null
                        && AuxFreeArgType.MAC_ADDRESS.equals(condition.getFreeArg().getType())
                        && StbContext.ESTB_MAC.equals(condition.getFreeArg().getName())
                        && StandardOperation.IN.equals(condition.getOperation())) {

                    HashSet<String> macAddresses = new HashSet<>();
                    if (condition.getFixedArg().getValue() instanceof Collection) {
                        for (Object macObject : ((Collection) condition.getFixedArg().getValue())) {
                            if (macObject instanceof MacAddress) {
                                MacAddress mac = (MacAddress) macObject;
                                macAddresses.add(mac.toString());
                            } else if (macObject instanceof Map) {
                                Map<String, String> macAddressMap = (Map<String, String>) macObject;
                                macAddresses.addAll(macAddressMap.values());
                            }
                        }
                    } else {
                        macAddresses.addAll(Arrays.asList(condition.getFixedArg().getValue().toString().split("[\\s,]+")));
                    }
                    condition.getFixedArg().setValue(macAddresses);
                    condition.setFreeArg(RuleFactory.MAC);

                    return true;
                }
                return false;
            }
        });
    }

    private Rule convertTimeFilterConditions(com.comcast.xconf.estbfirmware.FirmwareRule oldRule) {
        TimeFilter timeFilter = timeFilterLegacyConverter.convertFirmwareRuleToTimeFilter(oldRule);
        return timeFilterConverter.convert(timeFilter).getRule();
    }

    private Map<String, String> asMap(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private Rule fixNestedCompoundPartsOfRIRule(com.comcast.xconf.estbfirmware.FirmwareRule oldRule) {
        return Rule.Builder.copy(rebootImmediatelyLegacyConverter.convertRebootFilterToFirmwareRule(
                rebootImmediatelyLegacyConverter.convertFirmwareRuleToRebootFilter(oldRule)
        ));
    }
}
