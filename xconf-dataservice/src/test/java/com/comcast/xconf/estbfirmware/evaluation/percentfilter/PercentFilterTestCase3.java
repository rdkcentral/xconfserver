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
 *  Created: 12:49 PM
 */
package com.comcast.xconf.estbfirmware.evaluation.percentfilter;

import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.FirmwareConfigFacade;
import com.comcast.xconf.firmware.ApplicationType;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class PercentFilterTestCase3 extends BasePercentFilterTestCase {

    public PercentFilterTestCase3() {
    }

    @Before
    public void setUp() throws Exception {
        initPercentConditions();
    }

    @Test
    public void percentageIs100AndActive() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, null,
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(null, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs50AndInactive() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, null,
                50, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), false, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(null, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);
    }
}