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
 * Author: Maksym Dolina
 */
package com.comcast.xconf.validators.firmware;

import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class FirmwareConfigValidationUtils {

    public static boolean configUsedInAction(FirmwareConfig firmwareConfig, FirmwareRule rule) {
        String id = firmwareConfig.getId();
        if (rule != null && rule.getApplicableAction() != null && (rule.getApplicableAction() instanceof RuleAction)) {
            RuleAction action = (RuleAction) rule.getApplicableAction();
            if (id.equals(action.getConfigId())) {
                return true;
            }
            List<RuleAction.ConfigEntry> configEntries = action.getConfigEntries();
            if (configEntries != null) {
                for (RuleAction.ConfigEntry entry : configEntries) {
                    if (id.equals(entry.getConfigId())) {
                        return true;
                    }
                }
            }
            if (StringUtils.equals(id, action.getIntermediateVersion())
                    || CollectionUtils.isNotEmpty(action.getFirmwareVersions()) && action.getFirmwareVersions().contains(firmwareConfig.getFirmwareVersion())) {
                return true;
            }
        }
        return false;
    }
}
