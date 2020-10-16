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
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureExport;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.ContextValidator;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.search.rfc.FeaturePredicates;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.rfc.FeatureDataServiceValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class FeatureDataService extends AbstractService<Feature> {

    @Autowired
    private FeatureDataServiceValidator featureDataServiceValidator;

    @Autowired
    private FeatureRuleDataService featureRuleService;

    @Autowired
    private CachedSimpleDao<String, Feature> featureDAO;

    @Autowired
    private FeaturePredicates featurePredicates;

    @Override
    public IValidator getValidator() {
        return featureDataServiceValidator;
    }

    @Override
    protected void validateUsage(String id) {
        for (FeatureRule featureRule: featureRuleService.getAll()) {
            if (featureRule.getFeatureIds().contains(id)) {
                throw new EntityConflictException("This Feature linked to FeatureRule with name: " + featureRule.getName());
            }
        }
    }

    @Override
    protected void beforeUpdating(Feature feature) {
        String id = feature.getId();
        if (StringUtils.isBlank(id)) {
            throw new ValidationRuntimeException("Entity id is empty");
        }
        IPersistable existingFeature = getEntityDAO().getOne(id, false);
        if (existingFeature == null) {
            throw new ValidationRuntimeException("Entity with id: " + id + " does not exist");
        }
    }

    public List<Feature> getAll(String applicationType) {
        return findByContext(Collections.singletonMap(SearchFields.APPLICATION_TYPE, applicationType));
    }

    @Override
    public List<Feature> getAll() {
        return wrapByFeatureExport(getEntityDAO().getAll());
    }

    @Override
    public CachedSimpleDao<String, Feature> getEntityDAO() {
        return featureDAO;
    }

    public List<Feature> findByContext(final Map<String, String> searchContext) {
        ContextValidator.validateContext(searchContext);
        List<Feature> foundFeatures = super.findByContext(searchContext);
        return wrapByFeatureExport(foundFeatures);
    }

    @Override
    protected List<Predicate<Feature>> getPredicatesByContext(Map<String, String> context) {
        return featurePredicates.getPredicates(new ContextOptional(context));
    }

    private List<Feature> wrapByFeatureExport(List<Feature> features) {
        return features.stream()
                .map(FeatureExport::new)
                .collect(Collectors.toList());
    }
}
