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
 * Created: 09.11.15  13:25
 */

(function () {
    'use strict';
    angular
        .module('app.formula')
        .factory('formulaValidationService', formulaValidationService);


    formulaValidationService.$inject = ['utilsService'];

    function formulaValidationService(utilsService) {
        return {
            validateName: validateName,
            validatePercentage: validatePercentage,
            validateLevelPercentage: validateLevelPercentage,
            validateCondition: validateCondition,
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

        function validatePercentage(percentage) {
            if (percentage) {
                if (!utilsService.isInt(percentage) || percentage < 0 || percentage > 100) {
                    return buildReturnForValidate(false, 'Percentage must be number from 0 to 100');
                }
            }

            return buildReturnForValidate(true);
        }

        function validateLevelPercentage(rule, currentPercentage) {
            if (currentPercentage) {
                if (!utilsService.isInt(currentPercentage) || currentPercentage < 0) {
                    return buildReturnForValidate(false, 'Percentage must be non-negative number');
                }

                var percentL1 = rule.percentageL1 || 0;
                var percentL2 = rule.percentageL2 || 0;
                var percentL3 = rule.percentageL3 || 0;

                if (utilsService.isInt(percentL1) && utilsService.isInt(percentL2) && utilsService.isInt(percentL3)) {
                    var sum = parseInt(percentL1) + parseInt(percentL2) + parseInt(percentL3);
                    if (sum > 100) {
                        return buildReturnForValidate(false, 'Total percentage sum must be 100 or less');
                    }
                }

            }

            return buildReturnForValidate(true);
        }

        function validateCondition(rule) {
            if (utilsService.isNullOrUndefinedOrEmpty(rule.compoundParts) && !rule.condition) {
                return buildReturnForValidate(false, 'Please fill condition');
            }

            return buildReturnForValidate(true);
        }

        function validateAll(rule, usedNames) {
            return validateName(rule.name, usedNames).isValid &&
                validatePercentage(rule.name.percentage).isValid &&
                validateLevelPercentage(rule, rule.percentageL1).isValid &&
                validateLevelPercentage(rule, rule.percentageL2).isValid &&
                validateLevelPercentage(rule, rule.percentageL3).isValid &&
                validateCondition(rule).isValid;
        }
    }
})();