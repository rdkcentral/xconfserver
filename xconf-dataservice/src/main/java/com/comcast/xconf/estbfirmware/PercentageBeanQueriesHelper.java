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
 * Author: Igor Kostrov
 * Created: 4/19/2017
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.firmware.RuleAction;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PercentageBeanQueriesHelper {

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    public PercentageBean replaceConfigIdWithFirmwareVersion(PercentageBean bean) {
        PercentageBean result = CloneUtil.clone(bean);
        List<RuleAction.ConfigEntry> resultDistribution = new ArrayList<>();
        for (RuleAction.ConfigEntry entry : bean.getDistributions()) {
            String firmwareVersion = getFirmwareVersion(entry.getConfigId());
            RuleAction.ConfigEntry configEntry = new RuleAction.ConfigEntry(firmwareVersion, entry.getStartPercentRange(), entry.getEndPercentRange());
            configEntry.setPercentage(entry.getPercentage());
            resultDistribution.add(configEntry);
        }
        result.setDistributions(resultDistribution);
        result.setLastKnownGood(getFirmwareVersion(bean.getLastKnownGood()));
        result.setIntermediateVersion(getFirmwareVersion(bean.getIntermediateVersion()));
        return result;
    }

    private String getFirmwareVersion(String id) {
        if (StringUtils.isNotBlank(id)) {
            FirmwareConfig config = firmwareConfigDAO.getOne(id);
            if (config != null) {
                return config.getFirmwareVersion();
            }
        }
        return null;
    }
}
