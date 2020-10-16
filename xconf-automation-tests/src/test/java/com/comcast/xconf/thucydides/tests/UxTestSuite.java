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
 * Author: Alexander Binkovsky
 * Created: 2/28/2016  2:45 AM
 */
package com.comcast.xconf.thucydides.tests;

import com.comcast.xconf.thucydides.tests.common.EnvironmentPageTest;
import com.comcast.xconf.thucydides.tests.common.IpListPageTest;
import com.comcast.xconf.thucydides.tests.common.MacListPageTest;
import com.comcast.xconf.thucydides.tests.common.ModelPageTest;
import com.comcast.xconf.thucydides.tests.dcm.*;
import com.comcast.xconf.thucydides.tests.firmware.*;
import com.comcast.xconf.thucydides.tests.rfc.EditFeaturePageTest;
import com.comcast.xconf.thucydides.tests.rfc.EditFeatureRulePageTest;
import com.comcast.xconf.thucydides.tests.rfc.FeatureTestPageTest;
import com.comcast.xconf.thucydides.tests.setting.SettingProfilePageTest;
import com.comcast.xconf.thucydides.tests.setting.SettingRulePageTest;
import com.comcast.xconf.thucydides.tests.setting.SettingTestPageTest;
import com.comcast.xconf.thucydides.tests.telemetry.PermanentProfilePageTest;
import com.comcast.xconf.thucydides.tests.telemetry.TargetingRuleListPageTest;
import com.comcast.xconf.thucydides.tests.telemetry.TargetingRulePageTest;
import com.comcast.xconf.thucydides.tests.telemetry.TelemetryTestFormPageTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        EnvironmentPageTest.class,
        ModelPageTest.class,
        FirmwareConfigPageTest.class,
        FirmwareRuleTemplatePageTest.class,
        FirmwareRulePageTest.class,
        DownloadLocationFilterPageTest.class,
        SettingProfilePageTest.class,
        SettingRulePageTest.class,
        PermanentProfilePageTest.class,
        TargetingRulePageTest.class,
        TargetingRuleListPageTest.class,
        TelemetryTestFormPageTest.class,
        LogPageTest.class,
        FirmwareTestFormPageTest.class,
        ReportPageTest.class,
        DeviceSettingsPageTest.class,
        FormulaPageTest.class,
        LogUploadSettingsPageTest.class,
        VodSettingsPageTest.class,
        DcmTestFormPageTest.class,
        MacListPageTest.class,
        IpListPageTest.class,
        UploadRepositoryPageTest.class,
        PercentFilterPageTest.class,
        SettingTestPageTest.class,
        EditFeaturePageTest.class,
        EditFeatureRulePageTest.class,
        FeatureTestPageTest.class
})
public class UxTestSuite {

}
