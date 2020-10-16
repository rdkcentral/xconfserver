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
 *  Author: mdolina
 *  Created: 12/8/15 4:53 PM
 */
package com.comcast.xconf;

import com.comcast.xconf.admin.controller.change.ChangeControllerTest;
import com.comcast.xconf.admin.controller.common.EnvironmentControllerTest;
import com.comcast.xconf.admin.controller.common.IpListControllerTest;
import com.comcast.xconf.admin.controller.common.MacListControllerTest;
import com.comcast.xconf.admin.controller.common.ModelControllerTest;
import com.comcast.xconf.admin.controller.dcm.*;
import com.comcast.xconf.admin.controller.firmware.*;
import com.comcast.xconf.admin.controller.rfc.FeatureControllerTest;
import com.comcast.xconf.admin.controller.rfc.FeatureRuleControllerTest;
import com.comcast.xconf.admin.controller.setting.SettingProfileControllerTest;
import com.comcast.xconf.admin.controller.setting.SettingRuleControllerTest;
import com.comcast.xconf.admin.controller.setting.SettingTestPageControllerTest;
import com.comcast.xconf.admin.controller.shared.ChangeLogControllerTest;
import com.comcast.xconf.admin.controller.shared.StatisticsControllerTest;
import com.comcast.xconf.admin.controller.telemetry.PermanentProfileControllerTest;
import com.comcast.xconf.admin.controller.telemetry.TargetingRuleControllerTest;
import com.comcast.xconf.admin.converter.firmware.PercentageBeanConverterTest;
import com.comcast.xconf.admin.validator.UrlValidatorTest;
import com.comcast.xconf.admin.validator.change.ChangeValidatorTest;
import com.comcast.xconf.dcm.ruleengine.TelemetryProfileService;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // controller/common package
        EnvironmentControllerTest.class,
        IpListControllerTest.class,
        MacListControllerTest.class,
        ModelControllerTest.class,
        // controller/dcm
        DeviceSettingsControllerTest.class,
        FormulaQueryControllerTest.class,
        LogUploadSettingsControllerTest.class,
        UploadRepositoryControllerTest.class,
        VodSettingsControllerTest.class,
        // controller/firmware
        FirmwareConfigControllerTest.class,
        FirmwareRuleControllerTest.class,
        FirmwareRuleTemplateControllerTest.class,
        LogControllerTest.class,
        PercentageBeanControllerTest.class,
        PercentFilterControllerTest.class,
        RoundRobinFilterControllerTest.class,
        // converter/firmware
        PercentageBeanConverterTest.class,
        //rfc
        FeatureControllerTest.class,
        FeatureRuleControllerTest.class,
        // controller/settings
        SettingProfileControllerTest.class,
        SettingRuleControllerTest.class,
        SettingTestPageControllerTest.class,
        // controller/shared
        StatisticsControllerTest.class,
        ChangeLogControllerTest.class,
        // controller/telemetry
        PermanentProfileControllerTest.class,
        TargetingRuleControllerTest.class,
        // controller/change
        ChangeControllerTest.class,

        // validator/change
        ChangeValidatorTest.class,

        //utils
        UrlValidatorTest.class
})
public class CompleteTestSuite {

    public static final long telemetryProfileServiceExpireTimeMs = 1000L;

    @BeforeClass
    public static void setUpProperties() throws IOException {
        TelemetryProfileService.expireTime = telemetryProfileServiceExpireTimeMs;
    }

}
