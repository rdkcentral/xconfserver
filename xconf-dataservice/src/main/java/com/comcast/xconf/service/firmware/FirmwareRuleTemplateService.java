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
 * limitations under the License.=
 *
 * Author: Stanislav Menshykov
 * Created: 18.01.16  18:34
 */
package com.comcast.xconf.service.firmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.exception.EntityExistsException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.importing.OverwritePrioritizableWrapperComparator;
import com.comcast.xconf.importing.OverwriteWrapper;
import com.comcast.xconf.priority.PriorityUtils;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.firmware.FirmwareRuleTemplatePredicates;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.firmware.FirmwareRuleTemplateValidator;
import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class FirmwareRuleTemplateService extends AbstractService<FirmwareRuleTemplate> {

    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    @Autowired
    private FirmwareRuleService firmwareRuleService;

    @Autowired
    private FirmwareRuleTemplateValidator firmwareRuleTemplateValidator;

    @Autowired
    private FirmwareRuleTemplatePredicates templatePredicates;

    private static Logger log = LoggerFactory.getLogger(FirmwareRuleTemplateService.class);


    @Override
    public FirmwareRuleTemplate save(FirmwareRuleTemplate template) {
        List<FirmwareRuleTemplate> templatesOfCurrentType = getTemplatesByType(template.getApplicableAction().getActionType(), firmwareRuleTemplateDao.getAll());
        saveAll(PriorityUtils.addNewItemAndReorganize(template, templatesOfCurrentType));

        return template;
    }

    @Override
    public FirmwareRuleTemplate create(FirmwareRuleTemplate template) {
        final String id = template.getId();
        if (id != null && firmwareRuleTemplateDao.getOne(id) != null) {
            throw new EntityExistsException("FirmwareRuleTemplate with id " + id + " already exists");
        }
        firmwareRuleTemplateValidator.validate(template);
        List<FirmwareRuleTemplate> templatesOfCurrentType = getTemplatesByType(template.getApplicableAction().getActionType(), firmwareRuleTemplateDao.getAll());
        firmwareRuleTemplateValidator.validateAll(template, templatesOfCurrentType);
        saveAll(PriorityUtils.addNewItemAndReorganize(template, templatesOfCurrentType));

        return template;
    }

    @Override
    public FirmwareRuleTemplate update(FirmwareRuleTemplate template) {
        FirmwareRuleTemplate templateToUpdate = firmwareRuleTemplateDao.getOne(template.getId());
        if (templateToUpdate == null) {
            throw  new EntityNotFoundException("FirmwareRuleTemplate " + template.getId() + " doesn't exist");
        }
        firmwareRuleTemplateValidator.validate(template);
        List<FirmwareRuleTemplate> templatesOfCurrentType = getTemplatesByType(template.getApplicableAction().getActionType(), firmwareRuleTemplateDao.getAll());
        firmwareRuleTemplateValidator.validateAll(templateToUpdate, templatesOfCurrentType);
        saveAll(PriorityUtils.updateItemByPriorityAndReorganize(template, templatesOfCurrentType, templateToUpdate.getPriority()));

        return template;
    }

    @Override
    public FirmwareRuleTemplate delete(String id) {
        FirmwareRuleTemplate templateToDelete = firmwareRuleTemplateDao.getOne(id);
        super.delete(id);

        saveAll(PriorityUtils.packPriorities(getTemplatesByType(templateToDelete.getApplicableAction().getActionType(),
                firmwareRuleTemplateDao.getAll())));

        return templateToDelete;
    }

    @Override
    public CachedSimpleDao<String, FirmwareRuleTemplate> getEntityDAO() {
        return firmwareRuleTemplateDao;
    }

    @Override
    public IValidator<FirmwareRuleTemplate> getValidator() {
        return firmwareRuleTemplateValidator;
    }

    public Map<String, List<String>> importTemplates(List<OverwriteWrapper<FirmwareRuleTemplate>> wrappedTemplates) {
        final List<String> failedToImport = new ArrayList<>();
        final List<String> successfulImportIds = new ArrayList<>();
        Collections.sort(wrappedTemplates, new OverwritePrioritizableWrapperComparator());
        for (OverwriteWrapper<FirmwareRuleTemplate> wrappedTemplate : wrappedTemplates) {
            FirmwareRuleTemplate currentTemplate = wrappedTemplate.getEntity();
            String currentTemplateId = currentTemplate.getId();
            if (wrappedTemplate.getOverwrite()) {
                try {
                    update(currentTemplate);
                    successfulImportIds.add(currentTemplateId);
                } catch (RuntimeException e) {
                    failedToImport.add(e.getMessage());
                }
            } else {
                try {
                    create(currentTemplate);
                    successfulImportIds.add(currentTemplateId);
                } catch (RuntimeException e) {
                    failedToImport.add(e.getMessage());
                }
            }
        }

        return new HashMap<String, List<String>>(){{
            put("success", successfulImportIds);
            put("failure", failedToImport);
        }};
    }

    public void saveAll(List<FirmwareRuleTemplate> templateList) {
        for (FirmwareRuleTemplate template : templateList) {
            firmwareRuleTemplateDao.setOne(template.getId(), template);
        }
    }

    private List<FirmwareRuleTemplate> getTemplatesByType(final ApplicableAction.Type type, List<FirmwareRuleTemplate> templates) {
        return templates
                .stream()
                .filter(templatePredicates.byActionType(type.name()))
                .collect(Collectors.toList());
    }

    @Override
    protected void validateUsage(String id) {
        FirmwareRuleTemplate template = getOne(id);
        List<FirmwareRule> firmwareRulesByTemplate = firmwareRuleService.filterByTemplate(firmwareRuleService.getEntityDAO().getAll(), template.getId(), ApplicationType.ALL);
        if (CollectionUtils.isNotEmpty(firmwareRulesByTemplate)) {
            Set<String> ruleNames = getFirmwareRuleNames(firmwareRulesByTemplate);
            throw new EntityConflictException("Template " + template.getId() + " is used by rules: " + Joiner.on(", ").join(ruleNames));
        }
    }

    public List<FirmwareRuleTemplate> getByTypeAndEditableOption(ApplicableAction.Type type, boolean isEditable) {
        return getAll()
                .stream()
                .filter(templatePredicates.byActionType(type.name()))
                .filter(templatePredicates.byEditable(isEditable))
                .collect(Collectors.toList());
    }

    public List<String> getTemplateIds(final ApplicableAction.Type type) {
        return getByTypeAndEditableOption(type, true)
                .stream()
                .map(FirmwareRuleTemplate::getId)
                .collect(Collectors.toList());
    }

    public List<FirmwareRuleTemplate> changePriorities(String templateId, Integer newPriority) {
        final FirmwareRuleTemplate templateToUpdate = getOne(templateId);
        Integer oldPriority = templateToUpdate.getPriority();
        List<FirmwareRuleTemplate> templatesOfCurrentType = getTemplatesByType(templateToUpdate.getApplicableAction().getActionType(), getAll());
        List<FirmwareRuleTemplate> reorganizedTemplates = PriorityUtils.updatePriorities(templatesOfCurrentType, oldPriority, newPriority);
        saveAll(reorganizedTemplates);
        log.info("Priority of FirmwareRuleTemplate " + templateId + " has been changed, oldPriority=" + oldPriority + ", newPriority=" + newPriority);

        return reorganizedTemplates;
    }

    @Override
    public List<Predicate<FirmwareRuleTemplate>> getPredicatesByContext(Map<String, String> context) {
        return templatePredicates.getPredicates(new ContextOptional(context));
    }

    @Override
    protected void normalizeOnSave(FirmwareRuleTemplate firmwareRuleTemplate) {
        if (firmwareRuleTemplate != null) {
            RuleUtil.normalizeConditions(firmwareRuleTemplate.getRule());
        }
    }

    private Set<String> getFirmwareRuleNames(List<FirmwareRule> firmwareRules) {
        Set<String> names = new HashSet<>();
        for (FirmwareRule firmwareRule : firmwareRules) {
            names.add(firmwareRule.getName());
        }
        return names;
    }

}
