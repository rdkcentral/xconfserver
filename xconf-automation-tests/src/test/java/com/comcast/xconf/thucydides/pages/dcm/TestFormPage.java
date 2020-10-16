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
 * Author: Stanislav Menshykov
 * Created: 3/29/16  5:13 PM
 */
package com.comcast.xconf.thucydides.pages.dcm;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

@DefaultUrl("/dcm/testpage")
public class TestFormPage extends PageObject {

    public TestFormPage(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "div#dcmTestPage input.test-page-estbip")
    private WebElementFacade estbIpInput;

    @FindBy(css = "div#dcmTestPage input.test-page-estbmac")
    private WebElementFacade estbMacInput;

    @FindBy(css = "div#dcmTestPage input.test-page-ecmmac")
    private WebElementFacade ecmMacInput;

    @FindBy(css = "div#dcmTestPage input.test-page-env")
    private WebElementFacade environmentInput;

    @FindBy(css = "div#dcmTestPage input.test-page-model")
    private WebElementFacade modelInput;

    @FindBy(css = "div#dcmTestPage input.test-page-controller-id")
    private WebElementFacade controllerIdInput;

    @FindBy(css = "div#dcmTestPage input.test-page-channel-map-id")
    private WebElementFacade channelMapIdInput;

    @FindBy(css = "div#dcmTestPage input.test-page-vod-id")
    private WebElementFacade vodIdInput;

    @FindBy(id = "testButton")
    private WebElementFacade testButton;

    @FindBy(css = "td#ruleType")
    private WebElementFacade ruleType;

    @FindBy(css = "td#matchedRuleIds span.test-page-matched-rule-id")
    private WebElementFacade matchedRuleId;

    @FindBy(css = "td#dsName")
    private WebElementFacade dsName;

    @FindBy(css = "td#dsCheckOnReboot")
    private WebElementFacade dsCheckOnReboot;

    @FindBy(css = "td#dsCronExpression")
    private WebElementFacade dsCronExpression;

    @FindBy(css = "td#dsDurationMinutes")
    private WebElementFacade dsDurationMinutes;

    @FindBy(css = "td#lusName")
    private WebElementFacade lusName;

    @FindBy(css = "td#lusNumberOfDays")
    private WebElementFacade lusNumberOfDays;

    @FindBy(css = "td#lusUploadRepositoryName")
    private WebElementFacade lusUploadRepositoryName;

    @FindBy(css = "td#lusRepositoryUrl")
    private WebElementFacade lusRepositoryUrl;

    @FindBy(css = "td#lusUploadRepositoryUrl")
    private WebElementFacade lusUploadRepositoryUrl;

    @FindBy(css = "td#lucUploadProtocol")
    private WebElementFacade lucUploadProtocol;

    @FindBy(css = "td#lucUploadOnReboot")
    private WebElementFacade lucUploadOnReboot;

    @FindBy(css = "td#lucUpload")
    private WebElementFacade lucUpload;

    @FindBy(css = "td#lucCronExpression")
    private WebElementFacade lucCronExpression;

    @FindBy(css = "td#lucCronExpressionL1")
    private WebElementFacade lucCronExpressionL1;

    @FindBy(css = "td#lucCronExpressionL2")
    private WebElementFacade lucCronExpressionL2;

    @FindBy(css = "td#lucCronExpressionL3")
    private WebElementFacade lucCronExpressionL3;

    @FindBy(css = "td#lucMessage")
    private WebElementFacade lucMessage;

    @FindBy(css = "td#lucUploadImmediately")
    private WebElementFacade lucUploadImmediately;

    @FindBy(css = "td#lucDurationMinutes")
    private WebElementFacade lucDurationMinutes;

    @FindBy(css = "td#vsName")
    private WebElementFacade vsName;

    @FindBy(css = "td#vsLocationsUrl")
    private WebElementFacade vsLocationsUrl;

    @FindBy(css = "td#vsSrmIpList span.srm-name")
    private WebElementFacade vsSrmName;

    @FindBy(css = "td#vsSrmIpList span.srm-ip")
    private WebElementFacade vsSrmIp;

    @FindBy(css = "div input[ng-model='property.key']")
    private WebElementFacade keyParameterInput;

    @FindBy(css = "div input[ng-model='property.value']")
    private WebElementFacade valueParameterInput;

    @FindBy(css = "div#parameterEntries ul.dropdown-menu")
    private WebElementFacade typeaheadListItem;


    public void typeEstbIp(String value) {
        estbIpInput.type(value);
    }

    public void clickTestButton() {
        testButton.click();
    }

    public String getRuleType() {
        return ruleType.getText();
    }

    public String getMatchedRuleId() {
        return matchedRuleId.getText();
    }

    public String getDeviceSettingsName() {
        return dsName.getText();
    }

    public String getDeviceSettingsCheckOnReboot() {
        return dsCheckOnReboot.getText();
    }

    public String getDeviceSettingsCronExpression() {
        return dsCronExpression.getText();
    }

    public String getDeviceSettingsDurationMinutes() {
        return dsDurationMinutes.getText();
    }

    public String getLogUploadSettingsName() {
        return lusName.getText();
    }

    public String getLogUploadSettingsNumberOfDays() {
        return lusNumberOfDays.getText();
    }

    public String getLogUploadSettingsUploadRepositoryName() {
        return lusUploadRepositoryName.getText();
    }

    public String getLogUploadSettingsRepositoryUrl() {
        return lusRepositoryUrl.getText();
    }

    public String getLogUploadSettingsUploadRepositoryUrl() {
        return lusUploadRepositoryUrl.getText();
    }

    public String getLogUploadSettingsUploadProtocol() {
        return lucUploadProtocol.getText();
    }

    public String getLogUploadSettingsUploadOnReboot() {
        return lucUploadOnReboot.getText();
    }

    public String getLogUploadSettingsUpload() {
        return lucUpload.getText();
    }

    public String getLogUploadSettingsCronExpression() {
        return lucCronExpression.getText();
    }

    public String getLogUploadSettingsCronExpressionL1() {
        return lucCronExpressionL1.getText();
    }

    public String getLogUploadSettingsCronExpressionL2() {
        return lucCronExpressionL2.getText();
    }

    public String getLogUploadSettingsCronExpressionL3() {
        return lucCronExpressionL3.getText();
    }

    public String getLogUploadSettingsMessage() {
        return lucMessage.getText();
    }

    public String getLogUploadSettingsUploadImmediately() {
        return lucUploadImmediately.getText();
    }

    public String getLogUploadSettingsDurationMinutes() {
        return lucDurationMinutes.getText();
    }

    public String getVodSettingsName() {
        return vsName.getText();
    }

    public String getVodSettingsLocationsUrl() {
        return vsLocationsUrl.getText();
    }

    public String getVodSettingsSrmIpName() {
        return vsSrmName.getText();
    }

    public String getVodSettingsSrmIp() {
        return vsSrmIp.getText();
    }

    public void typeKey(String key) {
        keyParameterInput.type(key);
    }

    public void typeValue(String value) {
        valueParameterInput.type(value);
    }

    public void clickTypeHeadListItem(String itemName) {
        for (WebElementFacade li : typeaheadListItem.waitUntilPresent().thenFindAll("li")) {
            if (li.getText().equals(itemName)) {
                li.click();
                break;
            }
        }
    }
}
