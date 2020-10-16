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
 * Created: 3/28/16  4:51 PM
 */
package com.comcast.xconf.thucydides.util.dcm;

import com.beust.jcommander.internal.Lists;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.LogUploadSettings;
import com.comcast.xconf.logupload.Schedule;
import com.comcast.xconf.logupload.UploadRepository;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;

import java.util.List;
import java.util.UUID;

public class LogUploadSettingsUtils {
    private static final String LOG_UPLOAD_SETTINGS_URL = "dcm/logUploadSettings/";

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(LOG_UPLOAD_SETTINGS_URL, LogUploadSettings.class);
        UploadRepositoryUtils.doCleanup();
        FormulaUtils.doCleanup();
    }

    public static LogUploadSettings createDefaultLogUploadSettings() throws Exception {
        DCMGenericRule formula = FormulaUtils.createAndSaveDefaultFormula();
        LogUploadSettings result = createLogUploadSettingsWithoutFormula(formula.getId());

        return result;
    }

    public static LogUploadSettings createLogUploadSettingsWithoutFormula(String id) throws Exception {
        UploadRepository repository = UploadRepositoryUtils.createAndSaveDefaultUploadRepository();
        LogUploadSettings result = new LogUploadSettings();
        result.setId(id);
        result.setName("logUploadSettingsName");
        result.setUploadOnReboot(false);
        result.setNumberOfDays(10);
        result.setAreSettingsActive(true);
        result.setUploadRepositoryId(repository.getId());
        Schedule schedule = new Schedule();
        schedule.setType("ActNow");
        schedule.setExpression("cronExpression");
        schedule.setTimeWindowMinutes(10);
        schedule.setExpressionL1("l1");
        schedule.setExpressionL2("l2");
        schedule.setExpressionL3("l3");
        result.setSchedule(schedule);

        return result;
    }

    public static LogUploadSettings createAndSaveDefaultLogUploadSettings() throws Exception {
        LogUploadSettings result = createDefaultLogUploadSettings();
        HttpClient.post(GenericTestUtils.buildFullUrl(LOG_UPLOAD_SETTINGS_URL), result);

        return result;
    }

    public static LogUploadSettings createAndSaveLogUploadSettings(String name) throws Exception {
        LogUploadSettings logUploadSettings = createLogUploadSettingsWithoutFormula(UUID.randomUUID().toString());
        logUploadSettings.setName(name);
        HttpClient.post(GenericTestUtils.buildFullUrl(LOG_UPLOAD_SETTINGS_URL), logUploadSettings);
        return logUploadSettings;
    }

    public static List<LogUploadSettings> createAndSaveLogUploadSettingsList() throws Exception {
        return Lists.newArrayList(
                createAndSaveLogUploadSettings("logUploadSettings1"),
                createAndSaveLogUploadSettings("logUploadSettings2")
        );
    }
}
