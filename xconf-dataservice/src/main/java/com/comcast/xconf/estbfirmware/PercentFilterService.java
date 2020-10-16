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
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.comcast.xconf.estbfirmware.converter.PercentageBeanConverter;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.queries.beans.EnvModelPercentageWrapper;
import com.comcast.xconf.queries.beans.PercentFilterWrapper;
import com.comcast.xconf.search.firmware.FirmwareRulePredicates;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.comcast.xconf.estbfirmware.TemplateNames.ENV_MODEL_RULE;
import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

/**
 * User: ikostrov
 * Date: 13.08.14
 * Time: 20:48
 */
@Service
public class PercentFilterService {

    private final String envModelPercentagesFieldName = "envModelPercentages";
    private final String intermediateVersionFieldName = "intermediateVersion";
    private final String lastKnownGoodFieldName = "lastKnownGood";

    @Autowired
    private PercentageBeanConverter converter;

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    @Autowired
    protected CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    protected CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private FirmwareRulePredicates firmwareRulePredicates;

    public void save(PercentFilterValue filter, String applicationType) {
        FirmwareRule globalPercentage = converter.convertIntoGlobalPercentage(filter, applicationType);
        if (globalPercentage != null) {
            globalPercentage.setApplicationType(ApplicationType.get(applicationType));
            firmwareRuleDao.setOne(globalPercentage.getId(), globalPercentage);
        }

        for (FirmwareRule firmwareRule : getEnvModelFirmwareRules(applicationType)) {
            EnvModelPercentage envModelPercentage = getEnvModelPercentage(filter, firmwareRule.getName());
            if (envModelPercentage != null) {
                PercentageBean percentageBean = converter.migrateIntoPercentageBean(envModelPercentage, firmwareRule);
                FirmwareRule convertedRule = converter.convertIntoRule(percentageBean);
                convertedRule.setApplicationType(ApplicationType.get(applicationType));
                firmwareRuleDao.setOne(convertedRule.getId(), convertedRule);
            }
        }
    }

    private Iterable<FirmwareRule> getEnvModelFirmwareRules(String applicationType) {
        return firmwareRuleDao.getAll()
                .stream()
                .filter(firmwareRulePredicates.byTemplate(ENV_MODEL_RULE))
                .filter(byApplication(applicationType))
                .collect(Collectors.toSet());
    }


    private EnvModelPercentage getEnvModelPercentage(PercentFilterValue filter, String name) {
        if (filter.getEnvModelPercentages() != null) {
            return filter.getEnvModelPercentages().get(name);
        } else {
            return null;
        }
    }

    public PercentFilterValue get(String applicationType) {
        PercentFilterValue percentFilterValue = new PercentFilterValue();
        String globalPercentageId = getGlobalPercentageIdByApplication(applicationType);
        FirmwareRule globalPercentageRule = firmwareRuleDao.getOne(globalPercentageId);
        if (globalPercentageRule != null) {
            GlobalPercentage globalPercentage = converter.convertIntoGlobalPercentage(globalPercentageRule);
            percentFilterValue.setPercentage(globalPercentage.getPercentage());
            if (StringUtils.isNotBlank(globalPercentage.getWhitelist())) {
                percentFilterValue.setWhitelist(getIpAddressGroup(globalPercentage.getWhitelist()));
            }
        }
        percentFilterValue.setEnvModelPercentages(new HashMap<String, EnvModelPercentage>());
        for (FirmwareRule firmwareRule : getEnvModelFirmwareRules(applicationType)) {
            PercentageBean percentageBean = converter.convertIntoBean(firmwareRule);
            EnvModelPercentage envModelPercentage = convertPercentageBean(percentageBean);
            percentFilterValue.getEnvModelPercentages().put(firmwareRule.getName(), envModelPercentage);
        }
        return percentFilterValue;
    }

    private EnvModelPercentage convertPercentageBean(PercentageBean bean) {
        EnvModelPercentage percentage = new EnvModelPercentage();
        percentage.setActive(bean.isActive());
        if (StringUtils.isNotBlank(bean.getWhitelist())) {
            percentage.setWhitelist(getIpAddressGroup(bean.getWhitelist()));
        }
        percentage.setFirmwareCheckRequired(bean.isFirmwareCheckRequired());
        percentage.setLastKnownGood(bean.getLastKnownGood());
        percentage.setFirmwareVersions(bean.getFirmwareVersions());
        percentage.setIntermediateVersion(bean.getIntermediateVersion());
        percentage.setRebootImmediately(bean.isRebootImmediately());
        percentage.setPercentage(getPercentageSum(bean.getDistributions()));
        return percentage;
    }

    private double getPercentageSum(List<RuleAction.ConfigEntry> distribution) {
        double total = 0.0;
        if (distribution == null) {
            return total;
        }
        for (RuleAction.ConfigEntry entry : distribution) {
            if (entry.getPercentage() != null) {
                total += entry.getPercentage();
            }
        }
        return total;
    }

    private IpAddressGroup getIpAddressGroup(String groupId) {
        GenericNamespacedList list = genericNamespacedListQueriesService.getListById(groupId);
        return list != null ? GenericNamespacedListsConverter.convertToIpAddressGroup(list) : null;
    }

    public Map<String, Set<Object>> getPercentFilterFieldValues(String fieldName, String applicationType) throws IllegalAccessException {
        PercentFilterValue percentFilter = get(applicationType);
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
        } else if (fieldValue instanceof String && org.apache.commons.lang.StringUtils.isNotBlank((String) fieldValue)) {
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

    private String getGlobalPercentageIdByApplication(String applicationType) {
        if (ApplicationType.equals(ApplicationType.STB, applicationType)) {
            return TemplateNames.GLOBAL_PERCENT;
        }
        return applicationType.toUpperCase() + "_" + TemplateNames.GLOBAL_PERCENT;
    }

    public PercentFilterWrapper toHumanReadableForm(PercentFilterWrapper filter) {
        List<EnvModelPercentageWrapper> percentages = filter.getEnvModelPercentageWrappers();
        if (percentages != null) {
            for (EnvModelPercentage percentage : percentages) {
                percentage.setLastKnownGood(getFirmwareVersion(percentage.getLastKnownGood()));
                percentage.setIntermediateVersion(getFirmwareVersion(percentage.getIntermediateVersion()));
            }
        }
        return filter;
    }

    private String getFirmwareVersion(String id) {
        if (StringUtils.isBlank(id))
            return "";

        FirmwareConfig one = firmwareConfigDAO.getOne(id);
        return one != null ? one.getFirmwareVersion() : "";
    }
}
