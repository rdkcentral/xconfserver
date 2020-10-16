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
 * Created: 3/15/16  4:35 PM
 */
package com.comcast.xconf.thucydides.pages;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class RuleViewDirectivePageObjects extends PageObject {

    public RuleViewDirectivePageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "div[rule-type='ruleview'] div.ruleview-relation")
    private WebElementFacade relation;

    @FindBy(css = "div[rule-type='ruleview'] div.ruleview-negated")
    private WebElementFacade negated;

    @FindBy(css = "div[rule-type='ruleview'] div.ruleview-argument")
    private WebElementFacade freeArg;

    @FindBy(css = "div[rule-type='ruleview'] div.ruleview-operation")
    private WebElementFacade operation;

    @FindBy(css = "div[rule-type='ruleview'] div.ruleview-value")
    private WebElementFacade fixedArg;

    public String getRelation() {
        return relation.getText();
    }

    public Boolean isNegated() {
        return negated.isDisplayed();
    }

    public String getFreeArg() {
        return freeArg.getText();
    }

    public String getOperation() {
        return operation.getText();
    }

    public String getFixedArg() {
        return fixedArg.getText();
    }
}
