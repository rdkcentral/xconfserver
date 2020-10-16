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

package com.comcast.xconf.search.firmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.PercentageBean;
import com.comcast.xconf.firmware.RuleAction;
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
public class PercentageBeanPredicates extends XRulePredicates<PercentageBean> {

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    public Predicate<PercentageBean> byName(String name) {
        return percentageBean -> Objects.nonNull(percentageBean)
                && StringUtils.containsIgnoreCase(percentageBean.getName(), name);
    }

    public Predicate<PercentageBean> byModel(String modelId) {
        return percentageBean -> Objects.nonNull(percentageBean)
                && StringUtils.containsIgnoreCase(percentageBean.getModel(), modelId);
    }

    public Predicate<PercentageBean> byEnvironment(String environmentId) {
        return percentageBean -> Objects.nonNull(percentageBean)
                && StringUtils.containsIgnoreCase(percentageBean.getEnvironment(), environmentId);
    }

    public Predicate<PercentageBean> byLKG(String lkgVersion) {
        return percentageBean -> {
            if (Objects.isNull(percentageBean) || StringUtils.isBlank(percentageBean.getLastKnownGood())) {
                return false;
            }
            FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne(percentageBean.getLastKnownGood(), false);
            return Objects.nonNull(firmwareConfig)
                    && StringUtils.containsIgnoreCase(firmwareConfig.getFirmwareVersion(), lkgVersion);
        };
    }

    public Predicate<PercentageBean> byDistributionVersion(String distributionVersion) {
        return percentageBean -> {
            if (Objects.isNull(percentageBean) || CollectionUtils.isEmpty(percentageBean.getDistributions())) {
                return false;
            }
            return percentageBean.getDistributions()
                    .stream()
                    .filter(distribution -> StringUtils.isNotBlank(distribution.getConfigId()))
                    .filter(distribution -> Objects.nonNull(getConfig(distribution)))
                    .anyMatch(distribution -> StringUtils.containsIgnoreCase(getConfig(distribution).getFirmwareVersion(), distributionVersion));

        };
    }

    public Predicate<PercentageBean> byMinCheckVersion(String minCheckVersion) {
        return percentageBean -> Objects.nonNull(percentageBean)
                && CollectionUtils.isNotEmpty(percentageBean.getFirmwareVersions())
                && percentageBean.getFirmwareVersions()
                    .stream()
                    .anyMatch(existingMinCheck -> StringUtils.containsIgnoreCase(existingMinCheck, minCheckVersion));
    }

    public Predicate<PercentageBean> byIntermediateVersion(String intermediateVersion) {
        return percentageBean -> {
            if (Objects.nonNull(percentageBean) && StringUtils.isBlank(percentageBean.getIntermediateVersion())) {
                return false;
            }
            FirmwareConfig intermediateConfig = firmwareConfigDAO.getOne(percentageBean.getIntermediateVersion());
            return Objects.nonNull(intermediateConfig)
                    && StringUtils.containsIgnoreCase(intermediateConfig.getFirmwareVersion(), intermediateVersion);
        };
    }

    public List<Predicate<PercentageBean>> getPredicates(ContextOptional context) {
        List<Predicate<PercentageBean>> predicates = new ArrayList<>();

        predicates.addAll(getBaseRulePredicates(context));

        context.getModel().ifPresent(model -> predicates.add(byModel(model)))  ;
        context.getEnvironment().ifPresent(environment -> predicates.add(byEnvironment(environment)));
        context.getLKG().ifPresent(lkg -> predicates.add(byLKG(lkg)));
        context.getDistributionVersion().ifPresent(distributionVersion -> predicates.add(byDistributionVersion(distributionVersion)));
        context.getMinCheckVersion().ifPresent(minCheckVersion -> predicates.add(byMinCheckVersion(minCheckVersion)));
        context.getIntermediateVersion().ifPresent(intermediateVersion -> predicates.add(byIntermediateVersion(intermediateVersion)));
        context.getApplicationType().ifPresent(application -> predicates.add(byApplication(application)));

        return predicates;
    }

    private FirmwareConfig getConfig(RuleAction.ConfigEntry distribution) {
        return firmwareConfigDAO.getOne(distribution.getConfigId());
    }
}
