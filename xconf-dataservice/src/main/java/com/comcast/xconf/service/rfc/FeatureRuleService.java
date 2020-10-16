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
 * Author: Yury Stagit
 * Created: 11/06/16  12:00 PM
 */
package com.comcast.xconf.service.rfc;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.hydra.astyanax.config.XconfSpecificConfig;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.priority.PriorityUtils;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.rfc.FeatureRulePredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.rfc.FeatureRuleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class FeatureRuleService extends AbstractApplicationTypeAwareService<FeatureRule> {

    @Autowired
    private CachedSimpleDao<String, FeatureRule> featureRuleDAO;

    @Autowired
    private FeatureRuleValidator featureRuleValidator;

    @Autowired
    private DcmPermissionService permissionService;

    @Autowired
    private XconfSpecificConfig xconfSpecificConfig;

    @Autowired
    private FeatureRulePredicates featureRuleSearchService;

    private static Logger log = LoggerFactory.getLogger(FeatureService.class);

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    public CachedSimpleDao<String, FeatureRule> getEntityDAO() {
        return featureRuleDAO;
    }

    @Override
    public IValidator<FeatureRule> getValidator() {
        return featureRuleValidator;
    }

    @Override
    protected List<Predicate<FeatureRule>> getPredicatesByContext(Map<String, String> context) {
        return featureRuleSearchService.getPredicates(new ContextOptional(context));
    }

    public List<FeatureRule> changePriorities(String featureRuleId, Integer newPriority) {
        final FeatureRule featureRuleToUpdate = getOne(featureRuleId);
        Integer oldPriority = featureRuleToUpdate.getPriority();
        List<FeatureRule> reorganizedFeatureRules = PriorityUtils.updatePriorities(getAll(), featureRuleToUpdate.getPriority(), newPriority);
        saveAll(reorganizedFeatureRules);
        log.info("Priority of FeatureRule " + featureRuleId + " has been changed, oldPriority=" + oldPriority + ", newPriority=" + newPriority);
        return reorganizedFeatureRules;
    }

    private void saveAll(List<FeatureRule> featureRules) {
        for (FeatureRule featureRule : featureRules) {
            getEntityDAO().setOne(featureRule.getId(), featureRule);
        }
    }

    public Integer getAllowedNumberOfFeatures() {
        return xconfSpecificConfig.getAllowedNumberOfFeatures();
    }

    @Override
    public FeatureRule create(FeatureRule featureRule) {
        beforeCreating(featureRule);
        beforeSaving(featureRule);
        saveAll(PriorityUtils.addNewItemAndReorganize(featureRule, getAll()));
        return featureRule;
    }

    @Override
    public FeatureRule update(FeatureRule featureRule) {
        beforeUpdating(featureRule);
        beforeSaving(featureRule);
        FeatureRule featureRuleToUpdate = getEntityDAO().getOne(featureRule.getId());
        saveAll(PriorityUtils.updateItemByPriorityAndReorganize(featureRule, getAll(), featureRuleToUpdate.getPriority()));
        return featureRule;
    }

    @Override
    public FeatureRule delete(final String id) {
        FeatureRule removedFeatureRule = super.delete(id);
        saveAll(PriorityUtils.packPriorities(getAll()));
        return removedFeatureRule;
    }

    @Override
    public void normalizeOnSave(FeatureRule featureRule) {
        if(featureRule != null && featureRule.getRule() != null) {
            RuleUtil.normalizeConditions(featureRule.getRule());
        }
    }
}