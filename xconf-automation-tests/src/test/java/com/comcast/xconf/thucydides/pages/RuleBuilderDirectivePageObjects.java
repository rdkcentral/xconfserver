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
 * Created: 3/14/16  2:48 PM
 */
package com.comcast.xconf.thucydides.pages;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RuleBuilderDirectivePageObjects extends PageObject {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RuleBuilderDirectivePageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "div.rule-builder input[title='negateRelation']")
    private WebElementFacade negateCheckbox;

    @FindBy(css = "div.rule-builder input[title='freeArg']")
    private WebElementFacade freeArgInput;

    @FindBy(css = "div.rule-builder input[title='operation']")
    private WebElementFacade operationInput;

    @FindBy(css = "div.rule-builder input[title='fixedArg']")
    private WebElementFacade fixedArgInput;

    @FindBy(css = "div.rule-builder input[title='fixedArgIn']")
    private WebElementFacade fixedArgInInput;

    @FindBy(css = "div.rule-builder-negated input[title='negateRelation']")
    private WebElementFacade negateRelation;

    @FindBy(css = "div.rule-builder div.rule-builder-add")
    private WebElementFacade addConditionButton;

    @FindBy(css = "div[rule-type='ruleview-editor'] div.ruleview-rule")
    private WebElementFacade ruleCondition;

    @FindBy(css = "div[rule-type='ruleview-editor'] ul.ruleview-list")
    private WebElementFacade ruleConditionList;

    @FindBy(css = "div.rule-builder select[title='operation']")
    private WebElementFacade operationSelect;

    @FindBy(css = "div.rule-builder ul.dropdown-menu")
    private WebElementFacade typeaheadList;

    @FindBy(css = "div.rule-builder ul.rule-builder-relation li.or-relation")
    private WebElementFacade orRelationButton;

    @FindBy(css = "div.rule-builder ul.rule-builder-relation li.and-relation")
    private WebElementFacade andRelationButton;

    @FindBy(css="div.modal-dialog")
    private WebElementFacade modalDialog;

    @FindBy(css="div.modal-dialog ads-tagautocomplete-list input")
    private WebElementFacade searchText;

    public void clickNegateCheckbox() {
        negateCheckbox.click();
    }

    public String getFreeArgValue() {
        return freeArgInput.getValue();
    }

    public String getFixedArgValue(Operation operation) {
        String fixedArg = null;
        if (StandardOperation.IS.equals(operation) || StandardOperation.GTE.equals(operation) || StandardOperation.LTE.equals(operation)) {
            fixedArg = fixedArgInput.getValue();
        } else if (StandardOperation.IN.equals(operation)) {
            fixedArg = fixedArgInInput.getValue();
        } else {
            logger.warn("Didn't find any operations. fixedArg is null");
        }
        return fixedArg;
    }

    public void typeFreeArg(String value) {
        freeArgInput.type(value);
    }

    public void typeFixedArg(String value) {
        fixedArgInput.type(value);
    }

    public void clickOperationSelect() {
        operationInput.click();
    }

    public void clickFixedArgIn() {
        fixedArgInInput.click();
    }

    public boolean isSelectedNegateRelation() {
        return negateRelation.isSelected();
    }

    public void checkNegateRelation() {
        negateRelation.click();
    }

    public void waitModalDialog() {
        modalDialog.waitUntilVisible();
    }

    public void typeSearchText(String value) {
        modalDialog.then(By.tagName("input")).type(value);
    }

    public void selectSuggestion() {
        WebElementFacade suggestionsList = modalDialog.findBy(By.className("ads-suggestions-list"));
        suggestionsList.then(By.tagName("li")).waitUntilVisible();
        suggestionsList.thenFindAll(By.tagName("li")).get(0).click();
    }

    public void saveModalDialog() {
        modalDialog.thenFindAll(By.tagName("button")).get(2).click();
        modalDialog.waitUntilNotVisible();
    }

    public void closeModalDialog() {
        modalDialog.thenFindAll(By.tagName("button")).get(1).click();
        modalDialog.waitUntilNotVisible();
    }

    public WebElementFacade getRuleCondition() {
        return ruleCondition;
    }

    public List<WebElementFacade> getRuleConditionList() {
        return ruleConditionList.thenFindAll(By.tagName("li"));
    }

    public WebElementFacade getRuleCondition(int index) {
        return getRuleConditionList().get(index).then(By.cssSelector("div.ruleview-rule")).waitUntilVisible();
    }

    public void clickAddConditionButton() {
        addConditionButton.waitUntilPresent().click();
    }

    public void clickRuleCondition() {
        getRuleCondition().click();
    }

    public void clickRuleCondition(int index) {
        getRuleCondition(index).click();
    }

    public void selectOperation(String value) {
        operationSelect.selectByVisibleText(value);
    }

    public void clickTypeaheadItem(String name) {
        boolean isNotFoundItem = true;
        WebElementFacade typeaheadListItem = typeaheadList.findBy(By.tagName("li"));
        typeaheadListItem.waitUntilPresent();

        //selects on item of typeahead list if name exists in it
        List<WebElementFacade> typeaheadListItems = typeaheadList.thenFindAll(By.tagName("li"));
        for (WebElementFacade facade : typeaheadListItems) {
            if (StringUtils.equals(facade.getText(), name)) {
                isNotFoundItem = false;
                facade.click();
                break;
            }
        }

        // selects the first item if name was not found in the list
        if (isNotFoundItem) {
            typeaheadListItem.click();
        }
    }

    public void clickAndRelation() {
        andRelationButton.click();
    }

    public void clickOrRelation() {
        orRelationButton.click();
    }

    public void validateRuleCondition(WebElementFacade rule, boolean negated, String... dataArray) {

        /*
        * elementsList contains:
        * ruleview-negated -> 'Test will be failed, if 'negated' has true, but 'not' element was not found on a page'
        *   or 'negated' has false, but 'not' element was shown
        * ruleview-argument <=> freeArg
        * ruleview-operation
        * ruleview-value <=> fixedArg
        * ruleview-relation
        *
        * length is 4
        * */
        List<WebElementFacade> elementsList = new ArrayList<>();
        elementsList.addAll(rule.then(By.className("ruleview-condition")).thenFindAll(By.tagName("div")));
        elementsList.add(rule.then(By.className("ruleview-relation")));

        //dataArray.length + 1 because, 'negated' argument is as boolean type
        if (elementsList.size() != dataArray.length + 1) {
            assertTrue("Has different length: ", false);
        }

        for (int i = 0; i < elementsList.size(); i++) {
            if (elementsList.get(i).isCurrentlyVisible()) {
                if (i == 0) { // it's ruleview-negated
                    assertTrue("'not' element should be hidden", negated);
                } else {
                    String exceptedResult = dataArray[i - 1] != null ? dataArray[i - 1].toLowerCase() : null;
                    String actualResult = elementsList.get(i).getText() != null ? elementsList.get(i).getText().toLowerCase() : null;

                    if (exceptedResult != null) {
                        assertTrue("Expected result: " + exceptedResult + ", Actual result: " + actualResult, exceptedResult.equals(actualResult));
                    } else {
                        throw new NullPointerException("Data array has null. Element: " + elementsList.get(i));
                    }
                }
            } else {
                assertFalse("'not' element should be shown", negated);
            }
        }
    }
}
