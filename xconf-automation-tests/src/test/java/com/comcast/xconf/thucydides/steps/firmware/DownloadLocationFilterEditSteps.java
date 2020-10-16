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
 * Created: 3/17/16  12:37 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.xconf.thucydides.pages.firmware.DownloadLocationFilterEditPageObjects;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class DownloadLocationFilterEditSteps {

    private DownloadLocationFilterEditPageObjects page;

    @Step
    public DownloadLocationFilterEditSteps typeHttpLocation(String value) {
        page.typeHttpLocation(value);
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps typeFullUrlHttpLocation(String value) {
        page.typeFullUrlHttpLocation(value);
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps clickNeverUseHttp() {
        page.clickNeverUseHttp();
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps verifyNeverUseHttpIsChecked(Boolean isChecked) {
        assertEquals(isChecked, page.neverUseHttpIsChecked());
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps clickRogueModelsListItem() {
        page.clickRogueModelsListItem();
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps verifyRogueModelsListItemIsSelected(Boolean isSelected) {
        assertEquals(isSelected, page.rogueModelsListItemIsSelected());
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps typeFirmwareVersions(String value) {
        page.typeFirmwareVersions(value);
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps typeIpv4LocationIp(String value) {
        page.typeIpv4LocationIp(value);
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps typeIpv4LocationPercentage(Integer value) {
        page.typeIpv4LocationPercentage(value.toString());
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps removeIpv4Location() {
        page.clickRemoveIpv4LocationButton();
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps addIpv4Location() {
        page.clickAddIpv4LocationButton();
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps typeIpv6LocationIp(String value) {
        page.typeIpv6LocationIp(value);
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps typeIpv6LocationPercentage(Integer value) {
        page.typeIpv6LocationPercentage(value.toString());
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps removeIpv6Location() {
        page.clickRemoveIpv6LocationButton();
        return this;
    }

    @Step
    public DownloadLocationFilterEditSteps addIpv6Location() {
        page.clickAddIpv6LocationButton();
        return this;
    }
}
