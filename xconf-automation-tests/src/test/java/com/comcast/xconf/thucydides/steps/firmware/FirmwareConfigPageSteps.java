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
 * <p>
 * Author: Stanislav Menshykov
 * Created: 3/10/16  12:07 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.beust.jcommander.internal.Lists;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.thucydides.pages.firmware.FirmwareConfigPage;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.Steps;

import static org.junit.Assert.assertEquals;

public class FirmwareConfigPageSteps {

    private FirmwareConfigPage page;

    @Steps
    public GenericSteps genericSteps;

    @Step
    public FirmwareConfigPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public FirmwareConfigPageSteps typeDescription(String description) {
        page.typeDescription(description);
        return this;
    }

    @Step
    public FirmwareConfigPageSteps typeFileName(String fileName) {
        page.typeFileName(fileName);
        return this;
    }

    @Step
    public FirmwareConfigPageSteps typeVersion(String version) {
        page.typeVersion(version);
        return this;
    }

    @Step
    public FirmwareConfigPageSteps selectModel() {
        page.selectModel();
        return this;
    }

    @Step
    public FirmwareConfigPageSteps verifyViewModalWindow(FirmwareConfig config) {
        genericSteps.waitForModalWindowToOpen();
        genericSteps.verifyViewModalTitle("View Firmware Config");
        assertEquals(config.getDescription(), page.getDescription());
        assertEquals(config.getFirmwareFilename(), page.getFileName());
        assertEquals(config.getFirmwareVersion(), page.getVersion());
        assertEquals(Lists.newArrayList(config.getSupportedModelIds()).get(0), page.getSupportedModelId());
        page.clickModalCloseButton();

        return this;
    }

    @Step
    public FirmwareConfigPageSteps typeSearchDescription(String description) {
        page.typeSearchDescription(description);
        return this;
    }

    @Step
    public FirmwareConfigPageSteps typeSearchModelId(String modelId) {
        page.typeSearchModelId(modelId);
        return this;
    }

    @Step
    public FirmwareConfigPageSteps typeSearchFirmwareVersion(String firmwareVersion) {
        page.typeSearchFirmwareVersion(firmwareVersion);
        return this;
    }

    @Step
    public FirmwareConfigPageSteps verifyConfigsCount(Integer expectedConfigsCount) {
        assertEquals(expectedConfigsCount, page.getFirmwareConfigsCount());
        return this;
    }
}
