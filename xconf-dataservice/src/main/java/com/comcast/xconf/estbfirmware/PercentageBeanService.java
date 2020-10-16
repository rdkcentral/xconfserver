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
 * Created: 12/1/2016
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.converter.PercentageBeanConverter;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.firmware.PercentageBeanPredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.firmware.PercentageBeanValidator;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class PercentageBeanService extends AbstractApplicationTypeAwareService<PercentageBean> {

    @Autowired
    private PercentageBeanValidator percentageBeanValidator;

    @Autowired
    protected PercentageBeanConverter converter;

    @Autowired
    protected CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    protected PercentageBeanPredicates searchService;

    @Autowired
    private FirmwarePermissionService permissionService;

    @Override
    public CachedSimpleDao getEntityDAO() {
        return firmwareRuleDao;
    }

    @Override
    public IValidator<PercentageBean> getValidator() {
        return percentageBeanValidator;
    }

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    public PercentageBean getOne(String id) {
        FirmwareRule one = firmwareRuleDao.getOne(id);
        if (one == null) {
            throw new EntityNotFoundException("Entity with id: " + id + " does not exist");
        }
        return converter.convertIntoBean(one);
    }

    @Override
    protected PercentageBean save(PercentageBean entity) {
        beforeSaving(entity);
        FirmwareRule firmwareRule = converter.convertIntoRule(entity);
        RuleUtil.normalizeConditions(firmwareRule.getRule());
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);
        return entity;
    }

    @Override
    protected List<Predicate<PercentageBean>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());

        return searchService.getPredicates(contextOptional);
    }

    @Override
    public List<PercentageBean> getAll() {
        List<PercentageBean> result = new ArrayList<>();
        Iterable<FirmwareRule> firmwareRules = Optional.presentInstances(firmwareRuleDao.asLoadingCache().asMap().values());
        String firmwareReadApplication = permissionService.getReadApplication();
        for (FirmwareRule rule : firmwareRules) {
            if (TemplateNames.ENV_MODEL_RULE.equals(rule.getType()) && ApplicationType.equals(firmwareReadApplication, rule.getApplicationType())) {
                result.add(converter.convertIntoBean(rule));
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
    protected void beforeSaving(PercentageBean bean) {
        if (bean != null && StringUtils.isBlank(bean.getApplicationType())) {
            bean.setApplicationType(permissionService.getWriteApplication());
        }
        Collections.sort(bean.getDistributions());
        getValidator().validate(bean);
        getValidator().validateAll(bean, getAll());
    }
}
