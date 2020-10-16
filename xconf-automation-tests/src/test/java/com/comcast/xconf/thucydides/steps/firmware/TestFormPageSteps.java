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
 * Created: 3/21/16  3:42 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.xconf.thucydides.pages.firmware.TestFormPage;
import com.comcast.xconf.thucydides.util.firmware.FirmwareConfigUtils;
import com.comcast.xconf.thucydides.util.common.ModelUtils;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class TestFormPageSteps {

    private TestFormPage page;

    @Step
    public TestFormPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public TestFormPageSteps typeKeyParameter(String value) {
        page.typeKeyParameter(value);
        return this;
    }

    @Step
    public TestFormPageSteps typeValueParameter(String value) {
        page.typeValueParameter(value);
        return this;
    }

    @Step
    public TestFormPageSteps clickOnTypeaheadItem() {
        page.clickTypeaheadItem();
        return this;
    }

    @Step
    public TestFormPageSteps clickTestButton() {
        page.clickTestButton();
        return this;
    }

    @Step
    public TestFormPageSteps verifyDescription(String value) {
        assertEquals(value, page.getDescription());
        return this;
    }

    @Step
    public TestFormPageSteps verifyId(String value) {
        assertEquals(value, page.getId());
        return this;
    }

    @Step
    public TestFormPageSteps verifyDownloadProtocol(String value) {
        assertEquals(value, page.getDownloadProtocol());
        return this;
    }

    @Step
    public TestFormPageSteps verifyFileName(String value) {
        assertEquals(value, page.getFileName());
        return this;
    }

    @Step
    public TestFormPageSteps verifyVersion(String value) {
        assertEquals(value, page.getVersion());
        return this;
    }

    @Step
    public TestFormPageSteps verifyRebootImmediately(Boolean value) {
        assertEquals(value, page.getRebootImmediately());
        return this;
    }

    @Step
    public TestFormPageSteps verifySupportedModels(String value) {
        assertEquals("[\"" + value + "\"]", page.getSupportedModels());
        return this;
    }

    @Step
    public TestFormPageSteps waitUntilResultIsPresent() {
        page.waitUntilResultIsPresent();
        return this;
    }

    @Step
    public TestFormPageSteps verifyConfig() {
        verifyDescription(FirmwareConfigUtils.defaultDescription)
                .verifyId(FirmwareConfigUtils.defaultId)
                .verifyDownloadProtocol("tftp")
                .verifyFileName(FirmwareConfigUtils.defaultFileName)
                .verifyVersion(FirmwareConfigUtils.defaultVersion)
                .verifyRebootImmediately(false)
                .verifySupportedModels(ModelUtils.defaultModelId);

        return this;
    }
}
