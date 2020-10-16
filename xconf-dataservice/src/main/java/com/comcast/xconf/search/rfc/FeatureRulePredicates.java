/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.search.rfc;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.XRulePredicates;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class FeatureRulePredicates extends XRulePredicates {

    @Autowired
    private CachedSimpleDao<String, Feature> featureDAO;

    public Predicate<FeatureRule> byFeatureInstance(String featureInstance) {
        return featureRule -> {
            if (Objects.nonNull(featureRule) && CollectionUtils.isNotEmpty(featureRule.getFeatureIds())) {
                for (String featureId : featureRule.getFeatureIds()) {
                    Feature feature = featureDAO.getOne(featureId);
                    if (Objects.nonNull(feature) && StringUtils.containsIgnoreCase(feature.getFeatureName(), featureInstance)) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    public List<Predicate<FeatureRule>> getPredicates(ContextOptional context) {
        List<Predicate<FeatureRule>> predicates = new ArrayList<>();
        predicates.addAll(getBaseRulePredicates(context));

        context.getFeatureInstance().ifPresent(featureName -> predicates.add(byFeatureInstance(featureName)));
        context.getApplicationType().ifPresent(application -> predicates.add(byApplication(application)));

        return predicates;
    }
}
