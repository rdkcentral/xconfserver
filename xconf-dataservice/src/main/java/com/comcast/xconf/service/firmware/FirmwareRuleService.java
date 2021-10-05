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
 * Created: 1/20/16  11:45 AM
 */
package com.comcast.xconf.service.firmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.firmware.FirmwareRulePredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.firmware.FirmwareRuleValidator;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Component
public class FirmwareRuleService extends AbstractApplicationTypeAwareService<FirmwareRule> {

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    @Autowired
    private FirmwareRuleValidator firmwareRuleValidator;

    @Autowired
    private FirmwareRulePredicates firmwareRulePredicates;

    @Autowired
    private FirmwarePermissionService permissionService;

    public List<FirmwareRule> getAllByType(final ApplicableAction.Type type) {
        return filterByActionType(getAll(), type);
    }

    @Override
    public CachedSimpleDao<String, FirmwareRule> getEntityDAO() {
        return firmwareRuleDao;
    }

    @Override
    public IValidator<FirmwareRule> getValidator() {
        return firmwareRuleValidator;
    }

    @Override
    public void normalizeOnSave(FirmwareRule firmwareRule) {
        if(firmwareRule != null && firmwareRule.getRule() != null) {
            RuleUtil.normalizeConditions(firmwareRule.getRule());
        }
    }

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    public List<FirmwareRule> filterByTemplate(List<FirmwareRule> firmwareRules, String templateId, String applicationType) {
        return firmwareRules.stream()
                .filter(firmwareRulePredicates.byTemplate(templateId))
                .filter(byApplication(applicationType))
                .collect(Collectors.toList());
    }

    public List<FirmwareRule> filterByActionType(final List<FirmwareRule> firmwareRules, final ApplicableAction.Type type) {
        return firmwareRules
                .stream()
                .filter(firmwareRulePredicates.byEditableTemplate())
                .filter(firmwareRulePredicates.byActionType(type.name()))
                .collect(Collectors.toList());
    }

    public List<FirmwareRule> filterByActionAndApplicationTypes(ApplicableAction.Type actionType, String applicationType) {
        return getAll().stream().filter(firmwareRulePredicates.byEditableTemplate())
                .filter(firmwareRulePredicates.byActionType(actionType.name()))
                .filter(byApplication(applicationType))
                .collect(Collectors.toList());
    }

    public List<FirmwareRule> filterByApplicationType(final List<FirmwareRule> firmwareRules, String applicationType) {
        return firmwareRules
                .stream()
                .filter(byApplication(applicationType))
                .collect(Collectors.toList());
    }

    public List<FirmwareRule> getByCurrentApplication() {
        return filterByApplicationType(getAll(), permissionService.getReadApplication());
    }

    public Set<String> getFirmwareRuleNames(String templateId) {
        List<FirmwareRule> firmwareRulesByTemplate = filterByTemplate(getAll(), templateId, permissionService.getReadApplication());
        return FluentIterable.from(firmwareRulesByTemplate)
                .transform(new Function<FirmwareRule, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable FirmwareRule input) {
                        return input.getName();
                    }
                }).toSet();
    }

    public Map<String, String> getIdToNameMap(String type) {
        final Map<String, String> nameMap = new HashMap<>();
        for (FirmwareRule rule : getAll()) {
            if (rule.getType().equals(type)) {
                nameMap.put(rule.getId(), rule.getName());
            }
        }

        return nameMap;
    }

    @Override
    public void validateOnSave(FirmwareRule firmwareRule) {
        getValidator().validate(firmwareRule);
        getValidator().validateAll(firmwareRule, getAll());
    }

    @Override
    protected List<Predicate<FirmwareRule>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());
        return firmwareRulePredicates.getPredicates(contextOptional);
    }
}