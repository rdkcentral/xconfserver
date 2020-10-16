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
 * Created: 11/02/16  12:00 PM
 */
package com.comcast.xconf.service.rfc;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.search.rfc.FeaturePredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.rfc.FeatureValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class FeatureService extends AbstractApplicationTypeAwareService<Feature> {

    @Autowired
    private CachedSimpleDao<String, Feature> featureDAO;

    @Autowired
    private FeatureValidator featureValidator;

    @Autowired
    public FeatureRuleService featureRuleService;

    @Autowired
    private DcmPermissionService permissionService;

    @Autowired
    private FeaturePredicates featurePredicates;

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    public CachedSimpleDao<String, Feature> getEntityDAO() {
        return featureDAO;
    }

    @Override
    public IValidator getValidator() {
        return featureValidator;
    }

    @Override
    protected List<Predicate<Feature>> getPredicatesByContext(Map<String, String> context) {
        if (StringUtils.isBlank(context.get(SearchFields.APPLICATION_TYPE))) {
            context.put(SearchFields.APPLICATION_TYPE, permissionService.getReadApplication());
        }
        return featurePredicates.getPredicates(new ContextOptional(context));
    }

    @Override
    protected void validateUsage(String id) {
        for (FeatureRule featureRule: featureRuleService.getAll()) {
            if (featureRule.getFeatureIds().contains(id)) {
                throw new EntityConflictException("This Feature linked to FeatureRule with name: " + featureRule.getName());
            }
        }
    }

    public List<Feature> getFeaturesByIdList(List<String> featureIdList) {
        List<Feature> features = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(featureIdList)) {
            for (String featureId : featureIdList) {
                 Feature feature = getEntityDAO().getOne(featureId);
                 if (feature != null) {
                     features.add(feature);
                 }
            }
        }
        return features;
    }
}
