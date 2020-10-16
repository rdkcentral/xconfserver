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
 * Created: 3/29/2016
*/
package com.comcast.xconf.thucydides.pages.setting;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;

@DefaultUrl("/settingrules/all")
public class SettingRulePage extends PageObject {

    @FindBy(css = "#name")
    private WebElementFacade nameInput;

    @FindBy(css = "#boundSettingId")
    private WebElementFacade boundSettingIdSelect;

    @FindBy(css = "#nameSearch")
    private WebElementFacade nameSearchInput;

    @FindBy(css = "#freeArgSearch")
    private WebElementFacade freeArgSearchInput;

    @FindBy(css = "#fixedArgSearch")
    private WebElementFacade fixedArgSearchInput;

    @FindBy(css = ".setting-rule-name-value")
    private WebElementFacade settingRuleNameElement;

    @FindBy(css = "tbody")
    private WebElementFacade tbody;


    public void typeName(final String value) {
        nameInput.type(value);
    }

    public void typeBoundSettingId(final String value) {
        boundSettingIdSelect.selectByVisibleText(value);
    }

    public void typeNameSearch(String value) {
        nameSearchInput.type(value);
    }

    public void typeFreeArgSearch(String value) {
        freeArgSearchInput.type(value);
    }

    public void typeFixedArgSearch(String value) {
        fixedArgSearchInput.type(value);
    }

    public String getSettingRuleName() {
        return settingRuleNameElement.getText();
    }

    public int getSettingRulesCount() {
        return tbody.waitUntilVisible()
                .thenFindAll("tr").size();
    }
}
