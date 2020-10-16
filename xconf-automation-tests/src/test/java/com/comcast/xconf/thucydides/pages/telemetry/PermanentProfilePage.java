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
 * Author: rdolomansky
 * Created: 3/15/16  3:55 PM
 */
package com.comcast.xconf.thucydides.pages.telemetry;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/permanentprofile/all")
public class PermanentProfilePage extends PageObject {

    @FindBy(css = "input#searchByName")
    private WebElementFacade searchNameInput;

    @FindBy(css = "tbody")
    private WebElementFacade tbody;

    @FindBy(css = "div#notFoundResults")
    private WebElementFacade notFoundResults;

    @FindBy(css = "#name")
    private WebElementFacade nameInput;

    @FindBy(css = "#schedule")
    private WebElementFacade scheduleInput;

    @FindBy(css = "#protocol")
    private WebElementFacade protocolSelect;

    @FindBy(css = "#url")
    private WebElementFacade urlInput;


    @FindBy(css = "#profileEntries input[placeholder='Header']")
    private WebElementFacade header;

    @FindBy(css = "#profileEntries input[placeholder='Content']")
    private WebElementFacade content;

    @FindBy(css = "#profileEntries input[placeholder='Type']")
    private WebElementFacade type;

    @FindBy(css = "#profileEntries input[placeholder='Polling frequency (sec)']")
    private WebElementFacade pollingFrequency;


    public PermanentProfilePage(final WebDriver webDriver) {
        super(webDriver);
    }


    public void typeName(final String value) {
        nameInput.type(value);
    }

    public void typeSchedule(final String value) {
        scheduleInput.type(value);
    }

    public void selectProtocol(final String value) {
        protocolSelect.selectByValue(value);
    }

    public void typeUrl(final String value) {
        urlInput.type(value);
    }

    public void typeHeader(final String value) {
        header.type(value);
    }

    public void typeContent(final String value) {
        content.type(value);
    }

    public void typeType(final String value) {
        type.type(value);
    }

    public void typePollingFrequency(final String value) {
        pollingFrequency.type(value);
    }

    public void setSearchName(String value) {
        searchNameInput.type(value);
    }

    public int getPermanentProfilesCount() {
        return tbody.waitUntilVisible()
                .thenFindAll("tr").size();
    }

    public void waitNotFoundResultMessage() {
        notFoundResults.waitUntilVisible();
    }

}

