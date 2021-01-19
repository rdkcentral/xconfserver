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
package com.comcast.xconf.migration;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.TemplateFactory;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.utils.annotation.Migration;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.comcast.xconf.firmware.ApplicableAction.Type.DEFINE_PROPERTIES_TEMPLATE;

@RestController
@RequestMapping("migration")
@Migration
public class MigrationController {

    private static final Logger log = LoggerFactory.getLogger(MigrationController.class);

    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    @Autowired
    private TemplateFactory templateBuilder;

    @PostConstruct
    public void initializeTemplates() {
        if (firmwareRuleTemplateDao.getAll().size() == 0) {
            createTemplates(false);
        }
    }

    @RequestMapping(value = "/activationVersion", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity initActivationVersionTemplate() {
        FirmwareRuleTemplate activationVersionTemplate = templateBuilder.createActivationVersionTemplate();
        FirmwareRuleTemplate existingAMVTemplate = firmwareRuleTemplateDao.getOne(TemplateNames.ACTIVATION_VERSION);
        if (existingAMVTemplate != null && existingAMVTemplate.getPriority() != null) {
            activationVersionTemplate.setPriority(existingAMVTemplate.getPriority());
        } else {
            activationVersionTemplate.setPriority(getDefinePropertiesTemplateSize() + 1);
        }
        firmwareRuleTemplateDao.setOne(activationVersionTemplate.getId(), activationVersionTemplate);
        return new ResponseEntity<>("ActivationVersion template has been created", HttpStatus.OK);
    }

    private Integer getDefinePropertiesTemplateSize() {
        int count = 0;
        for (FirmwareRuleTemplate template : firmwareRuleTemplateDao.getAll()) {
            if (DEFINE_PROPERTIES_TEMPLATE.equals(template.getApplicableAction().getActionType())
                    && !TemplateNames.ACTIVATION_VERSION.equals(template.getId())) {
                count++;
            }
        }
        return count;
    }

    private String createTemplates(boolean override) {
        log.info("Creating templates...");

        List<FirmwareRuleTemplate> templates = Lists.newArrayList(
            templateBuilder.createMacRuleTemplate(),
            templateBuilder.createIpRuleTemplate(),
            templateBuilder.createIntermediateVersionRuleTemplate(),
            templateBuilder.createMinCheckRuleTemplate(),
            templateBuilder.createEnvModelRuleTemplate(),
            templateBuilder.createGlobalPercentTemplate(),
            templateBuilder.createIpFilterTemplate(),
            templateBuilder.createTimeFilterTemplate(),
            templateBuilder.createRiFilterTemplate(),
            templateBuilder.createMinCheckRiTemplate(),
            templateBuilder.createDownloadLocationTemplate(),
            templateBuilder.createActivationVersionTemplate()
        );

        if (!override) {
            filterOutExistingTemplates(templates);
        }

        return createAll(firmwareRuleTemplateDao, templates);
    }

    private void filterOutExistingTemplates(List<FirmwareRuleTemplate> templates) {
        // the cache might be not initialized at this point so we need to read directly from db
        List<FirmwareRuleTemplate> all = firmwareRuleTemplateDao.getAll();
        final List<String> keys = all.stream().map(XMLPersistable::getId).collect(Collectors.toList());
        log.info("Templates already exist: " + keys + ". They won't be changed.");
        templates.removeIf(next -> keys.contains(next.getId()));
        Map<ApplicableAction.Type, Integer> countersByType = countTemplatesByType(all);

        for (FirmwareRuleTemplate template : templates) {
            ApplicableAction.Type actionType = template.getApplicableAction().getActionType();
            Integer count = getCount(countersByType, actionType);
            count++;
            countersByType.put(actionType, count);
            template.setPriority(count);
        }
    }

    private Map<ApplicableAction.Type, Integer> countTemplatesByType(Iterable<FirmwareRuleTemplate> templates) {
        EnumMap<ApplicableAction.Type, Integer> map = new EnumMap<>(ApplicableAction.Type.class);
        for (FirmwareRuleTemplate template : templates) {
            ApplicableAction.Type actionType = template.getApplicableAction().getActionType();
            Integer count = getCount(map, actionType);
            count++;
            map.put(actionType, count);
        }
        return map;
    }

    private Integer getCount(Map<ApplicableAction.Type, Integer> map, ApplicableAction.Type actionType) {
        Integer count = map.get(actionType);
        return (count == null) ? 0 : count;
    }

    private <T extends IPersistable> String createAll(CachedSimpleDao<String, T> dao, List<T> list) {
        List<String> createdIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();
        for (T entity : list) {
            if (dao.getOne(entity.getId()) == null) {
                if (save(dao, entity)) {
                    createdIds.add(entity.getId());
                } else {
                    errorIds.add(entity.getId());
                }
            }
        }

        if (CollectionUtils.isNotEmpty(createdIds)) {
            log.info("Successfully created: " + createdIds);
        }
        if (CollectionUtils.isNotEmpty(errorIds)) {
            log.info("Failed to create: " + errorIds);
        }

        return "successfully saved: " + createdIds.size() + ", failed: " + errorIds.size() + ".";
    }

    private <T extends IPersistable> boolean save(CachedSimpleDao<String, T> dao, T entity) {
        try {
            dao.setOne(entity.getId(), entity);
            return true;
        } catch (Exception e) {
            log.error("Can't create: " + entity.getId(), e);
            return false;
        }
    }
}
