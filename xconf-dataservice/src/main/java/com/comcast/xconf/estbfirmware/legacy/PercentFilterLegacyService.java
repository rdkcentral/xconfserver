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
package com.comcast.xconf.estbfirmware.legacy;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.beans.PercentFilterWrapper;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

/**
 * User: ikostrov
 * Date: 13.08.14
 * Time: 20:48
 */
@Service
@Deprecated
public class PercentFilterLegacyService {

    private final String envModelPercentagesFieldName = "envModelPercentages";
    private final String intermediateVersionFieldName = "intermediateVersion";
    private final String lastKnownGoodFieldName = "lastKnownGood";

    @Autowired
    protected CachedSimpleDao<String, SingletonFilterValue> singletonFilterValueDAO;

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    protected CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    public void save(PercentFilterValue filter) {
        if(filter.getId() == null){
            filter.setId(PercentFilterValue.SINGLETON_ID);
        }
        if (filter.getEnvModelPercentages() != null) {
            Map<String, EnvModelPercentage> newMap = new HashMap<>();
            for (String key : filter.getEnvModelPercentages().keySet()) {
                EnvModelPercentage percentage = filter.getEnvModelPercentages().get(key);
                if ((percentage.isActive()
                        || percentage.isFirmwareCheckRequired()
                        || percentage.getPercentage() != 100
                        || percentage.getWhitelist() != null) && percentage.getPercentage() >= 0 && percentage.getPercentage() <= 100) {

                    if (!percentage.isFirmwareCheckRequired()) {
                        percentage.setRebootImmediately(false);
                    }
                    newMap.put(key, percentage);
                }
            }
            filter.setEnvModelPercentages(newMap);
        }
        singletonFilterValueDAO.setOne(PercentFilterValue.SINGLETON_ID, filter);
    }

    public PercentFilterValue getRaw() {
        PercentFilterValue filter = (PercentFilterValue) singletonFilterValueDAO.getOne(PercentFilterValue.SINGLETON_ID, false);
        if (filter == null) {
            filter = new PercentFilterValue();
        }
        return filter;
    }

    public PercentFilterValue get() {
        PercentFilterValue filter = (PercentFilterValue) singletonFilterValueDAO.getOne(PercentFilterValue.SINGLETON_ID);
        if (filter == null) {
            filter = new PercentFilterValue();
        }

        // read env/model rule percentages
        Map<String, EnvModelPercentage> newMap = new TreeMap<>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });
        List<String> envModelNames = getAllEnvModelRuleNames();
        for (String name : envModelNames) {
            EnvModelPercentage percentage = null;
            if (filter.getEnvModelPercentages() != null) {
                percentage = filter.getEnvModelPercentages().get(name);
            }
            if (percentage == null) {
                percentage = new EnvModelPercentage();
                percentage.setPercentage(100);
                percentage.setActive(false);
            }
            newMap.put(name, percentage);
        }
        filter.setEnvModelPercentages(newMap);

        return filter;
    }

    public Map<String, Set<Object>> getPercentFilterFieldValues(String fieldName) throws IllegalAccessException {
        PercentFilterValue percentFilter = get();
        Set<Object> fieldValues = new HashSet<>();
        Class percentFilterClass = null;
        if (percentFilter instanceof PercentFilterWrapper) {
            percentFilterClass = percentFilter.getClass().getSuperclass();
        } else {
            percentFilterClass = percentFilter.getClass();
        }
        for (Field field : percentFilterClass.getDeclaredFields()) {
            if (field.getName().equals(fieldName) && !field.getName().equals(envModelPercentagesFieldName)) {
                addFieldValue(field, percentFilter, fieldValues);
            } else if (field.getName().equals(envModelPercentagesFieldName)) {
                field.setAccessible(true);
                Map<String, EnvModelPercentage> envModelPercentages = (Map<String, EnvModelPercentage>) field.get(percentFilter);
                for (EnvModelPercentage envModelPercentage : envModelPercentages.values()) {
                    for (Field envModelPercentageField : envModelPercentage.getClass().getDeclaredFields()) {
                        if (envModelPercentageField.getName().equals(fieldName)) {
                            addFieldValue(envModelPercentageField, envModelPercentage, fieldValues);
                        }
                    }
                }
            }
        }
        if (fieldValues.isEmpty()) {
            throw new EntityNotFoundException("Field " + fieldName + " does not exist");
        }
        return Collections.singletonMap(fieldName, fieldValues);
    }

    private void addFieldValue(Field field, Object baseObject, Set<Object> fieldValues) throws IllegalAccessException {
        field.setAccessible(true);
        Object fieldValue = field.get(baseObject);
        if (fieldValue == null) {
            return;
        }
        if(fieldValue instanceof Collection) {
            fieldValues.addAll((Collection) fieldValue);
        } else if (fieldValue instanceof String && StringUtils.isNotBlank((String) fieldValue)) {
            if (intermediateVersionFieldName.equals(field.getName()) || lastKnownGoodFieldName.equals(field.getName())) {
                FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne((String) fieldValue);
                if (firmwareConfig != null) {
                    fieldValues.add(firmwareConfig.getFirmwareVersion());
                }
            }
        } else {
            fieldValues.add(fieldValue);
        }
    }

    public List<String> getAllEnvModelRuleNames() {
        return FluentIterable.from(Optional.presentInstances(firmwareRuleDao.asLoadingCache().asMap().values()))
                    .filter(new Predicate<FirmwareRule>() {
                        @Override
                        public boolean apply( FirmwareRule input) {
                            return TemplateNames.ENV_MODEL_RULE.equals(input.getType());
                        }
                    })
                .transform(new Function<FirmwareRule, String>() {
                    @Override
                    public String apply( FirmwareRule input) {
                        return input.getName();
                    }
                })
                .toList();
    }

    public void renameNamespacedListInAllWhitelist(String oldNamespacedListId, String newNamespacedListId) {
        PercentFilterValue percentFilter = get();
        if(percentFilter.getWhitelist() != null && oldNamespacedListId.equals(percentFilter.getWhitelist().getName())) {
            percentFilter.getWhitelist().setName(newNamespacedListId);
        }
        if (percentFilter.getEnvModelPercentages() != null) {
            for (EnvModelPercentage envModelPercentage : percentFilter.getEnvModelPercentages().values()) {
                if (envModelPercentage.getWhitelist() != null && envModelPercentage.getWhitelist().getName().equals(oldNamespacedListId)) {
                    envModelPercentage.getWhitelist().setName(newNamespacedListId);
                }
            }
        }
        save(percentFilter);
    }

    public void renameEnvModelRule(String oldRuleName, String newRuleName) {
        PercentFilterValue percentFilter = get();
        if (percentFilter.getEnvModelPercentages() != null && percentFilter.getEnvModelPercentages().containsKey(oldRuleName)) {
            EnvModelPercentage envModelPercentage = percentFilter.getEnvModelPercentages().get(oldRuleName);
            percentFilter.getEnvModelPercentages().put(newRuleName, envModelPercentage);
            percentFilter.getEnvModelPercentages().remove(oldRuleName);
        }
        save(percentFilter);
    }
}
