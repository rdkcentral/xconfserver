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
 * Created: 3/21/16  5:11 PM
 */
package com.comcast.xconf.thucydides.pages.firmware;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/firmware/reportpage")
public class ReportPage extends PageObject {

    public ReportPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "li.ads-list-item")
    private WebElementFacade macRulesListItem;

    @FindBy(css = "button#uncheckAll")
    private WebElementFacade uncheckAllButton;

    @FindBy(css = "button#checkAll")
    private WebElementFacade checkAllButton;

    @FindBy(css = "button#getReport")
    private WebElementFacade getReportButton;

    public void clickMacRulesListItem() {
        macRulesListItem.click();
    }

    public Boolean macRulesListItemIsSelected() {
        return macRulesListItem.getAttribute("class").contains("ads-list-item-green");
    }

    public void clickUncheckAllButton() {
        uncheckAllButton.click();
    }

    public void clickCheckAllButton() {
        checkAllButton.click();
    }

    public void clickGetReportButton() {
        getReportButton.click();
    }
}
