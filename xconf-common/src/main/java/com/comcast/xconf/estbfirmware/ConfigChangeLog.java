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
 * Created: 7/8/14  5:07 AM
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.annotation.ListingCF;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.CfNames;
import com.google.common.base.Predicate;

import java.util.Date;

@ListingCF(cfName = CfNames.Common.LOGS,
        key2FieldName = "column1",
        ttl = 90*24*60*60,
        compress = true)
public class ConfigChangeLog extends LastConfigLog {
    public ConfigChangeLog(EstbFirmwareContext context,
                         String explanation,
                         FirmwareConfigFacade config,
                         Iterable<Object> appliedFilters,
                         FirmwareRule evaluatedRule) {
        super(context, explanation, config, appliedFilters, evaluatedRule);
        updated = new Date();
    }

    public ConfigChangeLog() {
        super();
    }

    public static final Predicate<ConfigChangeLog> FILTER_LAST_CONFIG = new Predicate<ConfigChangeLog>() {
        @Override
        public boolean apply(ConfigChangeLog input) {
            return !input.getId().equals(LAST_CONFIG_LOG_ID);
        }
    };
}
