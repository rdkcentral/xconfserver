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

(function () {
    'use strict';

    angular
        .module('app.firmwarerule')
        .factory('firmwareRuleValidationService', service);

    service.$inject = ['utilsService', 'globalValidationService'];

    function service(utilsService, globalValidationService) {
        var vm = this;

        vm.cleanErrors = cleanErrors;
        vm.validatePropertyValue = validatePropertyValue;
        vm.validateDistributionPercentages = validateDistributionPercentages;

        vm.definePropertyError = '';
        vm.distributionPercentageError = '';

        return vm;

        function cleanErrors() {
            vm.definePropertyError = '';
            vm.distributionPercentageError = '';
        }

        function validatePropertyValue(property, value) {
            if (property.optional && !value) {
                return true;
            }
            if (!property.optional && !value) {
                return false;
            }
            var validationTypes = property.validationTypes;

            var isString = _.include(validationTypes, 'STRING') && true;
            var isNumber = _.include(validationTypes, 'NUMBER') && globalValidationService.isNumber(value);
            var isBoolean = _.include(validationTypes, 'BOOLEAN') && globalValidationService.isBoolean(value);
            var isPercent = _.include(validationTypes, 'PERCENT') && globalValidationService.isPercentValid(value);
            var isPort = _.include(validationTypes, 'PORT') && globalValidationService.isPortValid(value);
            var isUrl = _.include(validationTypes, 'URL') && (globalValidationService.isUrlValid(value) || globalValidationService.isUrlProtocolRequiredValid(value));
            var isIpV4 = _.include(validationTypes, 'IPV4') && globalValidationService.isIpV4(value);
            var isIpV6 =  _.include(validationTypes, 'IPV6') && globalValidationService.isIpV6(value);

            return isString || isNumber || isBoolean || isPercent || isPort || isUrl || isIpV4 || isIpV6;

        }

        function validateDistributionPercentages(items) {
            vm.distributionPercentageError = findDistributionPercentagesError(items);
            return vm.distributionPercentageError;
        }

        function findDistributionPercentagesError(items) {
            var totalPercentage = 0;
            for (var i = 0; i < items.length; i++) {
                var percent = items[i].percentage;
                if (percent !== '') {
                    totalPercentage += percent;
                }
            }
            if (totalPercentage > 100) {
                return 'Total percentage count should not be bigger than 100';
            }
            return '';
        }
    }
})();