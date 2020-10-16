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
 *  Author: mdolina
 *  Created: 2:36 PM
 */
package com.comcast.xconf.featurecontrol;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.MacAddressUtil;
import com.comcast.xconf.SortingManager;
import com.comcast.xconf.converter.FeatureConverter;
import com.comcast.xconf.evaluators.RuleProcessorFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureControl;
import com.comcast.xconf.rfc.FeatureResponse;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.util.HashCalculator;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.comcast.xconf.logupload.LogUploaderContext.PARTNER_ID;

@Service
public class FeatureControlRuleBase {

    @Autowired
    private CachedSimpleDao<String, FeatureRule> featureRuleDAO;

    @Autowired
    private CachedSimpleDao<String, Feature> featureDAO;

    @Autowired
    private RuleProcessorFactory ruleProcessorFactory;

    @Autowired
    private FeatureConverter featureConverter;

    private static final Logger log = LoggerFactory.getLogger(FeatureControlRuleBase.class);

    public FeatureControl eval(Map<String, String> context, String applicationType) {
        FeatureControl featureControl = new FeatureControl();
        List<FeatureRule> appliedFeatureRules = processFeatureRules(context, applicationType);
        Map<String, FeatureResponse> featureResponseResult = new HashMap<>();
        if (CollectionUtils.isNotEmpty(appliedFeatureRules)) {
            for (FeatureRule featureRule : appliedFeatureRules) {
                addFeaturesToResult(featureResponseResult, featureRule.getFeatureIds());
            }
            featureControl.setFeatures(Sets.newHashSet(featureResponseResult.values()));
        }

        logFeatureInfo(context.toString(), appliedFeatureRules, featureControl);
        return featureControl;
    }

    public String calculateConfigSetHash(Set<FeatureResponse> features) {
        String configSetHash = null;
        try {
            configSetHash = HashCalculator.calculateHash(JsonUtil.toJson(features).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            log.error("Exception: ", e);
        }
        return configSetHash;
    }

    private void addFeaturesToResult(Map<String, FeatureResponse> featureResponseResult, List<String> featureIds) {
        if (CollectionUtils.isEmpty(featureIds)) {
            return;
        }
        for (String featureId : featureIds) {
            if (StringUtils.isBlank(featureId)) {
                continue;
            }
            Feature feature = featureDAO.getOne(featureId);
            if (feature != null && !featureResponseResult.containsKey(feature.getName())) {
                FeatureResponse featureResponse = featureConverter.toRfcResponse(feature);
                featureResponseResult.put(featureResponse.getName(), QueriesHelper.nullifyUnwantedFields(featureResponse));
            }
        }
    }

    public List<FeatureRule> processFeatureRules(final Map<String, String> context, final String applicationType) {
        List<FeatureRule> featureRules = SortingManager.sortRulesByPriorityAsc(Optional.presentInstances(featureRuleDAO.asLoadingCache().asMap().values()));
        return Lists.newArrayList(Iterables.filter(featureRules, new Predicate<FeatureRule>() {
            @Override
            public boolean apply(@Nullable FeatureRule featureRule) {
                return ApplicationType.equals(applicationType, featureRule.getApplicationType())
                        && ruleProcessorFactory.get().evaluate(featureRule.getRule(), context);
            }
        }));
    }

    private void logFeatureInfo(String context, List<FeatureRule> appliedRules, FeatureControl featureControl) {
        StringBuffer sb = new StringBuffer();
        sb.append("context=" + context);
        List<String> appliedRuleNames = getAppliedRuleNames(appliedRules);
        sb.append(", appliedRules=[").append(CollectionUtils.isNotEmpty(appliedRuleNames) ? Joiner.on(",").join(appliedRuleNames) : "NO MATCH").append("]");
        sb.append(", features=").append(getFeatureNames(featureControl.getFeatures()));
        sb.append(", configSetHash=").append(calculateConfigSetHash(featureControl.getFeatures()));
        log.info(sb.toString());
    }

    private List<String> getAppliedRuleNames(List<FeatureRule> featureRules) {
        List<String> ruleNames = new ArrayList<>();
        for (FeatureRule featureRule : featureRules) {
            ruleNames.add(featureRule.getName());
        }
        return ruleNames;
    }

    private List<String> getFeatureNames(Set<FeatureResponse> features) {
        List<String> featureNames = new ArrayList<>();
        for (FeatureResponse feature : features) {
            featureNames.add(feature.getFeatureName());
        }
        return featureNames;
    }

    public Map<String, String> normalizeContext(Map<String, String> context) {
        if (StringUtils.isNotBlank(context.get(LogUploaderContext.MODEL))) {
            context.put(LogUploaderContext.MODEL, context.get(LogUploaderContext.MODEL).toUpperCase());
        }
        if (StringUtils.isNotBlank(context.get(LogUploaderContext.ENV))) {
            context.put(LogUploaderContext.ENV, context.get(LogUploaderContext.ENV).toUpperCase());
        }
        if (StringUtils.isNotBlank(context.get(LogUploaderContext.ESTB_MAC))) {
            context.put(LogUploaderContext.ESTB_MAC, MacAddressUtil.normalizeMacAddress(context.get(LogUploaderContext.ESTB_MAC)));
        }
        if (StringUtils.isNotBlank(context.get(LogUploaderContext.ECM_MAC))) {
            context.put(LogUploaderContext.ECM_MAC, MacAddressUtil.normalizeMacAddress(context.get(LogUploaderContext.ECM_MAC)));
        }
        if (StringUtils.isNotBlank(context.get(PARTNER_ID))) {
            context.put(PARTNER_ID, context.get(PARTNER_ID).toUpperCase());
        }
        return context;
    }
}