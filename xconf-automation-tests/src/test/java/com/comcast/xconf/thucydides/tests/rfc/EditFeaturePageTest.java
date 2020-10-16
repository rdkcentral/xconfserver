/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.thucydides.tests.rfc;

import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.thucydides.steps.GenericSteps;
import com.comcast.xconf.thucydides.steps.rfc.EditFeaturePageSteps;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.TestConstants;
import com.comcast.xconf.thucydides.util.rfc.FeatureUtils;
import net.thucydides.core.Thucydides;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.io.File;

@RunWith(ThucydidesRunner.class)
public class EditFeaturePageTest {

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @ManagedPages(defaultUrl = TestConstants.UX_URL + "#")
    private Pages pages;

    @Steps
    private EditFeaturePageSteps editFeaturePageSteps;

    @Steps
    private GenericSteps genericSteps;

    @Before
    @After
    public void cleanup() throws Exception {
        GenericTestUtils.deleteEntities(FeatureUtils.RFC_FEATURE, Feature.class);
    }

    @Test
    public void createFeature() throws Exception {
        FeatureUtils.createAndSaveFeature();
        final String name = "testCreate";
        editFeaturePageSteps.open();
        genericSteps.clickCreateButton();
        editFeaturePageSteps.typeName(name)
            .typeFeatureName(name)
            .typeKey(name)
            .typeValue(name);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void editFeature() throws Exception {
        FeatureUtils.createAndSaveFeature();
        final String name = "testEdit";
        editFeaturePageSteps.open();
        genericSteps.clickEditButton();
        editFeaturePageSteps.typeName(name)
            .typeFeatureName(name)
            .typeKey(name)
            .typeValue(name);
        genericSteps.clickSaveButton()
                .waitSuccessfullySavedToaster(name);
    }

    @Test
    public void deleteFeature() throws Exception {
        final Feature feature = FeatureUtils.createAndSaveFeature();
        editFeaturePageSteps.open();
        genericSteps.clickDeleteButton();
        genericSteps.verifyDeleteModalWindow(feature.getName(),"Feature")
                .clickModalOkButton()
                .waitSuccessfullyDeletedToaster(feature.getName());
    }

    @Test
    public void exportOneFeature() throws Exception {
        final Feature feature = FeatureUtils.createAndSaveFeature();
        File dirForDownload = GenericTestUtils.createTempDir();
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
        editFeaturePageSteps.open();
        genericSteps.clickExportOneButton()
                .checkSavedFile(dirForDownload,  "feature_" + feature.getId() + ".json");
    }
}
