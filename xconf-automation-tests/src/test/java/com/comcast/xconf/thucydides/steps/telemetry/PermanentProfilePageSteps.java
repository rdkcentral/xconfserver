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
 * Created: 3/14/16  8:21 PM
 */
package com.comcast.xconf.thucydides.steps.telemetry;

import com.comcast.xconf.thucydides.pages.telemetry.PermanentProfilePage;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.Steps;

import static org.junit.Assert.assertEquals;

public class PermanentProfilePageSteps {

    private PermanentProfilePage page;

    @Steps
    public GenericSteps genericSteps;


    @Step
    public PermanentProfilePageSteps open() {
        page.open();
        return this;
    }

    @Step
    public PermanentProfilePageSteps typeName(final String name) {
        page.typeName(name);
        return this;
    }

    @Step
    public PermanentProfilePageSteps typeSchedule(final String value) {
        page.typeSchedule(value);
        return this;
    }

    @Step
    public PermanentProfilePageSteps selectProtocol(final String value) {
        page.selectProtocol(value);
        return this;

    }

    @Step
    public PermanentProfilePageSteps typeUrl(final String value) {
        page.typeUrl(value);
        return this;
    }

    @Step
    public PermanentProfilePageSteps typeHeader(final String value) {
        page.typeHeader(value);
        return this;

    }

    @Step
    public PermanentProfilePageSteps typeContent(final String value) {
        page.typeContent(value);
        return this;

    }

    @Step
    public PermanentProfilePageSteps typeType(final String value) {
        page.typeType(value);
        return this;

    }

    @Step
    public PermanentProfilePageSteps typePollingFrequency(final String value) {
        page.typePollingFrequency(value);
        return this;
    }

    @Step
    public PermanentProfilePageSteps setSearchName(String value) {
        page.setSearchName(value);
        return this;
    }

    @Step
    public PermanentProfilePageSteps waitNotFoundResultMessage() {
        page.waitNotFoundResultMessage();
        return this;
    }

    @Step
    public PermanentProfilePageSteps verifyPermanentProfilesCount(int expectedCount) {
        assertEquals(expectedCount, page.getPermanentProfilesCount());
        return this;
    }
}
