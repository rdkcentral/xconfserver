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
 * Created: 6/19/14
 */
package com.comcast.xconf;

import java.util.Arrays;
import java.util.List;

public interface StbContext {
    String ESTB_MAC = "eStbMac";
    String ENVIRONMENT = "env";
    String MODEL = "model";
    String FIRMWARE_VERSION = "firmwareVersion";
    String ECM_MAC = "eCMMac";
    String RECEIVER_ID = "receiverId";
    String CONTROLLER_ID = "controllerId";
    String CHANNEL_MAP = "channelMapId";
    String VOD_ID = "vodId";
    String TIME_ZONE = "timeZone";
    String TIME_ZONE_OFFSET = "timeZoneOffset";
    String TIME = "time";
    String IP_ADDRESS = "ipAddress";
    String DOWNLOAD_PROTOCOL = "firmware_download_protocol";
    String REBOOT_DECOUPLED = "rebootDecoupled";
    String MATCHED_RULE_TYPE = "matchedRuleType";
    String BYPASS_FILTERS = "bypassFilters";
    String FORCE_FILTERS = "forceFilters";
    String CAPABILITIES = "capabilities";
    String PARTNER_ID = "partnerId";
    String ACCOUNT_HASH = "accountHash";
    String ACCOUNT_ID = "accountId";
    String XCONF_HTTP_HEADER = "HA-Haproxy-xconf-http";

    List<String> BASE_FIRMWARE_FIELDS = Arrays.asList(
            ESTB_MAC, ENVIRONMENT, MODEL, FIRMWARE_VERSION, IP_ADDRESS, TIME, TIME_ZONE_OFFSET, CAPABILITIES);
}
