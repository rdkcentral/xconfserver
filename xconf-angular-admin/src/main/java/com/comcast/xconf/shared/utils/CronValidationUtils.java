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
 *  Created: 24/3/17 4:35 PM
 */

package com.comcast.xconf.shared.utils;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class CronValidationUtils {

    private static final Integer numberOfDaysInLeapYear = 29;

    public static void validateCronDayAndMonth(String cronExpression) {
        if (StringUtils.isBlank(cronExpression)) {
            throw new ValidationRuntimeException("Cron expression is blank");
        }
        String[] cronFields = cronExpression.split(" ");
        if (cronFields.length < 4) {
            return;
        }
        String dayOfMonth = cronFields[2];
        String month = cronFields[3];
        if (StringUtils.equals("*", dayOfMonth) || StringUtils.equals("*", month)) {
            return;
        }

        if (Calendar.FEBRUARY == Integer.valueOf(month) && Integer.valueOf(dayOfMonth) == numberOfDaysInLeapYear) {
            return;
        }

        String DATE_TIME_FORMAT = "MM-dd";

        try {
            String time = (Integer.valueOf(month) + 1) + "-" + dayOfMonth;
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
            sdf.setLenient(false);
            sdf.parse(time);
        }
        catch (Exception e) {
            throw new ValidationRuntimeException("CronExpression has unparseable day or month value: " + cronExpression);
        }
    }
}
