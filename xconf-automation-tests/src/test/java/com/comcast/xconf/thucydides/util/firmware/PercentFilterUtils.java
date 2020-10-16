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
 * Author: Stanislav Menshykov
 * Created: 3/18/16  10:18 AM
 */
package com.comcast.xconf.thucydides.util.firmware;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.comcast.xconf.estbfirmware.EnvModelPercentage;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.PercentFilterValue;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;
import com.comcast.xconf.thucydides.util.common.GenericNamespacedListUtils;
import com.google.common.collect.Sets;

import java.util.HashMap;

public class PercentFilterUtils {
    public static final String PERCENT_FILTER_URL = "percentfilter";

    public static void doCleanup() throws Exception {
        FirmwareRuleUtils.doCleanup();
        GenericNamespacedListUtils.doCleanup();
        FirmwareConfigUtils.doCleanup();
    }

    public static PercentFilterValue createDefaultPercentFilter() throws Exception {
        PercentFilterValue result = new PercentFilterValue();
        result.setId(PercentFilterValue.SINGLETON_ID);
        result.setPercentage(100);
        final FirmwareRule envModelRule = FirmwareRuleUtils.createAndSaveDefaultEnvModelRule();
        result.setEnvModelPercentages(new HashMap<String, EnvModelPercentage>() {{
            put(envModelRule.getName(), createEnvModelPercentage());
        }});
        GenericNamespacedList ipList = GenericNamespacedListUtils.createAndSaveDefaultIpList();
        result.setWhitelist(GenericNamespacedListsConverter.convertToIpAddressGroup(ipList));

        return result;
    }

    public static PercentFilterValue createAndSavePercentFilter() throws Exception {
        return savePercentFilter(createDefaultPercentFilter());
    }

    public static PercentFilterValue savePercentFilter(PercentFilterValue filter) throws Exception {
        if (!filter.getEnvModelPercentages().isEmpty()) {
            String envModelRuleName = filter.getEnvModelPercentages().keySet().iterator().next();
            HttpClient.post(GenericTestUtils.buildFullUrl(PERCENT_FILTER_URL + "?key=" + envModelRuleName), filter);
        } else {
            HttpClient.post(GenericTestUtils.buildFullUrl(PERCENT_FILTER_URL), filter);
        }

        return filter;
    }

    private static EnvModelPercentage createEnvModelPercentage() throws Exception {
        EnvModelPercentage result = new EnvModelPercentage();
        result.setPercentage(90);
        result.setActive(true);
        result.setFirmwareCheckRequired(true);
        result.setRebootImmediately(true);
        FirmwareConfig firmwareConfig = FirmwareConfigUtils.createAndSaveDefaultFirmwareConfig();
        result.setFirmwareVersions(Sets.newHashSet(firmwareConfig.getFirmwareVersion()));
        result.setLastKnownGood(firmwareConfig.getId());
        result.setIntermediateVersion(firmwareConfig.getId());
        GenericNamespacedList ipList = GenericNamespacedListUtils.createAndSaveDefaultIpList();
        result.setWhitelist(GenericNamespacedListsConverter.convertToIpAddressGroup(ipList));

        return result;
    }
}
