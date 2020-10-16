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

public class PercentFilterTestCase6 extends BasePercentFilterTestCase {


    public PercentFilterTestCase6() {
    }

    @Before
    public void setUp() throws Exception {
        initPercentConditions();
    }


    @Test
    public void percentageIs0AndActiveAndRuleIsEqualIVButInMinChk() throws Exception {
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, lkgConfig.getId(), firmwareConfig.getId(),
                0, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), lkgConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);
        FirmwareConfigFacade expectedLkgConfig = createAndNullifyFirmwareConfigFacade(lkgConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedLkgConfig);

        performAndVerifyRequest(lkgConfig, HttpStatus.OK, expectedLkgConfig);
    }

    @Test
    public void percentageIs100AndActiveAndRuleIsEqualIVButInMinChk() throws Exception {
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, lkgConfig.getId(), firmwareConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), lkgConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(lkgConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs0AndActiveAndRuleIsNotEqualIVAndIVIsNotInMinChk() throws Exception {
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, lkgConfig.getId(), intermediateConfig.getId(),
                0, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), lkgConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedIntermediateConfig = createAndNullifyFirmwareConfigFacade(intermediateConfig);
        FirmwareConfigFacade expectedLkgConfig = createAndNullifyFirmwareConfigFacade(lkgConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedIntermediateConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedLkgConfig);

        performAndVerifyRequest(lkgConfig, HttpStatus.OK, expectedLkgConfig);
    }

    @Test
    public void percentageIs100AndActiveAndRuleIsNotEqualIVAndIVIsNotInMinChk() throws Exception {
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, lkgConfig.getId(), intermediateConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion(), lkgConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedIntermediateConfig = createAndNullifyFirmwareConfigFacade(intermediateConfig);
        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedIntermediateConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs0AndActiveAndRuleVersionIsEqualLkgAndIVIsNotInMinChk() throws Exception {
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, firmwareConfig.getId(), intermediateConfig.getId(),
                0, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedIntermediateConfig = createAndNullifyFirmwareConfigFacade(intermediateConfig);
        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedIntermediateConfig);

        performAndVerifyRequest(intermediateConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs100AndActiveAndRuleVersionIsEqualLkgAndIVIsNotInMinChk() throws Exception {
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, firmwareConfig.getId(), intermediateConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);
        FirmwareConfigFacade expectedIntermediateConfig = createAndNullifyFirmwareConfigFacade(intermediateConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedIntermediateConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs0AndActiveAndLkgAndIVAreEqual() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, lkgConfig.getId(), lkgConfig.getId(),
                0, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), lkgConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedLkgConfig = createAndNullifyFirmwareConfigFacade(lkgConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedLkgConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedLkgConfig);

        performAndVerifyRequest(lkgConfig, HttpStatus.OK, expectedLkgConfig);
    }

    @Test
    public void percentageIs100AndActiveAndLkgAndIVAreEqual() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, lkgConfig.getId(), lkgConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), lkgConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedLkgConfig = createAndNullifyFirmwareConfigFacade(lkgConfig);
        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedLkgConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs0AndActiveAndRuleVersionLkgAndIVAreEqual() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, firmwareConfig.getId(), firmwareConfig.getId(),
                0, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs100AndActiveAndRuleVersionLkgAndIVAreEqual() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, firmwareConfig.getId(), firmwareConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs0AndActiveAndAllConfigsAreDifferent() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, lkgConfig.getId(), intermediateConfig.getId(),
                0, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), lkgConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedIntermediateConfig = createAndNullifyFirmwareConfigFacade(intermediateConfig);
        FirmwareConfigFacade expectedLkgConfig = createAndNullifyFirmwareConfigFacade(lkgConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedIntermediateConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedLkgConfig);

        performAndVerifyRequest(lkgConfig, HttpStatus.OK, expectedLkgConfig);
    }

    @Test
    public void percentageIs100AndActiveAndAllConfigsAreDifferent() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig lkgConfig = createAndSaveFirmwareConfig("lkgVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, lkgConfig.getId(), intermediateConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), lkgConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedIntermediateConfig = createAndNullifyFirmwareConfigFacade(intermediateConfig);
        FirmwareConfigFacade expectedLkgConfig = createAndNullifyFirmwareConfigFacade(lkgConfig);
        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedIntermediateConfig);

        performAndVerifyRequest(intermediateConfig, HttpStatus.OK, expectedLkgConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs0AndActiveRuleIsEqualLkgAndIVInMinChk() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, firmwareConfig.getId(), intermediateConfig.getId(),
                0, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), firmwareConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);
        FirmwareConfigFacade expectedIntermediateConfig = createAndNullifyFirmwareConfigFacade(intermediateConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedIntermediateConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);
    }

    @Test
    public void percentageIs100AndActiveRuleIsEqualLkgAndIVInMinChk() throws Exception {
        FirmwareConfig notInMinChkConfig = createAndSaveFirmwareConfig("notInMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig inMinChkConfig = createAndSaveFirmwareConfig("inMinChkVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);
        FirmwareConfig intermediateConfig = createAndSaveFirmwareConfig("intermediateVersion", model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSavePercentFilter(envModelFirmwareRule.getName(), 100, firmwareConfig.getId(), intermediateConfig.getId(),
                100, Sets.newHashSet(firmwareConfig.getFirmwareVersion(), firmwareConfig.getFirmwareVersion(), inMinChkConfig.getFirmwareVersion()), true, true, true, ApplicationType.STB);

        FirmwareConfigFacade expectedRuleConfig = createAndNullifyFirmwareConfigFacade(firmwareConfig);
        FirmwareConfigFacade expectedIntermediateConfig = createAndNullifyFirmwareConfigFacade(intermediateConfig);

        performAndVerifyRequest(notInMinChkConfig, HttpStatus.OK, expectedIntermediateConfig);

        performAndVerifyRequest(inMinChkConfig, HttpStatus.OK, expectedRuleConfig);

        performAndVerifyRequest(firmwareConfig, HttpStatus.OK, expectedRuleConfig);
    }
}
