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
 * Created: 3/29/16  1:56 PM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.xconf.thucydides.pages.dcm.UploadRepositoryPage;
import net.thucydides.core.annotations.Step;

public class UploadRepositoryPageSteps {

    private UploadRepositoryPage page;

    @Step
    public UploadRepositoryPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public UploadRepositoryPageSteps typeName(String value) {
        page.typeName(value);
        return this;
    }

    @Step
    public UploadRepositoryPageSteps typeDescription(String value) {
        page.typeDescription(value);
        return this;
    }

    @Step
    public UploadRepositoryPageSteps selectProtocol(String value) {
        page.selectProtocol(value);
        return this;
    }

    @Step
    public UploadRepositoryPageSteps typeUrl(String value) {
        page.typeUrl(value);
        return this;
    }

    @Step
    public UploadRepositoryPageSteps fillForm(String name) {
        typeName(name)
                .typeDescription("description")
                .selectProtocol("HTTPS")
                .typeUrl("test.com");
        return this;
    }
}
