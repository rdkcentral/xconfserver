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
 * Author: mdolina
 * Created: 01/06/20  12:00 PM
 */
package com.comcast.xconf.service.rfc;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.priority.PriorityUtils;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.ContextValidator;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.search.rfc.FeatureRulePredicates;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.rfc.FeatureRuleDataServiceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class FeatureRuleDataService extends AbstractService<FeatureRule> {

    @Autowired
    private FeatureRuleDataServiceValidator featureDataServiceValidator;

    @Autowired
    private CachedSimpleDao<String, FeatureRule> featureRuleDAO;

    @Autowired
    private FeatureRulePredicates featureRuleSearchService;

    private static Logger logger = LoggerFactory.getLogger(FeatureRuleDataService.class);

    @Override
    public IValidator getValidator() {
        return featureDataServiceValidator;
    }

    public List<FeatureRule> getAll(String applicationType) {
        return findByContext(Collections.singletonMap(SearchFields.APPLICATION_TYPE, applicationType));
    }

    @Override
    public List<FeatureRule> getAll() {
        return getEntityDAO().getAll();
    }

    @Override
    public CachedSimpleDao<String, FeatureRule> getEntityDAO() {
        return featureRuleDAO;
    }

    public List<FeatureRule> findByContext(final Map<String, String> searchContext) {
        ContextValidator.validateContext(searchContext);
        return super.findByContext(searchContext);
    }

    @Override
    protected List<Predicate<FeatureRule>> getPredicatesByContext(Map<String, String> context) {
        return featureRuleSearchService.getPredicates(new ContextOptional(context));
    }

    private void saveAll(List<FeatureRule> featureRules) {
        for (FeatureRule featureRule : featureRules) {
            getEntityDAO().setOne(featureRule.getId(), featureRule);
        }
    }

    @Override
    public FeatureRule create(FeatureRule featureRule) {
        beforeCreating(featureRule);
        beforeSaving(featureRule);
        saveAll(PriorityUtils.addNewItemAndReorganize(featureRule, getAll(ApplicationType.get(featureRule.getApplicationType()))));
        return featureRule;
    }

    @Override
    public FeatureRule update(FeatureRule featureRule) {
        beforeUpdating(featureRule);
        beforeSaving(featureRule);
        FeatureRule featureRuleToUpdate = getEntityDAO().getOne(featureRule.getId());
        saveAll(PriorityUtils.updateItemByPriorityAndReorganize(featureRule, getAll(ApplicationType.get(featureRule.getApplicationType())), featureRuleToUpdate.getPriority()));
        return featureRule;
    }

    @Override
    public FeatureRule delete(final String id) {
        FeatureRule removedFeatureRule = super.delete(id);
        try {
            saveAll(PriorityUtils.packPriorities(getAll(ApplicationType.get(removedFeatureRule.getApplicationType()))));
        } catch (ValidationRuntimeException e) {
            logger.error("Failed to save all" + e);
        }
        return removedFeatureRule;
    }

    @Override
    public void normalizeOnSave(FeatureRule featureRule) {
        if(featureRule != null && featureRule.getRule() != null) {
            RuleUtil.normalizeConditions(featureRule.getRule());
        }
    }
}
