/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */


package com.comcast.xconf.estbfirmware.converter;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.ConfigNames;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.firmware.DefinePropertiesAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.util.RuleUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ActivationVersionConverter {

    public FirmwareRule convertIntoRule(ActivationVersion activationVersion) {
        FirmwareRule firmwareRule = new FirmwareRule();
        firmwareRule.setId(activationVersion.getId());
        firmwareRule.setApplicableAction(convertDefineProperties(activationVersion));
        firmwareRule.setApplicationType(activationVersion.getApplicationType());
        firmwareRule.setActive(true);
        firmwareRule.setName(activationVersion.getDescription());
        firmwareRule.setType(TemplateNames.ACTIVATION_VERSION);
        firmwareRule.setRule(buildActivationRule(activationVersion));
        return firmwareRule;
    }

    private DefinePropertiesAction convertDefineProperties(ActivationVersion activationVersion) {
        DefinePropertiesAction action = new DefinePropertiesAction();
        Map<String, String> properties = new HashMap<>();
        properties.put(ConfigNames.REBOOT_IMMEDIATELY, Boolean.FALSE.toString());
        action.setProperties(properties);
        Map<String, Set<String>> activationFirmwareVersions = new HashMap<>();
        activationFirmwareVersions.put(DefinePropertiesAction.FIRMWARE_VERSIONS, activationVersion.getFirmwareVersions());
        activationFirmwareVersions.put(DefinePropertiesAction.REGULAR_EXPRESSIONS, activationVersion.getRegularExpressions());
        action.setActivationFirmwareVersions(activationFirmwareVersions);
        return action;
    }

    private Rule buildActivationRule(ActivationVersion activationVersion) {
        Rule.Builder activationRuleBuilder = Rule.Builder.of(new Condition(RuleFactory.MODEL, StandardOperation.IS, FixedArg.from(activationVersion.getModel())));
        if (StringUtils.isNotBlank(activationVersion.getPartnerId())) {
            activationRuleBuilder.and(new Condition(RuleFactory.PARTNER_ID, StandardOperation.IS, FixedArg.from(activationVersion.getPartnerId())));
        }
        return activationRuleBuilder.build();
    }

    public ActivationVersion convertIntoActivationVersion(FirmwareRule firmwareRule) {
        ActivationVersion activationVersion = new ActivationVersion();
        activationVersion.setId(firmwareRule.getId());
        activationVersion.setDescription(firmwareRule.getName());
        activationVersion.setApplicationType(firmwareRule.getApplicationType());
        activationVersion.setModel(getModelFromRule(firmwareRule.getRule()));
        DefinePropertiesAction action = (DefinePropertiesAction) firmwareRule.getApplicableAction();
        activationVersion.setFirmwareVersions(action.getFirmwareVersions());
        activationVersion.setRegularExpressions(action.getFirmwareVersionRegExs());
        activationVersion.setPartnerId(getPartnerFromRule(firmwareRule.getRule()));
        return activationVersion;
    }

    private String getModelFromRule(Rule rule) {
        List<String> models = RuleUtil.getFixedArgsFromRuleByFreeArgAndOperation(rule, RuleFactory.MODEL, StandardOperation.IS);
        if (CollectionUtils.isNotEmpty(models)) {
            return models.get(0);
        }
        return null;
    }

    private String getPartnerFromRule(Rule rule) {
        List<String> partnerId = RuleUtil.getFixedArgsFromRuleByFreeArgAndOperation(rule, RuleFactory.PARTNER_ID, StandardOperation.IS);
        if (CollectionUtils.isNotEmpty(partnerId)) {
            return partnerId.get(0);
        }
        return null;
    }
}

