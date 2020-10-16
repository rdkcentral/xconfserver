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
 * Created: 3/16/16  12:02 PM
 */
package com.comcast.xconf.thucydides.steps.firmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.xconf.thucydides.pages.firmware.DownloadLocationFilterPage;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DownloadLocationFilterPageSteps {

    private DownloadLocationFilterPage page;

    @Step
    public DownloadLocationFilterPageSteps open() {
        page.open();
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyHttpLocation(String expectedHttpLocation) {
        assertEquals(expectedHttpLocation, page.getHttpLocation());
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyHttpFullUrlLocation(String expectedHttpFullUrlLocation) {
        assertEquals(expectedHttpFullUrlLocation, page.getHttpFullUrlLocation());
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyNeverUseHttpIsChecked(Boolean isChecked) {
        assertEquals(isChecked, page.neverUseHttpIsChecked());
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyNeverUseHttpIsDisabled() {
        assertTrue(page.neverUseHttpIsDisabled());
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyRogueModelListItem(String expectedRogueModelId) {
        assertEquals(expectedRogueModelId, page.getRogueModelsListItem());
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyFirmwareVersionListFirstItem(String expectedFirstItem) {
        assertEquals(expectedFirstItem, page.getFirmwareVersionListFirstItem());
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyFirmwareVersionUnfoldedListLastItem(String expectedLastItem) {
        assertEquals(expectedLastItem, page.getFirmwareVersionUnfoldedListLastItem());
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyFirmwareVersionFoldedListLastItem(String expectedLastItem) {
        assertEquals(expectedLastItem, page.getFirmwareVersionFoldedListLastItem());
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps clickShowMoreButton() {
        page.clickShowMoreButton();
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps clickShowLessButton() {
        page.clickShowLessButton();
        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyFirmwareVersionList(String firmwareVersionsList) {
        verifyFirmwareVersionListFirstItem(getNthFirmwareVersion(firmwareVersionsList, 1));
        verifyFirmwareVersionFoldedListLastItem(getNthFirmwareVersion(firmwareVersionsList, 10));
        clickShowMoreButton();
        verifyFirmwareVersionUnfoldedListLastItem(getLastFirmwareVersion(firmwareVersionsList));
        clickShowLessButton();

        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyIpv4Location(IpAddress locationIp, Double locationPercentage) {
        assertEquals(locationIp.toString(), page.getIpv4LocationIp());
        assertEquals(locationPercentage.intValue(), page.getIpv4LocationPercentage().intValue());

        return this;
    }

    @Step
    public DownloadLocationFilterPageSteps verifyIpv6Location(IpAddress locationIp, Double locationPercentage) {
        assertEquals(locationIp.toString(), page.getIpv6LocationIp());
        assertEquals(locationPercentage.intValue(), page.getIpv6LocationPercentage().intValue());

        return this;
    }

    private static String getLastFirmwareVersion(String firmwareVersions) {
        String[] allVersions = firmwareVersions.split(System.lineSeparator());

        return allVersions[allVersions.length - 1];
    }

    private static String getNthFirmwareVersion(String firmwareVersions, int index) {
        return firmwareVersions.split(System.lineSeparator())[index - 1];
    }
}
