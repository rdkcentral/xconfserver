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
 * Created: 21.10.15  16:17
 */
(function () {
    'use strict';
    angular
        .module('app.loguploadsettings')
        .factory('logUploadSettingsValidationService', service);


    service.$inject = ['utilsService', 'SCHEDULE_TYPE'];

    function service(utilsService, SCHEDULE_TYPE) {
        return {
            validateName: validateName,
            validateExpression: validateExpression,
            validateNonNegativeNumber: validateNonNegativeNumber,
            validateTimeWindowMinutes: validateTimeWindowMinutes,
            validateUploadRepositoryId: validateUploadRepositoryId,
            validateCronHours: validateCronHours,
            validateCronMinutes: validateCronMinutes,
            validateCronDayOfMonth: validateCronDayOfMonth,
            validateCronDayOfWeek: validateCronDayOfWeek,
            validateCronMonth: validateCronMonth,
            validateAll: validateAll
        };

        function buildReturnForValidate (isValid, message) {
            return {
                isValid: isValid,
                message: message
            };
        }

        function validateName(name, usedNames) {
            if (utilsService.isNullOrUndefinedOrEmptyOrWhiteSpaceString(name)) {
                return buildReturnForValidate(false, 'Name must not be empty');
            }

            if(usedNames.indexOf(name) >= 0) {
                return buildReturnForValidate(false, 'Such name already exists');
            }

            return buildReturnForValidate(true);
        }

        function validateExpression(expression, scheduleType) {
            if (utilsService.isNullOrUndefinedOrEmptyOrWhiteSpaceString(expression) && scheduleType !== SCHEDULE_TYPE.WHOLE_DAY_RANDOMIZED) {
                return buildReturnForValidate(false, 'Expression must not be empty');
            }

            return buildReturnForValidate(true);
        }

        function validateNonNegativeNumber(number) {
            if (utilsService.isNullOrUndefinedOrEmpty(number)) {
                return buildReturnForValidate(false, 'Field must not be empty');
            }

            if (!utilsService.isInt(number) || number < 0) {
                return buildReturnForValidate(false, 'Field must be non-negative number');
            }

            return buildReturnForValidate(true);
        }

        function validateTimeWindowMinutes(timeWindowMinutes, scheduleType) {
            if (!validateNonNegativeNumber(timeWindowMinutes).isValid && scheduleType !== SCHEDULE_TYPE.WHOLE_DAY_RANDOMIZED) {
                return buildReturnForValidate(false, validateNonNegativeNumber(timeWindowMinutes).message);
            }

            return buildReturnForValidate(true);
        }

        function validateUploadRepositoryId(uploadRepositoryId) {
            if (utilsService.isNullOrUndefinedOrEmptyOrWhiteSpaceString(uploadRepositoryId)) {
                return buildReturnForValidate(false, "You must create upload repository first");
            }

            return buildReturnForValidate(true);
        }

        function validateCronMinutes(minutes) {
            if (!minutes || minutes === '') {
                return buildReturnForValidate(false, "Minutes should not be empty");
            }
            if (/^-?[0-9]+$/.test(minutes) && utilsService.isInt(minutes)) {
                minutes = parseInt(minutes);
                if (minutes >= 0 && minutes <= 59) {
                    return buildReturnForValidate(true)
                } else {
                    return buildReturnForValidate(false, "Minutes should be from 0 to 59")
                }
            } else {
                return buildReturnForValidate(false, "Minutes should be from 0 to 59")
            }
        }

        function validateCronHours(hours) {
            if (!hours || hours === '') {
                return buildReturnForValidate(false, "Hours should not be empty");
            }
            if (/^-?[0-9]+$/.test(hours) && utilsService.isInt(hours)) {
                hours = parseInt(hours);
                if (hours >= 0 && hours <= 23) {
                    return buildReturnForValidate(true)
                } else {
                    return buildReturnForValidate(false, "Hours should be from 0 to 23")
                }
            } else {
                return buildReturnForValidate(false, "Hours should be from 0 to 23")
            }
        }

        function validateCronDayOfMonth(dayOfMonth) {
            if (/^-?[0-9]+$/.test(dayOfMonth) && utilsService.isInt(dayOfMonth)) {
                dayOfMonth = parseInt(dayOfMonth);
                if (dayOfMonth >= 1 && dayOfMonth <= 31 ) {
                    return buildReturnForValidate(true)
                } else {
                    return buildReturnForValidate(false, "Day of month should be from 1 to 31 or *")
                }
            } else if(/\*/.test(dayOfMonth) && dayOfMonth.length === 1) {
                return buildReturnForValidate(true);
            }
            return buildReturnForValidate(false, "Day of month should be from 1 to 31 or *")
        }

        function validateCronMonth(month) {
            if (/^-?[0-9]+$/.test(month) && utilsService.isInt(month)) {
                month = parseInt(month);
                if (month >= 0 && month <= 11) {
                    return buildReturnForValidate(true)
                } else {
                    return buildReturnForValidate(false, "Month should be from 0 to 11 or *")
                }
            } else if(/\*/.test(month) && month.length === 1) {
                return buildReturnForValidate(true);
            }
            return buildReturnForValidate(false, "Month should be from 0 to 11 or *")
        }

        function validateCronDayOfWeek(dayOfWeek) {
            if (/^-?[0-9]+$/.test(dayOfWeek) && utilsService.isInt(dayOfWeek)) {
                dayOfWeek = parseInt(dayOfWeek);
                if (dayOfWeek >= 1 && dayOfWeek <= 7 ) {
                    return buildReturnForValidate(true)
                } else {
                    return buildReturnForValidate(false, "Day of week should be from 1 to 7 or *")
                }
            } else if(/\*/.test(dayOfWeek) && dayOfWeek.length === 1) {
                return buildReturnForValidate(true);
            }
            return buildReturnForValidate(false, "Day of week should be from 1 to 7 or *")
        }

        function validateAll(logUploadSettings, usedNames, cronFields) {
            return validateName(logUploadSettings.name, usedNames).isValid
                && validateExpression(logUploadSettings.schedule.expression, logUploadSettings.schedule.type).isValid
                && validateTimeWindowMinutes(logUploadSettings.schedule.timeWindowMinutes, logUploadSettings.schedule.type).isValid
                && validateNonNegativeNumber(logUploadSettings.numberOfDays).isValid
                // && validateLogFilesIsPresent(logFilesArray).isValid
                && validateUploadRepositoryId(logUploadSettings.uploadRepositoryId).isValid
                && validateCronMinutes(cronFields.minutes).isValid
                && validateCronHours(cronFields.hours).isValid
                && validateCronDayOfMonth(cronFields.dayOfMonth).isValid
                && validateCronMonth(cronFields.month).isValid
                && validateCronDayOfWeek(cronFields.dayOfWeek).isValid;
        }
    }
})();