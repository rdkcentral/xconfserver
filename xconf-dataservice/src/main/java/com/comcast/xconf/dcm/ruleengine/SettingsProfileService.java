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
 * Author: Alexander Binkovsky
 * Created: 3/4/2016  5:46 AM
 */
package com.comcast.xconf.dcm.ruleengine;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.util.EvaluatorHelper;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SettingsProfileService {
    @Autowired
    private CachedSimpleDao<String, SettingProfile> settingProfileDao;
    @Autowired
    private CachedSimpleDao<String, SettingRule> settingRuleDAO;
    @Autowired
    private EvaluatorHelper evaluatorHelper;

    public SettingRule getSettingRuleByTypeForContext(final String settingType, final Map<String, String> context) {
        return evaluatorHelper.getEntityRuleForContext(getSettingRulesBySettingType(settingType), context);
    }

    public SettingProfile getSettingProfileBySettingRule(SettingRule settingRule) {
        return settingRule != null && StringUtils.isNotBlank(settingRule.getBoundSettingId())
                ? settingProfileDao.getOne(settingRule.getBoundSettingId()) : null;
    }

    public Iterable<SettingRule> getSettingRulesBySettingType(final String settingType) {
        return Iterables.filter(Optional.presentInstances(settingRuleDAO.asLoadingCache().asMap().values()),
                new Predicate<SettingRule>() {
                    @Override
                    public boolean apply(SettingRule input) {
                        final SettingProfile settingProfile = settingProfileDao.getOne(input.getBoundSettingId(), false);
                        return settingProfile != null
                                ? settingProfile.getSettingType().isApplicableTo(settingType) : false;
                    }
                });
    }

    public Map<String, List<SettingRule>> getSettingRulesWithConfig(Set<String> settingTypes, Map<String, String> context) {
        Map<String, List<SettingRule>> result = new HashMap<>();

        for (String settingType : settingTypes) {
            SettingRule settingRule = getSettingRuleByTypeForContext(settingType, context);
            SettingProfile settingProfile = getSettingProfileBySettingRule(settingRule);
            if (settingProfile == null) {
                continue;
            }

            String profileName = settingProfile.getSettingProfileId();
            List<SettingRule> settingRuleList = result.get(profileName);
            if (CollectionUtils.isEmpty(settingRuleList)) {
                settingRuleList = new ArrayList<>();
            }
            settingRuleList.add(settingRule);

            result.put(profileName, settingRuleList);
        }

        return result;
    }

}
