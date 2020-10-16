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
 * Author: Igor Kostrov
 * Created: 3/28/2016
*/
package com.comcast.xconf.thucydides.pages.setting;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/settingprofiles/all")
public class SettingProfilePage extends PageObject {

    @FindBy(css = "input#searchByName")
    private WebElementFacade searchNameInput;

    @FindBy(css = "tbody")
    private WebElementFacade tbody;

    @FindBy(css = "div#notFoundResults")
    private WebElementFacade notFoundResults;

    @FindBy(css = "#name")
    private WebElementFacade nameInput;

    @FindBy(css = "#settingType")
    private WebElementFacade settingType;

    @FindBy(css = "#profileEntries input[placeholder='Key']")
    private WebElementFacade entryKey;

    @FindBy(css = "#profileEntries input[placeholder='Value']")
    private WebElementFacade entryValue;

    public SettingProfilePage(final WebDriver webDriver) {
        super(webDriver);
    }


    public void typeName(final String value) {
        nameInput.type(value);
    }

    public void selectType(String value) {
        settingType.selectByValue(value);
    }

    public void typeEntryKey(final String value) {
        entryKey.type(value);
    }


    public void typeEntryValue(final String value) {
        entryValue.type(value);
    }

    public void setSearchName(String value) {
        searchNameInput.type(value);
    }

    public int getSettingProfilesCount() {
        return tbody.waitUntilVisible()
                .thenFindAll("tr").size();
    }

    public void waitNotFoundResultMessage() {
        notFoundResults.waitUntilVisible();
    }

}
