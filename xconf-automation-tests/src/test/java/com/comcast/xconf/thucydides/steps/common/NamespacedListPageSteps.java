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
 * Author: asilchenko
 * Created: 3/17/2016  2:25 PM
 */
package com.comcast.xconf.thucydides.steps.common;

import com.comcast.xconf.thucydides.pages.common.IpListPage;
import com.comcast.xconf.thucydides.pages.common.MacListPage;
import com.comcast.xconf.thucydides.pages.common.NamespacedListPageObjects;
import net.thucydides.core.annotations.Step;

import static org.junit.Assert.assertEquals;

public class NamespacedListPageSteps {

    private NamespacedListPageObjects page;
    private MacListPage macListPage;
    private IpListPage ipListPage;

    @Step
    public NamespacedListPageSteps openMacListPage() {
        macListPage.open();
        return this;
    }

    @Step
    public NamespacedListPageSteps openIpListPage() {
        ipListPage.open();
        return this;
    }

    @Step
    public NamespacedListPageSteps typeData(String value) {
        page.typeData(value);
        return this;
    }

    @Step
    public NamespacedListPageSteps setSearchName(String value) {
        page.setSearchName(value);
        return this;
    }

    @Step
    public NamespacedListPageSteps setSearchData(String value) {
        page.setSearchData(value);
        return this;
    }

    @Step
    public NamespacedListPageSteps clickAddItemToDataButton() {
        page.clickAddItemToDataButton();
        return this;
    }

    @Step
    public NamespacedListPageSteps clickRemoveDataItemButton() {
        page.clickRemoveDataItemButton();
        return this;
    }

    @Step
    public NamespacedListPageSteps clickRestoreDataItemButton() {
        page.clickRestoreDataItemButton();
        return this;
    }

    @Step
    public NamespacedListPageSteps waitNotFoundResultMessage() {
        page.waitNotFoundResultMessage();
        return this;
    }

    @Step
    public NamespacedListPageSteps verifyNamespacedListsCount(int expectedCount) {
        assertEquals(expectedCount, page.getNamespaceListsCount());
        return this;
    }

    @Step
    public NamespacedListPageSteps typeNewId(String newId) {
        page.typeNewId(newId);
        return this;
    }
}
