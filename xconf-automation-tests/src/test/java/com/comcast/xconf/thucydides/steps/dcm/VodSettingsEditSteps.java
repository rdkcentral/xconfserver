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
 * Created: 3/28/16  11:36 AM
 */
package com.comcast.xconf.thucydides.steps.dcm;

import com.comcast.xconf.thucydides.pages.dcm.VodSettingsEditPageObjects;
import net.thucydides.core.annotations.Step;

public class VodSettingsEditSteps {

    private VodSettingsEditPageObjects page;

    @Step
    public VodSettingsEditSteps typeName(String value) {
        page.typeName(value);
        return this;
    }

    @Step
    public VodSettingsEditSteps typeLocationUrl(String value) {
        page.typeLocationUrl(value);
        return this;
    }

    @Step
    public VodSettingsEditSteps removeSrmIpItem() {
        page.clickRemoveSrmIpItemButton();
        return this;
    }

    @Step
    public VodSettingsEditSteps addSrmIpItem() {
        page.clickAddSrmIpItemButton();
        return this;
    }

    @Step
    public VodSettingsEditSteps typeSrmName(String value) {
        page.typeSrmName(value);
        return this;
    }

    @Step
    public VodSettingsEditSteps typeSrmIp(String value) {
        page.typeSrmIp(value);
        return this;
    }

    @Step
    public VodSettingsEditSteps fillFormOnCreate(String name) {
        typeName(name)
                .typeLocationUrl("http://test.com")
                .addSrmIpItem()
                .removeSrmIpItem()
                .addSrmIpItem()
                .typeSrmName("srmName")
                .typeSrmIp("1.1.1.1");
        return this;
    }

    @Step
    public VodSettingsEditSteps fillFormOnEdit(String name) {
        typeName(name)
                .typeLocationUrl("http://test.com")
                .addSrmIpItem()
                .removeSrmIpItem()
                .typeSrmName("srmName")
                .typeSrmIp("1.1.1.1");
        return this;
    }
}
