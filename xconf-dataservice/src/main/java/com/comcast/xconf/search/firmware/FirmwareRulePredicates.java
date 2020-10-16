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

package com.comcast.xconf.search.firmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.XRulePredicates;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.comcast.xconf.firmware.ApplicableAction.readFromString;
import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class FirmwareRulePredicates extends XRulePredicates<FirmwareRule> {

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    public Predicate<FirmwareRule> byFirmwareVersion(String version) {
        return firmwareRule -> {
            if (Objects.nonNull(firmwareRule) && Objects.nonNull(firmwareRule.getApplicableAction())
                    && ApplicableAction.Type.RULE.equals(firmwareRule.getApplicableAction().getActionType())) {
                String configId = ((RuleAction) firmwareRule.getApplicableAction()).getConfigId();

                if (StringUtils.isNotBlank(configId)) {
                    FirmwareConfig ruleConfig = firmwareConfigDAO.getOne(configId);
                    return Objects.nonNull(ruleConfig)
                            && StringUtils.containsIgnoreCase(ruleConfig.getDescription(), version);
                }
            }
            return false;
        };
    }

    public Predicate<FirmwareRule> byTemplate(String template) {
        return firmwareRule -> Objects.nonNull(firmwareRule)
                && StringUtils.equals(firmwareRule.getType(), template);
    }

    public Predicate<FirmwareRule> byActionType(String type) {
        return firmwareRule -> Objects.nonNull(firmwareRule)
                && Objects.equals(firmwareRule.getApplicableAction().getActionType(), readFromString(type));
    }

    public Predicate<FirmwareRule> byEditableTemplate() {
        return firmwareRule -> {
            if (Objects.nonNull(firmwareRule) && StringUtils.isNotBlank(firmwareRule.getTemplateId())) {
                FirmwareRuleTemplate template = firmwareRuleTemplateDao.getOne(firmwareRule.getTemplateId());
                return Objects.isNull(template) || template.isEditable();
            }
            return false;
        };
    }

    public List<Predicate<FirmwareRule>> getPredicates(ContextOptional context) {
        List<Predicate<FirmwareRule>> predicates = new ArrayList<>();

        context.getTemplateId().ifPresent(templateId -> predicates.add(byTemplate(templateId)));
        context.getApplicableActionType().ifPresent(actionType -> predicates.add(byActionType(actionType)));
        context.getFirmwareVersion().ifPresent(firmwareVersion -> predicates.add(byFirmwareVersion(firmwareVersion)));
        context.getApplicationType().ifPresent(application -> predicates.add(byApplication(application)));
        predicates.addAll(getBaseRulePredicates(context));

        return predicates;
    }
}