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
 * Created: 3/28/16  3:43 PM
 */
package com.comcast.xconf.thucydides.pages.dcm;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class FormulaViewPageObjects extends PageObject {

    public FormulaViewPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#name")
    private WebElementFacade name;

    @FindBy(css = "input#description")
    private WebElementFacade description;

    @FindBy(css = "input#percentage")
    private WebElementFacade percentage;

    @FindBy(css = "input#priority")
    private WebElementFacade priority;

    @FindBy(css = "input#l1Percentage")
    private WebElementFacade l1Percentage;

    @FindBy(css = "input#l2Percentage")
    private WebElementFacade l2Percentage;

    @FindBy(css = "input#l3Percentage")
    private WebElementFacade l3Percentage;

    public String getName() {
        return name.getValue();
    }

    public String getDescription() {
        return description.getValue();
    }

    public String getPriority() {
        return priority.getValue();
    }

    public String getPercentage() {
        return percentage.getValue();
    }

    public String getL1Percentage() {
        return l1Percentage.getValue();
    }

    public String getL2Percentage() {
        return l2Percentage.getValue();
    }

    public String getL3Percentage() {
        return l3Percentage.getValue();
    }

//    public String
}
