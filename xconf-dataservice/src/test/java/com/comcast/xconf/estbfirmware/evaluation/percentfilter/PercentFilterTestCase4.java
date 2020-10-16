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

public class PercentFilterTestCase4 extends BasePercentFilterTestCase {

    public PercentFilterTestCase4() {
    }

    @Before
    public void setUp() throws Exception {
        initPercentConditions();
    }

    @Test
    public void percentageIs0AndInactiveRuleVersionEqualsIV() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, firmwareConfig.getId(),
                0, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), false, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(null, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs100AndInactiveRuleVersionEqualsIV() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, firmwareConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), false, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(null, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs0AndInactiveAndRuleIsNotEqualIV() throws Exception {
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("anotherFirmwareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, intermediateConfig.getId(),
                0, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), intermediateConfig.getFirmwareVersion()), false, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        verifyResponse(expectedRuleConfig, notInMinChkConfig);

    }

    @Test
    public void percentageIs100AndInactiveAndRuleIsNotEqualIV() throws Exception {
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("anotherFirmwareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, intermediateConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), intermediateConfig.getFirmwareVersion()), false, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        verifyResponse(expectedRuleConfig, notInMinChkConfig);
    }

    @Test
    public void percentageIs0AndInactiveAndIVIsNotInMinChkAndNotEqualRuleVersion() throws Exception {
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("anotherFirmwareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, intermediateConfig.getId(),
                0, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), false, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        verifyResponse(expectedRuleConfig, inMinChkConfig);
    }

    @Test
    public void percentageIs100AndInactiveAndIVIsNotInMinChkAndNotEqualRuleVersion() throws Exception {
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkConfig", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, intermediateConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), false, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        verifyResponse(expectedRuleConfig, inMinChkConfig);
    }

    @Test
    public void percentageIs100AndActiveAndIVIsEqualRuleVersion() throws Exception {
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("anotherFirmwareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkFirmwareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, firmwareConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(null, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs100AndActiveAndIVIsNotEqualRuleVersion() throws Exception {
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("anotherFirmwareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkFirmwareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, intermediateConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), intermediateConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);
        FirmwareConfigFacade expectedIntermediateConfig = createAndNullifyFirmwareConfigFacade(intermediateConfig);

        performAndVerifyRequest(null, HttpStatus.OK, expectedIntermediateConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedIntermediateConfig);
    }

    @Test
    public void percentageIs100AndActiveAndIVIsNotInMinChkAndNotEqualRuleVersion() throws Exception {
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("anotherFirmwareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkFirmwareVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, null, intermediateConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);
        FirmwareConfigFacade expectedIntermediateConfig = createAndNullifyFirmwareConfigFacade(intermediateConfig);

        performAndVerifyRequest(null, HttpStatus.OK, expectedIntermediateConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedIntermediateConfig);
    }

    private void verifyResponse(FirmwareConfigFacade expectedRuleConfig, FirmwareConfig notInMinChkConfig) throws Exception {
        performAndVerifyRequest(null, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);
    }
}
