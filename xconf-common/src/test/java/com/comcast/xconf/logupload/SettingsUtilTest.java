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
package com.comcast.xconf.logupload;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SettingsUtilTest {

    private SettingsUtil settingsUtil = new SettingsUtil();

    @Test
    public void testValidateCronExpression() throws Exception {
        Assert.assertTrue(SettingsUtil.validate("15 23 * * *"));
        Assert.assertTrue(SettingsUtil.validate("15 23 "));

        Assert.assertFalse(SettingsUtil.validate("15"));
        Assert.assertFalse(SettingsUtil.validate("15 * * *"));
        Assert.assertFalse(SettingsUtil.validate("* * *"));
    }

    @Test
    public void getAddedHoursToRandomizedCronByTimeZone() throws Exception {
        Integer hours = settingsUtil.getAddedHoursToRandomizedCronByTimeZone("US/Pacific");
        assertEquals(Integer.valueOf(3), hours);

        hours = settingsUtil.getAddedHoursToRandomizedCronByTimeZone("US/Eastern");
        assertEquals(Integer.valueOf(0), hours);

        hours = settingsUtil.getAddedHoursToRandomizedCronByTimeZone("unknown");
        assertEquals(Integer.valueOf(0), hours);
    }

}