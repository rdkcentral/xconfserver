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
 * Author: slavrenyuk
 * Created: 6/11/14
 */
package com.comcast.xconf.estbfirmware.evaluation;

import com.comcast.apps.hesperius.ruleengine.domain.RuleUtils;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.estbfirmware.legacy.PercentFilterLegacyService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Deprecated
public class PercentFilter {

    private static final Logger log = LoggerFactory.getLogger(PercentFilter.class);

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private PercentFilterLegacyService percentFilterService;

    /**
     * @return true if firmware output must be returned, false if must be blocked
     */
    public boolean filter(EvaluationResult evaluationResult, EstbFirmwareContext.Converted context) {

        PercentFilterValue filterValue = percentFilterService.getRaw();
        String matchedEnvModelName = null;
        if (evaluationResult.getMatchedRule() != null &&
                TemplateNames.ENV_MODEL_RULE.equals(evaluationResult.getMatchedRule().getType())) {
            matchedEnvModelName = evaluationResult.getMatchedRule().getName();
        }
        if (matchedEnvModelName != null && filterValue.getEnvModelPercentages() != null &&
                filterValue.getEnvModelPercentages().get(matchedEnvModelName) != null &&
                filterValue.getEnvModelPercentages().get(matchedEnvModelName).isActive()) {
            EnvModelPercentage envModelPercentage = filterValue.getEnvModelPercentages().get(matchedEnvModelName);
            IpAddressGroup whiteList = envModelPercentage.getWhitelist();
            double percentage = envModelPercentage.getPercentage();

            boolean firmwareVersionIsAbsentInFilter = envModelPercentage.getFirmwareVersions() == null
                    || context.getFirmwareVersion() == null
                    || !envModelPercentage.getFirmwareVersions().contains(context.getFirmwareVersion());

            if (envModelPercentage.isFirmwareCheckRequired() && firmwareVersionIsAbsentInFilter) {
                if (envModelPercentage.isRebootImmediately()) {
                    context.getForceFilters().add(TemplateNames.REBOOT_IMMEDIATELY_FILTER);
                }
                context.getBypassFilters().add(TemplateNames.TIME_FILTER);

                FirmwareConfig config = getFirmwareConfig(envModelPercentage.getIntermediateVersion());
                if (config != null && !StringUtils.equals(context.getFirmwareVersion(), config.getFirmwareVersion())) {
                    // return IntermediateVersion firmware config
                    evaluationResult.setFirmwareConfig(new FirmwareConfigFacade(config));
                    evaluationResult.getAppliedVersionInfo().put("firmwareVersionSource", "IV,doesntMeetMinCheck");
                } else {
                    config = getFirmwareConfig(envModelPercentage.getLastKnownGood());

                    if (config != null) {
                        // return LKG firmware config
                        evaluationResult.setFirmwareConfig(new FirmwareConfigFacade(config));
                        evaluationResult.getAppliedVersionInfo().put("firmwareVersionSource", "LKG,doesntMeetMinCheck");
                    }
                }
                return true;
            }
            boolean result = fitsPercent(evaluationResult, context, whiteList, percentage);
            if (!result) {
                FirmwareConfig config = getFirmwareConfig(envModelPercentage.getLastKnownGood());
                if (config != null && !StringUtils.equals(context.getFirmwareVersion(), config.getFirmwareVersion())) {
                    // return LKG firmware config if versions are different
                    evaluationResult.setFirmwareConfig(new FirmwareConfigFacade(config));
                    evaluationResult.getAppliedVersionInfo().put("firmwareVersionSource", "LKG,meetMinCheck");
                    return true;
                }
            }
            return result;
        } else {
            return fitsPercent(evaluationResult, context, filterValue.getWhitelist(), filterValue.getPercentage());
        }
    }

    private FirmwareConfig getFirmwareConfig(String id) {
        return StringUtils.isNotBlank(id) ? firmwareConfigDAO.getOne(id): null;
    }

    private boolean fitsPercent(EvaluationResult evaluationResult, EstbFirmwareContext.Converted context, IpAddressGroup whiteList, double percentage) {
        final boolean isInWhiteList = whiteList != null && whiteList.isInRange(context.getIpAddress());
        evaluationResult.getAppliedVersionInfo().put("inWhiteList", Boolean.toString(isInWhiteList));
        final boolean fitsPercent = RuleUtils.fitsPercent(context.getEstbMac() != null ? context.getEstbMac() : context, percentage);
        evaluationResult.getAppliedVersionInfo().put("fitsPercent", Boolean.toString(fitsPercent));
        return isInWhiteList || fitsPercent;
    }
}
