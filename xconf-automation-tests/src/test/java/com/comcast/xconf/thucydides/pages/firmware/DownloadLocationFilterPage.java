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
 * Created: 3/16/16  12:00 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/roundrobinfilter")
public class DownloadLocationFilterPage extends PageObject {

    public DownloadLocationFilterPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#httpLocation")
    private WebElementFacade httpLocation;

    @FindBy(css = "input#httpFullUrlLocation")
    private WebElementFacade httpFullUrlLocation;

    @FindBy(css = "input#neverUseHttp")
    private WebElementFacade neverUseHttp;

    @FindBy(css = "ul#rogueModelsList li")
    private WebElementFacade rogueModelsListItem;

    @FindBy(css = "ul#firmwareVersionsList li:first-child")
    private WebElementFacade firmwareVersionListFirstItem;

    @FindBy(css = "ul#firmwareVersionsList li:nth-last-child(2)")
    private WebElementFacade firmwareVersionUnfoldedListLastItem;

    @FindBy(css = "ul#firmwareVersionsList li:nth-last-child(4)")
    private WebElementFacade firmwareVersionFoldedListLastItem;

    @FindBy(css = "button#showMore")
    private WebElementFacade showMoreFirmwareVersionsButton;

    @FindBy(css = "button#showLess")
    private WebElementFacade showLessFirmwareVersionsButton;

    @FindBy(css = "div#ipv4Locations span.location-ip")
    private WebElementFacade ipv4LocationIp;

    @FindBy(css = "div#ipv4Locations input.location-percentage")
    private WebElementFacade ipv4Percentage;

    @FindBy(css = "div#ipv6Locations span.location-ip")
    private WebElementFacade ipv6LocationIp;

    @FindBy(css = "div#ipv6Locations input.location-percentage")
    private WebElementFacade ipv6Percentage;

    public String getHttpLocation() {
        return httpLocation.getValue();
    }

    public String getHttpFullUrlLocation() {
        return httpFullUrlLocation.getValue();
    }

    public Boolean neverUseHttpIsChecked() {
        return Boolean.valueOf(neverUseHttp.getValue());
    }

    public Boolean neverUseHttpIsDisabled() {
        return Boolean.valueOf(neverUseHttp.getAttribute("disabled"));
    }

    public String getRogueModelsListItem() {
        return rogueModelsListItem.getText();
    }

    public String getFirmwareVersionListFirstItem() {
        return firmwareVersionListFirstItem.getText();
    }

    public String getFirmwareVersionUnfoldedListLastItem() {
        return firmwareVersionUnfoldedListLastItem.getText();
    }

    public String getFirmwareVersionFoldedListLastItem() {
        return firmwareVersionFoldedListLastItem.getText();
    }

    public void clickShowMoreButton() {
        showMoreFirmwareVersionsButton.click();
    }

    public void clickShowLessButton() {
        showLessFirmwareVersionsButton.click();
    }

    public String getIpv4LocationIp() {
        return ipv4LocationIp.getText();
    }

    public Integer getIpv4LocationPercentage() {
        return Integer.valueOf(ipv4Percentage.getValue());
    }

    public String getIpv6LocationIp() {
        return ipv6LocationIp.getText();
    }

    public Integer getIpv6LocationPercentage() {
        return Integer.valueOf(ipv6Percentage.getValue());
    }
}
