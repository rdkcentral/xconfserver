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
 * Created: 4/19/2017
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.exception.EntityExistsException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.search.firmware.FirmwareRulePredicates;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.firmware.PercentageBeanDataServiceValidator;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Component
public class PercentageBeanQueriesService extends PercentageBeanService {

    @Autowired
    private PercentageBeanQueriesHelper helper;

    @Autowired
    private PercentageBeanDataServiceValidator percentageBeanDataServiceValidator;

    @Autowired
    private FirmwareRulePredicates firmwareRulePredicates;

    @Override
    public IValidator<PercentageBean> getValidator() {
        return percentageBeanDataServiceValidator;
    }
    private static final String PERCENTAGE_FIELD_NAME = "percentage";

    public PercentageBean getOne(String id) {
        PercentageBean bean = helper.replaceConfigIdWithFirmwareVersion(super.getOne(id));
        return QueriesHelper.nullifyUnwantedFields(bean);
    }

    public List<PercentageBean> getAll(String applicationType) {
        return firmwareRuleDao.getAll()
                .stream()
                .filter(firmwareRulePredicates.byTemplate(TemplateNames.ENV_MODEL_RULE))
                .filter(byApplication(applicationType))
                .map(firmwareRule -> converter.convertIntoBean(firmwareRule))
                .map(percentageBean -> helper.replaceConfigIdWithFirmwareVersion(percentageBean))
                .map(percentageBean -> QueriesHelper.nullifyUnwantedFields(percentageBean))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public PercentageBean delete(String id) {
        PercentageBean percentageBean = getOne(id);
        if (percentageBean == null) {
            throw new EntityNotFoundException("Entity with id: " + id + " does not exist");
        }
        validateUsage(id);
        getEntityDAO().deleteOne(id);

        return percentageBean;
    }

    @Override
    protected void beforeSaving(PercentageBean bean) {
        if (!CollectionUtils.isEmpty(bean.getDistributions())) {
            Collections.sort(bean.getDistributions());
        }
        getValidator().validate(bean);
        getValidator().validateAll(bean, getAll(bean.getApplicationType()));
    }

    @Override
    protected void beforeCreating(PercentageBean entity) {
        String id = entity.getId();
        if (StringUtils.isBlank(id)) {
            entity.setId(UUID.randomUUID().toString());
        } else if (getEntityDAO().getOne(id, false) != null) {
            throw new EntityExistsException("Entity with id: " + id + " already exists");
        }
    }

    @Override
    protected void beforeUpdating(PercentageBean percentageBean) {
        String id = percentageBean.getId();
        if (StringUtils.isBlank(id)) {
            throw new ValidationRuntimeException("Entity id is empty");
        }
        Object firmwareRule = getEntityDAO().getOne(id, false);
        if (firmwareRule == null) {
            throw new EntityNotFoundException("Entity with id: " + id + " does not exist");
        }
    }

    public Map<String, Set<Object>> getPercentFilterFieldValues(String fieldName, String applicationType) throws IllegalAccessException {
        Set<Object> resultFieldValues = getGlobalPercentageFields(fieldName, applicationType);
        resultFieldValues.addAll(getPercentageBeanFieldValues(fieldName, applicationType));
        return Collections.singletonMap(fieldName, resultFieldValues);
    }

    private Set<Object> getPercentageBeanFieldValues(String fieldName, String applicationType) throws IllegalAccessException {
        Set<Object> resultFieldValues = new HashSet<>();
        List<Field> percentageBeanFields = Lists.newArrayList(PercentageBean.class.getDeclaredFields());
        for (PercentageBean percentageBean : getAll(applicationType)) {
            for (Field field : percentageBeanFields) {
                if (StringUtils.equals(field.getName(), fieldName)) {
                    addValue(field, percentageBean, resultFieldValues);
                }
            }
        }
        return resultFieldValues;
    }

    private Set<Object> getGlobalPercentageFields(String fieldName, String applicationType) throws IllegalAccessException {
        String globalPercentageId = getGlobalPercentageIdByApplication(applicationType);
        FirmwareRule globalPercentageRule = firmwareRuleDao.getOne(globalPercentageId);
        Set<Object> resultFieldValues = new HashSet<>();
        if (globalPercentageRule == null) {
            if (PERCENTAGE_FIELD_NAME.equals(fieldName)) {
                resultFieldValues.add(100);
            }
            return resultFieldValues;
        }
        GlobalPercentage globalPercentage = converter.convertIntoGlobalPercentage(globalPercentageRule);
        for (Field field : GlobalPercentage.class.getDeclaredFields()) {
             if (StringUtils.equals(field.getName(), fieldName)) {
                 addValue(field, globalPercentage, resultFieldValues);
             }
        }
        return resultFieldValues;
    }

    private void addValue(Field field, Object baseObject, Set<Object> result) throws IllegalAccessException {
        field.setAccessible(true);
        Object fieldValue = field.get(baseObject);
        if (fieldValue == null) {
            return;
        }
        if(fieldValue instanceof Collection) {
            result.addAll((Collection) fieldValue);
        }  else {
            result.add(fieldValue);
        }
    }

    private String getGlobalPercentageIdByApplication(String applicationType) {
        if (ApplicationType.equals(applicationType, ApplicationType.STB)) {
            return TemplateNames.GLOBAL_PERCENT;
        }
        return applicationType.toUpperCase() + "_" + TemplateNames.GLOBAL_PERCENT;
    }
}
