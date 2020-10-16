/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.estbfirmware;

import com.comcast.xconf.estbfirmware.converter.TimeFilterConverter;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * User: ikostrov
 * Date: 12.08.14
 */
@Component
public class EstbFirmwareLogger {

    @Autowired
    private EnvModelRuleService envModelRuleService;

    @Autowired
    private TimeFilterConverter timeFilterConverter;

    public String toString(PercentageBean bean) {
        return String.format("DistributedEnvModelPercentage{id=%s, name=%s, firmwareCheckRequired=%s, lastKnownGood=%s, intermediateVersion=%s, firmwareVersions=%s}",
                bean.getId(), bean.getName(), bean.getFirmwareVersions(), bean.getLastKnownGood(), bean.getIntermediateVersion(), bean.getFirmwareVersions());
    }

    public String toString(PercentFilterValue filter) {
        StringBuilder info = new StringBuilder();
        info.append("com.comcast.xconf.estbfirmware.PercentFilter [");
        info.append(" percent=").append(filter.getPercentage());
        info.append(" , envModelPercentage=").append(filter.getEnvModelPercentages());
        return info.append(" ]").toString();
    }

    public String toString(FirmwareRule rule) {
        switch (rule.getType()) {
            case TemplateNames.TIME_FILTER:
                return processAsTimeFilter(rule, ApplicationType.get(rule.getApplicationType()));
            case TemplateNames.DOWNLOAD_LOCATION_FILTER:
            case TemplateNames.IP_FILTER:
                return processIdAndNameOnly(rule);
            default:
                return processAsDefault(rule);
        }
    }

    private String processAsDefault(FirmwareRule rule) {
        return rule.getType() + "[ " + rule.toString() + " ]";
    }

    private String processIdAndNameOnly(FirmwareRule firmwareRule) {
        StringBuilder info = new StringBuilder();
        info.append(" ").append(firmwareRule.getType()).append(" [ ");
        info.append(firmwareRule.getId());
        info.append(" ").append(firmwareRule.getName());
        return info.append(" ]").toString();
    }

    private String processAsTimeFilter(FirmwareRule firmwareRule, String applicationType) {

        TimeFilter timeFilter = timeFilterConverter.convert(firmwareRule);

        StringBuilder info = new StringBuilder();
        info.append(" ").append(firmwareRule.getType()).append("[");
        info.append(" id=").append(firmwareRule.getId());
        info.append(" name=").append(firmwareRule.getName());
        info.append(" start=").append(timeFilter.getStart());
        info.append(" end=").append(timeFilter.getEnd());
        info.append(" isLocalTime=").append(timeFilter.isLocalTime());
        info.append(" ipWhitelist=[").append(timeFilter.getIpWhitelist()).append("]");
        info.append(" neverBlockRebootDecoupled=").append(timeFilter.isNeverBlockRebootDecoupled());
        info.append(" neverBlockHttpDownload=").append(timeFilter.isNeverBlockHttpDownload());
        info.append(" envModelWhitelist=").append(getEnvModelWhitelistId(timeFilter, applicationType));
        return info.append(" ]").toString();
    }

    private String getEnvModelWhitelistId(TimeFilter filter, String applicationType) {
        if (filter.getEnvModelWhitelist() != null) {
            String model = filter.getEnvModelWhitelist().getModelId();
            String environment = filter.getEnvModelWhitelist().getEnvironmentId();
            EnvModelRuleBean envModelRuleBean = envModelRuleService.getOneByEnvModel(model, environment, applicationType);
            if (envModelRuleBean != null) {
                return envModelRuleBean.getId();
            }
        }
        return null;
    }

}
