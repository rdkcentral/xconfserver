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
/**
 * Created by izluben on 17.10.15.
 */

(function () {
    'use strict';
    angular
        .module('app.devicesettings')
        .factory('deviceSettingsValidationService', deviceSettingsValidationService);


    deviceSettingsValidationService.$inject = ['utilsService'];

    function deviceSettingsValidationService(utilsService) {
        return {
            validateName: validateName,
            validateExpression: validateExpression,
            validateTimeWindow: validateTimeWindow,
            validateAll: validateAll,
            validateCronHours: validateCronHours,
            validateCronMinutes: validateCronMinutes,
            validateCronDayOfMonth: validateCronDayOfMonth,
            validateCronDayOfWeek: validateCronDayOfWeek,
            validateCronMonth: validateCronMonth,
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

        function validateExpression(expression) {
            if (utilsService.isNullOrUndefinedOrEmptyOrWhiteSpaceString(expression)) {
                return buildReturnForValidate(false, 'Expression must not be empty');
            }

            return buildReturnForValidate(true);
        }

        function validateTimeWindow(timeWindow) {
            if (utilsService.isNullOrUndefined(timeWindow)) {
                return buildReturnForValidate(false, 'Time window must not be empty');
            }

            if (!utilsService.isInt(timeWindow) || timeWindow < 0) {
                return buildReturnForValidate(false, 'Time window must be non-negative number');
            }

            return buildReturnForValidate(true);
        }

        function validateAll(deviceSettings, usedNames, cronFields) {
            return validateName(deviceSettings.name, usedNames).isValid
                && validateExpression(deviceSettings.schedule.expression).isValid
                && validateTimeWindow(deviceSettings.schedule.timeWindowMinutes).isValid
                && validateCronMinutes(cronFields.minutes).isValid
                && validateCronHours(cronFields.hours).isValid
                && validateCronDayOfMonth(cronFields.dayOfMonth).isValid
                && validateCronMonth(cronFields.month).isValid
                && validateCronDayOfWeek(cronFields.dayOfWeek).isValid;
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
    }
})();