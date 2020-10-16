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
 * Created: 3/17/16  11:38 AM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class DownloadLocationFilterEditPageObjects extends PageObject {

    public DownloadLocationFilterEditPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#httpLocation")
    private WebElementFacade httpLocationInput;

    @FindBy(css = "input#httpFullUrlLocation")
    private WebElementFacade httpFullUrlLocationInput;

    @FindBy(css = "input#neverUseHttp")
    private WebElementFacade neverUseHttpInput;

    @FindBy(css = "ul#rogueModelsList li")
    private WebElementFacade rogueModelsListItem;

    @FindBy(css = "div#firmwareVersionsList textarea")
    private WebElementFacade firmwareVersionListInput;

    @FindBy(css = "div#ipv4Locations input.location-ip")
    private WebElementFacade ipv4LocationIpInput;

    @FindBy(css = "div#ipv4Locations input.location-percentage")
    private WebElementFacade ipv4PercentageInput;

    @FindBy(css = "div#ipv4Locations button.remove-location")
    private WebElementFacade removeIpv4LocationButton;

    @FindBy(css = "div#ipv4Locations button.add-location")
    private WebElementFacade addIpv4LocationButton;

    @FindBy(css = "div#ipv6Locations input.location-ip")
    private WebElementFacade ipv6LocationIpInput;

    @FindBy(css = "div#ipv6Locations input.location-percentage")
    private WebElementFacade ipv6PercentageInput;

    @FindBy(css = "div#ipv6Locations button.remove-location")
    private WebElementFacade removeIpv6LocationButton;

    @FindBy(css = "div#ipv6Locations button.add-location")
    private WebElementFacade addIpv6LocationButton;

    public void typeHttpLocation(String value) {
        httpLocationInput.type(value);
    }

    public void typeFullUrlHttpLocation(String value) {
        httpFullUrlLocationInput.type(value);
    }

    public void clickNeverUseHttp() {
        neverUseHttpInput.click();
    }

    public Boolean neverUseHttpIsChecked() {
        return neverUseHttpInput.isSelected();
    }

    public void clickRogueModelsListItem() {
        rogueModelsListItem.click();
    }

    public Boolean rogueModelsListItemIsSelected() {
        return rogueModelsListItem.getAttribute("class").contains("checked-in-list");
    }

    public void typeFirmwareVersions(String value) {
        firmwareVersionListInput.type(value);
    }

    public void typeIpv4LocationIp(String value) {
        ipv4LocationIpInput.type(value);
    }

    public void typeIpv4LocationPercentage(String value) {
        ipv4PercentageInput.type(value);
    }

    public void clickRemoveIpv4LocationButton() {
        removeIpv4LocationButton.click();
    }

    public void clickAddIpv4LocationButton() {
        addIpv4LocationButton.click();
    }

    public void typeIpv6LocationIp(String value) {
        ipv6LocationIpInput.type(value);
    }

    public void typeIpv6LocationPercentage(String value) {
        ipv6PercentageInput.type(value);
    }

    public void clickRemoveIpv6LocationButton() {
        removeIpv6LocationButton.click();
    }

    public void clickAddIpv6LocationButton() {
        addIpv6LocationButton.click();
    }
}
